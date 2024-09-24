/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo;

import com.odoojava.api.Session;

public class SessionHolder {

    public Session odooSession;

    private static final SessionHolder INSTANCE = new SessionHolder();

    public static boolean isSessionActive() {
        return getOdooSession() != null && getOdooSession().getUserID() != 0;
    }

    public static void setOdooSession(Session session) {
        INSTANCE.odooSession = session;
    }

    public static Session getOdooSession() {
        return INSTANCE.odooSession;
    }
}
