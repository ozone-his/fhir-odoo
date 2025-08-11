/*
 * Copyright (C) Amiyul LLC - All Rights Reserved
 *
 * This source code is protected under international copyright law. All rights
 * reserved and protected by the copyright holder.
 *
 * This file is confidential and only available to authorized individuals with the
 * permission of the copyright holder. If you encounter this file and do not have
 * permission, please contact the copyright holder and delete this file.
 */
package com.ozonehis.fhir.odoo.mappers;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.BaseOdooModel;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.Product;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Medication.MedicationStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MedicationMapper implements ToFhirMapping<BaseOdooModel, Medication> {

    @Override
    public Medication toFhir(Map<String, BaseOdooModel> resourceMap) {
        Medication medication = new Medication();
        Product product = (Product) resourceMap.get(OdooConstants.MODEL_PRODUCT);
        ExtId extId = (ExtId) resourceMap.get(OdooConstants.MODEL_EXTERNAL_IDENTIFIER);
        medication.setId(extId.getName());
        MedicationStatus status;
        if (product.isActive()) {
            status = MedicationStatus.ACTIVE;
        } else {
            status = MedicationStatus.INACTIVE;
        }

        medication.setStatus(status);
        return medication;
    }

}
