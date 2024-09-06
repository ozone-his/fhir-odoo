/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.BaseOdooModel;
import com.ozonehis.fhir.odoo.model.ExternalIdentifier;
import com.ozonehis.fhir.odoo.model.OdooResource;
import com.ozonehis.fhir.odoo.model.Product;
import java.util.Map;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Money;
import org.springframework.stereotype.Component;

@Component
public class ChargeItemDefinitionMapper<O extends BaseOdooModel & OdooResource>
        implements ToFhirMapping<O, ChargeItemDefinition> {

    @Override
    public ChargeItemDefinition toFhir(Map<String, O> resourceMap) {
        ChargeItemDefinition chargeItemDefinition = new ChargeItemDefinition();
        if (resourceMap == null || resourceMap.isEmpty()) {
            return null;
        }
        Product product = (Product) resourceMap.get(OdooConstants.MODEL_PRODUCT);
        ExternalIdentifier externalIdentifier =
                (ExternalIdentifier) resourceMap.get(OdooConstants.MODEL_EXTERNAL_IDENTIFIER);

        if (product == null || externalIdentifier == null) {
            return null;
        }
        chargeItemDefinition.setId(externalIdentifier.getName());
        chargeItemDefinition.setName(product.getName());
        chargeItemDefinition.setDescription(product.getDescription());

        chargeItemDefinition.setDate(product.getLastModifiedOn());

        // Possibly, this is wrong
        if (product.isActive()) {
            chargeItemDefinition.setStatus(Enumerations.PublicationStatus.ACTIVE);
        } else {
            chargeItemDefinition.setStatus(Enumerations.PublicationStatus.RETIRED);
        }

        // Price Component
        ChargeItemDefinition.ChargeItemDefinitionPropertyGroupPriceComponentComponent priceComponent =
                new ChargeItemDefinition.ChargeItemDefinitionPropertyGroupPriceComponentComponent();
        Money money = new Money();
        money.setValue(product.getPrice());
        money.setCurrency(product.getCurrencyId().toString());
        priceComponent.setAmount(money);
        // Base price
        priceComponent.setType(ChargeItemDefinition.ChargeItemDefinitionPriceComponentType.BASE);
        chargeItemDefinition.addPropertyGroup().addPriceComponent(priceComponent);

        return chargeItemDefinition;
    }
}
