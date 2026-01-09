/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

class SaleOrderServiceTest {

    @InjectMocks
    private SaleOrderService saleOrderService;

    private static AutoCloseable mockCloser;

    @BeforeEach
    void setUp() {
        mockCloser = openMocks(this);
        ReflectionTestUtils.setField(saleOrderService, "odooPartnerWeightField", "x_customer_weight");
        ReflectionTestUtils.setField(saleOrderService, "odooPartnerDobField", "x_customer_dob");
        ReflectionTestUtils.setField(saleOrderService, "odooPartnerIdField", "x_external_identifier");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        mockCloser.close();
    }

    @Test
    @DisplayName("should map row to SaleOrder")
    void shouldMapRowToSaleOrder() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(1);
        when(row.get("client_order_ref")).thenReturn("REF-001");
        when(row.get("state")).thenReturn("draft");
        when(row.get("partner_id")).thenReturn(100);
        when(row.get("type_name")).thenReturn("Sales Order");
        when(row.get("x_customer_weight")).thenReturn("70kg");
        when(row.get("x_customer_dob")).thenReturn("1990-01-01");
        when(row.get("x_external_identifier")).thenReturn("EXT-001");
        when(row.get("name")).thenReturn("SO001");
        when(row.get("display_name")).thenReturn("SO001 Display");
        when(row.get("create_date")).thenReturn(new Date());
        when(row.get("create_uid")).thenReturn(10);
        when(row.get("write_date")).thenReturn(new Date());
        when(row.get("write_uid")).thenReturn(20);

        SaleOrder saleOrder = saleOrderService.mapRowToResource(row);

        assertNotNull(saleOrder);
        assertEquals(1, saleOrder.getId());
        assertEquals("REF-001", saleOrder.getOrderClientOrderRef());
        assertEquals("draft", saleOrder.getOrderState());
        assertEquals(100, saleOrder.getOrderPartnerId());
        assertEquals("Sales Order", saleOrder.getOrderTypeName());
        assertEquals("70kg", saleOrder.getPartnerWeight());
        assertEquals("1990-01-01", saleOrder.getPartnerBirthDate());
        assertEquals("EXT-001", saleOrder.getOdooPartnerId());
        assertEquals("SO001", saleOrder.getName());
        assertEquals("SO001 Display", saleOrder.getDisplayName());
        assertNotNull(saleOrder.getCreatedOn());
        assertEquals(10, saleOrder.getCreatedBy());
        assertNotNull(saleOrder.getLastUpdatedOn());
        assertEquals(20, saleOrder.getLastUpdatedBy());
    }

    @Test
    @DisplayName("should handle null values")
    void shouldHandleNullValues() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(null);
        when(row.get("client_order_ref")).thenReturn(null);
        when(row.get("state")).thenReturn(null);
        when(row.get("partner_id")).thenReturn(0);
        when(row.get("type_name")).thenReturn(null);
        when(row.get("x_customer_weight")).thenReturn(null);
        when(row.get("x_customer_dob")).thenReturn(null);
        when(row.get("x_external_identifier")).thenReturn(null);
        when(row.get("name")).thenReturn(null);
        when(row.get("display_name")).thenReturn(null);
        when(row.get("create_date")).thenReturn(null);
        when(row.get("create_uid")).thenReturn(null);
        when(row.get("write_date")).thenReturn(null);
        when(row.get("write_uid")).thenReturn(null);

        SaleOrder saleOrder = saleOrderService.mapRowToResource(row);

        assertNotNull(saleOrder);
        assertEquals(0, saleOrder.getId());
        assertNull(saleOrder.getOrderClientOrderRef());
        assertNull(saleOrder.getOrderState());
        assertEquals(0, saleOrder.getOrderPartnerId());
        assertNull(saleOrder.getOrderTypeName());
        assertNull(saleOrder.getPartnerWeight());
        assertNull(saleOrder.getPartnerBirthDate());
        assertNull(saleOrder.getOdooPartnerId());
        assertNull(saleOrder.getName());
        assertNull(saleOrder.getDisplayName());
        assertNull(saleOrder.getCreatedOn());
        assertEquals(0, saleOrder.getCreatedBy());
        assertNull(saleOrder.getLastUpdatedOn());
        assertEquals(0, saleOrder.getLastUpdatedBy());
    }

    @Test
    @DisplayName("should handle null partner birth date")
    void shouldHandleNullPartnerBirthDate() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(1);
        when(row.get("client_order_ref")).thenReturn("REF-002");
        when(row.get("state")).thenReturn("draft");
        when(row.get("partner_id")).thenReturn(200);
        when(row.get("type_name")).thenReturn("Sales Order");
        when(row.get("x_customer_weight")).thenReturn("75kg");
        when(row.get("x_customer_dob")).thenReturn(null);
        when(row.get("x_external_identifier")).thenReturn("EXT-002");
        when(row.get("name")).thenReturn("SO002");

        SaleOrder saleOrder = saleOrderService.mapRowToResource(row);

        assertNotNull(saleOrder);
        assertNull(saleOrder.getPartnerBirthDate());
    }

    @Test
    @DisplayName("should convert SaleOrder to map correctly")
    void shouldConvertSaleOrderToMapCorrectly() {
        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(1);
        saleOrder.setOrderClientOrderRef("REF-001");
        saleOrder.setOrderState("draft");
        saleOrder.setOrderPartnerId(100);
        saleOrder.setOrderTypeName("Sales Order");
        saleOrder.setPartnerWeight("70kg");
        saleOrder.setPartnerBirthDate("1990-01-01");
        saleOrder.setOdooPartnerId("EXT-001");
        saleOrder.setName("SO001");
        saleOrder.setDisplayName("SO001 Display");
        Date createdOn = new Date();
        saleOrder.setCreatedOn(createdOn);
        saleOrder.setCreatedBy(10);
        Date lastUpdatedOn = new Date();
        saleOrder.setLastUpdatedOn(lastUpdatedOn);
        saleOrder.setLastUpdatedBy(20);

        Map<String, Object> map = saleOrderService.convertSaleOrderToMap(saleOrder);

        assertNotNull(map);
        assertEquals(1, map.get("id"));
        assertEquals("REF-001", map.get("client_order_ref"));
        assertEquals("draft", map.get("state"));
        assertEquals(100, map.get("partner_id"));
        assertEquals("Sales Order", map.get("type_name"));
        assertEquals("70kg", map.get("x_customer_weight"));
        assertEquals("1990-01-01", map.get("x_customer_dob"));
        assertEquals("EXT-001", map.get("x_external_identifier"));
        assertEquals("SO001", map.get("name"));
        assertEquals("SO001 Display", map.get("display_name"));
        assertEquals(createdOn, map.get("create_date"));
        assertEquals(10, map.get("create_uid"));
        assertEquals(lastUpdatedOn, map.get("write_date"));
        assertEquals(20, map.get("write_uid"));
    }
}
