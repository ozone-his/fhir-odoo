/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.medication.impl;

import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_EXTERNAL_IDENTIFIER;
import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_PRODUCT;
import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_PRODUCT_CATEGORY;

import com.odoojava.api.FilterCollection;
import com.odoojava.api.OdooApiException;
import com.ozonehis.fhir.odoo.api.ExtIdService;
import com.ozonehis.fhir.odoo.api.ProductService;
import com.ozonehis.fhir.odoo.mappers.MedicationMapper;
import com.ozonehis.fhir.odoo.medication.MedicationService;
import com.ozonehis.fhir.odoo.model.BaseOdooModel;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.Product;
import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Medication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MedicationServiceImpl implements MedicationService {

    public static final String PROP_DRUG_CATEGORY_EXT_ID = "odoo.drugs.category.ext.id";

    @Value("${" + PROP_DRUG_CATEGORY_EXT_ID + ":}")
    private String drugsCategoryExtId;

    private ExtIdService extIdService;

    private ProductService productService;

    private MedicationMapper mapper;

    private Integer drugsCategoryId;

    @Autowired
    public MedicationServiceImpl(ExtIdService extIdService, ProductService productService, MedicationMapper mapper) {
        this.extIdService = extIdService;
        this.productService = productService;
        this.mapper = mapper;
    }

    @Override
    public Bundle getAllMedications() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all drug products from Odoo");
        }

        FilterCollection filter = new FilterCollection();
        try {
            filter.add("categ_id", "=", getDrugsCategoryId());
        } catch (OdooApiException e) {
            throw new RuntimeException(e);
        }

        Collection<Product> products = productService.search(filter);
        if (log.isDebugEnabled()) {
            log.debug("Loading product external ids");
        }

        Bundle bundle = new Bundle();
        products.forEach(product -> {
            Optional<ExtId> extIdOptional = extIdService.getByResourceIdAndModel(product.getId(), MODEL_PRODUCT);
            ExtId extId;
            if (extIdOptional.isEmpty()) {
                log.info("Adding new external id for product with id {}", product.getId());
                final String identifier = UUID.randomUUID().toString();
                extIdService.createExternalId(MODEL_PRODUCT, product.getId(), identifier);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully added new external id for product with id {}", product.getId());
                }

                extId = new ExtId();
                extId.setName(identifier);
            } else {
                extId = extIdOptional.get();
            }

            Map<String, BaseOdooModel> resourceMap = Map.of(MODEL_PRODUCT, product, MODEL_EXTERNAL_IDENTIFIER, extId);
            bundle.addEntry().setResource(mapper.toFhir(resourceMap));
        });

        return bundle;
    }

    @Override
    public Optional<Medication> getById(@Nonnull String id) {
        throw new RuntimeException("Not yet implemented");
    }

    private Integer getDrugsCategoryId() {
        if (drugsCategoryId == null) {
            if (StringUtils.isBlank(drugsCategoryExtId)) {
                throw new RuntimeException(PROP_DRUG_CATEGORY_EXT_ID + " is not defined");
            }

            Optional<ExtId> extId = extIdService.getByNameAndModel(drugsCategoryExtId, MODEL_PRODUCT_CATEGORY);
            if (extId.isEmpty()) {
                throw new RuntimeException("No product category found with external id " + drugsCategoryExtId);
            }

            drugsCategoryId = extId.get().getResId();
        }

        return drugsCategoryId;
    }
}
