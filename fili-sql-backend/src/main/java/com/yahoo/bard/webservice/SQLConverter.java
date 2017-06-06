// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice;


import com.yahoo.bard.webservice.data.time.DefaultTimeGrain;
import com.yahoo.bard.webservice.druid.model.DefaultQueryType;
import com.yahoo.bard.webservice.druid.model.QueryType;
import com.yahoo.bard.webservice.druid.model.query.DruidAggregationQuery;
import com.yahoo.bard.webservice.druid.model.query.DruidQuery;
import com.yahoo.bard.webservice.druid.model.query.Granularity;
import com.yahoo.bard.webservice.druid.model.query.TimeSeriesQuery;
import com.yahoo.bard.webservice.mock.DruidResponse;
import com.yahoo.bard.webservice.mock.Simple;
import com.yahoo.bard.webservice.test.Database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlAggFunction;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelBuilder;
import org.h2.jdbc.JdbcSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts druid queries to sql, executes it, and returns a druid like response.
 */
public class SQLConverter {
    private static final Logger LOG = LoggerFactory.getLogger(SQLConverter.class);
    private static RelToSqlConverter relToSql;

    /**
     * No instances.
     */
    private SQLConverter() {

    }

    public static JsonNode convert(DruidAggregationQuery<?> druidQuery) throws Exception {
        LOG.debug("Processing druid query");
        QueryType queryType = druidQuery.getQueryType();
        if (DefaultQueryType.TIMESERIES.equals(queryType)) {
            TimeSeriesQuery timeSeriesQuery = (TimeSeriesQuery) druidQuery;
            return convert(timeSeriesQuery);
        }

        LOG.warn("Attempted to query unsupported type {}", queryType.toString());
        throw new RuntimeException("Unsupported query type");
    }

    public static JsonNode convert(TimeSeriesQuery druidQuery) throws Exception {
        LOG.debug("Processing time series query");

        Connection connection = Database.getDatabase();
        String generatedSql = buildTimeSeriesQuery(connection, druidQuery, builder());

        return query(druidQuery, generatedSql, connection);
    }

    public static JsonNode query(DruidQuery<?> druidQuery, String sql, Connection connection) throws Exception {
        LOG.debug("Executing \n{}", sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return read(druidQuery, connection, resultSet);
        } catch (JdbcSQLException e) {
            LOG.warn("Failed to query database {} with {}", connection.getCatalog(), sql);
            throw new RuntimeException("Could not finish query", e);
        }
    }

    /**
     * Reads the result set and converts it into a result that druid
     * would produce.
     *
     * @param druidQuery the druid query to be made.
     * @param connection the connection to the database.
     * @param resultSet  the result set of the druid query.
     * @return druid-like result from query.
     */
    private static JsonNode read(DruidQuery<?> druidQuery, Connection connection, ResultSet resultSet)
            throws Exception {
        // result set cannot be reset after rows have been read, this consumes results by reading them
        Database.ResultSetFormatter rf = new Database.ResultSetFormatter();
        rf.resultSet(resultSet);
        LOG.debug("Reading results \n{}", rf.string());

        int rows = 0;
        while (resultSet.next()) {
            ++rows;
            // process
        }
        LOG.debug("Fetched {} rows.", rows);

        ObjectMapper objectMapper = new ObjectMapper();

        LOG.debug("Original Query\n {}", objectMapper.valueToTree(druidQuery));
        DruidResponse druidResponse = Simple.druidResponse();
        JsonNode druidResponseJson = objectMapper.valueToTree(druidResponse);
        LOG.debug("Fake Druid Response\n {}", druidResponseJson);

        return druidResponseJson;
    }

    public static String buildTimeSeriesQuery(Connection connection, TimeSeriesQuery druidQuery, RelBuilder builder)
            throws SQLException {
        initRelToSqlConverter(connection);

        String name = druidQuery.getDataSource().getPhysicalTable().getName();
        String timeCol = Database.getDateTimeColumn(connection, name).toUpperCase();

        // =============================================================================================

        builder.scan(name); // choose table

        // =============================================================================================

        // select dimensions/metrics?
        if (druidQuery.getDimensions().size() != 0) {
            LOG.debug("Adding dimensions { {} }", druidQuery.getDimensions());
            builder.project(druidQuery.getDimensions()
                    .stream()
                    .map(Object::toString)
                    .map(builder::field)
                    .toArray(RexInputRef[]::new));
        }
        // druidQuery.getAggregations()
        //        .stream()
        //        .map(Aggregation::getDependentDimensions) // include dependent dimensions in select?

        // =============================================================================================

        // create filters to only select results within the given intervals
        List<RexNode> timeFilters = druidQuery.getIntervals().stream().map(interval -> {
            Timestamp start = TimeUtils.timestampFromMillis(interval.getStartMillis());
            Timestamp end = TimeUtils.timestampFromMillis(interval.getEndMillis());

            return builder.call(
                    SqlStdOperatorTable.AND,
                    builder.call(
                            SqlStdOperatorTable.GREATER_THAN,
                            builder.field(timeCol),
                            builder.literal(start.toString())
                    ),
                    builder.call(
                            SqlStdOperatorTable.LESS_THAN,
                            builder.field(timeCol),
                            builder.literal(end.toString())
                    )
            );
        }).collect(Collectors.toList());
        builder.filter(
                builder.call(
                        SqlStdOperatorTable.OR,
                        timeFilters
                )
        );

        // =============================================================================================

        List<RexNode> times = Arrays.asList(
                builder.call(SqlStdOperatorTable.YEAR, builder.field(timeCol)),
                builder.call(SqlStdOperatorTable.MONTH, builder.field(timeCol)),
                builder.call(SqlStdOperatorTable.WEEK, builder.field(timeCol)),
                builder.call(SqlStdOperatorTable.DAYOFYEAR, builder.field(timeCol)),
                builder.call(SqlStdOperatorTable.HOUR, builder.field(timeCol)),
                builder.call(SqlStdOperatorTable.MINUTE, builder.field(timeCol))
        );
        int length = getLength(druidQuery.getGranularity());

        //are there any custom aggregations or can we just list them all in an enum
        if (druidQuery.getAggregations().size() != 0) { // group by aggregations
            LOG.debug("Adding aggregations { {} }", druidQuery.getAggregations());

            //this makes a group by on all the parts in the sublist
            RelBuilder.GroupKey groupByTime = builder.groupKey(
                    times.subList(0, length)
            );

            druidQuery.getAggregations().forEach(aggregation -> {
                builder.aggregate(
                        groupByTime, // how to do grouping on time with granularity? UDF/SQL/manual in java?
                        AggregationType.getAggregation(
                                AggregationType.fromDruidType(aggregation.getType()),
                                builder,
                                aggregation.getFieldName()
                        )
                );
            });
        }

        // =============================================================================================

        // add WHERE/filters here

        // =============================================================================================

        builder.sort(builder.fields().subList(0, length)); // order by same time as grouping

        // =============================================================================================

        // find non overlapping intervals to include in meta part of druids response?
        // this will have to be implemented later if at all since we don't have information about partial data


        // post aggregations

        return relToSql(builder);
    }

