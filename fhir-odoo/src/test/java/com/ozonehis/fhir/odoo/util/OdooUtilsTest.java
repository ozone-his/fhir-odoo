/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.odoojava.api.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OdooUtilsTest {

    private Row row;

    @BeforeEach
    void setUp() {
        row = Mockito.mock(Row.class);
    }

    @Test
    void getOrElse_shouldReturnValueWhenKeyExists() {
        when(row.get("existingKey")).thenReturn("value");

        String result = OdooUtils.getOrElse(row, "existingKey", "defaultValue");

        assertEquals("value", result);
    }

    @Test
    void getOrElse_shouldReturnDefaultValueWhenKeyDoesNotExist() {
        when(row.get("nonExistingKey")).thenReturn(null);

        String result = OdooUtils.getOrElse(row, "nonExistingKey", "defaultValue");

        assertEquals("defaultValue", result);
    }

    @Test
    void getOrElse_shouldReturnDefaultValueWhenValueIsNull() {
        when(row.get("nullValueKey")).thenReturn(null);

        String result = OdooUtils.getOrElse(row, "nullValueKey", "defaultValue");

        assertEquals("defaultValue", result);
    }

    @Test
    void get_shouldReturnValueWhenKeyExists() {
        when(row.get("existingKey")).thenReturn("value");

        String result = OdooUtils.get(row, "existingKey");

        assertEquals("value", result);
    }

    @Test
    void get_shouldReturnNullWhenKeyDoesNotExist() {
        when(row.get("nonExistingKey")).thenReturn(null);

        String result = OdooUtils.get(row, "nonExistingKey");

        assertNull(result);
    }

    @Test
    void get_shouldReturnNullWhenValueIsNull() {
        when(row.get("nullValueKey")).thenReturn(null);

        String result = OdooUtils.get(row, "nullValueKey");

        assertNull(result);
    }
}
