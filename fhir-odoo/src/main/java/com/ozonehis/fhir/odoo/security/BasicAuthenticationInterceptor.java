/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.security;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import com.odoojava.api.Session;
import com.ozonehis.fhir.FhirOdooConfig;
import com.ozonehis.fhir.odoo.SessionHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
@Interceptor
public class BasicAuthenticationInterceptor {

    @Autowired
    private FhirOdooConfig fhirOdooConfig;

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if ((request.getRequestURI().contains("/.well-known")
                        || request.getRequestURI().endsWith("/metadata"))
                || SessionHolder.isSessionActive()) {
            return true;
        } else {
            // Extract the Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header missing or invalid");
                return false;
            }

            // Extract the credentials
            String[] credentials = new String(Base64.getDecoder().decode(authHeader.substring(6))).split(":");
            if (credentials.length < 2) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials format");
                return false;
            }
            String username = credentials[0];
            String password = credentials[1];

            // Authenticate the user
            if (authenticateWithUsernameAndPassword(username, password)) {
                return true;
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
                return false;
            }
        }
    }

    private boolean authenticateWithUsernameAndPassword(String username, String password) {
        Session odooSession = new Session(
                fhirOdooConfig.getRPCProtocol(),
                fhirOdooConfig.getOdooHost(),
                Integer.parseInt(fhirOdooConfig.getOdooPort()),
                fhirOdooConfig.getOdooDatabase(),
                username,
                password);

        try {
            odooSession.startSession();
            if (odooSession.getUserID() != 0) {
                SessionHolder.setOdooSession(odooSession);
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
