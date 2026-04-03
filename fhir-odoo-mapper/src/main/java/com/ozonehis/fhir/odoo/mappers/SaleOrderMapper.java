/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.OdooResource;
import com.ozonehis.fhir.odoo.model.Partner;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SaleOrderMapper<F extends IAnyResource & OdooResource> implements ToOdooMapping<F, SaleOrder> {

    @Override
    public SaleOrder toOdoo(Map<String, F> resourceMap) {
        if (MapUtils.isEmpty(resourceMap)) {
            return null;
        }
        SaleOrder saleOrder = new SaleOrder();
        ServiceRequest serviceRequest = (ServiceRequest) resourceMap.get(OdooConstants.MODEL_FHIR_SERVICE_REQUEST);
        Partner partner = (Partner) resourceMap.get(OdooConstants.MODEL_PARTNER);
        ExtId companyExtId = (ExtId) resourceMap.get(OdooConstants.MODEL_COMPANY);

        if (serviceRequest == null || partner == null) {
            return null;
        }
        if (!serviceRequest.hasId()) {
            throw new IllegalArgumentException("The ServiceRequest does not have an id, cannot map to sale order");
        }
        if (!serviceRequest.hasRequisition()) {
            log.error(
                    "ServiceRequest with id {} does not have a requisition value, cannot map to sale order",
                    serviceRequest.getIdPart());
            throw new UnprocessableEntityException(
                    "ServiceRequest does not have a requisition value, cannot map to sale order");
        }

        String serviceRequestId = serviceRequest.getIdPart();
        saleOrder.setOrderClientOrderRef(serviceRequestId);
        saleOrder.setOrderTypeName("Sales Order");
        saleOrder.setOrderState("draft"); // Default value is always `draft`

        saleOrder.setOrderPartnerId(partner.getId());
        saleOrder.setCompanyId(companyExtId.getResId());
        // Add Partner DOB to Odoo Quotation
        saleOrder.setPartnerBirthDate(partner.getPartnerBirthDate());
        // Add Partner id to Odoo Quotation
        saleOrder.setOdooPartnerId(partner.getPartnerExternalId().replaceAll("(?i)</?p>", ""));
        saleOrder.setName(serviceRequest.getRequisition().getValue());

        return saleOrder;
    }
}
