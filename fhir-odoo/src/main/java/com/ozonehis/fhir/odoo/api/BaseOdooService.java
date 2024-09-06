/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import com.odoojava.api.FilterCollection;
import com.odoojava.api.ObjectAdapter;
import com.odoojava.api.Row;
import com.odoojava.api.RowCollection;
import com.odoojava.api.Session;
import com.ozonehis.fhir.FhirOdooConfig;
import com.ozonehis.fhir.odoo.model.OdooResource;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Abstract base class for Odoo service implementations.
 * Provides generic methods to interact with the Odoo API.
 *
 * @param <T> the type parameter extending {@link OdooResource}
 */
@Slf4j
@Component
public abstract class BaseOdooService<T extends OdooResource> implements OdooService<T> {

    @Getter
    private Session odooSession;

    protected final ObjectAdapter objectAdapter;

    /**
     * Gets the model name for the Odoo object.
     *
     * @return the model name
     */
    protected abstract String modelName();

    /**
     * Gets the fields for the Odoo object.
     *
     * @return an array of field names
     */
    protected abstract String[] modelFields();

    @Autowired
    public BaseOdooService(FhirOdooConfig fhirOdooConfig) {
        fhirOdooConfig.validateOdooProperties();
        try {
            odooSession = new Session(
                    fhirOdooConfig.getOdooHost(),
                    Integer.parseInt(fhirOdooConfig.getOdooPort()),
                    fhirOdooConfig.getOdooDatabase(),
                    fhirOdooConfig.getOdooUsername(),
                    fhirOdooConfig.getOdooPassword());
            odooSession.startSession();
            this.objectAdapter = odooSession.getObjectAdapter(modelName());
        } catch (Exception e) {
            throw new RuntimeException("Error while initializing Odoo session", e);
        }
    }

    /**
     * Gets a resource by its ID.
     *
     * @param id the resource ID
     * @return an Optional containing the resource if found, otherwise empty
     */
    @Override
    public Optional<T> getById(@Nonnull String id) {
        try {
            FilterCollection filters = new FilterCollection();
            filters.add("id", "=", id);
            RowCollection rows = objectAdapter.searchAndReadObject(filters, modelFields());
            if (!rows.isEmpty()) {
                Row row = rows.get(0);
                T resource = mapRowToResource(row);
                return Optional.of(resource);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching Odoo resource with id " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Searches for resources based on the provided filters.
     *
     * @param filters the filter collection
     * @return a list of resources matching the filters
     */
    @Override
    public Collection<T> search(FilterCollection filters) {
        List<T> resources = new ArrayList<>();
        try {
            RowCollection rows = objectAdapter.searchAndReadObject(filters, modelFields());
            for (Row row : rows) {
                T resource = mapRowToResource(row);
                resources.add(resource);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while searching Odoo resources", e);
        }
        return resources;
    }

    /**
     * Maps a Row object to a resource.
     *
     * @param row the Row object
     * @return the mapped resource
     */
    protected abstract T mapRowToResource(Row row);
}
