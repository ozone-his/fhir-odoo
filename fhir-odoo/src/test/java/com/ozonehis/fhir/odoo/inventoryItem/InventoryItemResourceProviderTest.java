package com.ozonehis.fhir.odoo.inventoryItem;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.fhir.InventoryItem;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class InventoryItemResourceProviderTest {

    @Mock
    private InventoryItemService inventoryItemService;

    @InjectMocks
    private InventoryItemResourceProvider inventoryItemResourceProvider;

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
    void read_shouldReturnInventoryItemWhenIdExists() {
        IdType id = new IdType("1");
        InventoryItem expectedItem = new InventoryItem();
        when(inventoryItemService.getById("1")).thenReturn(Optional.of(expectedItem));

        InventoryItem result = inventoryItemResourceProvider.read(id);

        assertEquals(expectedItem, result);
    }

    @Test
    void read_shouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        IdType id = new IdType("1");
        when(inventoryItemService.getById("1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryItemResourceProvider.read(id));
    }

    @Test
    void search_shouldReturnBundleWhenCorrectParamsIsProvided() {
        TokenAndListParam code = new TokenAndListParam();
        Bundle expectedBundle = new Bundle();
        when(inventoryItemService.searchForInventoryItems(code)).thenReturn(expectedBundle);

        Bundle result = inventoryItemResourceProvider.searchForInventoryItems(code);

        assertEquals(expectedBundle, result);
    }
}
