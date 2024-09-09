package com.ozonehis.fhir.odoo.chargeItemDefinition;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.hl7.fhir.r4.model.IdType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class ChargeItemDefinitionResourceProviderTest {

    @Mock
    private ChargeItemDefinitionService chargeItemDefinitionService;

    @InjectMocks
    private ChargeItemDefinitionResourceProvider chargeItemDefinitionResourceProvider;

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
    @DisplayName("Should return ChargeItemDefinition when id exists")
    void read_shouldReturnChargeItemDefinitionWhenIdExists() {
        IdType id = new IdType("123");
        ChargeItemDefinition chargeItemDefinition = new ChargeItemDefinition();
        when(chargeItemDefinitionService.getById("123")).thenReturn(Optional.of(chargeItemDefinition));

        ChargeItemDefinition result = chargeItemDefinitionResourceProvider.read(id);

        assertNotNull(result);
        assertEquals(chargeItemDefinition, result);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when id does not exist")
    void read_shouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        IdType id = new IdType("123");
        when(chargeItemDefinitionService.getById("123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chargeItemDefinitionResourceProvider.read(id));
    }

    @Test
    @DisplayName("Should return ChargeItemDefinition when code is provided")
    void search_shouldReturnBundleWhenCodeIsProvided() {
        TokenAndListParam code = new TokenAndListParam();
        Bundle bundle = new Bundle();
        when(chargeItemDefinitionService.searchForChargeItemDefinitions(code)).thenReturn(bundle);

        Bundle result = chargeItemDefinitionResourceProvider.searchForChargeItemDefinitions(code);

        assertNotNull(result);
        assertEquals(bundle, result);
    }

    @Test
    @DisplayName("Should return empty bundle when code is null")
    void search_shouldReturnEmptyBundleWhenCodeIsNull() {
        TokenAndListParam code = null;
        Bundle bundle = new Bundle();
        when(chargeItemDefinitionService.searchForChargeItemDefinitions(code)).thenReturn(bundle);

        Bundle result = chargeItemDefinitionResourceProvider.searchForChargeItemDefinitions(code);

        assertNotNull(result);
        assertEquals(bundle, result);
    }
}
