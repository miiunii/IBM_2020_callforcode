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

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Callback interface used when a response event from the controller
 * device is received. Allows the main Context (most likely the owning
 * Activity but not necessarily) to communicate with the fragment in a
 * way that doesn't block the UI thread.
 */
public interface DeviceResponseCallback {

    /**
     * Called by the MQTT Listener thread when a device response message
     * comes in and needs to be processed.
     *
     * @param deviceResponseMessage
     */
    void onDeviceResponse(MqttMessage deviceResponseMessage);
}
