/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FhirOdooConfig.class)
class FhirOdooConfigTest {

    @Autowired
    FhirOdooConfig fhirOdooConfig;

    @Test
    @DisplayName("Should validate all properties are not empty")
    void shouldValidateAllPropertiesAreNotEmpty() {
        fhirOdooConfig.setOdooHost("localhost");
        fhirOdooConfig.setOdooDatabase("odoo_db");
        fhirOdooConfig.setOdooPort("8069");
        fhirOdooConfig.setOdooProtocol("http");

        assertEquals("localhost", fhirOdooConfig.getOdooHost());
        assertEquals("odoo_db", fhirOdooConfig.getOdooDatabase());
        assertEquals("8069", fhirOdooConfig.getOdooPort());
        assertEquals("http", fhirOdooConfig.getOdooProtocol());
        assertDoesNotThrow(() -> fhirOdooConfig.validateOdooProperties());
    }

    @Test
    @DisplayName("Should throw exception with appropriate message when OdooHost is empty")
    void shouldThrowExceptionWhenOdooHostIsEmpty() {
        fhirOdooConfig.setOdooHost("");
        fhirOdooConfig.setOdooDatabase("odoo_db");
        fhirOdooConfig.setOdooPort("8069");
        fhirOdooConfig.setOdooProtocol("http");

        assertThrows(
                IllegalArgumentException.class, () -> fhirOdooConfig.validateOdooProperties(), "OdooHost is required");
    }

    @Test
    @DisplayName("Should throw exception with appropriate message when OdooDatabase is empty")
    void shouldThrowExceptionWhenOdooDatabaseIsEmpty() {
        fhirOdooConfig.setOdooHost("localhost");
        fhirOdooConfig.setOdooDatabase("");
        fhirOdooConfig.setOdooPort("8069");
        fhirOdooConfig.setOdooProtocol("http");

        assertThrows(
                IllegalArgumentException.class,
                () -> fhirOdooConfig.validateOdooProperties(),
                "OdooDatabase is required");
    }

    @Test
    @DisplayName("Should throw exception with appropriate message when OdooPort is empty")
    void shouldThrowExceptionWhenOdooPortIsEmpty() {
        fhirOdooConfig.setOdooHost("localhost");
        fhirOdooConfig.setOdooDatabase("odoo_db");
        fhirOdooConfig.setOdooPort("");
        fhirOdooConfig.setOdooProtocol("http");

        assertThrows(
                IllegalArgumentException.class, () -> fhirOdooConfig.validateOdooProperties(), "OdooPort is required");
    }

    @Test
    @DisplayName("Should throw exception with appropriate when OdooProtocol is empty")
    void shouldThrowExceptionWhenOdooProtocolIsEmpty() {
        fhirOdooConfig.setOdooHost("localhost");
        fhirOdooConfig.setOdooDatabase("odoo_db");
        fhirOdooConfig.setOdooPort("8069");
        fhirOdooConfig.setOdooProtocol("");

        assertThrows(
                IllegalArgumentException.class,
                () -> fhirOdooConfig.validateOdooProperties(),
                "OdooProtocol is required");
    }
}
