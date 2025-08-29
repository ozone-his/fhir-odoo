/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.medication.impl;

import static com.ozonehis.fhir.odoo.OdooConstants.FHIR_OPENMRS_EXT_DRUG_NAME;
import static com.ozonehis.fhir.odoo.OdooConstants.FHIR_OPENMRS_FHIR_EXT_MEDICINE;
import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_PRODUCT;
import static com.ozonehis.fhir.odoo.OdooConstants.MODEL_PRODUCT_CATEGORY;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.odoojava.api.FilterCollection;
import com.ozonehis.fhir.odoo.api.ExtIdService;
import com.ozonehis.fhir.odoo.api.ProductService;
import com.ozonehis.fhir.odoo.mappers.MedicationMapper;
import com.ozonehis.fhir.odoo.model.ExtId;
import com.ozonehis.fhir.odoo.model.Product;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

@ExtendWith(MockitoExtension.class)
public class MedicationServiceImplTest {

    private static final String DRUG_CATEGORY_EXT_ID = "test-uuid";

    private static final Integer DRUG_CATEGORY_ID = 5;

    @Mock
    private ExtIdService mockExtIdService;

    @Mock
    private ProductService mockProductService;

    private MedicationMapper mapper = new MedicationMapper();

    private MedicationServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new MedicationServiceImpl(mockExtIdService, mockProductService, mapper);
        Whitebox.setInternalState(service, "drugsCategoryExtId", DRUG_CATEGORY_EXT_ID);
        ExtId extId = new ExtId();
        extId.setResId(DRUG_CATEGORY_ID);
        when(mockExtIdService.getByNameAndModel(DRUG_CATEGORY_EXT_ID, MODEL_PRODUCT_CATEGORY))
                .thenReturn(of(extId));
    }

    @Test
    public void getAllMedications_shouldReturnAllMedications() {
        final String externalId1 = "uuid-1";
        final String externalId2 = "uuid-2";
        Product drug1 = new Product();
        drug1.setId(1);
        drug1.setName("Tylenol");
        drug1.setActive(true);
        Product drug2 = new Product();
        drug2.setId(2);
        drug2.setName("Advil");
        drug2.setActive(true);
        ExtId extId1 = new ExtId();
        extId1.setName(externalId1);
        ExtId extId2 = new ExtId();
        extId2.setName(externalId2);
        when(mockExtIdService.getByResourceIdAndModel(drug1.getId(), MODEL_PRODUCT))
                .thenReturn(of(extId1));
        when(mockExtIdService.getByResourceIdAndModel(drug2.getId(), MODEL_PRODUCT))
                .thenReturn(of(extId2));
        ArgumentCaptor<FilterCollection> collArgCaptor = ArgumentCaptor.forClass(FilterCollection.class);
        when(mockProductService.search(collArgCaptor.capture())).thenReturn(List.of(drug1, drug2));

        Bundle bundle = service.getAllMedications();

        Object[] filter1 = (Object[]) collArgCaptor.getValue().getFilters()[0];
        Object[] filter2 = (Object[]) collArgCaptor.getValue().getFilters()[1];
        assertEquals(filter1.length, 3);
        assertEquals("categ_id", filter1[0]);
        assertEquals("=", filter1[1]);
        assertEquals(DRUG_CATEGORY_ID, filter1[2]);
        assertEquals(filter2.length, 3);
        assertEquals("active", filter2[0]);
        assertEquals("in", filter2[1]);
        assertEquals(List.of(true, false), filter2[2]);
        assertEquals(2, bundle.getEntry().size());
        Medication med1 = (Medication) bundle.getEntry().get(0).getResource();
        assertEquals(externalId1, med1.getIdElement().getIdPart());
        final Extension medExt1 = med1.getExtensionByUrl(FHIR_OPENMRS_FHIR_EXT_MEDICINE);
        assertEquals(drug1.getName(), medExt1.getExtensionString(FHIR_OPENMRS_EXT_DRUG_NAME));
        Medication med2 = (Medication) bundle.getEntry().get(1).getResource();
        assertEquals(externalId2, med2.getIdElement().getIdPart());
        final Extension medExt12 = med2.getExtensionByUrl(FHIR_OPENMRS_FHIR_EXT_MEDICINE);
        assertEquals(drug2.getName(), medExt12.getExtensionString(FHIR_OPENMRS_EXT_DRUG_NAME));
    }

    @Test
    public void getAllMedications_shouldAddExternalIdForDrugIfMissing() {
        final Integer id = 1;
        Product drug = new Product();
        drug.setId(id);
        drug.setName("Tylenol");
        drug.setActive(true);
        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        when(mockExtIdService.createExternalId(eq(MODEL_PRODUCT), eq(id), argCaptor.capture()))
                .thenReturn(23);
        when(mockProductService.search(ArgumentMatchers.any(FilterCollection.class)))
                .thenReturn(List.of(drug));

        Bundle bundle = service.getAllMedications();

        assertEquals(1, bundle.getEntry().size());
        Medication med = (Medication) bundle.getEntry().get(0).getResource();
        assertEquals(argCaptor.getValue(), med.getIdElement().getIdPart());
        final Extension medExt = med.getExtensionByUrl(FHIR_OPENMRS_FHIR_EXT_MEDICINE);
        assertEquals(drug.getName(), medExt.getExtensionString(FHIR_OPENMRS_EXT_DRUG_NAME));
    }
}
