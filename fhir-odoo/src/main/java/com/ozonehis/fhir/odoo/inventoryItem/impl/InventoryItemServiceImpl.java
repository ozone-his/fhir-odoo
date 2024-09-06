/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.inventoryItem.impl;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import com.odoojava.api.FilterCollection;
import com.odoojava.api.OdooApiException;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.api.ExternalIdentifierService;
import com.ozonehis.fhir.odoo.api.ProductService;
import com.ozonehis.fhir.odoo.inventoryItem.InventoryItemService;
import com.ozonehis.fhir.odoo.mappers.InventoryItemMapper;
import com.ozonehis.fhir.odoo.model.ExternalIdentifier;
import com.ozonehis.fhir.odoo.model.OdooResource;
import com.ozonehis.fhir.odoo.model.Product;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.fhir.InventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class InventoryItemServiceImpl implements InventoryItemService {

    private final ExternalIdentifierService externalIdentifierService;

    private final ProductService productService;

    private final InventoryItemMapper inventoryItemMapper;

    @Autowired
    public InventoryItemServiceImpl(ExternalIdentifierService externalIdentifierService, ProductService productService, InventoryItemMapper inventoryItemMapper) {
        this.externalIdentifierService = externalIdentifierService;
        this.productService = productService;
        this.inventoryItemMapper = inventoryItemMapper;
    }

    @Override
    public Optional<InventoryItem> getById(@Nonnull String id) {
        Optional<ExternalIdentifier> externalIdentifier = externalIdentifierService.getByNameAndModel(id, OdooConstants.MODEL_PRODUCT);
        if (externalIdentifier.isEmpty()) {
            log.warn("Inventory Item with ID {} missing an External ID Identifier", id);
            return Optional.empty();
        }
        Optional<Product> product = productService.getById(String.valueOf(externalIdentifier.get().getResId()));
        if (product.isEmpty()) {
            log.warn("Inventory Item with ID {} missing a Product", id);
            return Optional.empty();
        }
        Map<String, OdooResource> resourceMap = Map.of(
                OdooConstants.MODEL_PRODUCT, product.get(),
                OdooConstants.MODEL_EXTERNAL_IDENTIFIER, externalIdentifier.get()
        );

        return Optional.of(inventoryItemMapper.toFhir(resourceMap));
    }


    @Override
    public Bundle searchForInventoryItems(TokenAndListParam code) {
        Bundle bundle = new Bundle();

        List<String> codes = new ArrayList<>();
        code.getValuesAsQueryTokens()
                .forEach(value -> value.getValuesAsQueryTokens()
                        .forEach(v -> codes.add(v.getValue())));

        if (!codes.isEmpty()) {
           Collection<ExternalIdentifier> externalIdentifiers = externalIdentifierService.getResIdsByNameAndModel(codes, OdooConstants.MODEL_PRODUCT);
            externalIdentifiers.forEach(externalIdentifier -> {
                Optional<Product> product = productService.getById(String.valueOf(externalIdentifier.getResId()));
                if (product.isPresent() && product.get().isActive()) {
                    Map<String, OdooResource> resourceMap = Map.of(
                            OdooConstants.MODEL_PRODUCT, product.get(),
                            OdooConstants.MODEL_EXTERNAL_IDENTIFIER, externalIdentifier
                    );
                    bundle.addEntry().setResource(inventoryItemMapper.toFhir(resourceMap));
                }
            });
        }

        return bundle;
    }
}
