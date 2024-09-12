package com.ozonehis.fhir;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        assertThrows(IllegalArgumentException.class, () -> fhirOdooConfig.validateOdooProperties(), "OdooHost is required");
    }

    @Test
    @DisplayName("Should throw exception with appropriate message when OdooDatabase is empty")
    void shouldThrowExceptionWhenOdooDatabaseIsEmpty() {
        fhirOdooConfig.setOdooHost("localhost");
        fhirOdooConfig.setOdooDatabase("");
        fhirOdooConfig.setOdooPort("8069");
        fhirOdooConfig.setOdooProtocol("http");

        assertThrows(IllegalArgumentException.class, () -> fhirOdooConfig.validateOdooProperties(), "OdooDatabase is required");
    }

    @Test
    @DisplayName("Should throw exception with appropriate message when OdooPort is empty")
    void shouldThrowExceptionWhenOdooPortIsEmpty() {
        fhirOdooConfig.setOdooHost("localhost");
        fhirOdooConfig.setOdooDatabase("odoo_db");
        fhirOdooConfig.setOdooPort("");
        fhirOdooConfig.setOdooProtocol("http");

        assertThrows(IllegalArgumentException.class, () -> fhirOdooConfig.validateOdooProperties(), "OdooPort is required");
    }

    @Test
    @DisplayName("Should throw exception with appropriate when OdooProtocol is empty")
    void shouldThrowExceptionWhenOdooProtocolIsEmpty() {
        fhirOdooConfig.setOdooHost("localhost");
        fhirOdooConfig.setOdooDatabase("odoo_db");
        fhirOdooConfig.setOdooPort("8069");
        fhirOdooConfig.setOdooProtocol("");

        assertThrows(IllegalArgumentException.class, () -> fhirOdooConfig.validateOdooProperties(), "OdooProtocol is required");
    }
}
