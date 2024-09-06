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
import org.hl7.fhir.instance.model.api.IAnyResource;

import java.util.List;
import java.util.Map;

/**
 * This interface is used to map Odoo resources to FHIR resources.
 *
 * @param <O> Odoo resource
 * @param <F> FHIR resource
 */
public interface ToFhirMapping<O extends BaseOdooModel & OdooResource, F extends IAnyResource> {

    /**
     * Maps the Odoo resource to FHIR resource.
     *
     * @param odooResource Odoo resource
     * @return FHIR resource
     */
    F toFhir(Map<String, O> odooResource);
}
