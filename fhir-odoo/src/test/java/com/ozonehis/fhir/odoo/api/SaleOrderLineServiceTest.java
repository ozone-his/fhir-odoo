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
import com.ozonehis.fhir.odoo.model.SaleOrderLine;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class SaleOrderLineServiceTest {

    @InjectMocks
    private SaleOrderLineService saleOrderLineService;

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
    @DisplayName("should map row to SaleOrderLine")
    void shouldMapRowToSaleOrderLine() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(1);
        when(row.get("order_id")).thenReturn(100);
        when(row.get("product_id")).thenReturn(50);
        when(row.get("product_uom_qty")).thenReturn(2.5);
        when(row.get("product_uom")).thenReturn(1);
        when(row.get("name")).thenReturn("Test Product");
        when(row.get("display_name")).thenReturn("Test Product Display");
        when(row.get("create_date")).thenReturn(new Date());
        when(row.get("create_uid")).thenReturn(10);
        when(row.get("write_date")).thenReturn(new Date());
        when(row.get("write_uid")).thenReturn(20);

        SaleOrderLine saleOrderLine = saleOrderLineService.mapRowToResource(row);

        assertNotNull(saleOrderLine);
        assertEquals(1, saleOrderLine.getId());
        assertEquals(100, saleOrderLine.getSaleOrderLineOrderId());
        assertEquals(50, saleOrderLine.getSaleOrderLineProductId());
        assertEquals(2.5, saleOrderLine.getSaleOrderLineProductUomQty());
        assertEquals(1, saleOrderLine.getSaleOrderLineProductUom());
        assertEquals("Test Product", saleOrderLine.getName());
        assertEquals("Test Product Display", saleOrderLine.getDisplayName());
        assertNotNull(saleOrderLine.getCreatedOn());
        assertEquals(10, saleOrderLine.getCreatedBy());
        assertNotNull(saleOrderLine.getLastUpdatedOn());
        assertEquals(20, saleOrderLine.getLastUpdatedBy());
    }

    @Test
    @DisplayName("should handle null values")
    void shouldHandleNullValues() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(null);
        when(row.get("order_id")).thenReturn(null);
        when(row.get("product_id")).thenReturn(null);
        when(row.get("product_uom_qty")).thenReturn(null);
        when(row.get("product_uom")).thenReturn(0);
        when(row.get("name")).thenReturn(null);
        when(row.get("display_name")).thenReturn(null);
        when(row.get("create_date")).thenReturn(null);
        when(row.get("create_uid")).thenReturn(null);
        when(row.get("write_date")).thenReturn(null);
        when(row.get("write_uid")).thenReturn(null);

        SaleOrderLine saleOrderLine = saleOrderLineService.mapRowToResource(row);

        assertNotNull(saleOrderLine);
        assertEquals(0, saleOrderLine.getId());
        assertNull(saleOrderLine.getSaleOrderLineOrderId());
        assertNull(saleOrderLine.getSaleOrderLineProductId());
        assertNull(saleOrderLine.getSaleOrderLineProductUomQty());
        assertEquals(0, saleOrderLine.getSaleOrderLineProductUom());
        assertNull(saleOrderLine.getName());
        assertNull(saleOrderLine.getDisplayName());
        assertNull(saleOrderLine.getCreatedOn());
        assertEquals(0, saleOrderLine.getCreatedBy());
        assertNull(saleOrderLine.getLastUpdatedOn());
        assertEquals(0, saleOrderLine.getLastUpdatedBy());
    }

    @Test
    @DisplayName("should convert SaleOrderLine to map correctly")
    void shouldConvertSaleOrderLineToMapCorrectly() {
        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setId(1);
        saleOrderLine.setSaleOrderLineOrderId(100);
        saleOrderLine.setSaleOrderLineProductId(50);
        saleOrderLine.setSaleOrderLineProductUomQty(3.0);
        saleOrderLine.setSaleOrderLineProductUom(1);
        saleOrderLine.setName("Test Product");
        saleOrderLine.setDisplayName("Test Product Display");
        Date createdOn = new Date();
        saleOrderLine.setCreatedOn(createdOn);
        saleOrderLine.setCreatedBy(10);
        Date lastUpdatedOn = new Date();
        saleOrderLine.setLastUpdatedOn(lastUpdatedOn);
        saleOrderLine.setLastUpdatedBy(20);

        Map<String, Object> map = saleOrderLineService.convertSaleOrderLineToMap(saleOrderLine);

        assertNotNull(map);
        assertEquals(1, map.get("id"));
        assertEquals(100, map.get("order_id"));
        assertEquals(50, map.get("product_id"));
        assertEquals(3.0, map.get("product_uom_qty"));
        assertEquals(1, map.get("product_uom"));
        assertEquals("Test Product", map.get("name"));
        assertEquals("Test Product Display", map.get("display_name"));
        assertEquals(createdOn, map.get("create_date"));
        assertEquals(10, map.get("create_uid"));
        assertEquals(lastUpdatedOn, map.get("write_date"));
        assertEquals(20, map.get("write_uid"));
    }
}
