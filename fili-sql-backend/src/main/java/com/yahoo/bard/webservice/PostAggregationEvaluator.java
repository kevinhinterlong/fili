package com.yahoo.bard.webservice;

import com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation;
import com.yahoo.bard.webservice.druid.model.postaggregation.ConstantPostAggregation;
import com.yahoo.bard.webservice.druid.model.postaggregation.FieldAccessorPostAggregation;
import com.yahoo.bard.webservice.druid.model.postaggregation.PostAggregation;
import com.yahoo.bard.webservice.druid.model.postaggregation.PostAggregation.DefaultPostAggregationType;

import java.util.Map;

/**
 * Created by hinterlong on 6/6/17.
 */
public class PostAggregationEvaluator {
    private PostAggregationEvaluator() {

    }

    // todo post aggs have ordering
    public static Double evaluate(PostAggregation postAggregation, Map<String, String> aggregatedValues) {
        DefaultPostAggregationType aggregationType = (DefaultPostAggregationType) postAggregation
                .getType();

        switch (aggregationType) {
            case ARITHMETIC:
                ArithmeticPostAggregation arithmeticPostAggregation = (ArithmeticPostAggregation) postAggregation;
                return evaluate(arithmeticPostAggregation, aggregatedValues);
            case FIELD_ACCESS:
                FieldAccessorPostAggregation fieldAccessorPostAggregation = (FieldAccessorPostAggregation)
                        postAggregation;
                return evaluate(fieldAccessorPostAggregation, aggregatedValues);
            case CONSTANT:
                ConstantPostAggregation constantPostAggregation = (ConstantPostAggregation) postAggregation;
                return evaluate(constantPostAggregation);
            case SKETCH_ESTIMATE:
            case SKETCH_SET_OPER:
            case THETA_SKETCH_ESTIMATE:
            case THETA_SKETCH_SET_OP:
            default:
                throw new UnsupportedOperationException("Can't do post aggregation " + postAggregation.getType());
        }

    }

    private static Double evaluate(
            FieldAccessorPostAggregation fieldAccessorPostAggregation,
            Map<String, String> aggregatedValues
    ) {
        return Double.valueOf(aggregatedValues.get(fieldAccessorPostAggregation.getFieldName()));
    }

    private static Double evaluate(ArithmeticPostAggregation ap, Map<String, String> aggregatedValues) {
        switch (ap.getFn()) {
            case PLUS:
                Double sum = 0D;
                for (PostAggregation postAgg : ap.getFields()) {
                    sum += evaluate(postAgg, aggregatedValues);
                }
                return sum;
            case MULTIPLY:
                Double prod = 1D;
                for (PostAggregation postAgg : ap.getFields()) {
                    prod *= evaluate(postAgg, aggregatedValues);
                }
                return prod;
            case MINUS:
                if (ap.getFields().size() != 2) {
                    throw new IllegalStateException("Can only subtract on two fields");
                }
                Double firstAsDoubleSub = evaluate(ap.getFields().get(0), aggregatedValues);
                Double secondAsDoubleSub = evaluate(ap.getFields().get(1), aggregatedValues);
                return firstAsDoubleSub - secondAsDoubleSub;
            case DIVIDE:
                if (ap.getFields().size() != 2) {
                    throw new IllegalStateException("Can only divide on two fields");
                }
                Double firstAsDoubleDiv = evaluate(ap.getFields().get(0), aggregatedValues);
                Double secondAsDoubleDiv = evaluate(ap.getFields().get(1), aggregatedValues);
                if (secondAsDoubleDiv == 0) {
                    // if divisor is zero then result is zero
                    // as per druid's http://druid.io/docs/latest/querying/post-aggregations.html
                    return 0D;
                }
                return firstAsDoubleDiv / secondAsDoubleDiv;
        }
        throw new UnsupportedOperationException("Can't do post aggregation " + ap);
    }

    private static double evaluate(ConstantPostAggregation constantPostAggregation) {
        return constantPostAggregation.getValue();
    }

}
