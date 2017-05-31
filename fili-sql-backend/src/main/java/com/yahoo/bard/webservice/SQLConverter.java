// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.bard.webservice.data.time.DefaultTimeGrain;
import com.yahoo.bard.webservice.druid.model.DefaultQueryType;
import com.yahoo.bard.webservice.druid.model.QueryType;
import com.yahoo.bard.webservice.druid.model.datasource.DataSource;
import com.yahoo.bard.webservice.druid.model.query.DruidQuery;
import com.yahoo.bard.webservice.druid.model.query.TimeSeriesQuery;
import com.yahoo.bard.webservice.mock.DruidMockResponse;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.tools.RelBuilder;
import org.h2.jdbc.JdbcSQLException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;

/**
 * Converts druid queries to sql, executes it, and returns a druid like response.
 */
public class SQLConverter {
    private static final Logger LOG = LoggerFactory.getLogger(SQLConverter.class);

    /**
     * No instances.
     */
    private SQLConverter() {

    }

    public static JsonNode convert(DruidQuery<?> druidQuery) throws Exception {
        LOG.info("Processing druid query");
        QueryType queryType = druidQuery.getQueryType();
        if (DefaultQueryType.TIMESERIES.equals(queryType)) {
            TimeSeriesQuery timeSeriesQuery = (TimeSeriesQuery) druidQuery;
            return convert(timeSeriesQuery);
        }

        LOG.warn("Attempted to query unsupported type {}", queryType.toString());
        throw new RuntimeException("Unsupported query type");
    }

    public static JsonNode convert(TimeSeriesQuery druidQuery) throws Exception {
        LOG.info("Processing time series query");
        String datasource = druidQuery.getDataSource() == null ? null : druidQuery.getDataSource().getPhysicalTable().getName();
        datasource = "wikiticker";
        // select * from datasource
        // how does this work with granularity? will this have to be bucketed by granularity here
        // sql aggregations are done with groupBy (do we have to worry about makers?)

        return query(datasource, Database.getDatabase());
    }

    public static JsonNode query(String table, Connection connection) throws Exception {
        String generatedSql = "";
        // todo generate sql for query
        generatedSql = "select * from " + table;
        if (!validate(generatedSql)) {
            LOG.warn("Unable to validate sql query \"{}\"", generatedSql);
            // todo throw exception
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(generatedSql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return read(connection, resultSet);
        } catch (JdbcSQLException e) {
            LOG.warn("Failed to query database {}", connection.getCatalog());
            throw new RuntimeException("Could not finish query", e);
        }
    }

    /**
     * Reads the result set and converts it into a result that druid
     * would produce.
     *
     * @param connection the connection to the database.
     * @param resultSet  the result set of the druid query.
     * @return druid-like result from query.
     */
    private static JsonNode read(Connection connection, ResultSet resultSet) throws Exception {
        Database.printColTypes(resultSet.getMetaData());
        // todo figure out druid response layout
        DruidResponse response = new DruidResponse();
        response.name = "table name";
        response.type = "time series";

        DruidMockResponse mockResponse = new DruidMockResponse();
        DruidMockResponse.TimeStampResult t = new DruidMockResponse.TimeStampResult();
        t.timestamp = DateTime.now();
        t.result.resultsMap.put("sample_name1", 0d);
        t.result.resultsMap.put("sample_name2", 1d);
        mockResponse.results.add(t);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(mockResponse);
    }

    public static boolean validate(String sql) {
//        SqlValidatorUtil.newValidator(,new SqlV,new JavaTypeFactoryImpl(), SqlConformance.DEFAULT).
        return false;
    }

    public static RelBuilder buildTimeSeriesQuery(TimeSeriesQuery druidQuery, RelBuilder builder) {
        String name = druidQuery.getDataSource().getPhysicalTable().getTableName().asName();
        System.out.println(RelOptUtil.toString(builder.scan(name).build()));
        return builder.scan(name);
    }

/* Example query to druid --------------------------------------------------------------------------
{
  "queryType": "timeseries",
  "dataSource": "sample_datasource",
  "granularity": "day",
  "descending": "true",
  "filter": {
    "type": "and",
    "fields": [
      { "type": "selector", "dimension": "sample_dimension1", "value": "sample_value1" },
      { "type": "or",
        "fields": [
          { "type": "selector", "dimension": "sample_dimension2", "value": "sample_value2" },
          { "type": "selector", "dimension": "sample_dimension3", "value": "sample_value3" }
        ]
      }
    ]
  },
  "aggregations": [
    { "type": "longSum", "name": "sample_name1", "fieldName": "sample_fieldName1" },
    { "type": "doubleSum", "name": "sample_name2", "fieldName": "sample_fieldName2" }
  ],
  "postAggregations": [
    { "type": "arithmetic",
      "name": "sample_divide",
      "fn": "/",
      "fields": [
        { "type": "fieldAccess", "name": "postAgg__sample_name1", "fieldName": "sample_name1" },
        { "type": "fieldAccess", "name": "postAgg__sample_name2", "fieldName": "sample_name2" }
      ]
    }
  ],
  "intervals": [ "2012-01-01T00:00:00.000/2012-01-03T00:00:00.000" ]
}
 --------------------------------------------------------------------------------------------------- */

    public static void main(String[] args) throws Exception {
        JsonNode jsonNode = convert(getTimeSeriesQuery());
        System.out.println(jsonNode);
        // getSqlParser("select * from PERSON").parseQuery().toSqlString(SqlDialect.DUMMY)
        // todo figure out how to create a validator object (all queries must be validated before submitting)
    }

    public static SqlParser getSqlParser(String sql) {
        return SqlParser.create(sql,
                SqlParser.configBuilder()
                        .setParserFactory(SqlParserImpl.FACTORY)
                        .setQuoting(Quoting.BACK_TICK)
                        .setUnquotedCasing(Casing.UNCHANGED)
                        .setQuotedCasing(Casing.UNCHANGED)
                        .setConformance(SqlConformanceEnum.SQL_SERVER_2008)
                        .build()
        );
    }

    public static TimeSeriesQuery getTimeSeriesQuery() {
        return new TimeSeriesQuery(
                getDataSource(),
                DefaultTimeGrain.DAY,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptySet()

        );
    }

    private static DataSource getDataSource() {
        return null;
    }

    static class DruidResponse {
        public String name;
        public String type;
    }
}
