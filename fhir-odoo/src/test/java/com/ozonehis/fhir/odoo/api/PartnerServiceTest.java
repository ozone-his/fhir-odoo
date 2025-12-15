/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.odoojava.api.Row;
import com.ozonehis.fhir.odoo.model.Partner;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

class PartnerServiceTest {

    @InjectMocks
    private PartnerService partnerService;

    private static AutoCloseable mockCloser;

    @BeforeEach
    void setUp() {
        mockCloser = openMocks(this);
        ReflectionTestUtils.setField(partnerService, "odooPartnerDobField", "x_dob");
        ReflectionTestUtils.setField(partnerService, "odooPartnerIdField", "x_external_id");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        mockCloser.close();
    }

    @Test
    @DisplayName("should map row to Partner")
    void shouldMapRowToPartner() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(1);
        when(row.get("ref")).thenReturn("REF-001");
        when(row.get("type")).thenReturn("contact");
        when(row.get("street")).thenReturn("123 Main St");
        when(row.get("street2")).thenReturn("Apt 4B");
        when(row.get("city")).thenReturn("New York");
        when(row.get("zip")).thenReturn("10001");
        when(row.get("country_id")).thenReturn(1);
        when(row.get("state_id")).thenReturn(5);
        when(row.get("active")).thenReturn(true);
        when(row.get("comment")).thenReturn("100000Y");
        when(row.get("name")).thenReturn("John Doe");
        when(row.get("display_name")).thenReturn("John Doe");
        when(row.get("x_dob")).thenReturn("1990-01-01");
        when(row.get("x_external_id")).thenReturn("100000Y");
        when(row.get("create_date")).thenReturn(new Date());
        when(row.get("create_uid")).thenReturn(1);
        when(row.get("write_date")).thenReturn(new Date());
        when(row.get("write_uid")).thenReturn(2);

        Partner partner = partnerService.mapRowToResource(row);

