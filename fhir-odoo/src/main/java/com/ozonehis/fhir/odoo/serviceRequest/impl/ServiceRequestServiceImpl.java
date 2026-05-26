/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.serviceRequest.impl;

import static com.ozonehis.fhir.odoo.OdooConstants.LOINC_SOURCE;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.api.ExtIdService;
import com.ozonehis.fhir.odoo.api.PartnerService;
import com.ozonehis.fhir.odoo.api.ProductService;
import com.ozonehis.fhir.odoo.api.SaleOrderLineService;
import com.ozonehis.fhir.odoo.api.SaleOrderService;
import com.ozonehis.fhir.odoo.lock.DistributedLockManager;
import com.ozonehis.fhir.odoo.lock.LockPurpose;
import com.ozonehis.fhir.odoo.mappers.SaleOrderLineMapper;
import com.ozonehis.fhir.odoo.mappers.SaleOrderMapper;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.Partner;
import com.ozonehis.fhir.odoo.model.Product;
import com.ozonehis.fhir.odoo.model.SaleOrder;
import com.ozonehis.fhir.odoo.model.SaleOrderLine;
import com.ozonehis.fhir.odoo.serviceRequest.ServiceRequestService;
import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Identifier;
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

    private final ExtIdService extIdService;

    private final DistributedLockManager distributedLockManager;

    @Autowired
    public ServiceRequestServiceImpl(
            SaleOrderService saleOrderService,
            SaleOrderLineService saleOrderLineService,
            SaleOrderLineMapper saleOrderLineMapper,
            SaleOrderMapper saleOrderMapper,
            PartnerService partnerService,
            ProductService productService,
            ExtIdService extIdService,
            DistributedLockManager distributedLockManager) {
        this.saleOrderService = saleOrderService;
        this.saleOrderLineService = saleOrderLineService;
        this.saleOrderLineMapper = saleOrderLineMapper;
        this.saleOrderMapper = saleOrderMapper;
        this.partnerService = partnerService;
        this.productService = productService;
        this.extIdService = extIdService;
        this.distributedLockManager = distributedLockManager;
    }

    @Override
    public ServiceRequest create(ServiceRequest serviceRequest) {
        if (!serviceRequest.hasRequisition()) {
            log.error("ServiceRequest with id {} does not have a requisition value", serviceRequest.getIdPart());
            throw new UnprocessableEntityException("ServiceRequest does not have a requisition value");
        }

        if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.ACTIVE)) {
            String requisitionId = serviceRequest.getRequisition().getValue();
            distributedLockManager.executeWithLock(LockPurpose.SERVICE_REQUEST_REQUISITION, requisitionId, () -> {
                SaleOrder saleOrder = createSaleOrder(serviceRequest);
                createSaleOrderLine(serviceRequest, saleOrder);
            });
        } else if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.REVOKED)
                || serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.ENTEREDINERROR)) {
            // Delete sale order line and check if sale order is empty delete sale order also
            deleteSaleOrderLine(serviceRequest);
            cancelSaleOrder(serviceRequest);
        } else if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.COMPLETED)) {
            log.debug(
                    "ServiceRequest {} is completed; sale order confirmation is not implemented yet",
                    serviceRequest.getIdPart());
        } else if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.UNKNOWN)) {
            log.debug("Ignoring ServiceRequest {} with UNKNOWN status", serviceRequest.getIdPart());
        }

        return serviceRequest;
    }

    private SaleOrder createSaleOrder(ServiceRequest serviceRequest) {
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put(OdooConstants.MODEL_FHIR_SERVICE_REQUEST, serviceRequest);
        resourceMap.put(OdooConstants.MODEL_COMPANY, getCompanyExtId(getFacilityId(serviceRequest)));

        String patientIdReference = serviceRequest.getSubject().getReference().split("/")[1];
        Partner partner = partnerService.getByRef(patientIdReference).orElse(null);
        if (partner == null) {
            log.error("Partner with id {} doesn't exists in Odoo", patientIdReference);
            throw new UnprocessableEntityException("Partner doesn't exists in Odoo");
        }
        resourceMap.put(OdooConstants.MODEL_PARTNER, partner);
        SaleOrder saleOrder = saleOrderMapper.toOdoo(resourceMap);

        if (saleOrder == null) {
            log.error("Unable to create sale order in Odoo because required ServiceRequest data is missing");
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
            log.error("Unable to create sale order in Odoo");
            throw new InvalidRequestException("Unable to create sale order in Odoo");
        }
        saleOrder.setId(id);
        log.info("Created sale order with id {}", saleOrder.getId());

        return saleOrder;
    }

    private SaleOrderLine createSaleOrderLine(ServiceRequest serviceRequest, SaleOrder saleOrder) {
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put(OdooConstants.MODEL_COMPANY, getCompanyExtId(getFacilityId(serviceRequest)));

        String productCode = getProductCode(serviceRequest);
        Product product = productService.getByConceptCode(productCode).orElse(null);
        if (product == null) {
            log.error("Product with concept code {} doesn't exists in Odoo", productCode);
            throw new UnprocessableEntityException("Product doesn't exists in Odoo");
        }

        SaleOrderLine saleOrderLine = saleOrderLineService
                .getBySaleOrderIdAndProductId(saleOrder.getId(), product.getId())
                .orElse(null);
        if (saleOrderLine != null) {
            log.warn("Sale order line already exists for product {} in Odoo", productCode);
            throw new UnprocessableEntityException("Sale order line already exists in Odoo");
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

    private String getProductCode(ServiceRequest serviceRequest) {
        String productCode = null;
        for (int i = 0; i < serviceRequest.getCode().getCoding().size(); i++) {
            if (serviceRequest.getCode().getCoding().get(i).getSystem().equals(LOINC_SOURCE)) {
                productCode = serviceRequest.getCode().getCodingFirstRep().getCode();
                break;
            }
        }
        if (productCode == null || productCode.isEmpty()) {
            log.error("ServiceRequest with id {} doesn't have LOINC mapping for product", serviceRequest.getIdPart());
            throw new UnprocessableEntityException("ServiceRequest doesn't have LOINC mapping for product");
        }
        return productCode;
    }

    private void cancelSaleOrder(ServiceRequest serviceRequest) {
        SaleOrder existingSaleOrder = saleOrderService
                .getByName(serviceRequest.getRequisition().getValue())
                .orElse(null);
        if (existingSaleOrder == null) {
            log.error(
                    "Unable to cancel order, sale order doesn't exist for ServiceRequest with id {}",
                    serviceRequest.getId());
            throw new UnprocessableEntityException("Sale order doesn't exist for ServiceRequest");
        }

        Object orderLine = existingSaleOrder.getOrderLine();
        boolean hasOrderLines = false;
        if (orderLine != null) {
            if (orderLine instanceof Object[]) {
                hasOrderLines = ((Object[]) orderLine).length > 0;
            }
        }
        if (hasOrderLines) {
            log.warn("Sale order has order lines, unable to cancel sale order");
            return;
        }

        existingSaleOrder.setOrderState("cancel");

        Map<String, Object> saleOrderMap = saleOrderService.convertSaleOrderToMap(existingSaleOrder);
        int id = saleOrderService.update(String.valueOf(existingSaleOrder.getId()), saleOrderMap);
        if (id == 0) {
            log.error("Unable to cancel sale order {} in Odoo", existingSaleOrder.getName());
            throw new InvalidRequestException("Unable to cancel sale order in Odoo");
        }

        log.info("Cancelled sale order with id {} and name {}", existingSaleOrder.getId(), existingSaleOrder.getName());
    }

    private void deleteSaleOrderLine(ServiceRequest serviceRequest) {
        SaleOrder existingSaleOrder = saleOrderService
                .getByName(serviceRequest.getRequisition().getValue())
                .orElse(null);
        if (existingSaleOrder == null) {
            log.error(
                    "Unable to delete sale order line, sale order doesn't exist for ServiceRequest with id {} ",
                    serviceRequest.getIdPart());
            throw new UnprocessableEntityException("Sale order doesn't exist for ServiceRequest");
        }

        String productCode = getProductCode(serviceRequest);
        Product product = productService.getByConceptCode(productCode).orElse(null);
        if (product == null) {
            log.error("Product with concept code {} doesn't exists in Odoo", productCode);
            throw new UnprocessableEntityException("Product doesn't exists in Odoo");
        }

        SaleOrderLine saleOrderLine = saleOrderLineService
                .getBySaleOrderIdAndProductId(existingSaleOrder.getId(), product.getId())
                .orElse(null);
        if (saleOrderLine == null) {
            log.error("Sale order line doesn't exists for product {} in Odoo", productCode);
            throw new UnprocessableEntityException("Sale order line doesn't exists in Odoo");
        }

        saleOrderLineService.delete(String.valueOf(saleOrderLine.getId()));
        log.info(
                "Deleted sale order line with id {} from sale order with id {}",
                saleOrderLine.getId(),
                existingSaleOrder.getId());
    }

    private ExtId getCompanyExtId(String facilityId) {
        Collection<ExtId> extIds = extIdService.getResIdsByNameAndModel(
                Collections.singletonList(facilityId), OdooConstants.MODEL_COMPANY);
        if (extIds.stream().findFirst().isPresent()) {
            return extIds.stream().findFirst().get();
        }
        log.error("Missing company mapping in Odoo with facility id {}", facilityId);
        throw new UnprocessableEntityException("Missing company mapping in Odoo");
    }

    private String getFacilityId(ServiceRequest serviceRequest) {
        for (Identifier identifier : serviceRequest.getIdentifier()) {
            if (identifier.getSystem().equals(OdooConstants.IDENTIFIER_FACILITY_ID_SYSTEM)) {
                if (identifier.getAssigner() != null
                        && identifier.getAssigner().getReference() != null
                        && !identifier.getAssigner().getReference().isEmpty()) {
                    return identifier.getValue();
                }
            }
        }
        log.error("Facility identifier is missing in ServiceRequest with id {}", serviceRequest.getIdPart());
        throw new UnprocessableEntityException("Facility identifier is missing in ServiceRequest");
    }

    @Override
    public Optional<ServiceRequest> getById(@Nonnull String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
