/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model.metrics;

import io.strimzi.api.kafka.model.common.metrics.StrimziReporterMetrics;
import io.strimzi.api.kafka.model.common.metrics.StrimziReporterMetricsBuilder;
import io.strimzi.api.kafka.model.common.metrics.StrimziReporterValues;
import io.strimzi.api.kafka.model.connect.KafkaConnectSpecBuilder;
import io.strimzi.operator.common.model.InvalidResourceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class StrimziMetricsReporterModelTest {

    @Test
    public void testDisabled() {
        StrimziMetricsReporterModel metrics = new StrimziMetricsReporterModel(new KafkaConnectSpecBuilder().build());

        assertThat(metrics.isEnabled(), is(false));
    }

    @Test
    public void testStrimziMetrics() {
        StrimziReporterMetrics metricsConfig = new StrimziReporterMetricsBuilder()
                .withNewValues()
                .endValues()
                .build();
        StrimziMetricsReporterModel metrics = new StrimziMetricsReporterModel(new KafkaConnectSpecBuilder().withMetricsConfig(metricsConfig).build());

        assertThat(metrics.isEnabled(), is(true));
    }

    @Test
    public void testValidAllowlist() {
        StrimziReporterValues values = new StrimziReporterValues();
        values.setAllowlist(List.of("metric1.*", "metric2.*"));

        List<String> allowlist = values.getAllowlist();
        Assertions.assertFalse(allowlist.isEmpty());
        Assertions.assertEquals("metric1.*", allowlist.get(0));
        Assertions.assertEquals("metric2.*", allowlist.get(1));
    }

    @Test
    public void testInvalidAllowlist() {
        StrimziReporterValues values = new StrimziReporterValues();

        List<String> allowlist = values.getAllowlist();
        Assertions.assertFalse(allowlist.isEmpty());
        Assertions.assertEquals(".*", allowlist.get(0));

        values.setAllowlist(List.of("[a-z", "metric2.*"));
        Assertions.assertEquals("[a-z", allowlist.get(0));

    }


    @Test
    public void testEmptyAllowlistThrowsException() {
        StrimziReporterValues metricsConfig = new StrimziReporterValues();
        metricsConfig.setAllowlist(List.of()); // Empty allowlist

        InvalidResourceException ex = assertThrows(InvalidResourceException.class, () -> {
            StrimziMetricsReporterModel.validateStrimziReporterMetricsConfiguration(metricsConfig);
        });
        assertThat(ex.getMessage(), is("StrimziReporterMetrics configuration is invalid: [Allowlist configuration is missing or empty.]"));
    }
}
