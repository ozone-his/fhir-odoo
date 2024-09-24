/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

@SpringBootTest(classes = FhirOdooRestfulServletConfig.class)
class FhirOdooRestfulServletConfigTest {

    @MockBean
    FhirOdooRestfulServlet fhirOdooRestfulServlet;

    @Autowired
    ServletRegistrationBean<FhirOdooRestfulServlet> fhirOdooRestfulServletRegistrationBean;

    @Test
    @DisplayName("Should register FhirOdooRestfulServlet with URL mapping /odoo/fhir/R4/*")
    void shouldReturnFhirOdooRestfulServletRegistrationBeanWithCorrectMappings() {
        // Assert
        assertNotNull(fhirOdooRestfulServletRegistrationBean);
        assertNotNull(fhirOdooRestfulServletRegistrationBean.getServlet());
        assertTrue(fhirOdooRestfulServletRegistrationBean.getUrlMappings().contains("/odoo/fhir/R4/*"));
        assertEquals("FhirOdooRestfulServlet", fhirOdooRestfulServletRegistrationBean.getServletName());
    }
}
