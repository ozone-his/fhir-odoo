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
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.Product;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Medication.MedicationStatus;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

@Component
public class MedicationMapper implements ToFhirMapping<BaseOdooModel, Medication> {

    @Override
    public Medication toFhir(Map<String, BaseOdooModel> resourceMap) {
        Medication medication = new Medication();
        Product product = (Product) resourceMap.get(OdooConstants.MODEL_PRODUCT);
        ExtId extId = (ExtId) resourceMap.get(OdooConstants.MODEL_EXTERNAL_IDENTIFIER);
        medication.setId(extId.getName());
        CodeableConcept code = new CodeableConcept();
        code.addCoding().setCode(product.getConceptCode()).setSystem(product.getConceptSource());
        medication.setCode(code);
        MedicationStatus status;
        if (product.isActive()) {
            status = MedicationStatus.ACTIVE;
        } else {
            status = MedicationStatus.INACTIVE;
        }

        medication.setStatus(status);
        addExtension(medication, OdooConstants.FHIR_OPENMRS_EXT_DRUG_NAME, product.getName());
        if (StringUtils.isNotBlank(product.getDrugStrength())) {
            addExtension(medication, OdooConstants.FHIR_OPENMRS_EXT_DRUG_STRENGTH, product.getDrugStrength());
        }

        return medication;
    }

    private static void addExtension(Medication medication, String uri, String value) {
        Extension medExt = medication.getExtensionByUrl(OdooConstants.FHIR_OPENMRS_FHIR_EXT_MEDICINE);
        if (medExt == null) {
            medExt = medication.addExtension().setUrl(OdooConstants.FHIR_OPENMRS_FHIR_EXT_MEDICINE);
        }

        Extension ext = new Extension();
        ext.setUrl(uri);
        ext.setValue(new StringType(value));
        medExt.addExtension(ext);
    }
}
