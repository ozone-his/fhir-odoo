/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.mappers.odoo;

import com.ozonehis.fhir.odoo.OdooConstants;
import com.ozonehis.fhir.odoo.OdooUtils;
import com.ozonehis.fhir.odoo.model.Country;
import com.ozonehis.fhir.odoo.model.CountryState;
import com.ozonehis.fhir.odoo.model.Partner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PartnerMapper<F extends IAnyResource> implements ToOdooMapping<F, Partner> {

    @Override
    public Partner toOdoo(Map<String, F> resourceMap) {
        if (MapUtils.isEmpty(resourceMap)) {
            return null;
        }
        Partner partner = new Partner();
        Patient patient = (Patient) resourceMap.get(OdooConstants.MODEL_FHIR_PATIENT);
        Country country = (Country) resourceMap.get(OdooConstants.MODEL_COUNTRY);
        CountryState countryState = (CountryState) resourceMap.get(OdooConstants.MODEL_COUNTRY_STATE);

        if (patient == null || country == null || countryState == null) {
            return null;
        }
        partner.setPartnerRef(patient.getIdPart());
        partner.setPartnerActive(patient.getActive());
        String patientName = getPatientName(patient).orElse("");
        String patientIdentifier = getPreferredPatientIdentifier(patient).orElse("");
        partner.setPartnerComment(patientIdentifier);
        partner.setPartnerExternalId(patientIdentifier);
        partner.setName(patientName);
        partner.setPartnerBirthDate(
                OdooUtils.convertEEEMMMddDateToOdooFormat(patient.getBirthDate().toString()));

        addAddress(patient, partner, country, countryState);
        return partner;
    }

    protected Optional<String> getPreferredPatientIdentifier(Patient patient) {
        return patient.getIdentifier().stream()
                .filter(identifier -> identifier.getUse() == Identifier.IdentifierUse.OFFICIAL)
                .findFirst()
                .map(Identifier::getValue);
    }

    protected Optional<String> getPatientName(Patient patient) {
        return patient.getName().stream()
                .findFirst()
                .map(name -> name.getGiven().get(0) + " " + name.getFamily());
    }

    protected void addAddress(Patient patient, Partner partner, Country country, CountryState countryState) {
        if (patient.hasAddress()) {
            patient.getAddress().forEach(fhirAddress -> {
                partner.setPartnerCity(fhirAddress.getCity());
                partner.setPartnerCountryId(country.getId());
                partner.setPartnerZip(fhirAddress.getPostalCode());
                partner.setPartnerStateId(countryState.getId());
                if (fhirAddress.getType() != null) {
                    partner.setPartnerType(fhirAddress.getType().getDisplay());
                }

                if (fhirAddress.hasExtension()) {
                    List<Extension> extensions = fhirAddress.getExtension();
                    List<Extension> addressExtensions = extensions.stream()
                            .filter(extension -> extension.getUrl().equals(OdooConstants.FHIR_OPENMRS_EXT_ADDRESS))
                            .findFirst()
                            .map(Element::getExtension)
                            .orElse(new ArrayList<>());

                    addressExtensions.stream()
                            .filter(extension -> extension.getUrl().equals(OdooConstants.FHIR_OPENMRS_EXT_ADDRESS1))
                            .findFirst()
                            .ifPresent(extension -> partner.setPartnerStreet(
                                    extension.getValue().toString()));

                    addressExtensions.stream()
                            .filter(extension -> extension.getUrl().equals(OdooConstants.FHIR_OPENMRS_EXT_ADDRESS2))
                            .findFirst()
                            .ifPresent(extension -> partner.setPartnerStreet2(
                                    extension.getValue().toString()));
                }
            });
        }
    }
}
