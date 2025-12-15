/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.patient.impl;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.api.CountryService;
import com.ozonehis.fhir.odoo.api.CountryStateService;
import com.ozonehis.fhir.odoo.api.PartnerService;
import com.ozonehis.fhir.odoo.mappers.PatientMapper;
import com.ozonehis.fhir.odoo.model.Country;
import com.ozonehis.fhir.odoo.model.CountryState;
import com.ozonehis.fhir.odoo.model.Partner;
import com.ozonehis.fhir.odoo.patient.PatientService;
import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("rawtypes, unchecked")
public class PatientServiceImpl implements PatientService {

    private final CountryService countryService;

    private final CountryStateService countryStateService;

    private final PartnerService partnerService;

    private final PatientMapper patientMapper;

    @Autowired
    public PatientServiceImpl(
            CountryService countryService,
            CountryStateService countryStateService,
            PartnerService partnerService,
            PatientMapper patientMapper) {
        this.countryService = countryService;
        this.countryStateService = countryStateService;
        this.partnerService = partnerService;
        this.patientMapper = patientMapper;
    }

    @Override
    public Patient create(Patient patient) {
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put(OdooConstants.MODEL_FHIR_PATIENT, patient);
        Optional<Country> country =
                countryService.getByName(patient.getAddress().get(0).getCountry());
        country.ifPresent(value -> resourceMap.put(OdooConstants.MODEL_COUNTRY, value));

        Optional<CountryState> countryState =
                countryStateService.getByName(patient.getAddress().get(0).getState());
        countryState.ifPresent(value -> resourceMap.put(OdooConstants.MODEL_COUNTRY_STATE, value));

        Partner partner = patientMapper.toOdoo(resourceMap);

        if (partner == null) {
            log.error("Unable to create partner in Odoo because required patient data is missing");
            throw new UnprocessableEntityException("Fields missing in Patient payload");
        }

        Map<String, Object> partnerMap = partnerService.convertPartnerToMap(partner);

        int id = partnerService.create(partnerMap);
        if (id == 0) {
            log.error("Unable to create partner in Odoo");
            throw new InvalidRequestException("Unable to create Partner in Odoo");
        }
        return patient;
    }

    @Override
    public Optional<Patient> getById(@Nonnull String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
