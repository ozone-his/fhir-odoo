/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
