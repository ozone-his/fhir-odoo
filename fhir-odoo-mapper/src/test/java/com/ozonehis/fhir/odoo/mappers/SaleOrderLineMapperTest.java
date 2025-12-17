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

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.Product;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import com.ozonehis.fhir.odoo.model.SaleOrderLine;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked", "rawtypes"})
class SaleOrderLineMapperTest {

    private SaleOrderLineMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SaleOrderLineMapper();
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
        Product product = new Product();
        product.setId(1);
        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(100);

        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);
        resourceMap.put(OdooConstants.MODEL_SALE_ORDER, saleOrder);

        assertNull(mapper.toOdoo(resourceMap));
    }

    @Test
    @DisplayName("Should return null when product is null")
    void toOdoo_shouldReturnNullWhenProductIsNull() {
        Map<String, Object> resourceMap = new HashMap<>();
        ServiceRequest serviceRequest = new ServiceRequest();
        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(100);

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_SALE_ORDER, saleOrder);

        assertNull(mapper.toOdoo(resourceMap));
    }

    @Test
    @DisplayName("Should return null when saleOrder is null")
    void toOdoo_shouldReturnNullWhenSaleOrderIsNull() {
        Map<String, Object> resourceMap = new HashMap<>();
        ServiceRequest serviceRequest = new ServiceRequest();
        Product product = new Product();
        product.setId(1);

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);

        assertNull(mapper.toOdoo(resourceMap));
    }

    @Test
    @DisplayName("Should map ServiceRequest to SaleOrderLine correctly")
    void toOdoo_shouldMapServiceRequestToSaleOrderLineCorrectly() {
        Map<String, Object> resourceMap = new HashMap<>();

        ServiceRequest serviceRequest = new ServiceRequest();
        CodeableConcept category = new CodeableConcept();
        Coding categoryCoding = new Coding();
        categoryCoding.setDisplay("Laboratory");
        category.addCoding(categoryCoding);
        serviceRequest.addCategory(category);

        CodeableConcept code = new CodeableConcept();
        Coding codeCoding = new Coding();
        codeCoding.setDisplay("Blood Test");
        code.addCoding(codeCoding);
        serviceRequest.setCode(code);

        Product product = new Product();
        product.setId(50);

        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(200);

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);
        resourceMap.put(OdooConstants.MODEL_SALE_ORDER, saleOrder);

        SaleOrderLine result = mapper.toOdoo(resourceMap);

        assertEquals(1.0, result.getSaleOrderLineProductUomQty());
        assertEquals("Blood Test (Laboratory)", result.getName());
        assertEquals(50, result.getSaleOrderLineProductId());
        assertEquals(200, result.getSaleOrderLineOrderId());
        assertEquals(1, result.getSaleOrderLineProductUom());
    }

    @Test
    @DisplayName("Should handle ServiceRequest with multiple category codings")
    void toOdoo_shouldHandleServiceRequestWithMultipleCategoryCodings() {
        Map<String, Object> resourceMap = new HashMap<>();

        ServiceRequest serviceRequest = new ServiceRequest();
        CodeableConcept category = new CodeableConcept();
        Coding categoryCoding = new Coding();
        categoryCoding.setDisplay("Imaging");
        category.addCoding(categoryCoding);
        serviceRequest.addCategory(category);

        CodeableConcept code = new CodeableConcept();
        Coding codeCoding = new Coding();
        codeCoding.setDisplay("X-Ray");
        code.addCoding(codeCoding);
        serviceRequest.setCode(code);

        Product product = new Product();
        product.setId(75);

        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(300);

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);
        resourceMap.put(OdooConstants.MODEL_SALE_ORDER, saleOrder);

        SaleOrderLine result = mapper.toOdoo(resourceMap);

        assertEquals("X-Ray (Imaging)", result.getName());
        assertEquals(75, result.getSaleOrderLineProductId());
        assertEquals(300, result.getSaleOrderLineOrderId());
    }
}
