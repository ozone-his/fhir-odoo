/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.inventoryItem;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import com.ozonehis.fhir.odoo.FhirService;

import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.fhir.InventoryItem;

public interface InventoryItemService extends FhirService<InventoryItem> {

    Bundle searchForInventoryItems(TokenAndListParam code);
}
