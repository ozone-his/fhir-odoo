/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class PatientResourceProviderTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientResourceProvider patientResourceProvider;

    private static AutoCloseable mockCloser;

    @BeforeEach
    void setUp() {
        mockCloser = openMocks(this);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        mockCloser.close();
    }

    @Test
    @DisplayName("Should return Patient class as resource type")
    void getResourceType_shouldReturnPatientClass() {
        Class<?> resourceType = patientResourceProvider.getResourceType();

        assertEquals(Patient.class, resourceType);
    }

    @Test
    @DisplayName("Should create patient and return MethodOutcome")
    void createPatient_shouldCreatePatientAndReturnMethodOutcome() {
        Patient inputPatient = new Patient();
        inputPatient.setId("123");

        Patient createdPatient = new Patient();
        createdPatient.setId("123");

        when(patientService.create(inputPatient)).thenReturn(createdPatient);

        MethodOutcome outcome = patientResourceProvider.update(inputPatient);

        assertNotNull(outcome);
        assertNotNull(outcome.getId());
        assertEquals("Patient", outcome.getId().getResourceType());
        assertEquals("123", outcome.getId().getIdPart());
        assertEquals(createdPatient, outcome.getResource());
    }

    @Test
    @DisplayName("Should update patient and return MethodOutcome")
    void createPatient_shouldUpdatePatientAndReturnMethodOutcome() {
        Patient inputPatient = new Patient();
        inputPatient.setId("123");

        Patient updatedPatient = new Patient();
        updatedPatient.setId("123");
        updatedPatient.setActive(false);

        when(patientService.create(inputPatient)).thenReturn(updatedPatient);

        MethodOutcome outcome =
                patientResourceProvider.update(new IdType("Patient", inputPatient.getIdPart()), inputPatient);

        assertNotNull(outcome);
        assertNotNull(outcome.getId());
        assertEquals("Patient", outcome.getId().getResourceType());
        assertEquals("123", outcome.getId().getIdPart());
        assertEquals(updatedPatient, outcome.getResource());
    }
}
