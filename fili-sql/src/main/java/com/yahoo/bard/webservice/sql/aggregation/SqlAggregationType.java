package com.yahoo.bard.webservice.sql.aggregation;

import com.yahoo.bard.webservice.druid.model.aggregation.Aggregation;
import com.yahoo.bard.webservice.sql.ApiToFieldMapper;

import java.util.Set;

/**
 * A Sql Aggregation type which
 */
public interface SqlAggregationType {

    /**
     * Returns a set of supported druid aggregations.
     *
     * @return the set of supported druid aggregations.
     */
    Set<String> getSupportedDruidAggregations();

    /**
     * Builds a {@link SqlAggregation} which contains all the necessary information to build an aggregation in sql.
     *
     * @param aggregation  The druid aggregation.
     * @param apiToFieldMapper  The mapping between api and physical names for the query.
     *
     * @return the AggCall built from the aggregation type.
     */
    SqlAggregation getSqlAggregation(Aggregation aggregation, ApiToFieldMapper apiToFieldMapper);
}
