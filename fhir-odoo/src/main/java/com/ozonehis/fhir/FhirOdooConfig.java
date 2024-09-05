/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class FhirOdooConfig {

    @Value("${fhir.odoo.host}")
    private String OdooHost;

    @Value("${fhir.odoo.username}")
    private String OdooUsername;

    @Value("${fhir.odoo.password}")
    private String OdooPassword;

    @Value("${fhir.odoo.database}")
    private String OdooDatabase;

    @Value("${fhir.odoo.port}")
    private String OdooPort;

    @Value("${fhir.odoo.protocol}")
    private String OdooProtocol;

    public void validateOdooProperties() {
        if (StringUtils.isEmpty(OdooHost) && StringUtils.isBlank(OdooHost)) {
            throw new IllegalArgumentException("OdooHost is required");
        }
        if (StringUtils.isEmpty(OdooUsername) && StringUtils.isBlank(OdooUsername)) {
            throw new IllegalArgumentException("OdooUsername is required");
        }
        if (StringUtils.isEmpty(OdooPassword) && StringUtils.isBlank(OdooPassword)) {
            throw new IllegalArgumentException("OdooPassword is required");
        }
        if (StringUtils.isEmpty(OdooDatabase) && StringUtils.isBlank(OdooDatabase)) {
            throw new IllegalArgumentException("OdooDatabase is required");
        }
        if (StringUtils.isEmpty(OdooPort) && StringUtils.isBlank(OdooPort)) {
            throw new IllegalArgumentException("OdooPort is required");
        }
        if (StringUtils.isEmpty(OdooProtocol) && StringUtils.isBlank(OdooProtocol)) {
            throw new IllegalArgumentException("OdooProtocol is required");
        }
    }
}
