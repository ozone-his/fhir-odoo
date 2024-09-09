/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.chargeItemDefinition;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.ozonehis.fhir.annotations.FhirOdooProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.beans.factory.annotation.Autowired;

@FhirOdooProvider
@SuppressWarnings("unused")
public class ChargeItemDefinitionResourceProvider implements IResourceProvider {

    private final ChargeItemDefinitionService chargeItemDefinitionService;

    @Autowired
    public ChargeItemDefinitionResourceProvider(ChargeItemDefinitionService chargeItemDefinitionService) {
        this.chargeItemDefinitionService = chargeItemDefinitionService;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ChargeItemDefinition.class;
    }

    @Read
    public ChargeItemDefinition read(@IdParam IdType id) {
        return chargeItemDefinitionService
                .getById(id.getIdPart())
                .orElseThrow(() ->
                        new ResourceNotFoundException("ChargeItemDefinition with id " + id.getIdPart() + " not found"));
    }

    @Search
    public Bundle searchForChargeItemDefinitions(@OptionalParam(name = "code") TokenAndListParam code) {
        return chargeItemDefinitionService.searchForChargeItemDefinitions(code);
    }
}
