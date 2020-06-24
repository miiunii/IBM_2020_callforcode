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
 * Constrain the possible Events for which subscribers can be registered
 * for the system
 */
enum ControllerEvent {
    // The possible values:
    REQUEST ("request"),
    RESPONSE ("response")
    ;

    /**
     * The String representation of this enum
     */
    private String mStringRepresentation;

    /**
     * Constructor - used to specify the String representation of this
     * enum member.
     *
     * @param stringRepresentation The String representation
     */
    ControllerEvent(String stringRepresentation) {
        mStringRepresentation = stringRepresentation;
    }

    /**
     * Convenience method. Used to return a friendly value for this enum
     * member.
     *
     * @return String - a human-friendly representation of this enum member
     */
    public String toString() {
        return mStringRepresentation;
    }
}
