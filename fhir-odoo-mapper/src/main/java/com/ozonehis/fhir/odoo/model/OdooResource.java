/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.model;

import java.io.Serializable;

/**
 * Interface to mark all Odoo resources.
 */
public interface OdooResource extends Serializable {

    default String getOdooModelName() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
