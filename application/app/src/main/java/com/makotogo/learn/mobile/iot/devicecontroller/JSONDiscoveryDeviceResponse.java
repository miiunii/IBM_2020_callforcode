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

/**
 * Created by sperry on 3/3/18.
 */

/**
 * Enum for dealing with the Device Response from the MQTT server.
 * It's handy to constrain the values that are allowed, and also
 * there is just one place to go if the names change.
 *
 * There's just something about hardcoded literal strings that
 * goes with JSON processing that I don't like. This is my way of
 * dealing with it. There are probably better ways. Open an issue.
 * Let's talk about it.
 */
public enum JSONDiscoveryDeviceResponse {

    /**
     * The "root" of all evil :)
     */
    D("d"),
    /**
     * The device response property
     */
    DEVICE_RESPONSE("deviceResponse"),
    /**
     * The "discovery" value of deviceResponse
     */
    DISCOVERY("discovery"),
    /**
     *
     */
    DEVICES("devices"),
    /**
     *
     */
    DEVICE_ID("deviceId"),
    /**
     *
     */
    ACTIONS("actions"),
    /**
     *
     */
    ACTION("action"),
    /**
     *
     */
    DESCRIPTION("description"),
    ;

    private String mStringRepresentation;

    JSONDiscoveryDeviceResponse(String stringRepresentation) {
        mStringRepresentation = stringRepresentation;
    }

    public String toString() {
        return mStringRepresentation;
    }
}
