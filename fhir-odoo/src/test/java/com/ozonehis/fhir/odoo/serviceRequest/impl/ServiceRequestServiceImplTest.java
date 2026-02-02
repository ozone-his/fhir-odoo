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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
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
        ServiceRequest serviceRequest = createServiceRequest(
                "7b968d19-7324-43d7-af3a-e5ab5ff100ff", "REQ-001", "Patient/123", "Blood Test", "26464-8");

        Partner partner = new Partner();
        partner.setId(100);
        partner.setPartnerBirthDate("1990-01-01");
        partner.setPartnerExternalId("EXT-001");

        Product product = new Product();
        product.setId(50);
        product.setName("Blood Test");
        product.setConceptCode("26464-8");

        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(200);
        saleOrder.setOrderClientOrderRef("7b968d19-7324-43d7-af3a-e5ab5ff100ff");

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setId(300);

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "7b968d19-7324-43d7-af3a-e5ab5ff100ff");

        Map<String, Object> saleOrderLineMap = new HashMap<>();
        saleOrderLineMap.put("order_id", 200);

        when(saleOrderService.getByName("REQ-001")).thenReturn(Optional.empty());
        when(partnerService.getByRef("123")).thenReturn(Optional.of(partner));
        when(saleOrderMapper.toOdoo(any())).thenReturn(saleOrder);
        when(saleOrderService.convertSaleOrderToMap(saleOrder)).thenReturn(saleOrderMap);
        when(saleOrderService.create(saleOrderMap)).thenReturn(200);
        when(productService.getByConceptCode("26464-8")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(200, 50)).thenReturn(Optional.empty());
        when(saleOrderLineMapper.toOdoo(any())).thenReturn(saleOrderLine);
        when(saleOrderLineService.convertSaleOrderLineToMap(saleOrderLine)).thenReturn(saleOrderLineMap);
        when(saleOrderLineService.create(saleOrderLineMap)).thenReturn(300);

        ServiceRequest result = serviceRequestService.create(serviceRequest);

        assertNotNull(result);
        verify(saleOrderService).getByName("REQ-001");
        verify(partnerService).getByRef("123");
        verify(saleOrderMapper).toOdoo(any());
        verify(saleOrderService).create(saleOrderMap);
        verify(productService).getByConceptCode("26464-8");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(200, 50);
        verify(saleOrderLineMapper).toOdoo(any());
        verify(saleOrderLineService).create(saleOrderLineMap);
    }

    @Test
    @DisplayName("Should create ServiceRequest with existing SaleOrder")
    void create_shouldCreateServiceRequestWithExistingSaleOrder() {
        ServiceRequest serviceRequest = createServiceRequest(
                "da981cff-b3ef-4032-9082-a296e17e7e70", "REQ-002", "Patient/456", "X-Ray", "26464-8");

        Partner partner = new Partner();
        partner.setId(200);
        partner.setPartnerBirthDate("1985-05-15");
        partner.setPartnerExternalId("EXT-002");

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(250);
        existingSaleOrder.setOrderClientOrderRef("da981cff-b3ef-4032-9082-a296e17e7e70");

        SaleOrder mappedSaleOrder = new SaleOrder();
        mappedSaleOrder.setId(0);
        mappedSaleOrder.setOrderClientOrderRef("da981cff-b3ef-4032-9082-a296e17e7e70");

        Product product = new Product();
        product.setId(60);
        product.setName("X-Ray");
        product.setConceptCode("26464-8");

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "da981cff-b3ef-4032-9082-a296e17e7e70");

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setId(350);

        Map<String, Object> saleOrderLineMap = new HashMap<>();
        saleOrderLineMap.put("order_id", 250);

        when(partnerService.getByRef("456")).thenReturn(Optional.of(partner));
        when(saleOrderMapper.toOdoo(any())).thenReturn(mappedSaleOrder);
        when(saleOrderService.convertSaleOrderToMap(mappedSaleOrder)).thenReturn(saleOrderMap);
        when(saleOrderService.getByName("REQ-002")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByConceptCode("26464-8")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(250, 60)).thenReturn(Optional.empty());
        when(saleOrderLineMapper.toOdoo(any())).thenReturn(saleOrderLine);
        when(saleOrderLineService.convertSaleOrderLineToMap(saleOrderLine)).thenReturn(saleOrderLineMap);
        when(saleOrderLineService.create(saleOrderLineMap)).thenReturn(350);

        ServiceRequest result = serviceRequestService.create(serviceRequest);

        assertNotNull(result);
        verify(saleOrderService).getByName("REQ-002");
        verify(partnerService).getByRef("456");
        verify(saleOrderMapper).toOdoo(any());
        verify(saleOrderService).convertSaleOrderToMap(mappedSaleOrder);
        verify(saleOrderService, never()).create(any());
        verify(productService).getByConceptCode("26464-8");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(250, 60);
        verify(saleOrderLineService).create(saleOrderLineMap);
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when partner does not exist")
    void create_shouldThrowUnprocessableEntityExceptionWhenPartnerDoesNotExist() {
        ServiceRequest serviceRequest = createServiceRequest(
                "d30d786d-645f-464a-95a6-b295fdd087f1", "REQ-003", "Patient/999", "CT Scan", "26464-8");

        when(partnerService.getByRef("999")).thenReturn(Optional.empty());

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderMapper, never()).toOdoo(any());
        verify(saleOrderService, never()).create(any());
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when product does not exist")
    void create_shouldThrowUnprocessableEntityExceptionWhenProductDoesNotExist() {
        ServiceRequest serviceRequest = createServiceRequest(
                "50cc7ede-0dec-46d4-bb9e-1674f1c78664", "REQ-004", "Patient/123", "Unknown Test", "26464-8");

        Partner partner = new Partner();
        partner.setId(100);

        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(200);

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "50cc7ede-0dec-46d4-bb9e-1674f1c78664");

        when(saleOrderService.getByName("REQ-004")).thenReturn(Optional.empty());
        when(partnerService.getByRef("123")).thenReturn(Optional.of(partner));
        when(saleOrderMapper.toOdoo(any())).thenReturn(saleOrder);
        when(saleOrderService.convertSaleOrderToMap(saleOrder)).thenReturn(saleOrderMap);
        when(saleOrderService.create(saleOrderMap)).thenReturn(200);
        when(productService.getByConceptCode("26464-8")).thenReturn(Optional.empty());

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderLineService, never()).getBySaleOrderIdAndProductId(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when saleOrderLine already exists")
    void create_shouldThrowUnprocessableEntityExceptionWhenSaleOrderLineAlreadyExists() {
        ServiceRequest serviceRequest = createServiceRequest(
                "06a7e887-c407-4bb7-8f5b-97d802538fe7", "REQ-005", "Patient/123", "MRI", "26464-8");

        Partner partner = new Partner();
        partner.setId(150);

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(300);
        existingSaleOrder.setOrderClientOrderRef("06a7e887-c407-4bb7-8f5b-97d802538fe7");

        SaleOrder mappedSaleOrder = new SaleOrder();
        mappedSaleOrder.setId(0);
        mappedSaleOrder.setOrderClientOrderRef("06a7e887-c407-4bb7-8f5b-97d802538fe7");

        Product product = new Product();
        product.setId(70);
        product.setName("MRI");
        product.setConceptCode("26464-8");

        SaleOrderLine existingSaleOrderLine = new SaleOrderLine();
        existingSaleOrderLine.setId(400);

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "06a7e887-c407-4bb7-8f5b-97d802538fe7");

        when(partnerService.getByRef("123")).thenReturn(Optional.of(partner));
        when(saleOrderMapper.toOdoo(any())).thenReturn(mappedSaleOrder);
        when(saleOrderService.convertSaleOrderToMap(mappedSaleOrder)).thenReturn(saleOrderMap);
        when(saleOrderService.getByName("REQ-005")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByConceptCode("26464-8")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(300, 70)).thenReturn(Optional.of(existingSaleOrderLine));

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(partnerService).getByRef("123");
        verify(saleOrderMapper).toOdoo(any());
        verify(saleOrderService).getByName("REQ-005");
        verify(productService).getByConceptCode("26464-8");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(300, 70);
        verify(saleOrderLineMapper, never()).toOdoo(any());
        verify(saleOrderLineService, never()).create(any());
    }

    @Test
    @DisplayName(
            "Should delete SaleOrderLine and cancel SaleOrder when ServiceRequest status is REVOKED with empty order lines")
    void create_shouldDeleteSaleOrderLineAndCancelSaleOrderWhenStatusIsRevokedWithEmptyOrderLines() {
        ServiceRequest serviceRequest =
                createServiceRequest("revoked-001", "REQ-REVOKED-001", "Patient/123", "Blood Test", "26464-8");
        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.REVOKED);

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(400);
        existingSaleOrder.setOrderClientOrderRef("revoked-001");
        existingSaleOrder.setOrderLine(null); // Empty order lines

        Product product = new Product();
        product.setId(80);
        product.setName("Blood Test");
        product.setConceptCode("26464-8");

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setId(500);

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "revoked-001");
        saleOrderMap.put("state", "cancel");

        when(saleOrderService.getByName("REQ-REVOKED-001")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByConceptCode("26464-8")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(400, 80)).thenReturn(Optional.of(saleOrderLine));
        when(saleOrderService.convertSaleOrderToMap(existingSaleOrder)).thenReturn(saleOrderMap);
        when(saleOrderService.update("400", saleOrderMap)).thenReturn(400);

        ServiceRequest result = serviceRequestService.create(serviceRequest);

        assertNotNull(result);
        verify(saleOrderService, times(2)).getByName("REQ-REVOKED-001");
        verify(productService).getByConceptCode("26464-8");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(400, 80);
        verify(saleOrderLineService).delete("500");
        verify(saleOrderService).convertSaleOrderToMap(existingSaleOrder);
        verify(saleOrderService).update("400", saleOrderMap);
    }

    @Test
    @DisplayName(
            "Should delete SaleOrderLine and not cancel SaleOrder when ServiceRequest status is REVOKED with remaining order lines")
    void create_shouldDeleteSaleOrderLineAndCancelSaleOrderWhenStatusIsRevokedWithRemainingOrderLines() {
        ServiceRequest serviceRequest =
                createServiceRequest("revoked-002", "REQ-REVOKED-002", "Patient/123", "Blood Test", "26464-8");
        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.REVOKED);

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(401);
        existingSaleOrder.setOrderClientOrderRef("revoked-002");
        existingSaleOrder.setOrderLine(new Object[] {1, 2}); // Has remaining order lines

        Product product = new Product();
        product.setId(81);
        product.setName("Blood Test");
        product.setConceptCode("26464-8");

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setId(502);

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "revoked-002");

        when(saleOrderService.getByName("REQ-REVOKED-002")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByConceptCode("26464-8")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(401, 81)).thenReturn(Optional.of(saleOrderLine));

        ServiceRequest result = serviceRequestService.create(serviceRequest);

        assertNotNull(result);
        verify(saleOrderService, times(2)).getByName("REQ-REVOKED-002");
        verify(productService).getByConceptCode("26464-8");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(401, 81);
        verify(saleOrderLineService).delete("502");
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when sale order doesn't exist for REVOKED status")
    void create_shouldThrowUnprocessableEntityExceptionWhenSaleOrderDoesNotExistForRevokedStatus() {
        ServiceRequest serviceRequest =
                createServiceRequest("revoked-003", "REQ-REVOKED-003", "Patient/123", "Blood Test", "26464-8");
        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.REVOKED);

        when(saleOrderService.getByName("REQ-REVOKED-003")).thenReturn(Optional.empty());

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderService).getByName("REQ-REVOKED-003");
        verify(productService, never()).getByConceptCode(anyString());
        verify(saleOrderLineService, never()).delete(anyString());
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when product doesn't exist for REVOKED status")
    void create_shouldThrowUnprocessableEntityExceptionWhenProductDoesNotExistForRevokedStatus() {
        ServiceRequest serviceRequest =
                createServiceRequest("revoked-004", "REQ-REVOKED-004", "Patient/123", "Unknown Test", "99999-9");
        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.REVOKED);

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(404);
        existingSaleOrder.setOrderClientOrderRef("revoked-004");

        when(saleOrderService.getByName("REQ-REVOKED-004")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByConceptCode("99999-9")).thenReturn(Optional.empty());

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderService).getByName("REQ-REVOKED-004");
        verify(productService).getByConceptCode("99999-9");
        verify(saleOrderLineService, never()).getBySaleOrderIdAndProductId(anyInt(), anyInt());
        verify(saleOrderLineService, never()).delete(anyString());
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when sale order line doesn't exist for REVOKED status")
    void create_shouldThrowUnprocessableEntityExceptionWhenSaleOrderLineDoesNotExistForRevokedStatus() {
        ServiceRequest serviceRequest =
                createServiceRequest("revoked-005", "REQ-REVOKED-005", "Patient/123", "Blood Test", "26464-8");
        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.REVOKED);

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(406);
        existingSaleOrder.setOrderClientOrderRef("revoked-005");

        Product product = new Product();
        product.setId(84);
        product.setName("Blood Test");
        product.setConceptCode("26464-8");

        when(saleOrderService.getByName("REQ-REVOKED-005")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByConceptCode("26464-8")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(406, 84)).thenReturn(Optional.empty());

        assertThrows(UnprocessableEntityException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderService).getByName("REQ-REVOKED-005");
        verify(productService).getByConceptCode("26464-8");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(406, 84);
        verify(saleOrderLineService, never()).delete(anyString());
    }

    @Test
    @DisplayName("Should throw InvalidRequestException when sale order update fails for REVOKED status")
    void create_shouldThrowInvalidRequestExceptionWhenSaleOrderUpdateFailsForRevokedStatus() {
        ServiceRequest serviceRequest =
                createServiceRequest("revoked-006", "REQ-REVOKED-006", "Patient/123", "Blood Test", "26464-8");
        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.REVOKED);

        SaleOrder existingSaleOrder = new SaleOrder();
        existingSaleOrder.setId(408);
        existingSaleOrder.setOrderClientOrderRef("revoked-006");
        existingSaleOrder.setOrderLine(null); // Empty order lines

        Product product = new Product();
        product.setId(86);
        product.setName("Blood Test");
        product.setConceptCode("26464-8");

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setId(506);

        Map<String, Object> saleOrderMap = new HashMap<>();
        saleOrderMap.put("client_order_ref", "revoked-006");
        saleOrderMap.put("state", "cancel");

        when(saleOrderService.getByName("REQ-REVOKED-006")).thenReturn(Optional.of(existingSaleOrder));
        when(productService.getByConceptCode("26464-8")).thenReturn(Optional.of(product));
        when(saleOrderLineService.getBySaleOrderIdAndProductId(408, 86)).thenReturn(Optional.of(saleOrderLine));
        when(saleOrderService.convertSaleOrderToMap(existingSaleOrder)).thenReturn(saleOrderMap);
        when(saleOrderService.update("408", saleOrderMap)).thenReturn(0); // Update fails

        assertThrows(InvalidRequestException.class, () -> serviceRequestService.create(serviceRequest));
        verify(saleOrderService, times(2)).getByName("REQ-REVOKED-006");
        verify(productService).getByConceptCode("26464-8");
        verify(saleOrderLineService).getBySaleOrderIdAndProductId(408, 86);
        verify(saleOrderLineService).delete("506");
        verify(saleOrderService).update("408", saleOrderMap);
    }

    private ServiceRequest createServiceRequest(
            String id, String requisitionValue, String patientRef, String serviceDisplay, String conceptCode) {
        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setId(id);

        Identifier requisition = new Identifier();
        requisition.setValue(requisitionValue);
        serviceRequest.setRequisition(requisition);

        Reference subject = new Reference();
        subject.setReference(patientRef);
        serviceRequest.setSubject(subject);

        CodeableConcept code = new CodeableConcept();
        Coding codeCoding = new Coding();
        codeCoding.setDisplay(serviceDisplay);
        codeCoding.setCode(conceptCode);
        code.addCoding(codeCoding);
        serviceRequest.setCode(code);

        CodeableConcept category = new CodeableConcept();
        Coding categoryCoding = new Coding();
        categoryCoding.setDisplay("Laboratory");
        category.addCoding(categoryCoding);
        serviceRequest.addCategory(category);

        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);

        return serviceRequest;
    }
}
