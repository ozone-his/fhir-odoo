/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.chargeItemDefinition.impl;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.api.ExternalIdentifierService;
import com.ozonehis.fhir.odoo.api.ProductService;
import com.ozonehis.fhir.odoo.chargeItemDefinition.ChargeItemDefinitionService;
import com.ozonehis.fhir.odoo.mappers.ChargeItemDefinitionMapper;
import com.ozonehis.fhir.odoo.model.ExternalIdentifier;
import com.ozonehis.fhir.odoo.model.OdooResource;
import com.ozonehis.fhir.odoo.model.Product;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("rawtypes, unchecked")
public class ChargeItemDefinitionServiceImpl implements ChargeItemDefinitionService {

    private final ExternalIdentifierService externalIdentifierService;

    private final ProductService productService;

    private final ChargeItemDefinitionMapper chargeItemDefinitionMapper;

    public ChargeItemDefinitionServiceImpl(
            ExternalIdentifierService externalIdentifierService,
            ProductService productService,
            ChargeItemDefinitionMapper chargeItemDefinitionMapper) {
        this.externalIdentifierService = externalIdentifierService;
        this.productService = productService;
        this.chargeItemDefinitionMapper = chargeItemDefinitionMapper;
    }

    @Override
    public Bundle searchForChargeItemDefinitions(TokenAndListParam code) {
        Bundle bundle = new Bundle();

        List<String> codes = new ArrayList<>();
        code.getValuesAsQueryTokens()
                .forEach(value -> value.getValuesAsQueryTokens().forEach(v -> codes.add(v.getValue())));

        if (!codes.isEmpty()) {
            Collection<ExternalIdentifier> externalIdentifiers =
                    externalIdentifierService.getResIdsByNameAndModel(codes, OdooConstants.MODEL_PRODUCT);
            externalIdentifiers.forEach(externalIdentifier -> {
                Optional<Product> product = productService.getById(String.valueOf(externalIdentifier.getResId()));
                if (product.isPresent() && product.get().isActive()) {
                    Map<String, OdooResource> resourceMap = Map.of(
                            OdooConstants.MODEL_PRODUCT,
                            product.get(),
                            OdooConstants.MODEL_EXTERNAL_IDENTIFIER,
                            externalIdentifier);
                    bundle.addEntry().setResource(chargeItemDefinitionMapper.toFhir(resourceMap));
                }
            });
        }

        return bundle;
    }

    @Override
    public Optional<ChargeItemDefinition> getById(@Nonnull String id) {
        Optional<ExternalIdentifier> externalIdentifier =
                externalIdentifierService.getByNameAndModel(id, OdooConstants.MODEL_PRODUCT);
        if (externalIdentifier.isEmpty()) {
            log.warn("Inventory Item with ID {} missing an External ID Identifier", id);
            return Optional.empty();
        }
        Optional<Product> product =
                productService.getById(String.valueOf(externalIdentifier.get().getResId()));
        if (product.isEmpty()) {
            log.warn("Inventory Item with ID {} missing a Product", id);
            return Optional.empty();
        }
        Map<String, OdooResource> resourceMap = Map.of(
                OdooConstants.MODEL_PRODUCT, product.get(),
                OdooConstants.MODEL_EXTERNAL_IDENTIFIER, externalIdentifier.get());

        return Optional.of(chargeItemDefinitionMapper.toFhir(resourceMap));
    }
}
