/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import static com.ozonehis.fhir.odoo.OdooConstants.FHIR_OPENMRS_INVENTORY_ITEM;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.BaseOdooModel;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.OdooResource;
import com.ozonehis.fhir.odoo.model.Product;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Quantity;
import org.openmrs.fhir.InventoryItem;
import org.springframework.stereotype.Component;

@Component
public class InventoryItemMapper<O extends BaseOdooModel & OdooResource> implements ToFhirMapping<O, InventoryItem> {

    @Override
    public InventoryItem toFhir(Map<String, O> resourceMap) {
        if (MapUtils.isEmpty(resourceMap)) {
            return null;
        }
        InventoryItem inventoryItem = new InventoryItem();
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding openmrsCoding = new Coding();
        Product product = (Product) resourceMap.get(OdooConstants.MODEL_PRODUCT);
        ExtId extId = (ExtId) resourceMap.get(OdooConstants.MODEL_EXTERNAL_IDENTIFIER);
        if (product == null || extId == null) {
            return null;
        }

        inventoryItem.setId(extId.getName());
        InventoryItem.InventoryItemNameComponent nameComponent = new InventoryItem.InventoryItemNameComponent();
        nameComponent.setName(product.getName());
        inventoryItem.addName(nameComponent);

        InventoryItem.InventoryItemDescriptionComponent descriptionComponent =
                new InventoryItem.InventoryItemDescriptionComponent();
        descriptionComponent.setDescription(product.getDescription());
        inventoryItem.setDescription(descriptionComponent);

        if (product.isActive()) {
            inventoryItem.setStatus(InventoryItem.InventoryItemStatusCodes.ACTIVE);
        } else {
            inventoryItem.setStatus(InventoryItem.InventoryItemStatusCodes.INACTIVE);
        }

        Quantity quantity = new Quantity();
        quantity.setValue(product.getQuantityAvailable());
        quantity.setUnit(product.getUomName());
        inventoryItem.setNetContent(quantity);

        openmrsCoding.setCode(extId.getName());
        openmrsCoding.setDisplay(product.getDisplayName());
        openmrsCoding.setSystem(FHIR_OPENMRS_INVENTORY_ITEM);
        codeableConcept.setText(product.getDisplayName());
        codeableConcept.addCoding(openmrsCoding);
        inventoryItem.addCode(codeableConcept);

        return inventoryItem;
    }
}
