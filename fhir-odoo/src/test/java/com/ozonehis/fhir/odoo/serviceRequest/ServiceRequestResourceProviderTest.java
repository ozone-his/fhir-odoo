/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.serviceRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ServiceRequestResourceProviderTest {

    @Mock
    private ServiceRequestService serviceRequestService;

    @InjectMocks
    private ServiceRequestResourceProvider serviceRequestResourceProvider;

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
    @DisplayName("Should return ServiceRequest class as resource type")
    void getResourceType_shouldReturnServiceRequestClass() {
        Class<?> resourceType = serviceRequestResourceProvider.getResourceType();

        assertEquals(ServiceRequest.class, resourceType);
    }

    @Test
    @DisplayName("Should create serviceRequest and return MethodOutcome")
    void createServiceRequest_shouldCreateServiceRequestAndReturnMethodOutcome() {
        ServiceRequest inputServiceRequest = new ServiceRequest();
        inputServiceRequest.setId("123");

        ServiceRequest createdServiceRequest = new ServiceRequest();
        createdServiceRequest.setId("123");

        when(serviceRequestService.create(inputServiceRequest)).thenReturn(createdServiceRequest);

        MethodOutcome outcome = serviceRequestResourceProvider.createServiceRequest(inputServiceRequest);

        assertNotNull(outcome);
        assertNotNull(outcome.getId());
        assertEquals("ServiceRequest", outcome.getId().getResourceType());
        assertEquals("123", outcome.getId().getIdPart());
        assertEquals(createdServiceRequest, outcome.getResource());
    }

    @Test
    @DisplayName("Should update serviceRequest and return MethodOutcome")
    void update_shouldUpdateServiceRequestAndReturnMethodOutcome() {
        ServiceRequest inputServiceRequest = new ServiceRequest();
        inputServiceRequest.setId("456");

        ServiceRequest updatedServiceRequest = new ServiceRequest();
        updatedServiceRequest.setId("456");

        when(serviceRequestService.create(inputServiceRequest)).thenReturn(updatedServiceRequest);

        IdType idType = new IdType("ServiceRequest", inputServiceRequest.getIdPart());
        MethodOutcome outcome = serviceRequestResourceProvider.update(idType, inputServiceRequest);

        assertNotNull(outcome);
        assertNotNull(outcome.getId());
        assertEquals("ServiceRequest", outcome.getId().getResourceType());
        assertEquals("456", outcome.getId().getIdPart());
        assertEquals(updatedServiceRequest, outcome.getResource());
    }
}
