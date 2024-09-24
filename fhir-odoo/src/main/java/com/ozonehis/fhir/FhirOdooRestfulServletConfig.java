/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirOdooRestfulServletConfig {

    private final String FHIR_ODOO_SERVLET_NAME = "FhirOdooRestfulServlet";

    @Autowired
    private FhirOdooRestfulServlet fhirOdooRestfulServlet;

    @Bean
    public ServletRegistrationBean<FhirOdooRestfulServlet> fhirOdooRestfulServletRegistrationBean() {
        var servletRegistrationBean = new ServletRegistrationBean<>(fhirOdooRestfulServlet, "/odoo/fhir/R4/*");
        servletRegistrationBean.setName(FHIR_ODOO_SERVLET_NAME);
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }
}
