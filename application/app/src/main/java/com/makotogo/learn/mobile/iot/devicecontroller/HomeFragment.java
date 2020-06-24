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

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.paho.client.mqttv3.IMqttToken;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnHomeFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PROPERTIES = "arg-properties";
    public static final String FRAGMENT_TAG = HomeFragment.class.getName();

    /**
     * The ApplicationProperties object, which we use to retrieve and
     * store properties about the application (it's not just a clever name).
     */
    private ApplicationProperties mProperties;

    /**
     * Getter for mProperties. I'm not a fan of "raw" access to private variables.
     *
     * @return ApplicationProperties
     */
    private ApplicationProperties getProperties() {
        if (mProperties == null) {
            String notification = "ApplicationProperties is null. This may be fine, or it may indicate a configuration or other code issue.";
            Log.w(TAG, notification);
            getListener().pushNotification(notification);
        }
        return mProperties;
    }

    /**
     * The listener interface that allows communication with the
     * context (most likely the owning Activity). This is the Android
     * way for a Fragment to communicate with the outside.
     */
    private OnHomeFragmentInteractionListener mListener;

    /**
     * Returns the {@link OnHomeFragmentInteractionListener} reference.
     *
     * @return OnHomeFragmentInteractionListener - the listener interface.
     */
    private OnHomeFragmentInteractionListener getListener() {
        if (mListener == null) {
            String notification = "OnHomeFragmentInteractionListener is null. This may be fine, or it may indicate a configuration or other code issue.";
            Log.e(TAG, notification);
            throw new IllegalStateException(notification);
        }
        return mListener;
    }

    /**
     * (sigh) Required by the Fragment infrastructure.
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param applicationProperties The ApplicationProperties object to use for settings and whatnot
     *
     * @return HomeFragment - ready to roll
     */
    public static HomeFragment newInstance(ApplicationProperties applicationProperties) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROPERTIES, applicationProperties);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mProperties = (ApplicationProperties)getArguments().getSerializable(ARG_PROPERTIES);
        }
        // If running on version < Marshmallow, then the listener has to be initialized this way
        if (Build.VERSION.SDK_INT < 23) {
            Context context = getActivity();
            if (context instanceof OnHomeFragmentInteractionListener) {
                mListener = (OnHomeFragmentInteractionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnFragmentInteractionListener");
            }
        }
    }

    /**
     * Creates the view. That's all I've got to say about that.
     *
     * @param inflater The LayoutInflater to use.
     * @param container Not used.
     * @param savedInstanceState Any previously saved state.
     *
     * @return The view, fully initialized.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_home, container, false);
        // TODO: Break these out in "lazy load" getter fashion so as not to clutter this method
        //
        // Setup the view
        // Org ID
        // TODO: If any one of these change, and are currently connected, need to disconnect!
        // Like this:
        getOrgIdEditText(view);// access is initialization
        // TODO: Now do the rest.
        // Controller Device Type
        getDeviceTypeEditText(view);
        // Controller Device ID
        EditText deviceId = view.findViewById(R.id.deviceIdTextField);
        deviceId.setText(getProperties().getControllerDeviceId());
        // API Key
        EditText apiKey = view.findViewById(R.id.apiKeyTextField);
        apiKey.setText(getProperties().getApiKey());
        // Auth Token
        EditText authToken = view.findViewById(R.id.authTokenTextField);
        authToken.setText(getProperties().getAuthToken());
        // Connect button
        getConnectButton(view);//access is initialization
        return view;
    }

    /**
     * Note: this only works for API > 23
     * @param context The application context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Initializes the OrgId EditText field.
     */
    private void getOrgIdEditText(View view) {
        EditText editText = view.findViewById(R.id.orgIdTextField);
        editText.setText(getProperties().getOrgId());
        editText.addTextChangedListener(new TextWatcher() {
            private String before;
            private String after;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                after = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isAppConnected()) {
                    //
                    // Notify the context that the connection is no longer valid.
                    // The user has changed something about it.
                    String notification = "OrgId field changed from '" + before + "' to '" + after + "'. Connection is no longer valid.";
                    Log.w(TAG, notification);
                    getListener().invalidateConnection();
                }
            }
        });
    }

    private void getDeviceTypeEditText(View view) {
        EditText editText = view.findViewById(R.id.deviceTypeTextField);
        editText.setText(getProperties().getControllerDeviceType());
        editText.addTextChangedListener(new TextWatcher() {
            private String before;
            private String after;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                after = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isAppConnected()) {
                    //
                    // Notify the context that the connection is no longer valid.
                    // The user has changed something about it.
                    String notification = "Device Type field changed from '" + before + "' to '" + after + "'. Connection is no longer valid.";
                    Log.w(TAG, notification);
                    getListener().pushNotification(notification);
                    getListener().invalidateConnection();
                }
            }
        });
    }

    /**
     * Initializer for the Connect/Disconnect button
     *
     * @param view The view to which the Button will be attached.
     */
    private void getConnectButton(final View view) {
        final Button connect = view.findViewById(R.id.connectButton);
        connect.setText(isAppConnected() ? R.string.disconnect : R.string.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the current values to SharedPreferences so the user only
                // has to enter them once.
                saveCurrentValues(view);
                if (isAppConnected()) {
                    // TODO: Disconnect is very simple, but maybe pass a ConnectionMonitorCallback just in case before toggling the button text?
                    getListener().disconnect();
                    connect.setText(R.string.connect);
                } else {
                    // Reach out to the Activity and get the ApplicationClient reference.
                    // Drop the ApplicationClient reference on the floor, we don't care
                    // about it at the moment.
                    getListener().getApplicationClientAndConnectIfNecessary(new ConnectionMonitorCallback() {
                        @Override
                        public void onConnectSuccess(IMqttToken asyncActionToken) {
                            // Connect succeeded. Do stuff.
                            connect.setText(R.string.disconnect);
                        }

                        @Override
                        public void onConnectFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Connect failed. Bummer.
                            // TODO: Do something?
                        }
                    });
                }
            }
        });
    }

    //****************************************************************************
    //

    /**
     * Convenience method for testing the condition "is the app connected to the
     * MQTT broker?"
     *
     * @return boolean - returns true if connected, false otherwise.
     */
    private boolean isAppConnected() {
        return getListener() != null
                &&
                getListener().isConnected();
    }

    /**
     * Save the current field values in the ApplicationProperties object
     */
    private void saveCurrentValues(View view) {
        // OrgId
        EditText textField = view.findViewById(R.id.orgIdTextField);
        getProperties().setOrgId(textField.getText().toString());
        // DeviceType
        textField = view.findViewById(R.id.deviceTypeTextField);
        getProperties().setControllerDeviceType(textField.getText().toString());
        // DeviceId
        textField = view.findViewById(R.id.deviceIdTextField);
        getProperties().setControllerDeviceId(textField.getText().toString());
        // API Key
        textField = view.findViewById(R.id.apiKeyTextField);
        getProperties().setApiKey(textField.getText().toString());
        // AuthToken
        textField = view.findViewById(R.id.authTokenTextField);
        getProperties().setAuthToken(textField.getText().toString());
    }

    public interface OnHomeFragmentInteractionListener {

        /**
         * Return the ApplicationClient managed by MainActivity.
         * If ApplicationClient does not exist, it will be created.
         * Guaranteed to be connected upon successful return.
         *
         * @return ApplicationClient - a connected ApplicationClient that
         * can be used to communicate with the MQTT server.
         */
        @SuppressWarnings("UnusedReturnValue")
        ApplicationClient getApplicationClientAndConnectIfNecessary(ConnectionMonitorCallback connectionMonitorCallback);

        /**
         * Lets the context know that the current connection (if connected) is no
         * longer valid and should probably be terminated. Most likely the user
         * has used this screen to change something about the connection. That is,
         * after all, what this Fragment is for.
         */
        void invalidateConnection();

        /**
         * Returns a boolean from the owning activity indicating whether or not
         * the application is connected.
         *
         * @return true if connected, false otherwise.
         */
        boolean isConnected();

        /**
         * Disconnects the application
         */
        void disconnect();

        /**
         * Pushes the specified notification message onto the Notifications deque
         *
         * @param notificationMessage Duh
         */
        void pushNotification(String notificationMessage);

    }

}
