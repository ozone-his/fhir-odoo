/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_COUNTRY;
import static com.ozonehis.fhir.odoo.util.OdooUtils.get;

import com.odoojava.api.FilterCollection;
import com.odoojava.api.OdooApiException;
import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.model.Country;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CountryService extends BaseOdooService<Country> implements OdooService<Country> {

    @Override
    protected String modelName() {
        return MODEL_COUNTRY;
    }

    @Override
    protected String[] modelFields() {
        return new Country().fields();
    }

    @Override
    protected Country mapRowToResource(Row row) {
        Country country = new Country();
        country.setId(get(row, "id"));
        country.setDisplayName((String) row.get("display_name"));
        country.setName((String) row.get("name"));
        country.setCountryCode((String) row.get("code"));

        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            country.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            country.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            country.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            country.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        return country;
    }

    public Optional<Country> getByName(String name) {
        FilterCollection filters = new FilterCollection();
        try {
            filters.add("name", "=", name);
            Collection<Country> results = this.search(filters);
            if (results.size() > 1) {
                throw new RuntimeException("Multiple countries found for " + MODEL_COUNTRY + " with name " + name);
            } else if (results.size() == 1) {
                return results.stream().findFirst();
            }

            return Optional.empty();
        } catch (OdooApiException e) {
            log.warn("Error while searching country with name {} error {}", name, e.getMessage());
            return Optional.empty();
        }
    }
}
