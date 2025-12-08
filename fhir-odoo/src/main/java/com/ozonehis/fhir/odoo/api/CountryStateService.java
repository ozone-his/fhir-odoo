/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.Country;
import com.ozonehis.fhir.odoo.model.CountryState;
import static com.ozonehis.fhir.odoo.util.OdooUtils.get;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class CountryStateService extends BaseOdooService<CountryState> implements OdooService<CountryState> {

    @Override
    protected String modelName() {
        return OdooConstants.MODEL_COUNTRY_STATE;
    }

    @Override
    protected String[] modelFields() {
        return new CountryState().fields();
    }

    @Override
    protected CountryState mapRowToResource(Row row) {
        CountryState countryState = new CountryState();
        countryState.setId(get(row, "id"));
        countryState.setDisplayName((String) row.get("display_name"));
        countryState.setName((String) row.get("name"));
        countryState.setCountryStateCode((String) row.get("code"));

        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            countryState.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            countryState.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            countryState.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            countryState.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        return countryState;
    }
}
