// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.table;

import com.yahoo.bard.webservice.data.config.names.TableName;
import com.yahoo.bard.webservice.data.time.ZonedTimeGrain;
import com.yahoo.bard.webservice.metadata.DataSourceMetadataService;
import com.yahoo.bard.webservice.table.availability.ConcreteAvailability;

import java.util.Map;

import javax.validation.constraints.NotNull;

/**
 * An instance of Physical table that is backed by a single fact table.
 */
public class ConcretePhysicalTable extends BasePhysicalTable {
    /**
     * Create a concrete physical table.
     *
     * @param name name of the physical table as TableName
     * @param timeGrain time grain of the table
     * @param columns the columns for this table
     * @param logicalToPhysicalColumnNames mappings from logical to physical names
     * @param metadataService datasource metadata service containing availability data for the table
     */
    public ConcretePhysicalTable(
            @NotNull TableName name,
            @NotNull ZonedTimeGrain timeGrain,
            @NotNull Iterable<Column> columns,
            @NotNull Map<String, String> logicalToPhysicalColumnNames,
            @NotNull DataSourceMetadataService metadataService
    ) {
        super(
                name,
                timeGrain,
                columns,
                logicalToPhysicalColumnNames,
                new ConcreteAvailability(name, metadataService)
        );
    }

    /**
     * Create a concrete physical table.
     *
     * @param name name of the physical table as String
     * @param timeGrain time grain of the table
     * @param columns the columns for this table
     * @param logicalToPhysicalColumnNames mappings from logical to physical names
     * @param metadataService datasource metadata service containing availability data for the table
     *
     * @deprecated Should use constructor with TableName instead of String as table name
     */
    @Deprecated
    public ConcretePhysicalTable(
            @NotNull String name,
            @NotNull ZonedTimeGrain timeGrain,
            @NotNull Iterable<Column> columns,
            @NotNull Map<String, String> logicalToPhysicalColumnNames,
            @NotNull DataSourceMetadataService metadataService
    ) {
        this(TableName.of(name), timeGrain, columns, logicalToPhysicalColumnNames, metadataService);
    }

    public String getFactTableName() {
        return getAvailability().getDataSourceNames().stream().findFirst().get().asName();
    }

    @Override
    public String toString() {
        return super.toString() + " factTableName: " + getAvailability().getDataSourceNames() + " alignment: " +
                getTableAlignment();
    }
}
