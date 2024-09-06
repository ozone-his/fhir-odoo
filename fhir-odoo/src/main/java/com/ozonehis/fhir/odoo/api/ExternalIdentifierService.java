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
import com.ozonehis.fhir.odoo.model.ExternalIdentifier;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExternalIdentifierService extends BaseOdooService<ExternalIdentifier>
        implements OdooService<ExternalIdentifier> {

    @Autowired
    public ExternalIdentifierService(FhirOdooConfig fhirOdooConfig) {
        super(fhirOdooConfig);
    }

    @Override
    protected String modelName() {
        return OdooConstants.MODEL_EXTERNAL_IDENTIFIER;
    }

    @Override
    protected String[] modelFields() {
        return new ExternalIdentifier().fields();
    }

    @Override
    protected ExternalIdentifier mapRowToResource(Row row) {
        ExternalIdentifier externalIdentifier = new ExternalIdentifier();
        externalIdentifier.setModel((String) row.get("model"));
        externalIdentifier.setModule((String) row.get("module"));
        externalIdentifier.setResId((Integer) row.get("res_id"));
        externalIdentifier.setUpdatable((boolean) row.get("noupdate"));
        externalIdentifier.setReference((String) row.get("reference"));
        externalIdentifier.setCompleteName((String) row.get("complete_name"));
        externalIdentifier.setName((String) row.get("name"));
        externalIdentifier.setDisplayName((String) row.get("display_name"));
        externalIdentifier.setId((Integer) row.get("id"));
        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            externalIdentifier.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            externalIdentifier.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            externalIdentifier.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            externalIdentifier.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        var lastModifiedOn = get(row, "__last_update");
        if (lastModifiedOn != null) {
            externalIdentifier.setLastModifiedOn((Date) lastModifiedOn);
        }

        return externalIdentifier;
    }

    /**
     * Gets an external identifier by name and model.
     *
     * @param name  the name
     * @param model the model
     * @return the external identifier
     */
    public Optional<ExternalIdentifier> getByNameAndModel(String name, String model) {
        FilterCollection filters = new FilterCollection();
        try {
            filters.add("name", "=", name);
            filters.add("model", "=", model);
            Collection<ExternalIdentifier> results = this.search(filters);
            if (results.size() > 1) {
                log.warn("Multiple External Identifiers found for name: {} and model: {} ", name, model);
            }
            return results.stream().findFirst();
        } catch (OdooApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<ExternalIdentifier> getResIdsByNameAndModel(List<String> name, String model) {
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
