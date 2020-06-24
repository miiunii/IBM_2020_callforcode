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

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.security.KeyStore;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class MedeinaMainActivity extends Activity
        implements HomeFragment.OnHomeFragmentInteractionListener,
        DashboardFragment.OnDashboardFragmentInteractionListener,
        ActionDialogFragment.OnActionDialogFragmentInteractionListener,
        NotificationFragment.OnNotificationFragmentInteractionListener
{

        private static final String TAG = MainActivity.class.getSimpleName();

        // Fragment arguments
        private static final int FRAG_ARG_NUMBER_OF_DASHBOARD_COLUMNS_TO_DISPLAY = 1;
        private static final int FRAG_ARG_NUMBER_OF_NOTIFICATION_COLUMNS_TO_DISPLAY = 1;

        // Instance state tags
        private static final String INSTANCE_STATE_NOTIFICATIONS = TAG + ".instance.state.notifications";

        /**
         * Keep this little bread crumb around to drive what is displayed
         * in the FloatingActionButton
         */
        private TextView mFragmentTitle;

        /**
         * Application Properties
         */
        private ApplicationProperties mApplicationProperties;

        /**
         * The ApplicationClient, used to communicate with the MQTT server
         */
        private ApplicationClient mApplicationClient;

        /**
         * The resource ID of the active fragment's menu item. Establishes context.
         */
        private int mActiveFragmentId;

        /**
         * The notifications that have been generated for the current session
         */
        private ArrayDeque<NotificationContent.NotificationItem> notifications;

        /**
         * Private getter/NPE preventer
         */
        private ArrayDeque<NotificationContent.NotificationItem> getNotifications() {
            // No NPEs
            if (notifications == null) {
                notifications = new ArrayDeque<>();
            }
            return notifications;
        }

        /**
         * Create a message destined for the message store.
         *
         * @param topicString The message's topic String
         * @param messagePayload The message's payload (should always be a JSON object)
         *
         * @return String - the message to be stored.
         */
        private String createStoreMessage(String topicString, byte[] messagePayload) {
            return topicString + ":" + new String(messagePayload);
        }

        /**
         * The ubiquitous onCreate() method, where much of the magic happens.
         *
         * @param savedInstanceState The saved bundle from a previous incarnation. We don't really
         *                           care too much about it, just based on the nature of this
         *                           particular application.
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.medeina_activity_main);

            // Create the Deque that holds notifications
            // Try to retrieve it from saved instance state first
            if (savedInstanceState != null) {
                Serializable savedInstanceStateSerializable = savedInstanceState.getSerializable(INSTANCE_STATE_NOTIFICATIONS);
                if (savedInstanceStateSerializable != null) {
                    // This unchecked cast is okay
                    //noinspection unchecked
                    notifications = (ArrayDeque<NotificationContent.NotificationItem>) savedInstanceStateSerializable;
                } else {
                    notifications = null; // force recreate of the Deque
                }
            }

            // Create the FloatingActionBar, this provides information about each fragment
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Figure out which Fragment we're looking at, and display info
                    /// specific to that Fragment. Use the menu ID as the cracker.
                    Toast toast;
                    int activeFragmentId = getActiveFragmentId();
                    switch (getActiveFragmentId()) {
                        // Home Fragment
                        case R.id.navigation_home:
                            // TODO: Toast is temporary, replace with Dialog
                            toast = Toast.makeText(getApplicationContext(), "This (" + activeFragmentId + ") is the Home fragment", Toast.LENGTH_LONG);
                            break;
                        // Navigation Fragment
                        case R.id.navigation_dashboard:
                            // TODO: Toast is temporary, replace with Dialog
                            toast = Toast.makeText(getApplicationContext(), "This (" + activeFragmentId + ") is the Dashboard fragment", Toast.LENGTH_LONG);
                            break;
                        // Notifications Fragment
                        case R.id.navigation_notifications:
                            // TODO: Toast is temporary, replace with Dialog
                            toast = Toast.makeText(getApplicationContext(), "This (" + activeFragmentId + ") is the Notifications fragment", Toast.LENGTH_LONG);
                            break;
                        // Settings Fragment
                        case R.id.navigation_settings:
                            // TODO: Toast is temporary, replace with Dialog
                            toast = Toast.makeText(getApplicationContext(), "This (" + activeFragmentId + ") is the Settings fragment", Toast.LENGTH_LONG);
                            break;
                        default:
                            // TODO: Toast is temporary, replace with Dialog
                            toast = Toast.makeText(getApplicationContext(), "Unknown Fragment ID: " + activeFragmentId + ", no info", Toast.LENGTH_LONG);
                    }
                    if (toast != null) {
                        toast.show();
                    }
                }
            });

            // Load application properties from SharedPreferences
            setApplicationProperties(new ApplicationProperties(getApplicationContext()));

            // Create the BottomNavigationView
            mFragmentTitle = findViewById(R.id.message);
            BottomNavigationView navigation = findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            // Start at home (why not?)
            loadHomeFragment();

            // Disable the Dashboard until connected
            disableBottomNavigationMenuItem(R.id.navigation_dashboard);
        }

        /**
         * Store instance state to survive things like device config changes.
         *
         * @param outState           The Bundle in which to store instance state
         * @param outPersistentState Not really sure wtf this is for
         */
        @Override
        public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
            super.onSaveInstanceState(outState, outPersistentState);
            // Store the notifications deque
            outState.putSerializable(INSTANCE_STATE_NOTIFICATIONS, getNotifications());
        }

        /**
         * Handles the BottomNavigationView user interaction
         */
        private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean ret = false;
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        mFragmentTitle.setText(R.string.title_home);
                        loadHomeFragment();
                        ret = true;
                        break;
                    case R.id.navigation_dashboard:
                        mFragmentTitle.setText(R.string.title_dashboard);
                        loadDashboardFragment();
                        ret = true;
                        break;
                    case R.id.navigation_notifications:
                        mFragmentTitle.setText(R.string.title_notifications_long);
                        loadNotificationsFragment();
                        ret = true;
                        break;
                    case R.id.navigation_settings:
                        mFragmentTitle.setText(R.string.title_settings);
                        loadSettingsFragment();
                        ret = true;
                        break;
                }
                return ret;
            }
        };

        /**
         * Load the HomeFragment. Replaces whatever Fragment was there.
         */
        private void loadHomeFragment() {
            setActiveFragmentId(R.id.navigation_home);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, HomeFragment.newInstance(getApplicationProperties()), HomeFragment.FRAGMENT_TAG);
            fragmentTransaction.commit();

            // Update the connection status
            updateConnectionStatusTextView();
        }

        /**
         * Load the Dashboard fragment. Replaces whatever Fragment was there.
         */
        private void loadDashboardFragment() {
            setActiveFragmentId(R.id.navigation_dashboard);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, DashboardFragment.newInstance(FRAG_ARG_NUMBER_OF_DASHBOARD_COLUMNS_TO_DISPLAY), DashboardFragment.FRAGMENT_TAG);
            fragmentTransaction.commit();
        }

        /**
         * Load the Notifications (notifications) fragment. Replaces whatever Fragment was there.
         */
        private void loadNotificationsFragment() {
            setActiveFragmentId(R.id.navigation_notifications);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, NotificationFragment.newInstance(FRAG_ARG_NUMBER_OF_NOTIFICATION_COLUMNS_TO_DISPLAY, getNotifications()), NotificationFragment.FRAGMENT_TAG);
            fragmentTransaction.commit();
        }

        /**
         * Load the Settings fragment. Replaces whatever Fragment was there.
         */
        private void loadSettingsFragment() {
            setActiveFragmentId(R.id.navigation_settings);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, SettingsFragment.newInstance(), SettingsFragment.FRAGMENT_TAG);
            fragmentTransaction.commit();
        }

        /**
         * Disable the specified BottomNavigationView menu item
         *
         * @param menuResourceIdToDisable Duh
         */
        private void disableBottomNavigationMenuItem(int menuResourceIdToDisable) {
            BottomNavigationView navigationView = findViewById(R.id.navigation);
            navigationView.getMenu().findItem(menuResourceIdToDisable).setEnabled(false);
        }

        /**
         * Enable the specified BottomNavigationView menu item
         *
         * @param menuResourceIdToDisable Duh
         */
        private void enableBottomNavigationMenuItem(int menuResourceIdToDisable) {
            BottomNavigationView navigationView = findViewById(R.id.navigation);
            navigationView.getMenu().findItem(menuResourceIdToDisable).setEnabled(true);
        }

        /**
         * Updates the Status TextView based on the current connection state
         */
        public void updateConnectionStatusTextView() {
            TextView statusTextView = findViewById(R.id.connectionStatusTextView);
            int resourceId = R.string.connection_status_not_connected;// default
            if (getApplicationClient() != null && getApplicationClient().isConnected()) {
                resourceId = R.string.connection_status_connected;
            }
            statusTextView.setText(resourceId);
        }

        /**
         * Updates the state of the application view when the app is connected
         * to the MQTT server.
         */
        private void updateViewStateForConnected() {
            // Update the ConnectionStatus view
            updateConnectionStatusTextView();
            // Enable the Dashboard Fragment
            enableBottomNavigationMenuItem(R.id.navigation_dashboard);
        }

        /**
         * Updates the state of the application view when the app is disconnected
         * to the MQTT server.
         */
        private void updateViewstateForDisconnected() {
            // Update the ConnectionStatus view
            updateConnectionStatusTextView();
            // Disable the Dashboard Fragment
            disableBottomNavigationMenuItem(R.id.navigation_dashboard);
        }

        //***************************************************************************************
        //* Below this line is logic specific to the application, not Android-y "stuff"
        //***************************************************************************************


        /**
         * Normal Getter method. Just a personal preference. I don't like touching
         * member variables directly. Makes the code much harder to refactor later.
         *
         * @return ApplicationClient - the ApplicationClient reference. As-is.
         */
        private ApplicationClient getApplicationClient() {
            return mApplicationClient;
        }

        /**
         * Setter for the ApplicationClient property.
         *
         * @param applicationClient The ApplicationClient to set. May not be null.
         */
        private void setApplicationClient(ApplicationClient applicationClient) {
            if (applicationClient != null) {
                mApplicationClient = applicationClient;
            } else {
                throw new IllegalArgumentException("ApplicationClient cannot be null!");
            }
        }

        /**
         * Setter for the ApplicationProperties property.
         *
         * @param applicationProperties The ApplicationProperties to set. May not be null.
         */
        public void setApplicationProperties(ApplicationProperties applicationProperties) {
            if (applicationProperties != null) {
                mApplicationProperties = applicationProperties;
            } else {
                throw new IllegalArgumentException("ApplicationProperties cannot be null!");
            }
        }

        /**
         * Getter for the ApplicationProperties property. The ApplicationProperties object
         * will be created if it does not exist.
         *
         * @return ApplicationProperties
         */
        public ApplicationProperties getApplicationProperties() {
            if (mApplicationProperties == null) {
                mApplicationProperties = new ApplicationProperties(getApplicationContext());
            }
            return mApplicationProperties;
        }

        /**
         * Sets the active fragment. Since we're using a BottomNavigationView control, and a
         * single FloatingActionButton (to provide information about the "current" fragment
         * being displayed), we kinda need to know which fragment that is.
         *
         * @param activeFragment Returns the menu ID of the active (the one being displayed)
         *                       fragment.
         */
        public void setActiveFragmentId(int activeFragment) {
            mActiveFragmentId = activeFragment;
        }

        public int getActiveFragmentId() {
            return mActiveFragmentId;
        }

        /**
         * Create the application client
         *
         * @return ApplicationClient - the ApplicationClient
         */
        private ApplicationClient createApplicationClient() {
            String mqttServerProtocol =  getApplicationProperties().getMqttServerProtocol();
            String orgId = getApplicationProperties().getOrgId();
            String mqttServerHostName = getApplicationProperties().getMqttServerHostName();
            String mqttServerPort = getApplicationProperties().getMqttServerPort();

            String serverUri = mqttServerProtocol + "://" + orgId + "." + mqttServerHostName + ":" + mqttServerPort;

            return new ApplicationClient(getApplicationContext(), serverUri, generateApplicationClientId(orgId));
        }

        /**
         * Connect the ApplicationClient to MQTT Server
         */
        public void connectApplicationClient(final ConnectionMonitorCallback connectionMonitorCallback) {
            final String METHOD = "connectApplicationClient(): ";
            try {
                Context context = getApplicationContext();
                SSLSocketFactory factory = getSSLSocketFactory(context);
                getApplicationClient().setCallback(getMonitorCallback());

                String username = getApplicationProperties().getApiKey(); // Username is the API Key
                char[] password = getApplicationProperties().getAuthToken().toCharArray();

                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setUserName(username);
                options.setPassword(password);
                if (factory != null) {
                    options.setSocketFactory(factory);
                }
                IMqttToken token = getApplicationClient().connect(options, context, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        String notificationMessage = "Connect SUCCESS!";
                        Log.d(TAG, METHOD + notificationMessage);
                        // Store this in the Notifications deque
                        pushNotification(notificationMessage);
                        // TODO: Check application properties to see if we display this Toast or not
                        Toast.makeText(getApplication(), notificationMessage, Toast.LENGTH_LONG).show();
                        subscribeToDeviceEventsForController(ControllerEvent.REQUEST, ControllerEvent.RESPONSE);
                        // Update the state of the view for when connected to the MQTT server
                        updateViewStateForConnected();

                        // Notify the originator that the connection was successful
                        connectionMonitorCallback.onConnectSuccess(asyncActionToken);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, METHOD + "Connect FAIL!", exception);
                        String notificationMessage = "Connect FAIL";
                        // Store this in the Notifications deque
                        pushNotification(notificationMessage);
                        // Always show error Toasts
                        Toast.makeText(getApplication(), notificationMessage, Toast.LENGTH_LONG).show();
                        updateViewstateForDisconnected();

                        // Notify the originator that the connection failed
                        connectionMonitorCallback.onConnectFailure(asyncActionToken, exception);
                    }
                });
                Log.d(TAG, "Got back token: " + token.toString());
            } catch (Exception e) {
                String notificationMessage = "Exception thrown while connecting application client: ";
                Log.e(TAG, notificationMessage, e);
                // Store this in the Notification deque
                pushNotification(notificationMessage);
            }
        }

        /**
         * Returns the SSLSocketFactory to use to connect to the MQTT server over ssl://
         * @param context The ApplicationContext to use
         * @return SSLSocketFactory
         */
        private SSLSocketFactory getSSLSocketFactory(Context context) {
            SSLSocketFactory factory = null;
            try {
                ProviderInstaller.installIfNeeded(context);

                SSLContext sslContext;
                KeyStore ks = KeyStore.getInstance("bks");
                ks.load(context.getResources().openRawResource(R.raw.iot), "password".toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                tmf.init(ks);
                TrustManager[] tm = tmf.getTrustManagers();
                sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, tm, null);
                factory = sslContext.getSocketFactory();
            } catch (Exception e) {
                String notificationMessage = "Exception thrown trying to get SSLSocketFactory: ";
                Log.e(TAG, notificationMessage, e);
                // Store this in the Notification deque
                pushNotification(notificationMessage);
            }
            return factory;
        }

        /**
         * Generate a unique Application Client ID to use for this session.
         *
         * @param orgId The Organization ID to use
         *
         * @return String - the unique Application Client ID to use
         */
        private String generateApplicationClientId(String orgId) {
            return "a:" + orgId + ":" + UUID.randomUUID().toString();
        }

        /**
         * This MqttCallback implementation that acts as "monitor" for the
         * application. When connection is lost, a message has arrived, or delivery is
         * complete, this callback is invoked.
         */
        private MqttCallback mMonitorCallback;

        /**
         * Returns the MqttCallback used to monitor the connection to the MQTT
         * server once connected to it, creating the callback if necessary.
         *
         * @return MqttCallback - the callback to be used to monitor MQTT server
         * connection status.
         */
        private MqttCallback getMonitorCallback() {
            final String METHOD = "getMonitorCallback(): ";
            if (mMonitorCallback == null) {
                mMonitorCallback = new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e(TAG, METHOD + "Connection lost!");
                        // Always display error Toasts
                        String notificationMessage = "Connection Lost";
                        pushNotification(notificationMessage);
                        // TODO: Store this in the Notification deque
                        Toast.makeText(getApplicationContext(), notificationMessage, Toast.LENGTH_LONG).show();
                        // Update the connection status view
                        updateViewstateForDisconnected();
                        loadHomeFragment();// TODO: This is a stopgap to get the page state to look right
                    }

                    /**
                     * All subscribed-to notifications end up here. This method is hub for all
                     * notifications that we have subscribed to. From here, we'll just route them
                     * where they need to go.
                     *
                     * @param topic The topic String for the incoming message
                     *
                     * @param message The message itself
                     *
                     * @throws Exception If something goes horribly wrong.
                     */
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String payload = new String(message.getPayload());
                        // Store this in the Notification deque
                        Log.d(TAG, METHOD + "Message arrived from topic: " + topic + ", message: " + payload);
                        pushNotification(createStoreMessage(topic, message.getPayload()));
                        //
                        // Route the message to where it should go
                        routeMessage(topic, message);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // TODO: Store this in the Notification deque?
                        Log.d(TAG, METHOD + "Message delivery complete.");
                    }
                };
            }
            return mMonitorCallback;
        }

        /**
         * Handles message routing, keeps the Monitor callback from getting
         * cluttered.
         *
         * @param topicString Duh
         * @param message Duh
         */
        private void routeMessage(String topicString, MqttMessage message) {
            //
            // Q: Is this a request message? If so, we can probably ignore it
            /// since the job of this application is to send notifications (events) to
            /// the Device Controller and then process its responses.
            if (isRequest(topicString)) {
                handleRequestMessage(topicString, message);
            } else if (isResponse(topicString)) {
                handleResponseMessage(topicString, message);
            } else {
                String notificationMessage = "Unknown message type. Topic is: '" + topicString + "'. Was a new message type added without the corresponding logic in this method?";
                Log.e(TAG, notificationMessage);
                pushNotification(notificationMessage);
            }
        }

        /**
         * Compare the specified topic string with the canonical string for
         * a request event. If they match, return true, otherwise return false.
         *
         * @param topicString The topic string to compare
         *
         * @return - boolean - true if the specified topicString is for a request,
         * false otherwise.
         */
        private boolean isRequest(String topicString) {
            boolean ret = false;
            // First, generate the canonical topic string for a request
            String requestTopic = createEventTopicString(ControllerEvent.REQUEST);
            // Compare the requestTopic String to the topic string we're looking at.
            /// If they match, then the topic string is a request. Otherwise, bail.
            if (topicString.equals(requestTopic)) {
                ret = true;
            }
            return ret;
        }

        /**
         * Handle processing of the specified request message
         * @param topicString Duh
         * @param message Duh
         */
        private void handleRequestMessage(String topicString, MqttMessage message) {
            //
            // Store the message in the message store
            getNotifications().add(new NotificationContent.NotificationItem(
                    Long.toString(System.currentTimeMillis()),
                    createStoreMessage(topicString, message.getPayload())));
        }

        /**
         * Compare the specified topic string with the canonical string for
         * a response event. If they match, return true, otherwise return false.
         *
         * @param topicString The topic string to compare
         *
         * @return - boolean - true if the specified topicString is for a response,
         * false otherwise.
         */
        private boolean isResponse(String topicString) {
            boolean ret = false;
            // First, generate the canonical topic string for a request
            String requestTopic = createEventTopicString(ControllerEvent.RESPONSE);
            // Compare the requestTopic String to the topic string we're looking at.
            /// If they match, then the topic string is a request. Otherwise, bail.
            if (topicString.equals(requestTopic)) {
                ret = true;
            }
            return ret;
        }

        /**
         * Handle processing of the specified response message.
         * @param topicString Duh
         * @param message The response message.
         */
        private void handleResponseMessage(String topicString, MqttMessage message) {
            final String METHOD = "handleResponseMessage(): ";
            // Store the message in the message store
            getNotifications().add(new NotificationContent.NotificationItem(
                    Long.toString(System.currentTimeMillis()), createStoreMessage(topicString, message.getPayload())));
            //
            // Handle the payload of the message. If this method gets
            /// too cluttered it may make sense to create a class to
            /// handle it. Right now the set of response notifications is
            /// pretty small:
            //
            // - deviceResponse
            //
            String messageAsString = new String(message.getPayload());
            // Create a JSON object
            try {
                JSONObject jsonObject = new JSONObject(messageAsString);
                // Create the Device Response JSON
                String deviceResponse = jsonObject.getJSONObject(JSONDiscoveryDeviceResponse.D.toString()).getString(JSONDiscoveryDeviceResponse.DEVICE_RESPONSE.toString());
                // If we got this far, the mapping exists
                handleDeviceResponseCallbacks(deviceResponse, message);
            } catch (JSONException e) {
                String notificationMessage = "JSONException occurred while creating JSONObject from String: '" + messageAsString + "'";
                Log.e(TAG, METHOD + notificationMessage, e);
                // Store this in the Notification deque
                pushNotification(notificationMessage);
            }
        }

        /**
         * Handles delivery of the specified device response message.
         *
         * @param deviceResponseMessage Duh
         */
        private void handleDeviceResponseCallbacks(String deviceResponse, MqttMessage deviceResponseMessage) {
            DeviceResponseCallback callback = getDeviceResponseCallbacks().get(deviceResponse);
            if (callback != null) {
                callback.onDeviceResponse(deviceResponseMessage);
            } else {
                String notificationMessage = "DeviceResponseCallback for deviceResponse '" + deviceResponse + "' is not registered. Does not compute.";
                Log.e(TAG, notificationMessage);
                // Store this in the Notification deque
                pushNotification(notificationMessage);
            }
        }

        /**
         * A component registers their interest in receiving a callback when a
         * device response message comes in.
         *
         * @param callback The callback to invoke once the device response has been received. Allows
         *                 us to touch the Fragment in an appropriate way.
         */
        public void subscribeToDeviceResponse(String deviceResponse, DeviceResponseCallback callback) {
            getDeviceResponseCallbacks().put(deviceResponse, callback);
        }

        @Override
        public void onNotificationFragmentInteraction(NotificationContent.NotificationItem item) {
            // Nothing to do
        }

        @Override
        public void pushNotification(String notificationMessage) {
            // If the current size is at the limit, then remove the oldest message
            /// before adding a new one
            int notificationsSize = getNotifications().size();
            if (notificationsSize >= getApplicationProperties().getMaxNotificationCount()) {
                getNotifications().removeLast();
            }
            // Add this message to the deque
            getNotifications().add(new NotificationContent.NotificationItem(
                    Long.toString(System.currentTimeMillis()), notificationMessage));
        }

        private Map<String, DeviceResponseCallback> mDeviceResponseCallbacks;

        private Map<String, DeviceResponseCallback> getDeviceResponseCallbacks() {
            if (mDeviceResponseCallbacks == null) {
                mDeviceResponseCallbacks = new HashMap<>();
            }
            return mDeviceResponseCallbacks;
        }

        /**
         * Publish a "discovery" event, the intent of which is that it will be picked
         * up by the Home Automation Controller (which will be subscribed to all events
         * for itself), and repsonds with a "discovery response" that contains a list
         * of all of its managed devices, and the actions that may be performed on them
         * by an authenticated application.
         */
        public void publishDiscoveryEvent() {
            publishEvent(getApplicationProperties().getControllerDeviceId(), "discovery");
        }

        /**
         * Broadcasts an event to the specified controlled
         * @param deviceId The Device identity
         * @param deviceAction The action to perform on the specified device whose identity is deviceId
         */
        public void publishEvent(String deviceId, String deviceAction) {
            publishEvent(deviceId, deviceAction, null);
        }

        /**
         * Broadcasts an event to the specified controlled device on behalf of the
         * controller device.
         *
         * @param controlledDeviceId The controlled device Id (e.g., Outlet-1)
         * @param controlledDeviceAction The action to perform on the controlled device (e.g., on)
         * @param broadcastListener The optional broadcast listener provided by the caller. If null, this method will supply one.
         */
        public void publishEvent(String controlledDeviceId, String controlledDeviceAction, IMqttActionListener
        broadcastListener) {
            final String METHOD = "publishEvent(): ";
            MqttMessage mqttMessage = createJsonMessage(controlledDeviceId, controlledDeviceAction);
            try {
                String topic = createEventTopicString(ControllerEvent.REQUEST);
                // Use the specified IMqttActionListener if one provided, otherwise create a default one to use.
                IMqttActionListener mqttActionListener = (broadcastListener != null) ? broadcastListener :
                        new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                String notificationMessage = "Publish SUCCESS: " + asyncActionToken.getUserContext();
                                Log.d(TAG, notificationMessage);
                                // Store this in the Notification deque
                                pushNotification(notificationMessage);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                String notificationMessage = "Publish FAIL: " + asyncActionToken.getUserContext();
                                Log.e(TAG, notificationMessage);
                                // Store this in the Notification deque
                                pushNotification(notificationMessage);
                                // Always display error Toasts
                                Toast.makeText(getApplication(), notificationMessage, Toast.LENGTH_LONG).show();
                            }
                        };
                // Publish the message
                if (getApplicationClient() != null && getApplicationClient().isConnected()) {
                    getApplicationClient().publish(topic, mqttMessage, topic, mqttActionListener);
                } else {
                    // Store this in the Notification deque
                    String notificationMessage = "Not connected! Unable to publish message to topic '" + topic + "'. Please reconnect.";
                    pushNotification(notificationMessage);
                    // Always display error Toasts
                    Toast.makeText(getApplicationContext(), notificationMessage, Toast.LENGTH_LONG).show();
                }
            } catch (MqttException e) {
                String notificationMessage = "Exception thrown while broadcasting event: ";
                Log.e(TAG, METHOD + notificationMessage, e);
                // Store this in the Notification deque
                pushNotification(notificationMessage);
            }
        }

        /**
         * Creates a device message in JSON format.
         * TODO: Move to helper class?
         * @param deviceId The deviceID of the controlled device (NOT the controller device)
         * @param deviceAction The device action (e.g., ON/OFF, etc)
         * @return MqttMessage - The MqttMessage containing the JSON string
         */
        private MqttMessage createJsonMessage(String deviceId, String deviceAction) {
            String jsonMessage =  "{ \"d\": { \"deviceRequest\" : { \"deviceId\" : \"" + deviceId + "\", \"action\" : \"" + deviceAction + "\" } } }";
            return new MqttMessage(jsonMessage.getBytes());
        }

        /**
         * Create the Topic name to use for the specified Event ID. The topic string is the
         * name of the topic (which is the URI specific to the topic) to which a message is to
         * be published. It's very specific to the MQTT broker in use.
         *
         * TODO: Move to helper class?
         * TODO: Make more generic (not specific to the Controller)?
         *
         * @return String - the Topic String
         */
        private String createEventTopicString(ControllerEvent eventId) {
            return "iot-2/type/" + getApplicationProperties().getControllerDeviceType() + "/id/" + getApplicationProperties().getControllerDeviceId() + "/evt/" + eventId.toString() + "/fmt/json";
        }

        /**
         * Subscribes to all events. Seemed like a good idea at the time.
         */
        private void subscribeToDeviceEventsForController(ControllerEvent... controllerEvents) {
            final String METHOD = "subscribeToDeviceEvents(" + ArrayUtils.toString(controllerEvents) + "): ";
            for (ControllerEvent controllerEvent : controllerEvents) {
                try {
                    // Subscribe to the specified eventId as indicated below:
                    String deviceType = getApplicationProperties().getControllerDeviceType();
                    String deviceId = "+"; // All device IDs for the specified Device Type
                    String eventId = controllerEvent.toString();
                    String userContext = "SUBSCRIBE:" + deviceType + "/" + deviceId + "/" + eventId;
                    getApplicationClient().subscribe("iot-2/type/" + deviceType + "/id/" + deviceId + "/evt/" + eventId + "/fmt/json",
                            1,
                            userContext, // Not sure this really matters, but here goes...
                            new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    String notificationMessage = "Subscribe SUCCESS: " + asyncActionToken.getUserContext();
                                    Log.d(TAG, METHOD + notificationMessage);
                                    // Store this in the Notification deque
                                    pushNotification(notificationMessage);
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    String notificationMessage = "Subscribe FAIL: " + asyncActionToken.getUserContext();
                                    Log.e(TAG, METHOD + notificationMessage, exception);
                                    // Store this in the Notification deque
                                    pushNotification(notificationMessage);
                                    // Always show error Toasts
                                    Toast.makeText(getApplication(), "Subscribe FAIL!", Toast.LENGTH_LONG).show();
                                }
                            });
                } catch (MqttException e) {
                    String notificationMessage = "Exception thrown while subscribing to all device events: ";
                    Log.e(TAG, METHOD + notificationMessage, e);
                    // Store this in the Notification deque
                    pushNotification(notificationMessage);
                }
            }
        }

        // TODO: REMOVE THIS COMMENTED OUT METHOD
