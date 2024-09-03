package com.ozonehis.fhir.odoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product implements OdooResource {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Integer productId;

    @JsonProperty("display_name")
    private String productDisplayName; // Product Name

    @JsonProperty("name")
    private String productName; // Product ID

    @JsonProperty("res_id")
    private Integer productResId;

}
