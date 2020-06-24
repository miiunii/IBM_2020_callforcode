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
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.makotogo.learn.mobile.iot.devicecontroller.NotificationContent.NotificationItem;
import com.makotogo.learn.mobile.iot.devicecontroller.NotificationFragment.OnNotificationFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link NotificationItem} and makes a call to the
 * specified {@link OnNotificationFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.ViewHolder> {

    private final List<NotificationItem> mValues;
    private final OnNotificationFragmentInteractionListener mListener;

    public NotificationRecyclerViewAdapter(List<NotificationItem> items, OnNotificationFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        //holder.mNotificationIdView.setText(mValues.get(position).getNotificationTimestamp());
        holder.mNotificationMessageView.setText(mValues.get(position).getNotificationMessageWithTimestamp());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onNotificationFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        //public final TextView mNotificationIdView;
        public final TextView mNotificationMessageView;
        public NotificationItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            //mNotificationIdView = (TextView) view.findViewById(R.id.notificationId);
            mNotificationMessageView = (TextView) view.findViewById(R.id.notificationMessage);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNotificationMessageView.getText() + "'";
        }
    }
}
