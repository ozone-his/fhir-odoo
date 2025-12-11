/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.patient.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.ozonehis.fhir.odoo.api.CountryService;
import com.ozonehis.fhir.odoo.api.CountryStateService;
import com.ozonehis.fhir.odoo.api.PartnerService;
import com.ozonehis.fhir.odoo.mappers.odoo.PartnerMapper;
import com.ozonehis.fhir.odoo.model.Country;
import com.ozonehis.fhir.odoo.model.CountryState;
import com.ozonehis.fhir.odoo.model.Partner;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private CountryService countryService;

    @Mock
    private CountryStateService countryStateService;

    @Mock
    private PartnerService partnerService;

    @Mock
    private PartnerMapper partnerMapper;

    private PatientServiceImpl patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientServiceImpl(countryService, countryStateService, partnerService, partnerMapper);
    }

    @Test
    @DisplayName("Should create patient with country and state")
    void create_shouldCreatePatientWithCountryAndState() {
        Patient patient = new Patient();
        patient.setId("123");

        Address address = new Address();
        address.setCountry("United States");
        address.setState("California");
        patient.addAddress(address);

        Country country = new Country();
        country.setId(1);
        country.setName("United States");

        CountryState countryState = new CountryState();
        countryState.setId(5);
        countryState.setName("California");

        Partner partner = new Partner();
        partner.setName("Test Partner");

        Map<String, Object> partnerMap = new HashMap<>();
        partnerMap.put("name", "Test Partner");

        when(countryService.getByName("United States")).thenReturn(Optional.of(country));
        when(countryStateService.getByName("California")).thenReturn(Optional.of(countryState));
        when(partnerMapper.toOdoo(any())).thenReturn(partner);
        when(partnerService.convertPartnerToMap(partner)).thenReturn(partnerMap);
        when(partnerService.create(partnerMap)).thenReturn(100);

        Patient result = patientService.create(patient);

        assertNotNull(result);
        verify(countryService).getByName("United States");
        verify(countryStateService).getByName("California");
        verify(partnerMapper).toOdoo(any());
        verify(partnerService).convertPartnerToMap(partner);
        verify(partnerService).create(partnerMap);
    }

    @Test
    @DisplayName("Should create patient without country and state when not found")
    void create_shouldCreatePatientWithoutCountryAndStateWhenNotFound() {
        Patient patient = new Patient();
        patient.setId("123");

        Address address = new Address();
        address.setCountry("Unknown Country");
        address.setState("Unknown State");
        patient.addAddress(address);

        Partner partner = new Partner();
        partner.setName("Test Partner");

        Map<String, Object> partnerMap = new HashMap<>();
        partnerMap.put("name", "Test Partner");

        when(countryService.getByName("Unknown Country")).thenReturn(Optional.empty());
        when(countryStateService.getByName("Unknown State")).thenReturn(Optional.empty());
        when(partnerMapper.toOdoo(any())).thenReturn(partner);
        when(partnerService.convertPartnerToMap(partner)).thenReturn(partnerMap);
        when(partnerService.create(partnerMap)).thenReturn(100);

        Patient result = patientService.create(patient);

        assertNotNull(result);
        verify(countryService).getByName("Unknown Country");
        verify(countryStateService).getByName("Unknown State");
        verify(partnerMapper).toOdoo(any());
        verify(partnerService).convertPartnerToMap(partner);
        verify(partnerService).create(partnerMap);
    }

    @Test
    @DisplayName("Should throw error when partner mapper returns null")
    void create_shouldReturnPatientWhenPartnerMapperReturnsNull() {
        Patient patient = new Patient();
        patient.setId("123");

        Address address = new Address();
        address.setCountry("United States");
        patient.addAddress(address);

        when(countryService.getByName("United States")).thenReturn(Optional.empty());
        when(partnerMapper.toOdoo(any())).thenReturn(null);

        Assertions.assertThrows(UnprocessableEntityException.class, () -> patientService.create(patient));
    }

    @Test
    @DisplayName("Should throw error when partnerService returns zero")
    void create_shouldReturnPatientWhenPartnerServiceReturnsZero() {
        Patient patient = new Patient();
        patient.setId("123");

        Address address = new Address();
        address.setCountry("Unknown Country");
        address.setState("Unknown State");
        patient.addAddress(address);

        Partner partner = new Partner();
        partner.setName("Test Partner");

        Map<String, Object> partnerMap = new HashMap<>();
        partnerMap.put("name", "Test Partner");

        when(countryService.getByName("Unknown Country")).thenReturn(Optional.empty());
        when(countryStateService.getByName("Unknown State")).thenReturn(Optional.empty());
        when(partnerMapper.toOdoo(any())).thenReturn(partner);
        when(partnerService.convertPartnerToMap(partner)).thenReturn(partnerMap);
        when(partnerService.create(partnerMap)).thenReturn(0);

        Assertions.assertThrows(InvalidRequestException.class, () -> patientService.create(patient));
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when getById is called")
    void getById_shouldThrowUnsupportedOperationException() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> patientService.getById("123"));
    }
}
