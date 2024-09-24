/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.chargeItemDefinition;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import com.ozonehis.fhir.odoo.FhirService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ChargeItemDefinition;

public interface ChargeItemDefinitionService extends FhirService<ChargeItemDefinition> {

    /**
     * Search for ChargeItemDefinitions by code
     *
     * @param code TokenAndListParam code to search for
     * @return Bundle of ChargeItemDefinitions
     */
    Bundle searchForChargeItemDefinitions(TokenAndListParam code);
}
