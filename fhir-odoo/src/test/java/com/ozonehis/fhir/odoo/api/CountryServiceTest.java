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
import com.ozonehis.fhir.odoo.model.Country;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class CountryServiceTest {

    @InjectMocks
    private CountryService countryService;

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
    @DisplayName("should map row to Country")
    void shouldMapRowToCountry() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(1);
        when(row.get("display_name")).thenReturn("United States");
        when(row.get("name")).thenReturn("United States");
        when(row.get("code")).thenReturn("US");
        when(row.get("create_date")).thenReturn(new Date());
        when(row.get("create_uid")).thenReturn(1);
        when(row.get("write_date")).thenReturn(new Date());
        when(row.get("write_uid")).thenReturn(2);

        Country country = countryService.mapRowToResource(row);

        assertNotNull(country);
        assertEquals(1, country.getId());
        assertEquals("United States", country.getDisplayName());
        assertEquals("United States", country.getName());
        assertEquals("US", country.getCountryCode());
        assertNotNull(country.getCreatedOn());
        assertEquals(1, country.getCreatedBy());
        assertNotNull(country.getLastUpdatedOn());
        assertEquals(2, country.getLastUpdatedBy());
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

        Country country = countryService.mapRowToResource(row);

        assertNotNull(country);
        assertNull(country.getDisplayName());
        assertNull(country.getName());
        assertNull(country.getCountryCode());
        assertNull(country.getCreatedOn());
        assertEquals(0, country.getCreatedBy());
        assertNull(country.getLastUpdatedOn());
        assertEquals(0, country.getLastUpdatedBy());
    }
}
