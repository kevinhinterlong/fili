// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.mock;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hinterlong on 5/31/17.
 */
@JsonSerialize(using = DruidResponseSerializer.class)
public class DruidResponse<E extends DruidResult> {
    /**
     * todo figure out druid response layout
     * TimeBoundary  -> TimeseriesResult
     */

    public final List<E> results = new ArrayList<>();

}