/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.odoojava.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionHolderTest {

    private Session mockSession;

    @BeforeEach
    void setUp() {
        mockSession = mock(Session.class);
    }

    @Test
    void shouldReturnFalseWhenSessionIsNull() {
        SessionHolder.setOdooSession(null);

        assertFalse(SessionHolder.isSessionActive());
    }

    @Test
    void shouldReturnFalseWhenUserIDIsZero() {
        when(mockSession.getUserID()).thenReturn(0);

        SessionHolder.setOdooSession(mockSession);

        assertFalse(SessionHolder.isSessionActive());
    }

    @Test
    void shouldReturnTrueWhenSessionIsActive() {
        when(mockSession.getUserID()).thenReturn(1);

        SessionHolder.setOdooSession(mockSession);

        assertTrue(SessionHolder.isSessionActive());
    }

    @Test
    void shouldSetTheSessionCorrectly() {
        SessionHolder.setOdooSession(mockSession);

        assertEquals(mockSession, SessionHolder.getOdooSession());
    }

    @Test
    void shouldReturnNullWhenNoSessionIsSet() {
        SessionHolder.setOdooSession(null);

        assertNull(SessionHolder.getOdooSession());
    }
}
