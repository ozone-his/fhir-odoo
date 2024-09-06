/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import com.odoojava.api.Row;
import com.ozonehis.fhir.FhirOdooConfig;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.Product;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends BaseOdooService<Product> implements OdooService<Product> {

    @Autowired
    public ProductService(FhirOdooConfig fhirOdooConfig) {
        super(fhirOdooConfig);
    }

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
        product.setActive((boolean) row.get("active"));
        product.setCode((String) row.get("code"));
        product.setCurrencyId((Integer) row.get("currency_id"));
        // Audit fields
        product.setCreatedOn((Date) row.get("create_date"));
        product.setCreatedBy((Integer) row.get("create_uid"));
        product.setLastUpdatedOn((Date) row.get("write_date"));
        product.setLastUpdatedBy((Integer) row.get("write_uid"));
        product.setLastModifiedOn((Date) row.get("__last_update"));

        return product;
    }
}
