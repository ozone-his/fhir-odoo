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
import com.ozonehis.fhir.odoo.model.CountryState;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class CountryStateServiceTest {

    @InjectMocks
    private CountryStateService countryStateService;

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
    @DisplayName("should map row to CountryState")
    void shouldMapRowToCountryState() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(1);
        when(row.get("display_name")).thenReturn("California");
        when(row.get("name")).thenReturn("California");
        when(row.get("code")).thenReturn("CA");
        when(row.get("create_date")).thenReturn(new Date());
        when(row.get("create_uid")).thenReturn(1);
        when(row.get("write_date")).thenReturn(new Date());
        when(row.get("write_uid")).thenReturn(2);

        CountryState countryState = countryStateService.mapRowToResource(row);

        assertNotNull(countryState);
        assertEquals(1, countryState.getId());
        assertEquals("California", countryState.getDisplayName());
        assertEquals("California", countryState.getName());
        assertEquals("CA", countryState.getCountryStateCode());
        assertNotNull(countryState.getCreatedOn());
        assertEquals(1, countryState.getCreatedBy());
        assertNotNull(countryState.getLastUpdatedOn());
        assertEquals(2, countryState.getLastUpdatedBy());
    }

    @Test
    @DisplayName("should handle null values")
    void shouldHandleNullValues() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(0);
        when(row.get("display_name")).thenReturn(null);
        when(row.get("name")).thenReturn(null);
        when(row.get("code")).thenReturn(null);
        when(row.get("create_date")).thenReturn(null);
        when(row.get("create_uid")).thenReturn(null);
        when(row.get("write_date")).thenReturn(null);
        when(row.get("write_uid")).thenReturn(null);

        CountryState countryState = countryStateService.mapRowToResource(row);

        assertNotNull(countryState);
        assertNull(countryState.getDisplayName());
        assertNull(countryState.getName());
        assertNull(countryState.getCountryStateCode());
        assertNull(countryState.getCreatedOn());
        assertEquals(0, countryState.getCreatedBy());
        assertNull(countryState.getLastUpdatedOn());
        assertEquals(0, countryState.getLastUpdatedBy());
    }
}
