/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_SALE_ORDER;
import static com.ozonehis.fhir.odoo.util.OdooUtils.get;

import com.odoojava.api.FilterCollection;
import com.odoojava.api.OdooApiException;
import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SaleOrderService extends BaseOdooService<SaleOrder> implements OdooService<SaleOrder> {

    @Value("${odoo.partner.weight.field}")
    private String odooPartnerWeightField;

    @Value("${odoo.partner.dob.field}")
    private String odooPartnerDobField;

    @Value("${odoo.partner.id.field}")
    private String odooPartnerIdField;

    @Override
    protected String modelName() {
        return MODEL_SALE_ORDER;
    }

    @Override
    protected String[] modelFields() {
        return new SaleOrder().fields();
    }

    @Override
    protected SaleOrder mapRowToResource(Row row) {
        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setOrderClientOrderRef((String) row.get("client_order_ref"));
        saleOrder.setOrderState((String) row.get("state"));
        saleOrder.setOrderPartnerId((int) row.get("partner_id"));
        saleOrder.setOrderTypeName((String) row.get("type_name"));

        saleOrder.setPartnerWeight((String) row.get(odooPartnerWeightField));
        saleOrder.setPartnerBirthDate(
                row.get(odooPartnerDobField) != null
                        ? row.get(odooPartnerDobField).toString()
                        : null);
        saleOrder.setOdooPartnerId((String) row.get(odooPartnerIdField));

        saleOrder.setName((String) row.get("name"));
        saleOrder.setDisplayName((String) row.get("display_name"));

        var id = get(row, "id");
        if (id != null) {
            saleOrder.setId((Integer) row.get("id"));
        }
        // Audit fields
        var createdOn = get(row, "create_date");
        if (createdOn != null) {
            saleOrder.setCreatedOn((Date) createdOn);
        }
        var createdBy = get(row, "create_uid");
        if (createdBy != null) {
            saleOrder.setCreatedBy((Integer) createdBy);
        }

        var lastUpdatedOn = get(row, "write_date");
        if (lastUpdatedOn != null) {
            saleOrder.setLastUpdatedOn((Date) lastUpdatedOn);
        }

        var lastUpdatedBy = get(row, "write_uid");
        if (lastUpdatedBy != null) {
            saleOrder.setLastUpdatedBy((Integer) lastUpdatedBy);
        }

        return saleOrder;
    }

    public Map<String, Object> convertSaleOrderToMap(SaleOrder saleOrder) {
        Map<String, Object> map = new HashMap<>();

        map.put("client_order_ref", saleOrder.getOrderClientOrderRef());
        map.put("state", saleOrder.getOrderState());
        map.put("partner_id", saleOrder.getOrderPartnerId());
        map.put("type_name", saleOrder.getOrderTypeName());

        map.put(odooPartnerWeightField, saleOrder.getPartnerWeight());
        map.put(odooPartnerDobField, saleOrder.getPartnerBirthDate());
        map.put(odooPartnerIdField, saleOrder.getOdooPartnerId());

        map.put("name", saleOrder.getName());
        map.put("display_name", saleOrder.getDisplayName());

        // ID field
        if (saleOrder.getId() != 0) {
            map.put("id", saleOrder.getId());
        }

        // Audit fields
        if (saleOrder.getCreatedOn() != null) {
            map.put("create_date", saleOrder.getCreatedOn());
        }

        if (saleOrder.getCreatedBy() != 0) {
            map.put("create_uid", saleOrder.getCreatedBy());
        }

        if (saleOrder.getLastUpdatedOn() != null) {
            map.put("write_date", saleOrder.getLastUpdatedOn());
        }

        if (saleOrder.getLastUpdatedBy() != 0) {
            map.put("write_uid", saleOrder.getLastUpdatedBy());
        }

        return map;
    }

    public Optional<SaleOrder> getByOrderRef(String ref) {
        FilterCollection filters = new FilterCollection();
        try {
            filters.add("client_order_ref", "=", ref);
            Collection<SaleOrder> results = this.search(filters);
            if (results.size() > 1) {
                throw new RuntimeException(
                        "Multiple Sale order found for " + MODEL_SALE_ORDER + " with reference " + ref);
            } else if (results.size() == 1) {
                return results.stream().findFirst();
            }

            return Optional.empty();
        } catch (OdooApiException e) {
            throw new RuntimeException(e);
        }
    }
}
