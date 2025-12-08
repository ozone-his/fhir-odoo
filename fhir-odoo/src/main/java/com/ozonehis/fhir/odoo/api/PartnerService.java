/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_PARTNER;
import static com.ozonehis.fhir.odoo.util.OdooUtils.get;

import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.model.Partner;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PartnerService extends BaseOdooService<Partner> implements OdooService<Partner> {

    @Override
    protected String modelName() {
        return MODEL_PARTNER;
    }

    @Override
    protected String[] modelFields() {
        return new Partner().fields();
    }

    @Override
    protected Partner mapRowToResource(Row row) {
        Partner partner = new Partner();
        partner.setPartnerRef((String) row.get("ref"));
        partner.setPartnerType((String) row.get("type"));
        partner.setPartnerStreet((String) row.get("street"));
        partner.setPartnerStreet2((String) row.get("street2"));
        partner.setPartnerCity((String) row.get("city"));
        partner.setPartnerZip((String) row.get("zip"));
        partner.setPartnerCountryId((Integer) row.get("country_id"));
        partner.setPartnerStateId((Integer) row.get("state_id"));
        partner.setPartnerActive((Boolean) row.get("active"));
        partner.setPartnerComment((String) row.get("comment"));
        partner.setPartnerBirthDate((String) row.get("odoo.customer.dob.field"));
        partner.setPartnerExternalId((String) row.get("odoo.customer.id.field"));

        partner.setName((String) row.get("name"));
        partner.setDisplayName((String) row.get("display_name"));

        var id = get(row, "id");
        if (id != null) {
            partner.setId((Integer) row.get("id"));
        }
        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            partner.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            partner.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            partner.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            partner.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        return partner;
    }
}
