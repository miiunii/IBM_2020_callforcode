/*
 *    Copyright 2018 Makoto Consulting Group, Inc
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.makotogo.learn.mobile.iot.devicecontroller;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * A twist on the familiar Properties object.
 * Wraps an abstraction of the SharedPreferences API.
 * Provides named methods for accessing properties.
 * Does not allow direct access to setProperty() method.
 *
 // TODO: Support other property types than String?
 *
 */
public class ApplicationProperties extends Properties {
    /**
     * The SharedPreferences file name. Fully-qualified name of this class,
     * so it should be unique to this application.
     */
    private static final String SHARED_PREFERENCES_FILE_NAME = ApplicationProperties.class.getName();

    // The first-class properties supported by this class
    // Each has a getter and setter.
    private static final String MQTT_SERVER_HOST_NAME = "mqtt.server.host.name";
    private static final String MQTT_SERVER_PROTOCOL = "mqtt.server.protocol";
    private static final String MQTT_SERVER_PORT = "mqtt.server.port";
    private static final String ORG_ID = "org.id";
    private static final String API_KEY = "api.key";
    private static final String AUTH_TOKEN = "auth.token";
    private static final String CONTROLLER_DEVICE_TYPE = "controller.device.type";
    private static final String CONTROLLER_DEVICE_ID = "controller.device.id";
    private static final String MAX_NOTIFICATION_COUNT = "max.notification.count";

    /**
     * The application context. Needed by SharedPreferences.
     */
    private Context mContext;

    /**
     * The one and only way to create an instance of this class.
     *
     * @param context The application context. Needed by SharedPreferences.
     */
    public ApplicationProperties(Context context) {
        super();
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null!");
        }
        mContext = context;
    }

    //********************************************
    // Getters and Setters for first-class properties:
    // MqttServerHostName
    // MqttServerProtocol
    // MqttServerPort
    // OrgId
    // ApiKey
    // AuthToken
    // ControllerDeviceType
    // ControllerDeviceId

    public String getMqttServerHostName() {
        return fetchStringProperty(MQTT_SERVER_HOST_NAME);
    }

    public void setMqttServerHostName(String mqttServerHostName) {
        storeProperty(MQTT_SERVER_HOST_NAME, mqttServerHostName);
    }

    public String getMqttServerProtocol() {
        return fetchStringProperty(MQTT_SERVER_PROTOCOL);
    }

    public void setMqttServerProtocol(String mqttServerProtocol) {
        storeProperty(MQTT_SERVER_PROTOCOL, mqttServerProtocol);
    }

    public String getMqttServerPort() {
        return fetchStringProperty(MQTT_SERVER_PORT);
    }

    public void setMqttServerPort(String mqttServerPort) {
        storeProperty(MQTT_SERVER_PORT, mqttServerPort);
    }

    public String getOrgId() {
        return fetchStringProperty(ORG_ID);
    }

    public void setOrgId(String orgId) {
        storeProperty(ORG_ID, orgId);
    }

    public String getApiKey() {
        return fetchStringProperty(API_KEY);
    }

    public void setApiKey(String apiKey) {
        storeProperty(API_KEY, apiKey);
    }

    public String getAuthToken() {
        return fetchStringProperty(AUTH_TOKEN);
    }

    public void setAuthToken(String authToken) {
        storeProperty(AUTH_TOKEN, authToken);
    }

    public String getControllerDeviceType() {
        return fetchStringProperty(CONTROLLER_DEVICE_TYPE);
    }

    public void setControllerDeviceType(String controllerDeviceType) {
        storeProperty(CONTROLLER_DEVICE_TYPE, controllerDeviceType);
    }

    public String getControllerDeviceId() {
        return fetchStringProperty(CONTROLLER_DEVICE_ID);
    }

    public void setControllerDeviceId(String controllerDeviceId) {
        storeProperty(CONTROLLER_DEVICE_ID, controllerDeviceId);
    }

    public Integer getMaxNotificationCount() {
        return fetchIntegerProperty(MAX_NOTIFICATION_COUNT);
    }

    public void setMaxNotificationCount(Integer maxNotificationCount) {
        storeProperty(MAX_NOTIFICATION_COUNT, maxNotificationCount);
    }

    /**
     * The setProperty method from Properties.
     * Do not allow the application code to call this.
     * It circumvents the approach to store everything in SharedPreferences
     */
    @Override
    public synchronized Object setProperty(String key, String value) {
        throw new RuntimeException("DO NOT INVOKE THIS METHOD. CALL THE UNDERLYING setMyProperty() METHOD INSTEAD (OR CREATE ONE IF IT DOES NOT EXIST)!!!!");
    }

    /**
     * Helper method. Attempts to locate the specified property
     * in the "cache" (the super class' Properties internal store).
     * If that fails, the property will be loaded from SharedPreferences.
     *
     * @param propertyName The property name to fetch.
     *
     * @return String - the property value
     */
    private String fetchStringProperty(String propertyName) {
        String ret;
        //
        // First see if the property is in the "cache"
        ret = getProperty(propertyName);
        if (ret == null) {
            // Not there. Try SharedPreferences.
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
            ret = sharedPreferences.getString(propertyName, null);
            //
            // Property hit in SharedPreferences, store it in the "cache"
            if (ret != null) {
                super.setProperty(propertyName, ret);
            }
        }
        // Set the return value to Empty string (instead of null)
        if (ret == null) {
            ret = StringUtils.EMPTY;
        }
        return ret;
    }

    /**
     * Helper method. Attempts to locate the specified property
     * in the "cache" (the super class' Properties internal store).
     * If that fails, the property will be loaded from SharedPreferences.
     *
     * @param propertyName The property name to fetch.
     * @return Integer - the property value
     */
    private Integer fetchIntegerProperty(String propertyName) {
        Integer ret = null;
        //
        // First see if the property is in the "cache"
        String propertyValueAsString = getProperty(propertyName);
        if (propertyValueAsString == null) {
            // Not there. Try SharedPreferences.
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
            // Use String values with SharedPreferences to be consistent with java.util.Properties
            propertyValueAsString = sharedPreferences.getString(propertyName, null);
            //
            // Property hit in SharedPreferences, store it in the "cache"
            if (propertyValueAsString != null) {
                super.setProperty(propertyName, propertyValueAsString);
            }
        }
        // Set the return value to Empty string (instead of null)
        if (propertyValueAsString != null) {
            ret = Integer.valueOf(propertyValueAsString);
        }
        return ret;
    }

    /**
     * Helper method. Stores the property value in SharedPreferences, as
     * well as the "cache" (the internals of the parent Properties object).
     *
     * @param propertyName The property name
     * @param propertyValue The property value
     */
    private void storeProperty(String propertyName, Object propertyValue) {
        // Store the property in the cache
        super.setProperty(propertyName, propertyValue.toString());
        // Now store it in SharedPreferences
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(propertyName, propertyValue.toString());
        editor.commit();
    }

}
