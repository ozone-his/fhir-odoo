/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.model.Product;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

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
    @DisplayName("should map row to Product")
    void shouldMapRowToProduct() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(1);
        when(row.get("display_name")).thenReturn("Product A");
        when(row.get("name")).thenReturn("PA");
        when(row.get("uom_name")).thenReturn("Unit");
        when(row.get("qty_available")).thenReturn(100.0);
        when(row.get("price")).thenReturn(50.0);
        when(row.get("list_price")).thenReturn(55.0);
        when(row.get("lst_price")).thenReturn(60.0);
        when(row.get("standard_price")).thenReturn(45.0);
        when(row.get("active")).thenReturn(true);
        when(row.get("code")).thenReturn("P001");
        when(row.get("currency_id")).thenReturn(1);
        when(row.get("create_date")).thenReturn(new Date());
        when(row.get("create_uid")).thenReturn(1);
        when(row.get("write_date")).thenReturn(new Date());
        when(row.get("write_uid")).thenReturn(2);

        Product product = productService.mapRowToResource(row);

        assertNotNull(product);
        assertEquals(1, product.getId());
        assertEquals("Product A", product.getDisplayName());
        assertEquals("PA", product.getName());
        assertEquals("Unit", product.getUomName());
        assertEquals(100.0, product.getQuantityAvailable());
        assertEquals(55.0, product.getListPrice());
        assertEquals(60.0, product.getPublicPrice());
        assertEquals(45.0, product.getStandardPrice());
        assertTrue(product.isActive());
        assertEquals("P001", product.getCode());
        assertEquals(1, product.getCurrencyId());
        assertNotNull(product.getCreatedOn());
        assertEquals(1, product.getCreatedBy());
        assertNotNull(product.getLastUpdatedOn());
        assertEquals(2, product.getLastUpdatedBy());
    }

    @Test
    @DisplayName("should handle null values")
    void shouldHandleNullValues() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(1);
        when(row.get("display_name")).thenReturn(null);
        when(row.get("name")).thenReturn(null);
        when(row.get("uom_name")).thenReturn(null);
        when(row.get("qty_available")).thenReturn(null);
        when(row.get("price")).thenReturn(null);
        when(row.get("list_price")).thenReturn(null);
        when(row.get("lst_price")).thenReturn(null);
        when(row.get("standard_price")).thenReturn(null);
        when(row.get("active")).thenReturn(null);
        when(row.get("code")).thenReturn(null);
        when(row.get("currency_id")).thenReturn(null);
        when(row.get("create_date")).thenReturn(null);
        when(row.get("create_uid")).thenReturn(null);
        when(row.get("write_date")).thenReturn(null);
        when(row.get("write_uid")).thenReturn(null);

        Product product = productService.mapRowToResource(row);

        assertNotNull(product);
        assertNull(product.getDisplayName());
        assertNull(product.getName());
        assertNull(product.getUomName());
        assertNull(product.getQuantityAvailable());
        assertNull(product.getListPrice());
        assertNull(product.getPublicPrice());
        assertNull(product.getStandardPrice());
        assertFalse(product.isActive());
        assertNull(product.getCode());
        assertNull(product.getCurrencyId());
        assertNull(product.getCreatedOn());
        assertEquals(0, product.getCreatedBy());
        assertNull(product.getLastUpdatedOn());
        assertEquals(0, product.getLastUpdatedBy());
    }
}
