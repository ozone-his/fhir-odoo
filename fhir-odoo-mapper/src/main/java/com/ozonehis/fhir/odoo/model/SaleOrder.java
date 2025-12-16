/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
public class SaleOrder extends BaseOdooModel {

    @JsonProperty("client_order_ref")
    private String orderClientOrderRef;

    @JsonProperty("state")
    private String orderState;

    @JsonProperty("partner_id")
    private int orderPartnerId; // Can be used as a list or Integer

    //    @JsonProperty("order_line")
    //    private List<Integer> orderLine;

    @JsonProperty("type_name")
    private String orderTypeName;

    @JsonProperty("x_customer_weight")
    private String partnerWeight;

    @JsonProperty("x_customer_dob")
    private String partnerBirthDate;

    @JsonProperty("x_external_identifier")
    private String odooPartnerId;
}
