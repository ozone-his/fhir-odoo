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

import com.odoojava.api.OdooXmlRpcProxy;
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
        fhirOdooConfig.setOdooHost("http://localhost");
        fhirOdooConfig.setOdooDatabase("odoo_db");
        fhirOdooConfig.setOdooPort("8069");

        assertEquals("http://localhost", fhirOdooConfig.getOdooHost());
        assertEquals("odoo_db", fhirOdooConfig.getOdooDatabase());
        assertEquals("8069", fhirOdooConfig.getOdooPort());
        assertDoesNotThrow(() -> fhirOdooConfig.validateOdooProperties());
    }

    @Test
    @DisplayName("Should throw exception with appropriate message when OdooHost is empty")
    void shouldThrowExceptionWhenOdooHostIsEmpty() {
        fhirOdooConfig.setOdooHost("");
        fhirOdooConfig.setOdooDatabase("odoo_db");
        fhirOdooConfig.setOdooPort("8069");

        assertThrows(
                IllegalArgumentException.class, () -> fhirOdooConfig.validateOdooProperties(), "OdooHost is required");
    }

    @Test
    @DisplayName("Should throw exception with appropriate message when OdooDatabase is empty")
    void shouldThrowExceptionWhenOdooDatabaseIsEmpty() {
        fhirOdooConfig.setOdooHost("http://localhost");
        fhirOdooConfig.setOdooDatabase("");
        fhirOdooConfig.setOdooPort("8069");

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

        assertThrows(
                IllegalArgumentException.class, () -> fhirOdooConfig.validateOdooProperties(), "OdooPort is required");
    }

    @Test
    @DisplayName("Should return RPC_HTTPS when OdooHost starts with https")
    void shouldReturnRPC_HTTPSWhenOdooHostStartsWithHttps() {
        fhirOdooConfig.setOdooHost("https://localhost");
        assertEquals(fhirOdooConfig.getRPCProtocol(), OdooXmlRpcProxy.RPCProtocol.RPC_HTTPS);
    }

    @Test
    @DisplayName("Should return RPC_HTTP when OdooHost starts with http")
    void shouldReturnRPC_HTTPWhenOdooHostStartsWithHttp() {
        fhirOdooConfig.setOdooHost("http://localhost");
        assertEquals(fhirOdooConfig.getRPCProtocol(), OdooXmlRpcProxy.RPCProtocol.RPC_HTTP);
    }

    @Test
    @DisplayName("Should return hostname when getOdooHostName is called")
    void shouldReturnHostNameWhenGetOdooHostNameIsCalled() {
        fhirOdooConfig.setOdooHost("https://localhost");
        assertEquals(fhirOdooConfig.getOdooHostName(), "localhost");
    }
}
