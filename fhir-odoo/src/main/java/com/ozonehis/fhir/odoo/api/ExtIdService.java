/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static com.ozonehis.fhir.odoo.util.OdooUtils.get;

import com.odoojava.api.FilterCollection;
import com.odoojava.api.OdooApiException;
import com.odoojava.api.Row;
import com.ozonehis.fhir.FhirOdooConfig;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.ExtId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExtIdService extends BaseOdooService<ExtId> implements OdooService<ExtId> {

    @Autowired
    public ExtIdService(FhirOdooConfig config) {
        super(config);
    }

    @Override
    protected String modelName() {
        return OdooConstants.MODEL_EXTERNAL_IDENTIFIER;
    }

    @Override
    protected String[] modelFields() {
        return new ExtId().fields();
    }

    @Override
    protected ExtId mapRowToResource(Row row) {
        ExtId extId = new ExtId();
        extId.setModel((String) row.get("model"));
        extId.setModule((String) row.get("module"));
        extId.setResId((Integer) row.get("res_id"));
        extId.setUpdatable((boolean) row.get("noupdate"));
        extId.setReference((String) row.get("reference"));
        extId.setCompleteName((String) row.get("complete_name"));
        extId.setName((String) row.get("name"));
        extId.setDisplayName((String) row.get("display_name"));
        extId.setId((Integer) row.get("id"));
        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            extId.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            extId.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            extId.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            extId.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        var lastModifiedOn = get(row, "__last_update");
        if (lastModifiedOn != null) {
            extId.setLastModifiedOn((Date) lastModifiedOn);
        }

        return extId;
    }

    /**
     * Gets an external identifier by name and model.
     *
     * @param name  the name
     * @param model the model
     * @return the external identifier
     */
    public Optional<ExtId> getByNameAndModel(String name, String model) {
        FilterCollection filters = new FilterCollection();
        try {
            filters.add("name", "=", name);
            filters.add("model", "=", model);
            Collection<ExtId> results = this.search(filters);
            if (results.size() > 1) {
                log.warn("Multiple External Identifiers found for name: {} and model: {} ", name, model);
            }
            return results.stream().findFirst();
        } catch (OdooApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<ExtId> getResIdsByNameAndModel(List<String> name, String model) {
        FilterCollection filters = new FilterCollection();
        try {
            name.forEach(n -> {
                try {
                    filters.add("name", "=", n);
                } catch (OdooApiException e) {
                    throw new RuntimeException(e);
                }
            });
            filters.add("model", "=", model);
            return this.search(filters);
        } catch (OdooApiException e) {
            throw new RuntimeException(e);
        }
    }
}
