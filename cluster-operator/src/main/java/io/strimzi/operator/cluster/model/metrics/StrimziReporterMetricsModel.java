/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model.metrics;

import io.strimzi.api.kafka.model.common.HasConfigurableMetrics;
import io.strimzi.api.kafka.model.common.metrics.StrimziReporterMetrics;
import io.strimzi.operator.common.model.InvalidResourceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a model for components with configurable metrics using Strimzi Reporter
 */
public class StrimziReporterMetricsModel {

    /**
     * Name of the Strimzi metrics port
     */
    public static final String METRICS_PORT_NAME = "tcp-prometheus";

    /**
     * Number of the Strimzi metrics port
     */
    public static final int METRICS_PORT = 9404;
    private final boolean isEnabled;
    private final List<String> allowList;

    /**
     * Constructs the StrimziMetricsReporterModel for managing configurable metrics with Strimzi Reporter
     *
     * @param specSection StrimziReporterMetrics object containing the metrics configuration
     */
    public StrimziReporterMetricsModel(HasConfigurableMetrics specSection) {
        if (specSection.getMetricsConfig() != null) {
            if (specSection.getMetricsConfig() instanceof StrimziReporterMetrics config) {
                validate(config);
                this.isEnabled = true;
                this.allowList = config.getValues() != null &&
                        config.getValues().getAllowList() != null
                        ? config.getValues().getAllowList() : null;
            } else {
                throw new InvalidResourceException("Unsupported metrics type " + specSection.getMetricsConfig().getType());
            }
        } else {
            this.isEnabled = false;
            this.allowList = null;
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
     * @param config StrimziReporterMetrics configuration to validate
     */
    /* test */  static void validate(StrimziReporterMetrics config) {
        List<String> errors = new ArrayList<>();
        if (config.getValues() != null && config.getValues().getAllowList() != null) {
            if (config.getValues().getAllowList().isEmpty()) {
                errors.add("Allowlist should contain at least one element");
            }
            for (String regex : config.getValues().getAllowList()) {
                try {
                    Pattern.compile(regex);
                } catch (PatternSyntaxException e) {
                    errors.add(String.format("Invalid regex: %s, %s", regex, e.getDescription()));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidResourceException("Metrics configuration is invalid: " + errors);
        }
    }

    /**
     * Returns the allowlist as a comma-separated string wrapped in an Optional.
     *
     * @return an Optional containing the comma-separated allowlist if it is not null, otherwise an empty Optional
     */
    public Optional<String> getAllowList() {
        return allowList != null ? Optional.of(String.join(",", allowList)) : Optional.empty();
    }
}