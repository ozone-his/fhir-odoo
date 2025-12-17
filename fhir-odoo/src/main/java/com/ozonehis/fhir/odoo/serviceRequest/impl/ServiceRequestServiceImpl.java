/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.serviceRequest.impl;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.api.PartnerService;
import com.ozonehis.fhir.odoo.api.ProductService;
import com.ozonehis.fhir.odoo.api.SaleOrderLineService;
import com.ozonehis.fhir.odoo.api.SaleOrderService;
import com.ozonehis.fhir.odoo.mappers.SaleOrderLineMapper;
import com.ozonehis.fhir.odoo.mappers.SaleOrderMapper;
import com.ozonehis.fhir.odoo.model.Partner;
import com.ozonehis.fhir.odoo.model.Product;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import com.ozonehis.fhir.odoo.model.SaleOrderLine;
import com.ozonehis.fhir.odoo.serviceRequest.ServiceRequestService;
import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("rawtypes, unchecked")
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final SaleOrderService saleOrderService;

    private final SaleOrderLineService saleOrderLineService;

    private final SaleOrderLineMapper saleOrderLineMapper;

    private final SaleOrderMapper saleOrderMapper;

    private final PartnerService partnerService;

    private final ProductService productService;

    @Autowired
    public ServiceRequestServiceImpl(
            SaleOrderService saleOrderService,
            SaleOrderLineService saleOrderLineService,
            SaleOrderLineMapper saleOrderLineMapper,
            SaleOrderMapper saleOrderMapper,
            PartnerService partnerService,
            ProductService productService) {
        this.saleOrderService = saleOrderService;
        this.saleOrderLineService = saleOrderLineService;
        this.saleOrderLineMapper = saleOrderLineMapper;
        this.saleOrderMapper = saleOrderMapper;
        this.partnerService = partnerService;
        this.productService = productService;
    }

    @Override
    public ServiceRequest create(ServiceRequest serviceRequest) {
        SaleOrder saleOrder;
        if (serviceRequest.hasRequisition()) {
            String requisitionValue = serviceRequest.getRequisition().getValue();
            saleOrder = saleOrderService.getByOrderRef(requisitionValue).orElse(null);
            if (saleOrder == null) {
                saleOrder = createSaleOrder(serviceRequest);
                log.info("Created sale order with id {}", saleOrder.getId());
            } else {
                log.info(
                        "Sale order already exists with id {} and ref {}",
                        saleOrder.getId(),
                        saleOrder.getOrderClientOrderRef());
            }

            SaleOrderLine saleOrderLine = createSaleOrderLine(serviceRequest, saleOrder);
            log.info("Created sale order line with id {}", saleOrderLine.getId());
        }

        return serviceRequest;
    }

    private SaleOrder createSaleOrder(ServiceRequest serviceRequest) {
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);

        String patientIdReference = serviceRequest.getSubject().getReference().split("/")[1];
        Partner partner = partnerService.getByRef(patientIdReference).orElse(null);
        if (partner == null) {
            throw new UnprocessableEntityException("Partner with id {} doesn't exists in Odoo", patientIdReference);
        }
        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);
        SaleOrder saleOrder = saleOrderMapper.toOdoo(resourceMap);

        if (saleOrder == null) {
            log.error("Unable to create saleOrder in Odoo because required ServiceRequest data is missing");
            throw new UnprocessableEntityException("Fields missing in ServiceRequest payload");
        }

        Map<String, Object> saleOrderMap = saleOrderService.convertSaleOrderToMap(saleOrder);

        log.error("saleOrderMap {}", saleOrderMap);
        int id = saleOrderService.create(saleOrderMap);
        if (id == 0) {
            log.error("Unable to create saleOrder in Odoo");
            throw new InvalidRequestException("Unable to create saleOrder in Odoo");
        }
        saleOrder.setId(id);

        return saleOrder;
    }

    private SaleOrderLine createSaleOrderLine(ServiceRequest serviceRequest, SaleOrder saleOrder) {
        Map<String, Object> resourceMap = new HashMap<>();
        String productName = serviceRequest.getCode().getCodingFirstRep().getDisplay();
        Product product = productService.getByName(productName).orElse(null);
        if (product == null) {
            throw new UnprocessableEntityException("Product with externalId {} doesn't exists in Odoo", productName);
        }

        SaleOrderLine saleOrderLine = saleOrderLineService
                .getBySaleOrderIdAndProductId(saleOrder.getId(), product.getId())
                .orElse(null);
        if (saleOrderLine != null) {
            throw new UnprocessableEntityException(
                    "Sale order line already exists for product {} in Odoo", productName);
        }

        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_SALE_ORDER, saleOrder);
        resourceMap.put(OdooConstants.MODEL_PRODUCT, product);

        saleOrderLine = saleOrderLineMapper.toOdoo(resourceMap);

        if (saleOrderLine == null) {
            log.error("Unable to create saleOrderLine in Odoo because required ServiceRequest data is missing");
            throw new UnprocessableEntityException("Fields missing in ServiceRequest payload");
        }

        Map<String, Object> saleOrderLineMap = saleOrderLineService.convertSaleOrderLineToMap(saleOrderLine);

        int id = saleOrderLineService.create(saleOrderLineMap);
        if (id == 0) {
            log.error("Unable to create saleOrderLine in Odoo");
            throw new InvalidRequestException("Unable to create saleOrderLine in Odoo");
        }
        return saleOrderLine;
    }

    @Override
    public Optional<ServiceRequest> getById(@Nonnull String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
