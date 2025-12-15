/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.OdooResource;
import com.ozonehis.fhir.odoo.model.Product;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import com.ozonehis.fhir.odoo.model.SaleOrderLine;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SaleOrderLineMapper<F extends IAnyResource & OdooResource> implements ToOdooMapping<F, SaleOrderLine> {

    @Override
    public SaleOrderLine toOdoo(Map<String, F> resourceMap) {
        if (MapUtils.isEmpty(resourceMap)) {
            return null;
        }
        SaleOrderLine saleOrderLine = new SaleOrderLine();
        ServiceRequest serviceRequest = (ServiceRequest) resourceMap.get(OdooConstants.MODEL_FHIR_SERVICE_REQUEST);
        Product product = (Product) resourceMap.get(OdooConstants.MODEL_PRODUCT);
        SaleOrder saleOrder = (SaleOrder) resourceMap.get(OdooConstants.MODEL_SALE_ORDER);

        if (serviceRequest == null || product == null || saleOrder == null) {
            return null;
        }
        saleOrderLine.setSaleOrderLineProductUomQty(1.0f); // default quantity is 1 for serviceRequests.
        String requesterDisplay = serviceRequest.getRequester().getDisplay();
        String serviceDisplay = serviceRequest.getCode().getText();
        saleOrderLine.setName(serviceDisplay + " | Orderer: " + requesterDisplay);

        saleOrderLine.setSaleOrderLineProductId(product.getId());
        saleOrderLine.setSaleOrderLineOrderId(saleOrder.getId());
        saleOrderLine.setSaleOrderLineProductUom(1);

        return saleOrderLine;
    }
}
