/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import com.ozonehis.fhir.odoo.model.BaseOdooModel;
import com.ozonehis.fhir.odoo.model.OdooResource;
import java.util.Map;
import org.hl7.fhir.instance.model.api.IAnyResource;

/**
 * This interface is used to map FHIR resources to Odoo resources.
 *
 * @param <F> FHIR resource
 * @param <O> Odoo resource
 */
public interface ToOdooMapping<F extends IAnyResource, O extends BaseOdooModel & OdooResource> {

    /**
     * Maps the FHIR resource to Odoo resource.
     *
     * @param fhirResource FHIR resource
     * @return Odoo resource
     */
    O toOdoo(Map<String, F> fhirResource);
}
