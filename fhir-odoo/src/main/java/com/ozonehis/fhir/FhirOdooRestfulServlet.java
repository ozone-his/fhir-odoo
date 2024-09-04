/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import com.ozonehis.fhir.annotations.FhirOdooProvider;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirOdooRestfulServlet extends RestfulServer {

    @Override
    protected void initialize() {
        setFhirContext(FhirContext.forR4());
        setDefaultResponseEncoding(EncodingEnum.JSON);
    }

    @Override
    @Autowired
    @FhirOdooProvider
    public void setResourceProviders(Collection<IResourceProvider> theProviders) {
        super.setResourceProviders(theProviders);
    }
}
