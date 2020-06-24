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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for handling IoT Device content.
 */
public class IotDeviceContent {

    /**
     * An array of IoT Device items.
     */
    public static final List<IotDeviceItem> ITEMS = new ArrayList<IotDeviceItem>();

    /**
     * A map of IoT Device items, by ID.
     */
    public static final Map<String, IotDeviceItem> ITEM_MAP = new HashMap<String, IotDeviceItem>();

    /**
     * Add an IoT Device item to the internal collections.
     *
     * @param item The {@link IotDeviceItem} to add
     */
    public static void addItem(IotDeviceItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * Clears all items, as the underlying store is reused between
     * invocations.
     */
    public static void clearAll() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    /**
     * An IoT Device. Nothing fancy.
     * For the record, I do not like this public attribute thing Android is so fond of.
     * But this is generated code and it works, so I'm sticking with it. Under protest.
     * TODO: Fix this. Encapsulate these properties. This is just embarrassing.
     */
    public static class IotDeviceItem {
        public final String id;
        public final String deviceId;
        public final String deviceDescription;
        public final List<Pair<String, String>> actions = new ArrayList<>();

        public IotDeviceItem(String id, String deviceId, String deviceDescription, List<Pair<String, String>> actions) {
            this.id = id;
            this.deviceId = deviceId;
            this.deviceDescription = deviceDescription;
            this.actions.addAll(actions);
        }

        @Override
        public String toString() {
            // TODO: Make this better. Return JSON string?
            return ReflectionToStringBuilder.toString(this);
        }
    }
}
