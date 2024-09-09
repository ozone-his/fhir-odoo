/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OdooConstants {

    public static final String FHIR_OPENMRS_SYSTEM_PREFIX = "https://fhir.openmrs.org/";

    public static final String FHIR_OPENMRS_CONCEPT_SYSTEM_PREFIX = FHIR_OPENMRS_SYSTEM_PREFIX + "concept-system/";

    public static final String FHIR_OPENMRS_INVENTORY_ITEM = FHIR_OPENMRS_CONCEPT_SYSTEM_PREFIX + "inventory-item";


    public static final String MODEL_EXTERNAL_IDENTIFIER = "ir.model.data";

    public static final String MODEL_PRODUCT = "product.product";

    public static final String MODEL_CURRENCY = "res.currency";
}
