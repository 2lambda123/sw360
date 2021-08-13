/*
 * Copyright Siemens AG, 2013-2015, 2019. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.datahandler.couchdb;

import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;

import java.net.MalformedURLException;
import java.util.Optional;
import java.util.Properties;

/**
 * Properties class for database connection tests.
 *
 * @author cedric.bodet@tngtech.com
 */
public class DatabaseTestProperties {

    private static final String PROPERTIES_FILE_PATH = "/couchdb-test.properties";

    private static final String COUCH_DB_URL;
    public static final String COUCH_DB_DATABASE;

    private static final Optional<String> COUCH_DB_USERNAME;
    private static final Optional<String> COUCH_DB_PASSWORD;

    static {
        Properties props = CommonUtils.loadProperties(DatabaseTestProperties.class, PROPERTIES_FILE_PATH);

        COUCH_DB_URL = props.getProperty("couchdb.url", "http://localhost:5984");
        COUCH_DB_DATABASE = props.getProperty("couchdb.database", "sw360_test_db");
        COUCH_DB_USERNAME = Optional.ofNullable(props.getProperty("couchdb.user", null));
        COUCH_DB_PASSWORD = Optional.ofNullable(props.getProperty("couchdb.password", null));
    }

    public static HttpClient getConfiguredHttpClient() throws MalformedURLException {
        StdHttpClient.Builder httpClientBuilder = new StdHttpClient.Builder().url(COUCH_DB_URL);
        if(COUCH_DB_USERNAME.isPresent() && COUCH_DB_PASSWORD.isPresent()) {
            httpClientBuilder.username(COUCH_DB_USERNAME.get());
            httpClientBuilder.password(COUCH_DB_PASSWORD.get());
        }
        return httpClientBuilder.build();
    }

    public static String getCouchDbUrl() {
        return COUCH_DB_URL;
    }
}
