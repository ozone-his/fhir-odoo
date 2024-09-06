/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.hl7.fhir.instance.model.api.IAnyResource;

/**
 * The interface Fhir service.
 *
 * @param <R> the type parameter
 */
public interface FhirService<R extends IAnyResource> {

    /**
     * Get FHIR resource by id.
     *
     * @param id the id
     * @return Optional of {@link R}
     */
    Optional<R> getById(@Nonnull String id);
}
