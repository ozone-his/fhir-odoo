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
import com.ozonehis.fhir.odoo.api.CurrencyService;
import com.ozonehis.fhir.odoo.api.ExtIdService;
import com.ozonehis.fhir.odoo.api.ProductService;
import com.ozonehis.fhir.odoo.chargeItemDefinition.ChargeItemDefinitionService;
import com.ozonehis.fhir.odoo.mappers.ChargeItemDefinitionMapper;
import com.ozonehis.fhir.odoo.model.Currency;
import com.ozonehis.fhir.odoo.model.ExtId;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("rawtypes, unchecked")
public class ChargeItemDefinitionServiceImpl implements ChargeItemDefinitionService {

    private final ExtIdService extIdService;

    private final ProductService productService;

    private final CurrencyService currencyService;

    private final ChargeItemDefinitionMapper chargeItemDefinitionMapper;

    @Autowired
    public ChargeItemDefinitionServiceImpl(
            ExtIdService extIdService,
            ProductService productService,
            CurrencyService currencyService,
            ChargeItemDefinitionMapper chargeItemDefinitionMapper) {
        this.extIdService = extIdService;
        this.productService = productService;
        this.currencyService = currencyService;
        this.chargeItemDefinitionMapper = chargeItemDefinitionMapper;
    }

    @Override
    public Bundle searchForChargeItemDefinitions(TokenAndListParam code) {
        Bundle bundle = new Bundle();
        List<String> codes = new ArrayList<>();
        code.getValuesAsQueryTokens()
                .forEach(value -> value.getValuesAsQueryTokens().forEach(v -> codes.add(v.getValue())));

        if (!codes.isEmpty()) {
            Collection<ExtId> extIds = extIdService.getResIdsByNameAndModel(codes, OdooConstants.MODEL_PRODUCT);
            extIds.forEach(externalIdentifier -> {
                Optional<Product> product = productService.getById(String.valueOf(externalIdentifier.getResId()));
                if (product.isPresent() && product.get().isActive()) {
                    Map<String, OdooResource> resourceMap = new java.util.HashMap<>(Map.of(
                            OdooConstants.MODEL_PRODUCT,
                            product.get(),
                            OdooConstants.MODEL_EXTERNAL_IDENTIFIER,
                            externalIdentifier));
                    Optional<Currency> currency =
                            currencyService.getById(String.valueOf(product.get().getCurrencyId()));
                    currency.ifPresent(value -> resourceMap.put(OdooConstants.MODEL_CURRENCY, value));

                    bundle.addEntry().setResource(chargeItemDefinitionMapper.toFhir(resourceMap));
                }
            });
        }

        return bundle;
    }

    @Override
    public Optional<ChargeItemDefinition> getById(@Nonnull String id) {
        Optional<ExtId> externalIdentifier = extIdService.getByNameAndModel(id, OdooConstants.MODEL_PRODUCT);
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

        Map<String, OdooResource> resourceMap = new java.util.HashMap<>(Map.of(
                OdooConstants.MODEL_PRODUCT, product.get(),
                OdooConstants.MODEL_EXTERNAL_IDENTIFIER, externalIdentifier.get()));

        currencyService
                .getById(String.valueOf(product.get().getCurrencyId()))
                .ifPresent(currency -> resourceMap.put(OdooConstants.MODEL_CURRENCY, currency));

        return Optional.of(chargeItemDefinitionMapper.toFhir(resourceMap));
    }
}
