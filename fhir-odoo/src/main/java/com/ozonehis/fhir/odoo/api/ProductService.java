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
import com.ozonehis.fhir.odoo.model.Product;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends BaseOdooService<Product> implements OdooService<Product> {

    @Override
    protected String modelName() {
        return OdooConstants.MODEL_PRODUCT;
    }

    @Override
    protected String[] modelFields() {
        return new Product().fields();
    }

    @Override
    protected Product mapRowToResource(Row row) {
        Product product = new Product();
        product.setDisplayName((String) row.get("display_name"));
        product.setName((String) row.get("name"));
        product.setUomName((String) row.get("uom_name"));
        product.setQuantityAvailable((Double) row.get("qty_available"));
        product.setPrice((Double) row.get("price"));
        product.setListPrice((Double) row.get("list_price"));
        product.setPublicPrice((Double) row.get("lst_price"));
        product.setStandardPrice((Double) row.get("standard_price"));

        var type = row.get("type");
        if (type != null) {
            product.setType((String) row.get("type"));
        }

        var active = row.get("active");
        if (active != null) {
            product.setActive((boolean) row.get("active"));
        }
        product.setCode((String) row.get("code"));

        var currencyId = row.get("currency_id");
        if (currencyId != null) {
            product.setCurrencyId((Integer) row.get("currency_id"));
        }
        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            product.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            product.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            product.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            product.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        var lastModifiedOn = get(row, "__last_update");
        if (lastModifiedOn != null) {
            product.setLastModifiedOn((Date) lastModifiedOn);
        }

        return product;
    }
}
