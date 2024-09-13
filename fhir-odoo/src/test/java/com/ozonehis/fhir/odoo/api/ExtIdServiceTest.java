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
import com.ozonehis.fhir.odoo.model.ExtId;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class ExtIdServiceTest {

    @InjectMocks
    private ExtIdService extIdService;

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
    @DisplayName("should map row to ExtId")
    void shouldMapRowToExtId() {
        Row row = mock(Row.class);
        when(row.get("model")).thenReturn("res.partner");
        when(row.get("module")).thenReturn("base");
        when(row.get("res_id")).thenReturn(1);
        when(row.get("noupdate")).thenReturn(true);
        when(row.get("reference")).thenReturn("base.res_partner_1");
        when(row.get("complete_name")).thenReturn("base.res_partner_1");
        when(row.get("name")).thenReturn("res_partner_1");
        when(row.get("display_name")).thenReturn("Partner 1");
        when(row.get("id")).thenReturn(1);
        when(row.get("create_date")).thenReturn(new Date());
        when(row.get("create_uid")).thenReturn(1);
        when(row.get("write_date")).thenReturn(new Date());
        when(row.get("write_uid")).thenReturn(2);
        when(row.get("__last_update")).thenReturn(new Date());

        ExtId extId = extIdService.mapRowToResource(row);

        assertNotNull(extId);
        assertEquals("res.partner", extId.getModel());
        assertEquals("base", extId.getModule());
        assertEquals(1, extId.getResId());
        assertTrue(extId.isUpdatable());
        assertEquals("base.res_partner_1", extId.getReference());
        assertEquals("base.res_partner_1", extId.getCompleteName());
        assertEquals("res_partner_1", extId.getName());
        assertEquals("Partner 1", extId.getDisplayName());
        assertEquals(1, extId.getId());
        assertNotNull(extId.getCreatedOn());
        assertEquals(1, extId.getCreatedBy());
        assertNotNull(extId.getLastUpdatedOn());
        assertEquals(2, extId.getLastUpdatedBy());
        assertNotNull(extId.getLastModifiedOn());
    }

    @Test
    @DisplayName("should handle null values")
    void shouldHandleNullValues() {
        Row row = mock(Row.class);
        when(row.get("model")).thenReturn(null);
        when(row.get("module")).thenReturn(null);
        when(row.get("res_id")).thenReturn(null);
        when(row.get("noupdate")).thenReturn(null);
        when(row.get("reference")).thenReturn(null);
        when(row.get("complete_name")).thenReturn(null);
        when(row.get("name")).thenReturn(null);
        when(row.get("display_name")).thenReturn(null);
        when(row.get("id")).thenReturn(null);
        when(row.get("create_date")).thenReturn(null);
        when(row.get("create_uid")).thenReturn(null);
        when(row.get("write_date")).thenReturn(null);
        when(row.get("write_uid")).thenReturn(null);
        when(row.get("__last_update")).thenReturn(null);

        ExtId extId = extIdService.mapRowToResource(row);

        assertNotNull(extId);
        assertNull(extId.getModel());
        assertNull(extId.getModule());
        assertEquals(0, extId.getResId());
        assertFalse(extId.isUpdatable());
        assertNull(extId.getReference());
        assertNull(extId.getCompleteName());
        assertNull(extId.getName());
        assertNull(extId.getDisplayName());
        assertEquals(0, extId.getId());
        assertNull(extId.getCreatedOn());
        assertEquals(0, extId.getCreatedBy());
        assertNull(extId.getLastUpdatedOn());
        assertEquals(0, extId.getLastUpdatedBy());
        assertNull(extId.getLastModifiedOn());
    }
}
