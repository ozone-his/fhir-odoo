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
