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

import org.eclipse.paho.android.service.MqttAndroidClient;

/**
 * Client abstraction for connecting to MQTT server.
 * Completely unnecessary? Probably.
 * TODO: Get rid of this and just use MqttAndroidClient natively in MainActivity? There doesn't seem to be a whole lot of point in this class now.
 */
public class ApplicationClient extends MqttAndroidClient {

    public ApplicationClient(Context context, String serverURI, String clientId) {
        super(context, serverURI, clientId);
    }
}
