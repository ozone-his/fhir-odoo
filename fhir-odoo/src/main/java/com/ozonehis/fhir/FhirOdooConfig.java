/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir;

import com.odoojava.api.OdooXmlRpcProxy;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class FhirOdooConfig {

    @Value("${fhir.odoo.host}")
    private String OdooHost;

    @Value("${fhir.odoo.database}")
    private String OdooDatabase;

    @Value("${fhir.odoo.port}")
    private String OdooPort;

    public void validateOdooProperties() {
        if (StringUtils.isEmpty(OdooHost) || StringUtils.isBlank(OdooHost)) {
            throw new IllegalArgumentException("OdooHost is required");
        }
        if (StringUtils.isEmpty(OdooDatabase) || StringUtils.isBlank(OdooDatabase)) {
            throw new IllegalArgumentException("OdooDatabase is required");
        }
        if (StringUtils.isEmpty(OdooPort) || StringUtils.isBlank(OdooPort)) {
            throw new IllegalArgumentException("OdooPort is required");
        }
    }

    public OdooXmlRpcProxy.RPCProtocol getRPCProtocol() {
        // OdooHost is expected to be in the format: http(s)://<host>
        return OdooHost.startsWith("https")
                ? OdooXmlRpcProxy.RPCProtocol.RPC_HTTPS
                : OdooXmlRpcProxy.RPCProtocol.RPC_HTTP;
    }

    public String getOdooHostName() {
        // OdooHost is expected to be in the format: http(s)://<host>
        var hostname = OdooHost.substring(OdooHost.indexOf("://") + 3);
        return hostname.endsWith("/") ? hostname.substring(0, hostname.length() - 1) : hostname;
    }
}
