/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseOdooModel implements OdooResource {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("create_date")
    private Date createdOn;

    @JsonProperty("create_uid")
    private int createdBy;

    @JsonProperty("write_date")
    private Date lastUpdatedOn;

    @JsonProperty("write_uid")
    private int lastUpdatedBy;

    /**
     * Retrieves the fields of the current class and its superclasses that are annotated with {@link JsonProperty}.
     * This method uses reflection to gather all fields annotated with {@link JsonProperty} from the current class
     * and its superclasses, and returns them as an array of field names.
     *
     * @return an array of field names annotated with {@link JsonProperty}
     */
    public String[] fields() {
        List<String> fields = new ArrayList<>();
        Class<?> currentClass = getClass();
        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(JsonProperty.class)) {
                    JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                    fields.add(jsonProperty.value());
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return fields.toArray(new String[0]);
    }
}
