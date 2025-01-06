/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model.metrics;

import io.strimzi.api.kafka.model.common.HasConfigurableMetrics;
import io.strimzi.api.kafka.model.common.metrics.StrimziReporterMetrics;
import io.strimzi.api.kafka.model.common.metrics.StrimziReporterValues;
import io.strimzi.kafka.oauth.common.ConfigException;
import io.strimzi.operator.common.model.InvalidResourceException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Represents a model for components with configurable metrics using Strimzi Reporter
 */
public class StrimziMetricsReporterModel {

    /**
     * Name of the Strimzi metrics port
     */
    public static final String METRICS_PORT_NAME = "http-prometheus";

    /**
     * Number of the Strimzi metrics port
     */
    public static final int METRICS_PORT = 9404;
    private final boolean isEnabled;
    private final List<String> allowlist;

    /**
     * Constructs the StrimziMetricsReporterModel for managing configurable metrics with Strimzi Reporter
     *
     * @param specSection StrimziReporterMetrics object containing the metrics configuration
     */
    public StrimziMetricsReporterModel(HasConfigurableMetrics specSection) {
        if (specSection.getMetricsConfig() != null) {
            if (specSection.getMetricsConfig() instanceof StrimziReporterMetrics strimziMetrics) {
                validateStrimziReporterMetricsConfiguration(strimziMetrics.getValues());
                this.isEnabled = true;
                this.allowlist = strimziMetrics.getValues().getAllowlist();
            } else {
                throw new ConfigException("Unsupported metrics type " + specSection.getMetricsConfig().getType());
            }
        } else {
            this.isEnabled = false;
            this.allowlist = null;
        }
    }

    /**
     * @return True if metrics are enabled. False otherwise.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Validates the Strimzi Reporter Metrics configuration
     *
     * @param metricsConfig StrimziReporterMetrics configuration to validate
     */
    static void validateStrimziReporterMetricsConfiguration(StrimziReporterValues metricsConfig) {
        List<String> errors = new ArrayList<>();
        List<String> allowList = metricsConfig.getAllowlist();

        if (allowList == null || allowList.isEmpty()) {
            errors.add("Allowlist configuration is missing or empty.");
        } else {
            for (String regex : allowList) {
                try {
                    Pattern.compile(regex); // Attempt to compile the regex
                } catch (PatternSyntaxException e) {
                    System.out.println("Exception message: " + e.getMessage());
                    errors.add("Invalid regular expression in allowlist: " + regex + " - " + e.getMessage());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidResourceException("StrimziReporterMetrics configuration is invalid: " + errors);
        }
    }
}
