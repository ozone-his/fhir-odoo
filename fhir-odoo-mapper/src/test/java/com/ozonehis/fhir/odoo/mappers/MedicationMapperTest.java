/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers;

import static com.ozonehis.fhir.odoo.OdooConstants.FHIR_OPENMRS_EXT_DRUG_NAME;
import static com.ozonehis.fhir.odoo.OdooConstants.FHIR_OPENMRS_EXT_DRUG_STRENGTH;
import static com.ozonehis.fhir.odoo.OdooConstants.FHIR_OPENMRS_FHIR_EXT_MEDICINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.BaseOdooModel;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.Product;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.Test;

public class MedicationMapperTest {

    private MedicationMapper mapper = new MedicationMapper();

    @Test
    public void toFhir_shouldConvertADrugProductToAMedication() {
        final String externalId = "some-uuid";
        final String name = "Tylenol";
        final String strength = "500mg";
        final String sourceUri = "http://localhost";
        final String code = "12345";
        Map<String, BaseOdooModel> map = new HashMap<>();
        Product product = new Product();
        product.setName(name);
        product.setConceptSource(sourceUri);
        product.setConceptCode(code);
        product.setDrugStrength(strength);
        product.setActive(true);
        ExtId extId = new ExtId();
        extId.setName(externalId);
        map.put(OdooConstants.MODEL_PRODUCT, product);
        map.put(OdooConstants.MODEL_EXTERNAL_IDENTIFIER, extId);

        Medication m = mapper.toFhir(map);

        assertEquals(externalId, m.getIdElement().getIdPart());
        assertEquals(1, m.getCode().getCoding().size());
        assertEquals(sourceUri, m.getCode().getCoding().get(0).getSystem());
        assertEquals(code, m.getCode().getCoding().get(0).getCode());
        assertEquals(Medication.MedicationStatus.ACTIVE, m.getStatus());
        Extension medExt = m.getExtensionByUrl(FHIR_OPENMRS_FHIR_EXT_MEDICINE);
        assertEquals(
                name,
                medExt.getExtensionByUrl(FHIR_OPENMRS_EXT_DRUG_NAME).getValue().toString());
        assertEquals(
                strength,
                medExt.getExtensionByUrl(FHIR_OPENMRS_EXT_DRUG_STRENGTH)
                        .getValue()
                        .toString());
    }

    @Test
    public void toFhir_shouldConvertADrugProductWithNoStrengthOrConceptMapping() {
        final String externalId = "some-uuid";
        final String name = "Tylenol";
        Map<String, BaseOdooModel> map = new HashMap<>();
        Product product = new Product();
        product.setName(name);
        product.setActive(true);
        ExtId extId = new ExtId();
        extId.setName(externalId);
        map.put(OdooConstants.MODEL_PRODUCT, product);
        map.put(OdooConstants.MODEL_EXTERNAL_IDENTIFIER, extId);

        Medication m = mapper.toFhir(map);

        assertEquals(externalId, m.getIdElement().getIdPart());
        assertTrue(m.getCode().getCoding().isEmpty());
        assertEquals(Medication.MedicationStatus.ACTIVE, m.getStatus());
        Extension medExt = m.getExtensionByUrl(FHIR_OPENMRS_FHIR_EXT_MEDICINE);
        assertEquals(
                name,
                medExt.getExtensionByUrl(FHIR_OPENMRS_EXT_DRUG_NAME).getValue().toString());
        assertNull(medExt.getExtensionByUrl(FHIR_OPENMRS_EXT_DRUG_STRENGTH));
    }
}
