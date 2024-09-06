/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import com.odoojava.api.FilterCollection;
import com.ozonehis.fhir.odoo.model.OdooResource;
import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

/**
 * The interface Odoo service.
 *
 * @param <O> the type parameter
 */
public interface OdooService<O extends OdooResource> {

    /**
     * Get Odoo resource by id.
     *
     * @param id the id
     * @return Optional of {@link O}
     */
    Optional<O> getById(@Nonnull String id);

    /**
     * Search collection of {@link O}.
     *
     * @param filters the filter collection
     * @return the collection of {@link O}
     */
    Collection<O> search(FilterCollection filters);
}
