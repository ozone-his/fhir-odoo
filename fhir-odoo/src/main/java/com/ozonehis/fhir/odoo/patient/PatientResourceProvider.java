/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.patient;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.ozonehis.fhir.annotations.FhirOdooProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;

@FhirOdooProvider
@SuppressWarnings("unused")
public class PatientResourceProvider implements IResourceProvider {

    private final PatientService patientService;

    @Autowired
    public PatientResourceProvider(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Create
    public MethodOutcome createPatient(@ResourceParam Patient patient) {
        Patient createdPatient = patientService.create(patient);
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("Patient", createdPatient.getIdPart()));
        outcome.setResource(createdPatient);
        return outcome;
    }
}
