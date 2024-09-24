/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.Currency;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.Product;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class ChargeItemDefinitionMapperTest {

    private ChargeItemDefinitionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ChargeItemDefinitionMapper<>();
    }

    @Test
    @DisplayName("Should return null when resource map is null")
    void shouldReturnNullWhenResourceMapIsNull() {
        assertNull(mapper.toFhir(null));
    }

    @Test
    @DisplayName("Should return null when resource map is empty")
    void shouldReturnNullWhenResourceMapIsEmpty() {
        assertNull(mapper.toFhir(new HashMap<>()));
    }

    @Test
    @DisplayName("Should return null when product or external identifier is null")
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
        Currency currency = mock(Currency.class);

        when(product.getName()).thenReturn("Test Product");
        when(product.getDescription()).thenReturn("Test Description");
        when(product.getLastModifiedOn()).thenReturn(new java.util.Date());
        when(product.isActive()).thenReturn(true);
        when(product.getPrice()).thenReturn(100.0);
        when(extId.getName()).thenReturn("TestID");
        when(currency.getName()).thenReturn("USD");

        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);
        resourceMap.put(OdooConstants.MODEL_EXTERNAL_IDENTIFIER, extId);
        resourceMap.put(OdooConstants.MODEL_CURRENCY, currency);

        ChargeItemDefinition result = mapper.toFhir(resourceMap);

        assertNotNull(result);
        assertEquals("TestID", result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(Enumerations.PublicationStatus.ACTIVE, result.getStatus());
        assertEquals(1, result.getPropertyGroup().size());
        assertEquals(1, result.getPropertyGroup().get(0).getPriceComponent().size());
        assertEquals(
                100.0,
                result.getPropertyGroup()
                        .get(0)
                        .getPriceComponent()
                        .get(0)
                        .getAmount()
                        .getValue()
                        .doubleValue());
        assertEquals(
                "USD",
                result.getPropertyGroup()
                        .get(0)
                        .getPriceComponent()
                        .get(0)
                        .getAmount()
                        .getCurrency());
    }

    @Test
    @DisplayName("Should set status to RETIRED when product is inactive")
    void shouldSetStatusToRetiredWhenProductIsInactive() {
        var resourceMap = new HashMap<>();
        Product product = mock(Product.class);
        ExtId extId = mock(ExtId.class);
        Currency currency = mock(Currency.class);

        when(product.isActive()).thenReturn(false);
        when(product.getName()).thenReturn("Test Product");
        when(product.getDescription()).thenReturn("Test Description");
        when(product.getLastModifiedOn()).thenReturn(new java.util.Date());
        when(product.getPrice()).thenReturn(100.0);
        when(extId.getName()).thenReturn("TestID");
        when(currency.getName()).thenReturn("USD");

        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);
        resourceMap.put(OdooConstants.MODEL_EXTERNAL_IDENTIFIER, extId);
        resourceMap.put(OdooConstants.MODEL_CURRENCY, currency);

        ChargeItemDefinition result = mapper.toFhir(resourceMap);

        assertNotNull(result);
        assertEquals(Enumerations.PublicationStatus.RETIRED, result.getStatus());
    }
}
