package com.ozonehis.fhir.odoo;

import com.odoojava.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
