// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice;


import com.fasterxml.jackson.databind.JsonNode;
import com.yahoo.bard.webservice.druid.model.query.DruidAggregationQuery;
import com.yahoo.bard.webservice.druid.model.query.DruidQuery;
import com.yahoo.bard.webservice.druid.model.query.TimeSeriesQuery;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelBuilder;

import java.util.List;

public class SqlConverter {
    private SqlConverter() {

    }

    public JsonNode convert(DruidQuery<?> druidQuery) {

        return null;
    }

    public JsonNode convert(DruidAggregationQuery<?> druidQuery) {
        return null;
    }

    public JsonNode convert(TimeSeriesQuery druidQuery) {


        return null;
    }

    public static RelBuilder buildTimeSeriesQuery(TimeSeriesQuery druidQuery, RelBuilder builder) {
        String name = druidQuery.getDataSource().getPhysicalTable().getTableName().asName();
        System.out.println(RelOptUtil.toString(builder.scan(name).build()));
        return builder.scan(name);
    }

    public static void main(String[] args) {
        final FrameworkConfig config = config().build();
        final RelBuilder builder = RelBuilder.create(config);
        System.out.println(RelOptUtil.toString(builder.scan("test").build()));
    }

    /** Creates a config based on the "scott" schema. */
    public static Frameworks.ConfigBuilder config() {
        final SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        return Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .traitDefs((List<RelTraitDef>) null)
                .programs(Programs.heuristicJoinOrder(Programs.RULE_SET, true, 2));
    }
}
