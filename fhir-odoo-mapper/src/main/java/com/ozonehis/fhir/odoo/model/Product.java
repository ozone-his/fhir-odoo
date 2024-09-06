package com.ozonehis.fhir.odoo.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product extends BaseOdooModel {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("description")
    private String description;

    @JsonProperty("uom_name")
    private String uomName;

    // Quantity on hand
    @JsonProperty("qty_available")
    private Double quantityAvailable;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("list_price")
    private Double listPrice;

    @JsonProperty("lst_price")
    private Double publicPrice;

    @JsonProperty("active")
    @JsonFormat(shape = JsonFormat.Shape.BOOLEAN)
    private boolean active;

    @JsonProperty("code")
    private String code;

    @JsonProperty("currency_id")
    private Integer currencyId;

}
