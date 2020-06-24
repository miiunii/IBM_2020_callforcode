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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class NotificationContent {

    /**
     * An array of sample (dummy) items.
     */
    private static final List<NotificationItem> ITEMS = new ArrayList<NotificationItem>();

    /**
     * I like this better.
     *
     * @return List<NotificationItem>
     */
    public static List<NotificationItem> getItems() {
        return ITEMS;
    }

    public static void addAll(ArrayDeque<NotificationItem> notificationItems) {
        // First, clear out what is currently there
        clearAll();
        // Now add all of the NotificationItems
        Iterator<NotificationItem> itemIterator = notificationItems.descendingIterator();
        while (itemIterator.hasNext()) {
            addItem(itemIterator.next());
        }
    }

    /**
     * Adds the specified NotificationItem
     *
     * @param notificationItem Duh
     */
    private static void addItem(NotificationItem notificationItem) {
        ITEMS.add(notificationItem);
    }

    /**
     * Clears all items, as the underlying store is reused between
     * invocations.
     */
    private static void clearAll() {
        ITEMS.clear();
    }

    /**
     * A NotificationItem. So it's not just a clever name.
     */
    public static class NotificationItem {
        private final String mNotificationTimestamp;
        private final String mNotificationMessage;

        public NotificationItem(String notificationTimestamp, String notificationMessage) {
            this.mNotificationTimestamp = notificationTimestamp;
            this.mNotificationMessage = notificationMessage;
        }

        public String getNotificationTimestamp() {
            return mNotificationTimestamp;
        }

        public String getNotificationMessage() {
            return mNotificationMessage;
        }

        public String getNotificationMessageWithTimestamp() {
            return getNotificationTimestamp() + ":" + getNotificationMessage();
        }

        @Override
        public String toString() {
            return mNotificationMessage;
        }
    }
}
