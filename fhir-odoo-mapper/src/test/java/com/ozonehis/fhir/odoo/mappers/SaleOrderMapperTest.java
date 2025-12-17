/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.Partner;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked", "rawtypes"})
class SaleOrderMapperTest {

    private SaleOrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SaleOrderMapper();
    }

    @Test
    @DisplayName("Should return null when resource map is null")
    void toOdoo_shouldReturnNullWhenResourceMapIsNull() {
        assertNull(mapper.toOdoo(null));
    }

    @Test
    @DisplayName("Should return null when resource map is empty")
    void toOdoo_shouldReturnNullWhenResourceMapIsEmpty() {
        assertNull(mapper.toOdoo(new HashMap<>()));
    }

    @Test
    @DisplayName("Should return null when serviceRequest is null")
    void toOdoo_shouldReturnNullWhenServiceRequestIsNull() {
        Map<String, Object> resourceMap = new HashMap<>();
        Partner partner = new Partner();
        partner.setId(1);

        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);

        assertNull(mapper.toOdoo(resourceMap));
    }

    @Test
    @DisplayName("Should return null when partner is null")
    void toOdoo_shouldReturnNullWhenPartnerIsNull() {
        Map<String, Object> resourceMap = new HashMap<>();
        ServiceRequest serviceRequest = new ServiceRequest();
        Identifier requisition = new Identifier();
        requisition.setValue("REQ-001");
        serviceRequest.setRequisition(requisition);

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);

        assertNull(mapper.toOdoo(resourceMap));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ServiceRequest has no requisition")
    void toOdoo_shouldThrowIllegalArgumentExceptionWhenServiceRequestHasNoRequisition() {
        Map<String, Object> resourceMap = new HashMap<>();
        ServiceRequest serviceRequest = new ServiceRequest();
        Partner partner = new Partner();
        partner.setId(1);

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);

        assertThrows(IllegalArgumentException.class, () -> mapper.toOdoo(resourceMap));
    }

    @Test
    @DisplayName("Should map ServiceRequest to SaleOrder correctly")
    void toOdoo_shouldMapServiceRequestToSaleOrderCorrectly() {
        Map<String, Object> resourceMap = new HashMap<>();

        ServiceRequest serviceRequest = new ServiceRequest();
        Identifier requisition = new Identifier();
        requisition.setValue("REQ-12345");
        serviceRequest.setRequisition(requisition);

        Partner partner = new Partner();
        partner.setId(100);
        partner.setPartnerBirthDate("1990-01-15");
        partner.setPartnerExternalId("<p>EXT-001</p>");

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);

        SaleOrder result = mapper.toOdoo(resourceMap);

        assertEquals("REQ-12345", result.getOrderClientOrderRef());
        assertEquals("Sales Order", result.getOrderTypeName());
        assertEquals("draft", result.getOrderState());
        assertEquals(100, result.getOrderPartnerId());
        assertEquals("1990-01-15", result.getPartnerBirthDate());
        assertEquals("EXT-001", result.getOdooPartnerId());
        assertEquals("Test Order", result.getName());
    }

    @Test
    @DisplayName("Should strip HTML tags from partner external ID")
    void toOdoo_shouldStripHtmlTagsFromPartnerExternalId() {
        Map<String, Object> resourceMap = new HashMap<>();

        ServiceRequest serviceRequest = new ServiceRequest();
        Identifier requisition = new Identifier();
        requisition.setValue("REQ-999");
        serviceRequest.setRequisition(requisition);

        Partner partner = new Partner();
        partner.setId(200);
        partner.setPartnerExternalId("<P>EXT-002</P>");

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);

        SaleOrder result = mapper.toOdoo(resourceMap);

        assertEquals("EXT-002", result.getOdooPartnerId());
    }

    @Test
    @DisplayName("Should handle partner external ID with mixed case HTML tags")
    void toOdoo_shouldHandlePartnerExternalIdWithMixedCaseHtmlTags() {
        Map<String, Object> resourceMap = new HashMap<>();

        ServiceRequest serviceRequest = new ServiceRequest();
        Identifier requisition = new Identifier();
        requisition.setValue("REQ-888");
        serviceRequest.setRequisition(requisition);

        Partner partner = new Partner();
        partner.setId(300);
        partner.setPartnerExternalId("<p>EXT-003</P>");

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);

        SaleOrder result = mapper.toOdoo(resourceMap);

        assertEquals("EXT-003", result.getOdooPartnerId());
    }

    @Test
    @DisplayName("Should handle null partner birth date")
    void toOdoo_shouldHandleNullPartnerBirthDate() {
        Map<String, Object> resourceMap = new HashMap<>();

        ServiceRequest serviceRequest = new ServiceRequest();
        Identifier requisition = new Identifier();
        requisition.setValue("REQ-777");
        serviceRequest.setRequisition(requisition);

        Partner partner = new Partner();
        partner.setId(400);
        partner.setPartnerBirthDate(null);
        partner.setPartnerExternalId("EXT-004");

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);

        SaleOrder result = mapper.toOdoo(resourceMap);

        assertNull(result.getPartnerBirthDate());
        assertEquals("EXT-004", result.getOdooPartnerId());
    }
}
