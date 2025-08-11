/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.medication;

import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.ozonehis.fhir.annotations.FhirOdooProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Medication;
import org.springframework.beans.factory.annotation.Autowired;

@FhirOdooProvider
@SuppressWarnings("unused")
public class MedicationResourceProvider implements IResourceProvider {

    private MedicationService service;

    @Autowired
    public MedicationResourceProvider(MedicationService service) {
        this.service = service;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Medication.class;
    }

    @Search
    public Bundle getAllMedications() {
        return service.getAllMedications();
    }

}
