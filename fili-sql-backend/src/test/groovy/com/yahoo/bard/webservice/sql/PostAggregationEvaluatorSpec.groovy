// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.sql

import static com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation.ArithmeticPostAggregationFunction.DIVIDE
import static com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation.ArithmeticPostAggregationFunction.MINUS
import static com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation.ArithmeticPostAggregationFunction.MULTIPLY
import static com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation.ArithmeticPostAggregationFunction.PLUS
import static com.yahoo.bard.webservice.helper.Aggregator.sum
import static com.yahoo.bard.webservice.helper.PostAggregator.arithmetic
import static com.yahoo.bard.webservice.helper.PostAggregator.constant
import static com.yahoo.bard.webservice.helper.PostAggregator.field

import com.yahoo.bard.webservice.druid.model.postaggregation.SketchEstimatePostAggregation
import com.yahoo.bard.webservice.druid.model.postaggregation.SketchSetOperationPostAggregation
import com.yahoo.bard.webservice.druid.model.postaggregation.ThetaSketchEstimatePostAggregation
import com.yahoo.bard.webservice.druid.model.postaggregation.ThetaSketchSetOperationPostAggregation

import spock.lang.Specification

class PostAggregationEvaluatorSpec extends Specification {
    private static final Map<String, String> fieldToValue = new HashMap<>()
    private static final String ONE = "one"
    private static final String FIVE = "five"

    void setup() {
        fieldToValue.put(ONE, "1")
        fieldToValue.put(FIVE, "5")
    }

    def "Evaluate Post Aggregations "() {
        expect:
        Double result = PostAggregationEvaluator.evaluate(postAgg, {it -> fieldToValue.get(it)})
        result == value

        where: "given"
        postAgg                                                      | value
        arithmetic(PLUS, field(sum(ONE)))                            | 1
        arithmetic(PLUS, constant(1))                                | 1
        arithmetic(PLUS, arithmetic(PLUS, constant(1)), constant(1)) | 2
        arithmetic(PLUS, field(sum(ONE)), field(sum(FIVE)))          | 6
        arithmetic(MINUS, field(sum(ONE)), field(sum(FIVE)))         | -4
        arithmetic(MULTIPLY, field(sum(ONE)), field(sum(FIVE)))      | 5
        arithmetic(DIVIDE, field(sum(ONE)), field(sum(FIVE)))        | 1 / 5
        arithmetic(DIVIDE, field(sum(ONE)), constant(0))             | 0
    }

    def "Test unsupported post aggregations and bad inputs"() {
        when:
        Double result = PostAggregationEvaluator.evaluate(postAgg, {it -> fieldToValue.get(it)})

        then:
        thrown thrownException

        where:
        postAgg                                                    | thrownException
        arithmetic(DIVIDE, constant(1), constant(1), constant(1))  | IllegalArgumentException
        arithmetic(MINUS, constant(1), constant(1), constant(1))   | IllegalArgumentException
        new ThetaSketchEstimatePostAggregation("", null)           | UnsupportedOperationException
        new ThetaSketchSetOperationPostAggregation("", null, null) | UnsupportedOperationException
        new SketchSetOperationPostAggregation("", null, null)      | UnsupportedOperationException
        new SketchEstimatePostAggregation("", null)                | UnsupportedOperationException
    }
}