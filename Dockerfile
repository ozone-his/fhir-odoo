#
# Copyright © 2024, Ozone HIS <info@ozone-his.com>
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

# Use the Maven image with OpenJDK 17 as the base image
FROM maven:3.9-eclipse-temurin-17

# Set the working directory in the container
WORKDIR /app
# Copy the Maven build files to the container
COPY pom.xml ./
COPY fhir-odoo-app/ fhir-odoo-app/
COPY fhir-odoo/ fhir-odoo/
COPY fhir-odoo-mapper/ fhir-odoo-mapper/

# Build the application
RUN mvn clean package -DskipTests --no-transfer-progress --batch-mode

# Copy the built JAR file to the container
COPY fhir-odoo-app/target/fhir-odoo-app-*.jar fhir-odoo.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "fhir-odoo.jar"]
