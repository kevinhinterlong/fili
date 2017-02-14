// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.table.availability;

import com.yahoo.bard.webservice.data.config.names.TableName;
import com.yahoo.bard.webservice.metadata.DataSourceMetadataService;
import com.yahoo.bard.webservice.table.resolver.DataSourceConstraint;
import com.yahoo.bard.webservice.util.IntervalUtils;
import com.yahoo.bard.webservice.util.SimplifiedIntervalList;

import org.joda.time.Interval;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

/**
 * An availability which guarantees immutability on its contents.
 */
public class ConcreteAvailability implements Availability {

    private final TableName name;
    private final DataSourceMetadataService metadataService;

    /**
     * Constructor.
     *
     * @param tableName The name of the data source associated with this ImmutableAvailability
     * @param metadataService A service containing the datasource segment data
     */
    public ConcreteAvailability(TableName tableName, @NotNull DataSourceMetadataService metadataService) {
        this.name = tableName;
        this.metadataService = metadataService;
    }

    @Override
    public Set<TableName> getDataSourceNames() {
        return Collections.singleton(name);
    }

    @Override
    public Map<String, Set<Interval>> getAllAvailableIntervals() {
        return metadataService.getAvailableIntervalsByTable(name);
    }

    @Override
    public SimplifiedIntervalList getAvailableIntervals(DataSourceConstraint constraints) {
        Map<String, Set<Interval>> allAvailableIntervals = getAllAvailableIntervals();

        Map<String, Set<Interval>> temp = constraints.getAllColumnNames().stream().collect(
                Collectors.toMap(
                        Function.identity(),
                        columnName -> allAvailableIntervals.getOrDefault(columnName, Collections.emptySet())
                )
        );

        return constraints.getAllColumnNames().isEmpty() ?
                new SimplifiedIntervalList() :
                new SimplifiedIntervalList(
                        temp.entrySet().stream()
                                .map(Map.Entry::getValue)
                                .reduce(null, IntervalUtils::getOverlappingSubintervals)
                );
    }
}
