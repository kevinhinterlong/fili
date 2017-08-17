// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.wiki.webservice.application;

import static com.yahoo.bard.webservice.web.handlers.SqlRequestHandler.DATABASE_DRIVER;
import static com.yahoo.bard.webservice.web.handlers.SqlRequestHandler.DATABASE_PASSWORD;
import static com.yahoo.bard.webservice.web.handlers.SqlRequestHandler.DATABASE_URL;
import static com.yahoo.bard.webservice.web.handlers.SqlRequestHandler.DATABASE_USERNAME;

import com.yahoo.bard.webservice.application.AbstractBinderFactory;
import com.yahoo.bard.webservice.application.DimensionValueLoadTask;
import com.yahoo.bard.webservice.application.SqlDimensionValueLoader;
import com.yahoo.bard.webservice.config.SystemConfig;
import com.yahoo.bard.webservice.config.SystemConfigProvider;
import com.yahoo.bard.webservice.data.config.dimension.DimensionConfig;
import com.yahoo.bard.webservice.data.config.metric.MetricLoader;
import com.yahoo.bard.webservice.data.config.table.TableLoader;
import com.yahoo.bard.webservice.data.dimension.DimensionDictionary;
import com.yahoo.bard.webservice.druid.client.DruidWebService;
import com.yahoo.bard.webservice.sql.DefaultSqlBackedClient;
import com.yahoo.bard.webservice.sql.SqlBackedClient;
import com.yahoo.bard.webservice.table.PhysicalTableDictionary;
import com.yahoo.bard.webservice.web.handlers.workflow.RequestWorkflowProvider;
import com.yahoo.bard.webservice.web.handlers.workflow.SqlWorkflow;
import com.yahoo.wiki.webservice.data.config.dimension.WikiDimensions;
import com.yahoo.wiki.webservice.data.config.metric.WikiMetricLoader;
import com.yahoo.wiki.webservice.data.config.table.WikiTableLoader;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wiki specialization of the Abstract Binder factory, applying Wiki configuration objects.
 */
public class WikiBinderFactory extends AbstractBinderFactory {
    public static final SystemConfig SYSTEM_CONFIG = SystemConfigProvider.getInstance();

    @Override
    protected MetricLoader getMetricLoader() {
        return new WikiMetricLoader();
    }

    @Override
    protected LinkedHashSet<DimensionConfig> getDimensionConfigurations() {
        return new LinkedHashSet<>(new WikiDimensions().getAllDimensionConfigurations());
    }

    @Override
    protected TableLoader getTableLoader() {
        return new WikiTableLoader(getDataSourceMetadataService());
    }

    @Override
    protected Class<? extends RequestWorkflowProvider> getWorkflow() {
        return SqlWorkflow.class;
    }

    @Override
    protected DimensionValueLoadTask buildDruidDimensionsLoader(
            DruidWebService webService,
            PhysicalTableDictionary physicalTableDictionary,
            DimensionDictionary dimensionDictionary
    ) {
        List<String> dimensionsList = getDimensionConfigurations().stream()
                .map(DimensionConfig::getApiName)
                .collect(Collectors.toList());

        SqlDimensionValueLoader sqlDimensionRowProvider = new SqlDimensionValueLoader(
                physicalTableDictionary,
                dimensionDictionary,
                dimensionsList, // Put Sql dimensions here
                buildDefaultSqlBackedClient()
        );
        return new DimensionValueLoadTask(Collections.singletonList(sqlDimensionRowProvider));
    }

    private SqlBackedClient buildDefaultSqlBackedClient() {
        String dbUrl = SYSTEM_CONFIG.getStringProperty(DATABASE_URL);
        String driver = SYSTEM_CONFIG.getStringProperty(DATABASE_DRIVER);
        String user = SYSTEM_CONFIG.getStringProperty(DATABASE_USERNAME);
        String pass = SYSTEM_CONFIG.getStringProperty(DATABASE_PASSWORD);
        try {
            return new DefaultSqlBackedClient(dbUrl, driver, user, pass, getMapper());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
