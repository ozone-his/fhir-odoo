/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.patient;

import com.ozonehis.fhir.odoo.FhirService;
import org.hl7.fhir.r4.model.Patient;

public interface PatientService extends FhirService<Patient> {

    /**
     * Create Partner by FHIR Patient
     *
     * @param patient Patient FHIR resource
     */
    Patient create(Patient patient);
}
