package com.yahoo.wiki.webservice.data.config.auto;

import com.yahoo.bard.webservice.data.config.dimension.DefaultDimensionField;
import com.yahoo.bard.webservice.data.config.dimension.DefaultKeyValueStoreDimensionConfig;
import com.yahoo.bard.webservice.data.config.dimension.DimensionConfig;
import com.yahoo.bard.webservice.data.dimension.MapStoreManager;
import com.yahoo.bard.webservice.data.dimension.impl.ScanSearchProviderManager;
import com.yahoo.bard.webservice.data.time.DefaultTimeGrain;
import com.yahoo.bard.webservice.data.time.TimeGrain;
import com.yahoo.bard.webservice.data.time.ZonedTimeGrain;
import com.yahoo.bard.webservice.data.time.ZonelessTimeGrain;
import com.yahoo.bard.webservice.util.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 *
 */
public class FileTableConfigLoader implements Supplier<List<? extends DataSourceConfiguration>> {
    private final List<DataSourceConfiguration> datasources;

    public FileTableConfigLoader(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemResourceAsStream(filename));
        datasources = new ArrayList<>();
        parse(jsonNode);
    }

    private void parse(JsonNode jsonNode) {
        for (JsonNode table : jsonNode.get("tables")) {
            DataSourceConfiguration dataSourceConfiguration = parseDataSourceConfiguration(table);
            datasources.add(dataSourceConfiguration);
        }

    }

    private DataSourceConfiguration parseDataSourceConfiguration(JsonNode table) {
        String apiTableName = table.get("apiTableName").asText();
        String physicalTableName = table.has("physicalTableName") ?
                table.get("physicalTableName").asText()
                : apiTableName;

        JsonNode jsonZonedTimeGrain = table.get("zonedTimeGrain");
        TimeGrain timeGrain = DefaultTimeGrain.valueOf(jsonZonedTimeGrain.get("timeGrain").asText());
        DateTimeZone timeZone = DateTimeZone.forTimeZone(
                TimeZone.getTimeZone(jsonZonedTimeGrain.get("timeZone").asText())
        );
        ZonedTimeGrain zonedTimeGrain = new ZonedTimeGrain((ZonelessTimeGrain) timeGrain, timeZone);

        List<TimeGrain> allValidTimeGrains = parseTimeGrains(table.get("timeGrains"));

        Set<MetricConfig> metricConfigs = parseMetricConfigs(table.get("metrics"), allValidTimeGrains);
        Set<DimensionConfig> dimensionConfigs = parseDimensionConfigs(table.get("dimensions"));

        return new DataSourceConfiguration() {
            @Override
            public String getPhysicalTableName() {
                return physicalTableName;
            }

            @Override
            public String getApiTableName() {
                return apiTableName;
            }

            @Override
            public List<String> getMetrics() {
                return getMetricConfigs()
                        .stream()
                        .map(MetricConfig::getApiMetricName)
                        .collect(Collectors.toList());
            }

            @Override
            public List<String> getDimensions() {
                return getDimensionConfigs()
                        .stream()
                        .map(DimensionConfig::getApiName)
                        .collect(Collectors.toList());
            }

            @Override
            public Set<MetricConfig> getMetricConfigs() {
                return metricConfigs;
            }

            @Override
            public Set<DimensionConfig> getDimensionConfigs() {
                return dimensionConfigs;
            }

            @Override
            public ZonedTimeGrain getZonedTimeGrain() {
                return zonedTimeGrain;
            }

            @Override
            public List<TimeGrain> getValidTimeGrains() {
                return allValidTimeGrains;
            }
        };
    }

    private List<TimeGrain> parseTimeGrains(JsonNode timeGrainsArray) {
        List<TimeGrain> allValidTimeGrains = new ArrayList<>();
        timeGrainsArray.forEach(timeGrainNode -> {
            TimeGrain validTimeGrain = DefaultTimeGrain.valueOf(timeGrainNode.asText());
            allValidTimeGrains.add(validTimeGrain);
        });
        return allValidTimeGrains;
    }

    private Set<MetricConfig> parseMetricConfigs(JsonNode metrics, List<TimeGrain> tableTimeGrains) {
        Set<MetricConfig> metricConfigs = new HashSet<>();

        metrics.forEach(metricConfig -> {
            String apiMetricName = metricConfig.get("apiMetricName").asText();
            String physicalMetricName = metricConfig.has("physicalMetricName") ?
                    metricConfig.get("physicalMetricName").asText()
                    : apiMetricName;
            String aggregationType = metricConfig.get("type").asText();
            List<TimeGrain> metricTimeGrains = metricConfig.has("timeGrains") ?
                    parseTimeGrains(metricConfig.get("timeGrains"))
                    : new ArrayList<>(tableTimeGrains);

            metricConfigs.add(
                    new MetricConfig(
                            apiMetricName,
                            physicalMetricName,
                            metricTimeGrains,
                            aggregationType
                    )
            );
        });

        return metricConfigs;
    }

    private Set<DimensionConfig> parseDimensionConfigs(JsonNode dimensions) {
        Set<DimensionConfig> dimensionConfigs = new HashSet<>();

        dimensions.forEach(dimensionConfig -> {
            String apiDimensionName = dimensionConfig.get("apiDimensionName").asText();
            String physicalDimensionName = dimensionConfig.has("physicalDimensionName") ?
                    dimensionConfig.get("physicalDimensionName").asText()
                    : apiDimensionName;
            String description = dimensionConfig.has("description") ?
                    dimensionConfig.get("description").asText()
                    : "";
            String longName = dimensionConfig.has("longName") ?
                    dimensionConfig.get("longName").asText()
                    : "";
            String category = dimensionConfig.has("category") ?
                    dimensionConfig.get("category").asText()
                    : "General";

            dimensionConfigs.add(
                    new DefaultKeyValueStoreDimensionConfig(
                            () -> apiDimensionName,
                            physicalDimensionName,
                            description,
                            longName,
                            category,
                            Utils.asLinkedHashSet(DefaultDimensionField.ID),
                            MapStoreManager.getInstance(apiDimensionName),
                            ScanSearchProviderManager.getInstance(apiDimensionName)
                    )
            );
        });

        return dimensionConfigs;
    }

    @Override
    public List<? extends DataSourceConfiguration> get() {
        return datasources;
    }
}