    private static int getLength(Granularity granularity) {
        if (!(granularity instanceof DefaultTimeGrain)) {
            throw new IllegalStateException("Must be a DefaultTimeGrain");
        }
        DefaultTimeGrain timeGrain = (DefaultTimeGrain) granularity;
        switch (timeGrain) {
            case MINUTE:
                return 6;
            case HOUR:
                return 5;
            case DAY:
                return 4;
            case WEEK:
                return 3;
            case MONTH:
                return 2;
            case YEAR:
                return 1;
            case QUARTER:
                throw new IllegalStateException("Quarter timegrain not supported");
            default:
                throw new IllegalStateException("Timegrain not known " + timeGrain);
        }
    }

    public enum AggregationType {
        SUM("sum"),
        MIN("min"),
        MAX("max");

        private final String type;

        AggregationType(String type) {
            this.type = type;
        }

        public static AggregationType fromDruidType(String type) {
            for (AggregationType a : values()) {
                if (type.toLowerCase().contains(a.type)) {
                    return a;
                }
            }
            throw new InputMismatchException("No corresponding type for " + type);
        }

        public static RelBuilder.AggCall getAggregation(AggregationType a, RelBuilder builder, String fieldName) {
            String alias = "ALIAS_";
            SqlAggFunction aggFunction = null;
            switch (a) {
                case SUM:
                    aggFunction = SqlStdOperatorTable.SUM;
                    break;
                case MAX:
                    aggFunction = SqlStdOperatorTable.MAX;
                    break;
                case MIN:
                    aggFunction = SqlStdOperatorTable.MIN;
                    break;
            }

            if (aggFunction != null) {
                return builder.aggregateCall(aggFunction, false, null, alias + fieldName, builder.field(fieldName));
            }

            throw new UnsupportedOperationException("No corresponding AggCall for " + a);
        }
    }

    private static void initRelToSqlConverter(final Connection connection) throws SQLException {
        if (relToSql == null) {
            relToSql = new RelToSqlConverter(SqlDialect.create(connection.getMetaData()));
        }
    }

    private static String relToSql(RelBuilder builder) {
        return relToSql.visitChild(0, builder.build()).asSelect().toString();
    }


    public static void main(String[] args) throws Exception {
        DruidAggregationQuery<?> druidQuery = Simple.timeSeriesQuery("WIKITICKER");
        JsonNode jsonNode = convert(druidQuery);
        //        Connection database = Database.getDatabase();
        //        test(database);
        // todo validate?
    }

    private static void test(Connection database) throws SQLException {
        RelBuilder builder = builder();
        // how to floor timestamp to hour/day/minute etc not working
        builder.scan("WIKITICKER");
        RexNode hour = builder.call(
                SqlStdOperatorTable.HOUR,
                builder.field("TIME")
        );
        builder.call(
                SqlStdOperatorTable.TIMESTAMP_ADD,
                hour,
                builder.call(
                        SqlStdOperatorTable.TIMESTAMP_DIFF,
                        hour,
                        builder.field("TIME")
                )
        );


        builder.sort(builder.field(0)); // order by time
        initRelToSqlConverter(database);
        relToSql(builder);
    }

    public static RelBuilder builder() {
        final SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        return RelBuilder.create(
                Frameworks.newConfigBuilder()
                        .parserConfig(SqlParser.Config.DEFAULT)
                        .defaultSchema(addSchema(rootSchema))
                        .traitDefs((List<RelTraitDef>) null)
                        .programs(Programs.heuristicJoinOrder(Programs.RULE_SET, true, 2))
                        .build()
        );
    }

    public static SchemaPlus addSchema(SchemaPlus rootSchema) {
        return rootSchema.add(
                Database.THE_SCHEMA,
                JdbcSchema.create(rootSchema, null, Database.getDataSource(), null, Database.THE_SCHEMA)
        );
    }

}

