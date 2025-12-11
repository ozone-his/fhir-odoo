/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers.odoo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.model.Country;
import com.ozonehis.fhir.odoo.model.CountryState;
import com.ozonehis.fhir.odoo.model.Partner;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked", "rawtypes"})
class PartnerMapperTest {

    private PartnerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PartnerMapper<>();
    }

    @Test
    @DisplayName("Should return null when resource map is null")
    void shouldReturnNullWhenResourceMapIsNull() {
        assertNull(mapper.toOdoo(null));
    }

    @Test
    @DisplayName("Should return null when resource map is empty")
    void shouldReturnNullWhenResourceMapIsEmpty() {
        assertNull(mapper.toOdoo(new HashMap<>()));
    }

    @Test
    @DisplayName("Should return null when patient is null")
    void shouldReturnNullWhenPatientIsNull() {
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put(OdooConstants.MODEL_FHIR_PATIENT, null);
        assertNull(mapper.toOdoo(resourceMap));
    }

    @Test
    @DisplayName("Should map patient to partner correctly")
    void shouldMapPatientToPartnerCorrectly() {
        Map<String, Object> resourceMap = new HashMap<>();
        Patient patient = new Patient();
        patient.setId("123");
        patient.setActive(true);

        HumanName name = new HumanName();
        name.addGiven("John");
        name.setFamily("Doe");
        patient.addName(name);

        Identifier identifier = new Identifier();
        identifier.setUse(Identifier.IdentifierUse.OFFICIAL);
        identifier.setValue("PATIENT-001");
        patient.addIdentifier(identifier);

        Date birthDate = new Date();
        patient.setBirthDate(birthDate);

        Address address = new Address();
        address.setCity("New York");
        address.setPostalCode("10001");
        address.setCountry("USA");
        address.setType(Address.AddressType.BOTH);
        patient.addAddress(address);

        resourceMap.put(OdooConstants.MODEL_FHIR_PATIENT, patient);

        Partner partner = mapper.toOdoo(resourceMap);

        assertNotNull(partner);
        assertEquals("123", partner.getPartnerRef());
        assertEquals(true, partner.getPartnerActive());
        assertEquals("John Doe", partner.getName());
        assertEquals("PATIENT-001", partner.getPartnerComment());
        assertEquals("PATIENT-001", partner.getPartnerExternalId());
        assertEquals("New York", partner.getPartnerCity());
        assertEquals("10001", partner.getPartnerZip());
        assertEquals("Postal & Physical", partner.getPartnerType());
    }

    @Test
    @DisplayName("Should map patient with country and state")
    void shouldMapPatientWithCountryAndState() {
        Map<String, Object> resourceMap = new HashMap<>();
        Patient patient = new Patient();
        patient.setId("123");
        patient.setActive(true);

        HumanName name = new HumanName();
        name.addGiven("Jane");
        name.setFamily("Smith");
        patient.addName(name);

        Address address = new Address();
        address.setCity("Los Angeles");
        address.setPostalCode("90001");
        address.setCountry("USA");
        address.setState("California");
        patient.addAddress(address);

        Date birthDate = new Date();
        patient.setBirthDate(birthDate);

        Country country = new Country();
        country.setId(1);
        country.setName("United States");

        CountryState countryState = new CountryState();
        countryState.setId(5);
        countryState.setName("California");

        resourceMap.put(OdooConstants.MODEL_FHIR_PATIENT, patient);
        resourceMap.put(OdooConstants.MODEL_COUNTRY, country);
        resourceMap.put(OdooConstants.MODEL_COUNTRY_STATE, countryState);

        Partner partner = mapper.toOdoo(resourceMap);

        assertNotNull(partner);
        assertEquals(1, partner.getPartnerCountryId());
        assertEquals(5, partner.getPartnerStateId());
    }

    @Test
    @DisplayName("Should map address extensions correctly")
    void shouldMapAddressExtensionsCorrectly() {
        Map<String, Object> resourceMap = new HashMap<>();
        Patient patient = new Patient();
        patient.setId("123");
        patient.setActive(true);

        HumanName name = new HumanName();
        name.addGiven("Test");
        name.setFamily("User");
        patient.addName(name);

        Date birthDate = new Date();
        patient.setBirthDate(birthDate);

        Address address = new Address();
        address.setCity("Boston");

        Extension addressExtension = new Extension();
        addressExtension.setUrl(OdooConstants.FHIR_OPENMRS_EXT_ADDRESS);

        Extension address1Extension = new Extension();
        address1Extension.setUrl(OdooConstants.FHIR_OPENMRS_EXT_ADDRESS1);
        address1Extension.setValue(new StringType("123 Main St"));

        Extension address2Extension = new Extension();
        address2Extension.setUrl(OdooConstants.FHIR_OPENMRS_EXT_ADDRESS2);
        address2Extension.setValue(new StringType("Apt 4B"));

        addressExtension.addExtension(address1Extension);
        addressExtension.addExtension(address2Extension);
        address.addExtension(addressExtension);

        patient.addAddress(address);

        resourceMap.put(OdooConstants.MODEL_FHIR_PATIENT, patient);

        Partner partner = mapper.toOdoo(resourceMap);

        assertNotNull(partner);
        assertEquals("123 Main St", partner.getPartnerStreet());
        assertEquals("Apt 4B", partner.getPartnerStreet2());
    }

    @Test
    @DisplayName("Should handle patient without address")
    void shouldHandlePatientWithoutAddress() {
        Map<String, Object> resourceMap = new HashMap<>();
        Patient patient = new Patient();
        patient.setId("123");
        patient.setActive(true);

        HumanName name = new HumanName();
        name.addGiven("Test");
        name.setFamily("Patient");
        patient.addName(name);

        Date birthDate = new Date();
        patient.setBirthDate(birthDate);

        resourceMap.put(OdooConstants.MODEL_FHIR_PATIENT, patient);

        Partner partner = mapper.toOdoo(resourceMap);

        assertNotNull(partner);
        assertNull(partner.getPartnerCity());
        assertNull(partner.getPartnerZip());
    }
}
