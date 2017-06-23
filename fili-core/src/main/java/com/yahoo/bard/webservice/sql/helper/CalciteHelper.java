// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.sql.helper;

import org.apache.calcite.adapter.clone.CloneSchema;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.pretty.SqlPrettyWriter;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

/**
 * Created by hinterlong on 6/20/17.
 */
public class CalciteHelper {
    public static final String DEFAULT_SCHEMA = "PUBLIC";
    private final DataSource dataSource;
    private final String schemaName;
    private final SqlDialect dialect;

    public CalciteHelper(DataSource dataSource, String schemaName)
            throws SQLException {
        this.dataSource = dataSource;
        this.schemaName = schemaName;
        dialect = SqlDialect.create(getConnection().getMetaData());
    }

    public RelBuilder getNewRelBuilder() {
        try {
            return getBuilder(dataSource, schemaName);
        } catch (SQLException ignored) {
        }
        return null;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public RelToSqlConverter getNewRelToSqlConverter() throws SQLException {
        return new RelToSqlConverter(dialect);
    }

    /**
     * Creates a {@link RelBuilder} with a root scema of {@link #DEFAULT_SCHEMA}.
     *
     * @param dataSource  The dataSource for the jdbc schema.
     *
     * @return the relbuilder from Calcite.
     *
     * @throws SQLException if can't readSqlResultSet from database.
     */
    public static RelBuilder getBuilder(DataSource dataSource) throws SQLException {
        return getBuilder(dataSource, DEFAULT_SCHEMA);
    }


    /**
     * Creates a {@link RelBuilder} with the given schema.
     *
     * @param dataSource  The dataSource for the jdbc schema.
     * @param schemaName  The name of the schema used for the database.
     *
     * @return the relbuilder from Calcite.
     *
     * @throws SQLException if can't readSqlResultSet from database.
     */
    public static RelBuilder getBuilder(DataSource dataSource, String schemaName) throws SQLException {
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        return RelBuilder.create(
                Frameworks.newConfigBuilder()
                        .parserConfig(SqlParser.Config.DEFAULT)
                        .defaultSchema(addSchema(rootSchema, dataSource, schemaName))
                        .traitDefs((List<RelTraitDef>) null)
                        .programs(Programs.heuristicJoinOrder(Programs.RULE_SET, true, 2))
                        .build()
        );
    }

    /**
     * Adds the schema name to the rootSchema.
     *
     * @param rootSchema  The calcite schema for the database.
     * @param dataSource  The dataSource for the jdbc schema.
     * @param schemaName  The name of the schema used for the database.
     *
     * @return the schema.
     */
    private static SchemaPlus addSchema(SchemaPlus rootSchema, DataSource dataSource, String schemaName) {
        return rootSchema.add( // avg tests run at ~75-100ms
                schemaName,
                JdbcSchema.create(rootSchema, null, dataSource, null, null)
        );
        // todo what do these actually do?
        // todo look into timing (not using cloneschema was faster, but this could just be because of H2)
//        rootSchema.setCacheEnabled(true); //almost no effect
//        return rootSchema.add( // avg tests run at ~200ms
//                schemaName,
//                new CloneSchema(
//                        rootSchema.add(schemaName, JdbcSchema.create(rootSchema, null, dataSource, null, null))
//                )
//        );
    }

    public SqlPrettyWriter getNewSqlWriter() {
        return new SqlPrettyWriter(dialect);
    }

    public String escapeTableName(String sqlTableName) {
        return dialect.quoteIdentifier(schemaName) + "." + dialect.quoteIdentifier(sqlTableName);
    }
}
