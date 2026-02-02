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
        if (!serviceRequest.hasRequisition()) {
            log.error("ServiceRequest with id {} does not have a requisition value", serviceRequest.getIdPart());
            throw new UnprocessableEntityException(
                    "ServiceRequest with id {} does not have a requisition value", serviceRequest.getIdPart());
        }

        if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.ACTIVE)) {
            // Create Sale order and Sale order line
            SaleOrder saleOrder = createSaleOrder(serviceRequest);
            SaleOrderLine saleOrderLine = createSaleOrderLine(serviceRequest, saleOrder);
        } else if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.REVOKED)
                || serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.ENTEREDINERROR)) {
            // Delete sale order line and check if sale order is empty delete sale order also
            deleteSaleOrderLine(serviceRequest);
            cancelSaleOrder(serviceRequest);
        } else if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.COMPLETED)) {
            // Mark sale order as confirmed if all sale order lines (tests) are completed
        } else if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.UNKNOWN)) {
            // Do nothing
        }

        return serviceRequest;
    }

    private SaleOrder createSaleOrder(ServiceRequest serviceRequest) {
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);

        String patientIdReference = serviceRequest.getSubject().getReference().split("/")[1];
        Partner partner = partnerService.getByRef(patientIdReference).orElse(null);
        if (partner == null) {
            throw new UnprocessableEntityException("Partner with id " + patientIdReference + " doesn't exists in Odoo");
        }
        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);
        SaleOrder saleOrder = saleOrderMapper.toOdoo(resourceMap);

        if (saleOrder == null) {
            log.error("Unable to create saleOrder in Odoo because required ServiceRequest data is missing");
            throw new UnprocessableEntityException("Fields missing in ServiceRequest payload");
        }

        Map<String, Object> saleOrderMap = saleOrderService.convertSaleOrderToMap(saleOrder);

        SaleOrder existingSaleOrder = saleOrderService
                .getByName(serviceRequest.getRequisition().getValue())
                .orElse(null);
        if (existingSaleOrder != null) {
            log.info(
                    "Sale order already exists with id {} and ref {}",
                    saleOrder.getId(),
                    saleOrder.getOrderClientOrderRef());
            return existingSaleOrder;
        }

        int id = saleOrderService.create(saleOrderMap);
        if (id == 0) {
            log.error("Unable to create saleOrder in Odoo");
            throw new InvalidRequestException("Unable to create saleOrder in Odoo");
        }
        saleOrder.setId(id);
        log.info("Created sale order with id {}", saleOrder.getId());

        return saleOrder;
    }

    private SaleOrderLine createSaleOrderLine(ServiceRequest serviceRequest, SaleOrder saleOrder) {
        Map<String, Object> resourceMap = new HashMap<>();
        String productCode = serviceRequest.getCode().getCodingFirstRep().getCode();
        Product product = productService.getByConceptCode(productCode).orElse(null);
        if (product == null) {
            throw new UnprocessableEntityException(
                    "Product with concept code " + productCode + " doesn't exists in Odoo");
        }

        SaleOrderLine saleOrderLine = saleOrderLineService
                .getBySaleOrderIdAndProductId(saleOrder.getId(), product.getId())
                .orElse(null);
        if (saleOrderLine != null) {
            throw new UnprocessableEntityException(
                    "Sale order line already exists for product " + productCode + " in Odoo");
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
        log.info("Created sale order line with id {}", saleOrderLine.getId());

        return saleOrderLine;
    }

    private void cancelSaleOrder(ServiceRequest serviceRequest) {
        SaleOrder existingSaleOrder = saleOrderService
                .getByName(serviceRequest.getRequisition().getValue())
                .orElse(null);
        if (existingSaleOrder == null) {
            log.error("Sale order doesn't exist for ServiceRequest with id {} ", serviceRequest.getId());
            throw new UnprocessableEntityException(
                    "Sale order doesn't exist for ServiceRequest with id {} ", serviceRequest.getId());
        }

        Object orderLine = existingSaleOrder.getOrderLine();
        boolean hasOrderLines = false;
        if (orderLine != null) {
            if (orderLine instanceof Object[]) {
                hasOrderLines = ((Object[]) orderLine).length > 0;
            }
        }
        if (hasOrderLines) {
            log.info("Sale order has order lines, not cancelling sale order");
            return;
        }

        existingSaleOrder.setOrderState("cancel");

        Map<String, Object> saleOrderMap = saleOrderService.convertSaleOrderToMap(existingSaleOrder);
        int id = saleOrderService.update(String.valueOf(existingSaleOrder.getId()), saleOrderMap);
        if (id == 0) {
            log.error("Unable to cancel saleOrder in Odoo");
            throw new InvalidRequestException("Unable to cancel saleOrder in Odoo");
        }

        log.info("Cancelled sale order with id {}", existingSaleOrder.getId());
    }

    private void deleteSaleOrderLine(ServiceRequest serviceRequest) {
        SaleOrder existingSaleOrder = saleOrderService
                .getByName(serviceRequest.getRequisition().getValue())
                .orElse(null);
        if (existingSaleOrder == null) {
            log.error("Sale order doesn't exist for ServiceRequest with id {} ", serviceRequest.getId());
            throw new UnprocessableEntityException(
                    "Sale order doesn't exist for ServiceRequest with id {} ", serviceRequest.getId());
        }

        String productCode = serviceRequest.getCode().getCodingFirstRep().getCode();
        Product product = productService.getByConceptCode(productCode).orElse(null);
        if (product == null) {
            throw new UnprocessableEntityException(
                    "Product with concept code " + productCode + " doesn't exists in Odoo");
        }

        SaleOrderLine saleOrderLine = saleOrderLineService
                .getBySaleOrderIdAndProductId(existingSaleOrder.getId(), product.getId())
                .orElse(null);
        if (saleOrderLine == null) {
            throw new UnprocessableEntityException(
                    "Sale order line doesn't exists for product " + productCode + " in Odoo");
        }

        saleOrderLineService.delete(String.valueOf(saleOrderLine.getId()));
        log.info(
                "Deleted sale order line with id {} from sale order with id {}",
                saleOrderLine.getId(),
                existingSaleOrder.getId());
    }

    @Override
    public Optional<ServiceRequest> getById(@Nonnull String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
