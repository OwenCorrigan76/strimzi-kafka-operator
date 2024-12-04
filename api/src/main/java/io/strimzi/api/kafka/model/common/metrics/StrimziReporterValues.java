/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.api.kafka.model.common.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.strimzi.api.kafka.model.common.Constants;
import io.strimzi.api.kafka.model.common.UnknownPropertyPreserving;
import io.strimzi.crdgenerator.annotations.Description;
import io.sundr.builder.annotations.Buildable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing the values section in the YAML.
 */
@Buildable(
        editableEnabled = false,
        builderPackage = Constants.FABRIC8_KUBERNETES_API
)
@JsonPropertyOrder({"allowList"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class StrimziReporterValues implements UnknownPropertyPreserving {

    private Map<String, Object> additionalProperties;
    private final String regex = ".*";
    private List<String> allowList = List.of(regex); // Default value

    @Description("Get a list of only non-null allowed metrics for the Strimzi Metrics Reporter.")
    public List<String> getAllowList() {
        return allowList;
    }

    @Description("Set a list of allowed metrics for the Strimzi Metrics Reporter.")
    public void setAllowlist(List<String> allowList) {
        this.allowList = allowList;
    }

    @Override
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties != null ? this.additionalProperties : Map.of();
    }

    @Override
    public void setAdditionalProperty(String name, Object value) {
        if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap<>(2);
        }
        this.additionalProperties.put(name, value);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StrimziReporterValues that = (StrimziReporterValues) o;

        if (!regex.equals(that.regex)) return false;
        if (!allowList.equals(that.allowList)) return false;
        return additionalProperties != null ? additionalProperties.equals(that.additionalProperties) : that.additionalProperties == null;
    }

    @Override
    public int hashCode() {
        int result = regex.hashCode();
        result = 31 * result + (allowList != null ? allowList.hashCode() : 0);
        result = 31 * result + (additionalProperties != null ? additionalProperties.hashCode() : 0);
        return result;
    }

}
