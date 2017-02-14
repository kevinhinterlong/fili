// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.table.resolver;

import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.druid.model.query.DruidAggregationQuery;
import com.yahoo.bard.webservice.web.ApiFilter;
import com.yahoo.bard.webservice.web.DataApiRequest;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constraints for retrieving potential table availability for a given query.
 */
public class DataSourceConstraint {

    private final Set<Dimension> requestDimensions;
    private final Set<Dimension> filterDimensions;
    private final Set<Dimension> metricDimensions;
    private final Set<String> metricNames;
    private final Map<Dimension, Set<ApiFilter>> apiFilters;

    /**
     * Constructor.
     *
     * @param dataApiRequest Api request containing the constraints information.
     * @param templateDruidQuery Query containing metric constraint information.
     */
    public DataSourceConstraint(DataApiRequest dataApiRequest, DruidAggregationQuery<?> templateDruidQuery) {
        this.requestDimensions = dataApiRequest.getDimensions();
        this.filterDimensions = dataApiRequest.getFilterDimensions();
        this.metricDimensions = templateDruidQuery.getMetricDimensions();
        this.metricNames = templateDruidQuery.getDependentFieldNames();
        this.apiFilters = dataApiRequest.getFilters();
    }

    public Set<Dimension> getRequestDimensions() {
        return requestDimensions;
    }

    public Set<Dimension> getFilterDimensions() {
        return filterDimensions;
    }

    public Set<Dimension> getMetricDimensions() {
        return metricDimensions;
    }

    public Set<String> getRequestDimensionNames() {
        return getRequestDimensions().stream().map(Dimension::getApiName).collect(Collectors.toSet());
    }

    public Set<Dimension> getAllDimensions() {
        return Stream.of(
                getRequestDimensions().stream(),
                getFilterDimensions().stream(),
                getMetricDimensions().stream()
        ).flatMap(Function.identity()).collect(Collectors.toSet());
    }

    public Set<String> getAllDimensionNames() {
        return getAllDimensions().stream().map(Dimension::getApiName).collect(Collectors.toSet());
    }

    public Set<String> getMetricNames() {
        return metricNames;
    }

    public Set<String> getAllColumnNames() {
        return Stream.of(
                getAllDimensionNames().stream(),
                getMetricNames().stream()
        ).flatMap(Function.identity()).collect(Collectors.toSet());
    }

    public Map<Dimension, Set<ApiFilter>> getApiFilters() {
        return apiFilters;
    }
}
