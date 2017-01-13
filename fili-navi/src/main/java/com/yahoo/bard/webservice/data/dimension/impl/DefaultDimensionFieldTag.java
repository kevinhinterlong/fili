// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.data.dimension.impl;

import com.yahoo.bard.webservice.data.dimension.Tag;
import com.yahoo.bard.webservice.util.EnumUtils;

/**
 * Default dimension field tag provided in fili-navi to match fili-core concept "key" field.
 */
public enum DefaultDimensionFieldTag implements Tag {
    PRIMARY_KEY
    ;

    private final String tagName;

    /**
     * Constructor, build name using camel cased enum name.
     */
    DefaultDimensionFieldTag() {
        this.tagName = EnumUtils.camelCase(name());
    }

    @Override
    public String getName() {
        return tagName;
    }
}
