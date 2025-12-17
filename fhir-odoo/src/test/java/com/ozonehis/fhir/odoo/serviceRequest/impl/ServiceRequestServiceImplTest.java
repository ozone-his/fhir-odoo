/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.serviceRequest.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.ozonehis.fhir.odoo.api.PartnerService;
import com.ozonehis.fhir.odoo.api.ProductService;
import com.ozonehis.fhir.odoo.api.SaleOrderLineService;
import com.ozonehis.fhir.odoo.api.SaleOrderService;
import com.ozonehis.fhir.odoo.mappers.SaleOrderLineMapper;
import com.ozonehis.fhir.odoo.mappers.SaleOrderMapper;
import com.ozonehis.fhir.odoo.model.Partner;
import com.ozonehis.fhir.odoo.model.Product;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import com.ozonehis.fhir.odoo.model.SaleOrderLine;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class ServiceRequestServiceImplTest {

    @Mock
    private SaleOrderService saleOrderService;

    @Mock
    private SaleOrderLineService saleOrderLineService;

    @Mock
    private SaleOrderLineMapper saleOrderLineMapper;

    @Mock
    private SaleOrderMapper saleOrderMapper;

    @Mock
    private PartnerService partnerService;

    @Mock
    private ProductService productService;

    private ServiceRequestServiceImpl serviceRequestService;

    @BeforeEach
    void setUp() {
        serviceRequestService = new ServiceRequestServiceImpl(
                saleOrderService,
                saleOrderLineService,
                saleOrderLineMapper,
                saleOrderMapper,
                partnerService,
                productService);
    }

    @Test
    @DisplayName("Should create ServiceRequest with new SaleOrder and SaleOrderLine")
    void create_shouldCreateServiceRequestWithNewSaleOrderAndSaleOrderLine() {
        ServiceRequest serviceRequest = createServiceRequest("REQ-001", "Patient/123", "Blood Test");

        Partner partner = new Partner();
        partner.setId(100);
        partner.setPartnerBirthDate("1990-01-01");
        partner.setPartnerExternalId("EXT-001");

        Product product = new Product();
        product.setId(50);
        product.setName("Blood Test");

        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(200);
        saleOrder.setOrderClientOrderRef("REQ-001");

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setId(300);

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "REQ-001");

        Map<String, Object> saleOrderLineMap = new HashMap<>();
        saleOrderLineMap.put("order_id", 200);

        when(saleOrderService.getByOrderRef("REQ-001")).thenReturn(Optional.empty());
        when(partnerService.getByRef("123")).thenReturn(Optional.of(partner));
        when(saleOrderMapper.toOdoo(any())).thenReturn(saleOrder);
        when(saleOrderService.convertSaleOrderToMap(saleOrder)).thenReturn(saleOrderMap);
        when(saleOrderService.create(saleOrderMap)).thenReturn(200);
        when(productService.getByName("Blood Test")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(200, 50)).thenReturn(Optional.empty());
        when(saleOrderLineMapper.toOdoo(any())).thenReturn(saleOrderLine);
        when(saleOrderLineService.convertSaleOrderLineToMap(saleOrderLine)).thenReturn(saleOrderLineMap);
        when(saleOrderLineService.create(saleOrderLineMap)).thenReturn(300);

        ServiceRequest result = serviceRequestService.create(serviceRequest);

        assertNotNull(result);
        verify(saleOrderService).getByOrderRef("REQ-001");
        verify(partnerService).getByRef("123");
        verify(saleOrderMapper).toOdoo(any());
        verify(saleOrderService).create(saleOrderMap);
        verify(productService).getByName("Blood Test");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(200, 50);
        verify(saleOrderLineMapper).toOdoo(any());
        verify(saleOrderLineService).create(saleOrderLineMap);
    }

    @Test
    @DisplayName("Should create ServiceRequest with existing SaleOrder")
    void create_shouldCreateServiceRequestWithExistingSaleOrder() {
        ServiceRequest serviceRequest = createServiceRequest("REQ-002", "Patient/456", "X-Ray");

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(250);
        existingSaleOrder.setOrderClientOrderRef("REQ-002");

        Product product = new Product();
        product.setId(60);
        product.setName("X-Ray");

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setId(350);

        Map<String, Object> saleOrderLineMap = new HashMap<>();
        saleOrderLineMap.put("order_id", 250);

        when(saleOrderService.getByOrderRef("REQ-002")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByName("X-Ray")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(250, 60)).thenReturn(Optional.empty());
        when(saleOrderLineMapper.toOdoo(any())).thenReturn(saleOrderLine);
        when(saleOrderLineService.convertSaleOrderLineToMap(saleOrderLine)).thenReturn(saleOrderLineMap);
        when(saleOrderLineService.create(saleOrderLineMap)).thenReturn(350);

        ServiceRequest result = serviceRequestService.create(serviceRequest);

        assertNotNull(result);
        verify(saleOrderService).getByOrderRef("REQ-002");
        verify(partnerService, never()).getByRef(anyString());
        verify(saleOrderMapper, never()).toOdoo(any());
        verify(saleOrderService, never()).create(any());
        verify(productService).getByName("X-Ray");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(250, 60);
        verify(saleOrderLineService).create(saleOrderLineMap);
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when partner does not exist")
    void create_shouldThrowUnprocessableEntityExceptionWhenPartnerDoesNotExist() {
        ServiceRequest serviceRequest = createServiceRequest("REQ-003", "Patient/999", "CT Scan");

        when(saleOrderService.getByOrderRef("REQ-003")).thenReturn(Optional.empty());
        when(partnerService.getByRef("999")).thenReturn(Optional.empty());

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderMapper, never()).toOdoo(any());
        verify(saleOrderService, never()).create(any());
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when product does not exist")
    void create_shouldThrowUnprocessableEntityExceptionWhenProductDoesNotExist() {
        ServiceRequest serviceRequest = createServiceRequest("REQ-004", "Patient/123", "Unknown Test");

        Partner partner = new Partner();
        partner.setId(100);

        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(200);

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "REQ-004");

        when(saleOrderService.getByOrderRef("REQ-004")).thenReturn(Optional.empty());
        when(partnerService.getByRef("123")).thenReturn(Optional.of(partner));
        when(saleOrderMapper.toOdoo(any())).thenReturn(saleOrder);
        when(saleOrderService.convertSaleOrderToMap(saleOrder)).thenReturn(saleOrderMap);
        when(saleOrderService.create(saleOrderMap)).thenReturn(200);
        when(productService.getByName("Unknown Test")).thenReturn(Optional.empty());

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderLineService, never()).getBySaleOrderIdAndProductId(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when saleOrderLine already exists")
    void create_shouldThrowUnprocessableEntityExceptionWhenSaleOrderLineAlreadyExists() {
        ServiceRequest serviceRequest = createServiceRequest("REQ-005", "Patient/123", "MRI");

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(300);

        Product product = new Product();
        product.setId(70);
        product.setName("MRI");

        SaleOrderLine existingSaleOrderLine = new SaleOrderLine();
        existingSaleOrderLine.setId(400);

        when(saleOrderService.getByOrderRef("REQ-005")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByName("MRI")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(300, 70)).thenReturn(Optional.of(existingSaleOrderLine));

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderLineMapper, never()).toOdoo(any());
        verify(saleOrderLineService, never()).create(any());
    }

    private ServiceRequest createServiceRequest(String requisitionValue, String patientRef, String serviceDisplay) {
        ServiceRequest serviceRequest = new ServiceRequest();

        Identifier requisition = new Identifier();
        requisition.setValue(requisitionValue);
        serviceRequest.setRequisition(requisition);

        Reference subject = new Reference();
        subject.setReference(patientRef);
        serviceRequest.setSubject(subject);

        CodeableConcept code = new CodeableConcept();
        Coding codeCoding = new Coding();
        codeCoding.setDisplay(serviceDisplay);
        code.addCoding(codeCoding);
        serviceRequest.setCode(code);

        CodeableConcept category = new CodeableConcept();
        Coding categoryCoding = new Coding();
        categoryCoding.setDisplay("Laboratory");
        category.addCoding(categoryCoding);
        serviceRequest.addCategory(category);

        return serviceRequest;
    }
}
