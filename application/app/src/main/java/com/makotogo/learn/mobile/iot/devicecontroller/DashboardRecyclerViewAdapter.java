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
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makotogo.learn.mobile.iot.devicecontroller.DashboardFragment.OnDashboardFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.makotogo.learn.mobile.iot.devicecontroller.IotDeviceContent.IotDeviceItem} and makes a call to the
 * specified {@link OnDashboardFragmentInteractionListener}.
 *
 * Most of this was generated by the Wizard. I ain't gonna lie to ya.
 *
 */
public class DashboardRecyclerViewAdapter extends RecyclerView.Adapter<DashboardRecyclerViewAdapter.ViewHolder> {

    private final List<IotDeviceContent.IotDeviceItem> mValues;
    private final OnDashboardFragmentInteractionListener mListener;

    public DashboardRecyclerViewAdapter(List<IotDeviceContent.IotDeviceItem> items, DashboardFragment.OnDashboardFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDeviceIdView.setText(mValues.get(position).deviceId);
        holder.mDeviceDescriptionView.setText(mValues.get(position).deviceDescription);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onDashboardFragmentInteraction(holder.mItem);
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
        public final TextView mDeviceIdView;
        public final TextView mDeviceDescriptionView;
        public IotDeviceContent.IotDeviceItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDeviceIdView = (TextView) view.findViewById(R.id.deviceId);
            mDeviceDescriptionView = (TextView) view.findViewById(R.id.deviceDescription);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDeviceDescriptionView.getText() + "'";
        }
    }
}
