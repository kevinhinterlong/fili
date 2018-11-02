// Copyright 2018 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.util

import com.yahoo.bard.webservice.web.util.ResponseUtils

import spock.lang.Specification
import spock.lang.Unroll

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.PathSegment
import javax.ws.rs.core.UriInfo


class ResponseUtilsSpec extends Specification {
    def responseUtils = new ResponseUtils()

    def mockRequestContext(pathSegments, dateTime) {
        def segments = pathSegments.split("/").collect { segment ->
            Stub(PathSegment) {
                getPath() >> {
                    segment
                }
            }
        }

        ContainerRequestContext ctx = Stub(ContainerRequestContext) {
            getUriInfo() >> Stub(UriInfo) {
                getPathSegments() >> {
                    segments
                }
                getQueryParameters() >> {
                    ["dateTime": dateTime] as MultivaluedHashMap
                }
            }
        }

        ctx
    }

    @Unroll
    def "getCsvContentDispositionValue returns #expectedMatch with intervals #pathSegments and #dateTime"() {
        given:
        def ctx = mockRequestContext(pathSegments, dateTime)

        and:
        def response = responseUtils.getCsvContentDispositionValue(ctx)
        response = response.substring("attachment; filename=".length())

        expect:
        expectedMatch == response

        where:
        pathSegments               | dateTime       | expectedMatch
        "data/table/day/dimension" | "P10D/current" | "data-table-day-dimension_P10D_current.csv"
        "data/table/day/CAPS" | "P10D/current" | "data-table-day-CAPS_P10D_current.csv"
    }

    def "getCsvContentDispositionValue ends with .csv even with long file names"() {
        given:
        def ctx = mockRequestContext("a" * ResponseUtils.MAX_EXCEL_FILE_PATH_LENGTH , "P10D/current")

        and:
        def response = responseUtils.getCsvContentDispositionValue(ctx)
        response = response.substring("attachment; filename=".length())

        expect:
        response.endsWith(".csv")
    }
}