//    /**
//     * Load the application properties as which we will connect.
//     * This will eventually be replaced by the Settings fragment.
//     * @return Properties - the properties specific to the application
//     */
//    private ApplicationProperties loadApplicationPropertiesUntilSettingsFragmentIsCoded() {
//        // TODO: Move this functionality to Settings fragment
//        ApplicationProperties properties = new ApplicationProperties(getApplicationContext());
//        properties.setMqttServerProtocol("ssl");
//        properties.setMqttServerHostName("messaging.internetofthings.ibmcloud.com");
//        properties.setMqttServerPort("8883");
//        properties.setMaxNotificationCount(100);
//        return properties;
//    }

        /**
         * Return the ApplicationClient in use. If it is not connected,
         * then an attempt is made to connect before returning the object.
         *
         * @return ApplicationClient - A connected ApplicationClient object, which
         * can be used to communicate with the MQTT server.
         */
        @Override
        public ApplicationClient getApplicationClientAndConnectIfNecessary(ConnectionMonitorCallback connectionMonitorCallback) {
            // If ApplicationClient is null, create it.
            if (getApplicationClient() == null) {
                setApplicationClient(createApplicationClient());
            }
            // If ApplicationClient is not connected, connect it.
            if (!getApplicationClient().isConnected()) {
                connectApplicationClient(connectionMonitorCallback);
            }
            return getApplicationClient();
        }

        /**
         * Invalidates the current connection. Something most likely changed in the
         * connection settings (at the user's request), so it's best to disconnect
         * and start fresh.
         */
        @Override
        public void invalidateConnection() {
            disconnect();
        }

        @Override
        public boolean isConnected() {
            return getApplicationClient() != null &&
                    getApplicationClient().isConnected();
        }

        @Override
        public void disconnect() {
            try {
                getApplicationClient().disconnect();
            } catch (MqttException e) {
                // TODO: Store this in the Notification deque
                Log.e(TAG, "Exception occurred while disconnecting: ", e);
            }
        }

        /**
         * The DashboardFragment wants the MainActivity to know that the user has
         * interacted with it. It's up to the MainActivity to take action, leaving
         * the Fragment uncluttered.
         *
         * @param item The item that was touched by the user and that this method needs
         *             to do something with.
         */
        @Override
        public void onDashboardFragmentInteraction(IotDeviceContent.IotDeviceItem item) {
            Toast.makeText(getApplicationContext(), "Howdy from IotDeviceItem: " + item.toString(), Toast.LENGTH_LONG).show();

            // Display the SelectAction DialogFragment to let the user pick the action they
            /// want to invoke. For now, all actions must be no-arg.
            List<Pair<String, String>> actions = item.actions;
            String[] actionsArray = new String[actions.size()];
            int aa = 0; // It's a throw-away variable. Get over it.
            for (Pair<String, String> action : actions) {
                actionsArray[aa++] = action.getLeft() + ":=> " + action.getRight();
            }
            DialogFragment dialogFragment = ActionDialogFragment.newInstance(item.deviceId, actionsArray);
            dialogFragment.show(getFragmentManager(), ActionDialogFragment.FRAGMENT_TAG);
        }

        /**
         * The DialogFragment wants to MainActivity to know the user has interacted with it.
         * It's up to the MainActivity to take action, leaving the Fragment uncluttered.
         *
         * @param deviceId       The deviceId of the device on which the action is to be taken
         * @param selectedAction The action that was chosen
         */
        @Override
        public void onActionDialogFragmentInteraction(String deviceId, String selectedAction) {
            //Toast.makeText(getApplicationContext(), "You have chosen action '" + selectedAction + "'", Toast.LENGTH_LONG).show();
            // As a convenience to the user, the action contains its description. Strip that off
            /// so we just have the action.
            StringTokenizer strtok = new StringTokenizer(selectedAction, ":=>");
            String action = strtok.nextToken();
            publishEvent(deviceId, action);
        }
}
