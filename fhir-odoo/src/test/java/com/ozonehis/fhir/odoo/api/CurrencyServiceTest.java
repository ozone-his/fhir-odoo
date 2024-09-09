package com.ozonehis.fhir.odoo.api;

import com.odoojava.api.Row;
import com.ozonehis.fhir.FhirOdooConfig;
import com.ozonehis.fhir.odoo.model.Currency;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class CurrencyServiceTest {

    @Mock
    private FhirOdooConfig fhirOdooConfig;

    @InjectMocks
    private CurrencyService currencyService;

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
    void mapRowToResource_shouldMapRowToCurrency() {
        Row row = mock(Row.class);
        when(row.get("active")).thenReturn(true);
        when(row.get("name")).thenReturn("USD");
        when(row.get("display_name")).thenReturn("US Dollar");
        when(row.get("currency_unit_label")).thenReturn("Dollar");
        when(row.get("currency_subunit_label")).thenReturn("Cent");
        when(row.get("decimal_places")).thenReturn(2);
        when(row.get("create_date")).thenReturn(new Date());
        when(row.get("create_uid")).thenReturn(1);
        when(row.get("write_date")).thenReturn(new Date());
        when(row.get("write_uid")).thenReturn(2);
        when(row.get("__last_update")).thenReturn(new Date());

        Currency currency = currencyService.mapRowToResource(row);

        assertNotNull(currency);
        assertTrue(currency.isActive());
        assertEquals("USD", currency.getName());
        assertEquals("US Dollar", currency.getDisplayName());
        assertEquals("Dollar", currency.getCurrencyUnitLabel());
        assertEquals("Cent", currency.getCurrencySubunitLabel());
        assertEquals(2, currency.getDecimalPlaces());
        assertNotNull(currency.getCreatedOn());
        assertEquals(1, currency.getCreatedBy());
        assertNotNull(currency.getLastUpdatedOn());
        assertEquals(2, currency.getLastUpdatedBy());
        assertNotNull(currency.getLastModifiedOn());
    }

    @Test
    void mapRowToResource_shouldHandleNullValues() {
        Row row = mock(Row.class);
        when(row.get("active")).thenReturn(null);
        when(row.get("name")).thenReturn(null);
        when(row.get("display_name")).thenReturn(null);
        when(row.get("currency_unit_label")).thenReturn(null);
        when(row.get("currency_subunit_label")).thenReturn(null);
        when(row.get("decimal_places")).thenReturn(null);
        when(row.get("create_date")).thenReturn(null);
        when(row.get("create_uid")).thenReturn(null);
        when(row.get("write_date")).thenReturn(null);
        when(row.get("write_uid")).thenReturn(null);
        when(row.get("__last_update")).thenReturn(null);

        Currency currency = currencyService.mapRowToResource(row);

        assertNotNull(currency);
        assertFalse(currency.isActive());
        assertNull(currency.getName());
        assertNull(currency.getDisplayName());
        assertNull(currency.getCurrencyUnitLabel());
        assertNull(currency.getCurrencySubunitLabel());
        assertEquals(0, currency.getDecimalPlaces());
        assertNull(currency.getCreatedOn());
        assertNull(currency.getCreatedBy());
        assertNull(currency.getLastUpdatedOn());
        assertNull(currency.getLastUpdatedBy());
        assertNull(currency.getLastModifiedOn());
    }
}
