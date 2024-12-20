/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model.metrics;

import io.strimzi.api.kafka.model.common.metrics.StrimziReporterMetrics;
import io.strimzi.api.kafka.model.common.metrics.StrimziReporterMetricsBuilder;
import io.strimzi.api.kafka.model.common.metrics.StrimziReporterValues;
import io.strimzi.api.kafka.model.connect.KafkaConnectSpecBuilder;
import io.strimzi.api.kafka.model.kafka.KafkaClusterSpecBuilder;
import io.strimzi.operator.common.model.InvalidResourceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrimziMetricsReporterModelTest {

    @Test
    public void testDisabled() {
        StrimziMetricsReporterModel metrics = new StrimziMetricsReporterModel(new KafkaConnectSpecBuilder().build());

        assertThat(metrics.isEnabled(), is(false));
        assertThat(metrics.getAllowList(), is(Optional.empty()));
        assertTrue(metrics.getAllowList().isEmpty());
    }

    @Test
    public void testStrimziMetrics() {
        StrimziReporterMetrics metricsConfig = new StrimziReporterMetricsBuilder()
                .withNewValues()
                    .withAllowList(List.of("kafka_log.*", "kafka_network.*"))
                .endValues()
                .build();
        StrimziMetricsReporterModel metrics = new StrimziMetricsReporterModel(new KafkaClusterSpecBuilder()
                .withMetricsConfig(metricsConfig).build());

        assertThat(metrics.isEnabled(), is(true));
        assertTrue(metrics.getAllowList().isPresent());
        assertThat(metrics.getAllowList().get(), is("kafka_log.*,kafka_network.*"));
    }

    @Test
    public void testValidAllowlist() {
        StrimziReporterValues values = new StrimziReporterValues();
        values.setAllowList(List.of("kafka_log.*", "kafka_network.*"));

        List<String> allowlist = values.getAllowList();
        Assertions.assertFalse(allowlist.isEmpty());
        Assertions.assertEquals("metric1.*", allowlist.get(0));
        Assertions.assertEquals("metric2.*", allowlist.get(1));
    }

    @Test
    public void testValidation() {
        // if I pass valid list, doesn't throw error
        Assertions.assertDoesNotThrow(() -> StrimziMetricsReporterModel.validate(new StrimziReporterMetricsBuilder()
                .withNewValues()
                .withAllowList(List.of("kafka_log.*", "kafka_network.*"))
                .endValues()
                .build())
        );

        InvalidResourceException ise0 = Assertions.assertThrows(InvalidResourceException.class, () -> StrimziMetricsReporterModel.validate(
                new StrimziReporterMetricsBuilder()
                        .withNewValues()
                        .withAllowList(List.of())
                        .endValues()
                        .build())
        );
        assertThat(ise0.getMessage(), is("Metrics configuration is invalid: [Allowlist should contain at least one element]"));


        InvalidResourceException ise1 = Assertions.assertThrows(InvalidResourceException.class, () -> StrimziMetricsReporterModel.validate(
                new StrimziReporterMetricsBuilder()
                        .withNewValues()
                        .withAllowList(List.of("kafka_network.*", "kafka_log.***", "[a+"))
                        .endValues()
                        .build())
        );
        assertThat(ise1.getMessage(), is("Metrics configuration is invalid: [Invalid regex: kafka_log.***, Dangling meta character '*', Invalid regex: [a+, Unclosed character class]"));
    }

}
