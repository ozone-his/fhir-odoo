/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static com.ozonehis.fhir.odoo.util.OdooUtils.get;

import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.Currency;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService extends BaseOdooService<Currency> implements OdooService<Currency> {

    @Override
    protected String modelName() {
        return OdooConstants.MODEL_CURRENCY;
    }

    @Override
    protected String[] modelFields() {
        return new Currency().fields();
    }

    @Override
    protected Currency mapRowToResource(Row row) {
        Currency currency = new Currency();
        var active = get(row, "active");
        if (active != null) {
            currency.setActive((boolean) active);
        }
        currency.setName(get(row, "name"));
        currency.setDisplayName(get(row, "display_name"));
        currency.setCurrencyUnitLabel(get(row, "currency_unit_label"));
        currency.setCurrencySubunitLabel(get(row, "currency_subunit_label"));

        var decimalPlaces = get(row, "decimal_places");
        if (decimalPlaces != null) {
            currency.setDecimalPlaces((Integer) decimalPlaces);
        }

        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            currency.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            currency.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            currency.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            currency.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        var lastModifiedOn = get(row, "__last_update");
        if (lastModifiedOn != null) {
            currency.setLastModifiedOn((Date) lastModifiedOn);
        }

        return currency;
    }
}
