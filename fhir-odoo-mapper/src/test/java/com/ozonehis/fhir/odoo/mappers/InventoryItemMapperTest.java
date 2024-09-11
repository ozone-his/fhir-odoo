/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.Product;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openmrs.fhir.InventoryItem;

@SuppressWarnings({"unchecked", "rawtypes"})
class InventoryItemMapperTest {

    private InventoryItemMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InventoryItemMapper<>();
    }

    @Test
    void shouldReturnNullWhenResourceMapIsNull() {
        assertNull(mapper.toFhir(null));
    }

    @Test
    void shouldReturnNullWhenResourceMapIsEmpty() {
        assertNull(mapper.toFhir(new HashMap<>()));
    }

    @Test
    void shouldReturnNullWhenProductOrExternalIdentifierIsNull() {
        var resourceMap = new HashMap<>();
        resourceMap.put(OdooConstants.MODEL_PRODUCT, null);
        resourceMap.put(OdooConstants.MODEL_EXTERNAL_IDENTIFIER, null);
        assertNull(mapper.toFhir(resourceMap));
    }

    @Test
    @DisplayName("Should map fields correctly")
    void shouldMapFieldsCorrectly() {
        var resourceMap = new HashMap<>();
        Product product = mock(Product.class);
        ExtId extId = mock(ExtId.class);

        when(product.getName()).thenReturn("Test Product");
        when(product.getDescription()).thenReturn("Test Description");
        when(product.isActive()).thenReturn(true);
        when(product.getQuantityAvailable()).thenReturn(50.0);
        when(product.getUomName()).thenReturn("units");
        when(product.getDisplayName()).thenReturn("Test Display Name");
        when(product.getCode()).thenReturn("TP001");
        when(extId.getName()).thenReturn("TestID");

        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);
        resourceMap.put(OdooConstants.MODEL_EXTERNAL_IDENTIFIER, extId);

        InventoryItem result = mapper.toFhir(resourceMap);

        assertNotNull(result);
        assertEquals("TestID", result.getId());
        assertEquals("Test Product", result.getName().get(0).getName());
        assertEquals("Test Description", result.getDescription().getDescription());
        assertEquals(InventoryItem.InventoryItemStatusCodes.ACTIVE, result.getStatus());
        assertEquals(50.0, result.getNetContent().getValue().doubleValue());
        assertEquals("units", result.getNetContent().getUnit());
        assertEquals("Test Display Name", result.getCode().get(0).getText());
        assertEquals("TestID", result.getCode().get(0).getCoding().get(0).getCode());
        assertEquals(
                "Test Display Name", result.getCode().get(0).getCoding().get(0).getDisplay());
    }

    @Test
    @DisplayName("Should set status to INACTIVE when product is inactive")
    void shouldSetStatusToInactiveWhenProductIsInactive() {
        var resourceMap = new HashMap<>();
        Product product = mock(Product.class);
        ExtId extId = mock(ExtId.class);

        when(product.isActive()).thenReturn(false);
        when(product.getName()).thenReturn("Test Product");
        when(product.getDescription()).thenReturn("Test Description");
        when(product.getQuantityAvailable()).thenReturn(50.0);
        when(product.getUomName()).thenReturn("units");
        when(product.getDisplayName()).thenReturn("Test Display Name");
        when(product.getCode()).thenReturn("TP001");
        when(extId.getName()).thenReturn("TestID");

        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);
        resourceMap.put(OdooConstants.MODEL_EXTERNAL_IDENTIFIER, extId);

        InventoryItem result = mapper.toFhir(resourceMap);

        assertNotNull(result);
        assertEquals(InventoryItem.InventoryItemStatusCodes.INACTIVE, result.getStatus());
    }
}
