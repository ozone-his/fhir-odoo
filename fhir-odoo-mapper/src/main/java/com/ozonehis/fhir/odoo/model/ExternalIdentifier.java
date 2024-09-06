package com.ozonehis.fhir.odoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalIdentifier extends BaseOdooModel {

    @JsonProperty("complete_name")
    private String completeName;

    @JsonProperty("model")
    private String model;

    @JsonProperty("module")
    private String module;

    @JsonProperty("res_id")
    private int resId;

    @JsonProperty("noupdate")
    private boolean updatable;

    @JsonProperty("reference")
    private String reference;
}
