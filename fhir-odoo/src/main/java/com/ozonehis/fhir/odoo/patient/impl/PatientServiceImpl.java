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
import com.ozonehis.fhir.odoo.model.Partner;
import com.ozonehis.fhir.odoo.patient.PatientService;
import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Address;
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
        validatePatient(patient);

        Map<String, Object> resourceMap = buildResourceMap(patient);
        Partner partner = patientMapper.toOdoo(resourceMap);

        if (partner == null) {
            log.error("Unable to map Patient to Partner: required data missing");
            throw new UnprocessableEntityException("Required fields missing in Patient payload");
        }

        Map<String, Object> partnerMap = partnerService.convertPartnerToMap(partner);
        int partnerId = createOrUpdatePartner(patient.getIdPart(), partnerMap);

        if (partnerId == 0) {
            log.error("Failed to persist Partner in Odoo");
            throw new InvalidRequestException("Unable to persist Partner in Odoo");
        }

        return patient;
    }

    private void validatePatient(Patient patient) {
        if (patient == null || !patient.hasId()) {
            log.error("Patient ID is missing");
            throw new UnprocessableEntityException("Patient ID is required");
        }

        if (patient.getAddress().isEmpty()) {
            log.error("Patient address is missing");
            throw new UnprocessableEntityException("Patient address is required");
        }
    }

    private Map<String, Object> buildResourceMap(Patient patient) {
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put(OdooConstants.MODEL_FHIR_PATIENT, patient);

        Address address = patient.getAddress().get(0);

        countryService
                .getByName(address.getCountry())
                .ifPresent(country -> resourceMap.put(OdooConstants.MODEL_COUNTRY, country));

        countryStateService
                .getByName(address.getState())
                .ifPresent(state -> resourceMap.put(OdooConstants.MODEL_COUNTRY_STATE, state));

        return resourceMap;
    }

    private int createOrUpdatePartner(String patientRef, Map<String, Object> partnerMap) {
        Partner existingPartner = partnerService.getByRef(patientRef).orElse(null);

        if (existingPartner != null) {
            log.info("Partner with reference {} already exists, updating", patientRef);
            return partnerService.update(String.valueOf(existingPartner.getId()), partnerMap);
        } else {
            log.info("Creating new Partner with reference {}", patientRef);
            return partnerService.create(partnerMap);
        }
    }

    @Override
    public Optional<Patient> getById(@Nonnull String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
