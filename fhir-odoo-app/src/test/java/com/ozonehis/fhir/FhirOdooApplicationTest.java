/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class FhirOdooApplicationTest {

    @Autowired
    ApplicationContext context;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("EIP_ODOO_DRUGS_CATEGORY_EXT_ID", "test");
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty("EIP_ODOO_DRUGS_CATEGORY_EXT_ID");
    }

    @Test
    @DisplayName("Should load ApplicationContext")
    void contextLoads() {
        assertNotNull(context);
    }
}
