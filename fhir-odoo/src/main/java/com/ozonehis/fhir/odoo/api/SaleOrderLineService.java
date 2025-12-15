/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_SALE_ORDER_LINE;
import static com.ozonehis.fhir.odoo.util.OdooUtils.get;

import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.model.Partner;
import com.ozonehis.fhir.odoo.model.SaleOrderLine;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SaleOrderLineService extends BaseOdooService<SaleOrderLine> implements OdooService<SaleOrderLine> {

    @Override
    protected String modelName() {
        return MODEL_SALE_ORDER_LINE;
    }

    @Override
    protected String[] modelFields() {
        return new Partner().fields();
    }

    @Override
    protected SaleOrderLine mapRowToResource(Row row) {
        SaleOrderLine saleOrderLine = new SaleOrderLine();
        saleOrderLine.setSaleOrderLineOrderId(row.get("order_id"));
        saleOrderLine.setSaleOrderLineProductId(row.get("product_id"));
        saleOrderLine.setSaleOrderLineProductUomQty((Float) row.get("product_uom_qty"));
        saleOrderLine.setSaleOrderLineProductUom(row.get("product_uom"));

        saleOrderLine.setName((String) row.get("name"));
        saleOrderLine.setDisplayName((String) row.get("display_name"));

        var id = get(row, "id");
        if (id != null) {
            saleOrderLine.setId((Integer) row.get("id"));
        }
        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            saleOrderLine.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            saleOrderLine.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            saleOrderLine.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            saleOrderLine.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        return saleOrderLine;
    }

    public Map<String, Object> convertSaleOrderLineToMap(SaleOrderLine saleOrderLine) {
        Map<String, Object> map = new HashMap<>();

        map.put("order_id", saleOrderLine.getSaleOrderLineOrderId());
        map.put("product_id", saleOrderLine.getSaleOrderLineProductId());
        map.put("product_uom_qty", saleOrderLine.getSaleOrderLineProductUomQty());
        map.put("product_uom", saleOrderLine.getSaleOrderLineProductUom());

        map.put("name", saleOrderLine.getName());
        map.put("display_name", saleOrderLine.getDisplayName());

        // ID field
        if (saleOrderLine.getId() != 0) {
            map.put("id", saleOrderLine.getId());
        }

        // Audit fields
        if (saleOrderLine.getCreatedOn() != null) {
            map.put("create_date", saleOrderLine.getCreatedOn());
        }

        if (saleOrderLine.getCreatedBy() != 0) {
            map.put("create_uid", saleOrderLine.getCreatedBy());
        }

        if (saleOrderLine.getLastUpdatedOn() != null) {
            map.put("write_date", saleOrderLine.getLastUpdatedOn());
        }

        if (saleOrderLine.getLastUpdatedBy() != 0) {
            map.put("write_uid", saleOrderLine.getLastUpdatedBy());
        }
        return map;
    }
}
