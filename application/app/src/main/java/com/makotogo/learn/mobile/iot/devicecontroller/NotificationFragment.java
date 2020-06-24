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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.makotogo.learn.mobile.iot.devicecontroller.NotificationContent.NotificationItem;

import java.util.ArrayDeque;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnNotificationFragmentInteractionListener}
 * interface.
 */
public class NotificationFragment extends Fragment {

    // Fragment arguments
    private static final String ARG_COLUMN_COUNT = "arg-column-count";
    private static final String ARG_NOTIFICATIONS = "arg-notifications";

    public static final String FRAGMENT_TAG = NotificationFragment.class.getName();

    //
    private int mColumnCount;
    private ArrayDeque<NotificationItem> mNotifications;

    private ArrayDeque<NotificationItem> getNotifications() {
        if (mNotifications == null) {
            throw new IllegalStateException("Notifications is null! Fix your code, dude.");
        }
        return mNotifications;
    }

    // Lets us touch the MainActivity in an appropriate way
    private OnNotificationFragmentInteractionListener mListener;

    private OnNotificationFragmentInteractionListener getListener() {
        if (mListener == null) {
            throw new IllegalStateException("Listener is null! Fix your code, dude.");
        }
        return mListener;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationFragment() {
    }

    public static NotificationFragment newInstance(Integer columnCount, ArrayDeque<NotificationItem> notifications) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putSerializable(ARG_NOTIFICATIONS, notifications);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            //noinspection unchecked
            mNotifications = (ArrayDeque<NotificationItem>) getArguments().getSerializable(ARG_NOTIFICATIONS);
        }
        // If running on version < Marshmallow, then the listener has to be initialized this way
        if (Build.VERSION.SDK_INT < 23) {
            Context context = getActivity();
            if (context instanceof HomeFragment.OnHomeFragmentInteractionListener) {
                mListener = (NotificationFragment.OnNotificationFragmentInteractionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnFragmentInteractionListener");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);

        // Create the items
        NotificationContent.addAll(getNotifications());

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new NotificationRecyclerViewAdapter(NotificationContent.getItems(), getListener()));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNotificationFragmentInteractionListener) {
            mListener = (OnNotificationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNotificationFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNotificationFragmentInteractionListener {

        /**
         * Tell the owning Activity that the user has pressed an item
         * in the RecyclerView.
         *
         * @param item The item that was pressed
         */
        void onNotificationFragmentInteraction(NotificationItem item);

        /**
         * Pushes the specified notification message onto the Notifications deque
         *
         * @param notificationMessage Duh
         */
        void pushNotification(String notificationMessage);

    }
}
