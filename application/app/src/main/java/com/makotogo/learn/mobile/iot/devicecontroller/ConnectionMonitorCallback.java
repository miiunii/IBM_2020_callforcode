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

import org.eclipse.paho.client.mqttv3.IMqttToken;

/**
 * Callback that allows an isolated block of code (like a Fragment)
 * to be notified when a connection is successful or failed.
 */
public interface ConnectionMonitorCallback {
    /**
     * Invoked when the connection succeeds. The <code>asyncActionToken</code>
     * that was returned from MQTT middleware is passed along for context.
     *
     * @param asyncActionToken The MQTT Token returned by the middleware
     *                         upon successful connection.
     */
    void onConnectSuccess(IMqttToken asyncActionToken);

    /**
     * Invoked when the connection attempt fails. The <code>asyncActionToken</code>
     * and the <code>exception</code> are passed along.
     *
     * @param asyncActionToken The MQTT Token returned by the middleware
     *                         upon successful connection.
     *
     * @param exception The underlying cause of the connection failure.
     */
    void onConnectFailure(IMqttToken asyncActionToken, Throwable exception);


}