        assertNotNull(partner);
        assertEquals(1, partner.getId());
        assertEquals("REF-001", partner.getPartnerRef());
        assertEquals("contact", partner.getPartnerType());
        assertEquals("123 Main St", partner.getPartnerStreet());
        assertEquals("Apt 4B", partner.getPartnerStreet2());
        assertEquals("New York", partner.getPartnerCity());
        assertEquals("10001", partner.getPartnerZip());
        assertEquals(1, partner.getPartnerCountryId());
        assertEquals(5, partner.getPartnerStateId());
        assertTrue(partner.getPartnerActive());
        assertEquals("100000Y", partner.getPartnerComment());
        assertEquals("John Doe", partner.getName());
        assertEquals("John Doe", partner.getDisplayName());
        assertEquals("1990-01-01", partner.getPartnerBirthDate());
        assertEquals("100000Y", partner.getPartnerExternalId());
        assertNotNull(partner.getCreatedOn());
        assertEquals(1, partner.getCreatedBy());
        assertNotNull(partner.getLastUpdatedOn());
        assertEquals(2, partner.getLastUpdatedBy());
    }

    @Test
    @DisplayName("should handle null values")
    void shouldHandleNullValues() {
        Row row = mock(Row.class);
        when(row.get("id")).thenReturn(null);
        when(row.get("ref")).thenReturn(null);
        when(row.get("type")).thenReturn(null);
        when(row.get("street")).thenReturn(null);
        when(row.get("street2")).thenReturn(null);
        when(row.get("city")).thenReturn(null);
        when(row.get("zip")).thenReturn(null);
        when(row.get("country_id")).thenReturn(null);
        when(row.get("state_id")).thenReturn(null);
        when(row.get("active")).thenReturn(null);
        when(row.get("comment")).thenReturn(null);
        when(row.get("name")).thenReturn(null);
        when(row.get("display_name")).thenReturn(null);
        when(row.get("x_dob")).thenReturn(null);
        when(row.get("x_external_id")).thenReturn(null);
        when(row.get("create_date")).thenReturn(null);
        when(row.get("create_uid")).thenReturn(null);
        when(row.get("write_date")).thenReturn(null);
        when(row.get("write_uid")).thenReturn(null);

        Partner partner = partnerService.mapRowToResource(row);

        assertNotNull(partner);
        assertEquals(0, partner.getId());
        assertNull(partner.getPartnerRef());
        assertNull(partner.getPartnerType());
        assertNull(partner.getPartnerStreet());
        assertNull(partner.getPartnerStreet2());
        assertNull(partner.getPartnerCity());
        assertNull(partner.getPartnerZip());
        assertNull(partner.getPartnerCountryId());
        assertNull(partner.getPartnerStateId());
        assertNull(partner.getPartnerActive());
        assertNull(partner.getPartnerComment());
        assertNull(partner.getName());
        assertNull(partner.getDisplayName());
        assertNull(partner.getPartnerBirthDate());
        assertNull(partner.getPartnerExternalId());
        assertNull(partner.getCreatedOn());
        assertEquals(0, partner.getCreatedBy());
        assertNull(partner.getLastUpdatedOn());
        assertEquals(0, partner.getLastUpdatedBy());
    }

    @Test
    @DisplayName("should convert partner to map correctly")
    void shouldConvertPartnerToMapCorrectly() {
        Partner partner = new Partner();
        partner.setId(1);
        partner.setPartnerRef("REF-001");
        partner.setPartnerType("contact");
        partner.setPartnerStreet("123 Main St");
        partner.setPartnerStreet2("Apt 4B");
        partner.setPartnerCity("New York");
        partner.setPartnerZip("10001");
        partner.setPartnerCountryId(1);
        partner.setPartnerStateId(5);
        partner.setPartnerActive(true);
        partner.setPartnerComment("100000Y");
        partner.setName("John Doe");
        partner.setDisplayName("John Doe");
        partner.setPartnerBirthDate("1990-01-01");
        partner.setPartnerExternalId("100000Y");
        Date createdOn = new Date();
        partner.setCreatedOn(createdOn);
        partner.setCreatedBy(1);
        Date lastUpdatedOn = new Date();
        partner.setLastUpdatedOn(lastUpdatedOn);
        partner.setLastUpdatedBy(2);

        Map<String, Object> map = partnerService.convertPartnerToMap(partner);

        assertNotNull(map);
        assertEquals(1, map.get("id"));
        assertEquals("REF-001", map.get("ref"));
        assertEquals("contact", map.get("type"));
        assertEquals("123 Main St", map.get("street"));
        assertEquals("Apt 4B", map.get("street2"));
        assertEquals("New York", map.get("city"));
        assertEquals("10001", map.get("zip"));
        assertEquals(1, map.get("country_id"));
        assertEquals(5, map.get("state_id"));
        assertTrue((Boolean) map.get("active"));
        assertEquals("100000Y", map.get("comment"));
        assertEquals("John Doe", map.get("name"));
        assertEquals("John Doe", map.get("display_name"));
        assertEquals("1990-01-01", map.get("x_dob"));
        assertEquals("100000Y", map.get("x_external_id"));
        assertEquals(createdOn, map.get("create_date"));
        assertEquals(1, map.get("create_uid"));
        assertEquals(lastUpdatedOn, map.get("write_date"));
        assertEquals(2, map.get("write_uid"));
    }

    @Test
    @DisplayName("should convert partner to map")
    void shouldConvertPartnerToMapWithoutIdWhenIdIsZero() {
        Partner partner = new Partner();
        partner.setId(1);
        partner.setName("Test Partner");

        Map<String, Object> map = partnerService.convertPartnerToMap(partner);

        assertNotNull(map);
        assertTrue(map.containsKey("id"));
        assertEquals("Test Partner", map.get("name"));
    }
}
