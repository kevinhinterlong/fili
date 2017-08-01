// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.table;

import com.yahoo.bard.webservice.data.config.names.TableName;
import com.yahoo.bard.webservice.data.time.ZonedTimeGrain;
import com.yahoo.bard.webservice.metadata.DataSourceMetadataService;
import com.yahoo.bard.webservice.table.availability.StrictAvailability;

import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link StrictPhysicalTable} specific for Sql Backed datasources.
 */
public class StrictSqlPhysicalTable extends StrictPhysicalTable {
    private final String schemaName;
    private final String timestampColumn;

    /**
     * Create a strict physical table.
     *
     * @param schemaName  The name of sql schema this table is on.
     * @param timestampColumn  The name of the timestamp column to be used for the database.
     * @param name  Name of the physical table as TableName, also used as data source name
     * @param timeGrain  time grain of the table
     * @param columns  The columns for this table
     * @param logicalToPhysicalColumnNames  Mappings from logical to physical names
     * @param metadataService  Datasource metadata service containing availability data for the table
     */
    public StrictSqlPhysicalTable(
            String schemaName,
            String timestampColumn,
            TableName name,
            ZonedTimeGrain timeGrain,
            Set<Column> columns,
            Map<String, String> logicalToPhysicalColumnNames,
            DataSourceMetadataService metadataService
    ) {
        super(name, timeGrain, columns, logicalToPhysicalColumnNames, metadataService);
        this.schemaName = schemaName;
        this.timestampColumn = timestampColumn;
    }

    /**
     * Create a strict physical table with the availability on this table built externally.
     *
     * @param schemaName  The name of sql schema this table is on.
     * @param timestampColumn  The name of the timestamp column to be used for the database.
     * @param name  Name of the physical table as TableName, also used as fact table name
     * @param timeGrain  time grain of the table
     * @param columns  The columns for this table
     * @param logicalToPhysicalColumnNames  Mappings from logical to physical names
     * @param availability  Availability that serves interval availability for columns
     */
    public StrictSqlPhysicalTable(
            String schemaName,
            String timestampColumn,
            TableName name,
            ZonedTimeGrain timeGrain,
            Set<Column> columns,
            Map<String, String> logicalToPhysicalColumnNames,
            StrictAvailability availability
    ) {
        super(name, timeGrain, columns, logicalToPhysicalColumnNames, availability);
        this.schemaName = schemaName;
        this.timestampColumn = timestampColumn;
    }

    /**
     * Gets the sql schema name this table belongs to.
     *
     * @return the schema name.
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Gets the name of the timestamp column backing this table.
     *
     * @return the name of the timestamp column.
     */
    public String getTimestampColumn() {
        return timestampColumn;
    }
}
