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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

/**
 * DialogFragment for allowing the user to select which action they
 * want to invoke on the selected device. Nothing fancy.
 */
public class ActionDialogFragment extends DialogFragment {

    private static final String TAG = ActionDialogFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG = ActionDialogFragment.class.getName();

    public static final String ARG_DEVICE_ID = "device-id";
    public static final String ARG_CHOICE_ITEMS = "choice-items";

    private String mDeviceId;
    private CharSequence[] mChoiceItems;

    private OnActionDialogFragmentInteractionListener mListener;

    /**
     * This is how you create a Fragment. Yep.
     *
     * @param deviceId The Device ID on which the action is to be invoked.
     *
     * @param choiceItems The list of choices of actions that can be invoked.
     *
     * @return The DialogFragment, ready to rumble.
     */
    public static ActionDialogFragment newInstance(String deviceId, String[] choiceItems) {

        Bundle args = new Bundle();

        ActionDialogFragment fragment = new ActionDialogFragment();
        args.putString(ARG_DEVICE_ID, deviceId);
        args.putSerializable(ARG_CHOICE_ITEMS, choiceItems);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates the Dialog itself.
     *
     * @param savedInstanceState Any previously saved state.
     *
     * @return Dialog - the fully initialized dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //
        if (getArguments() != null) {
            mDeviceId = getArguments().getString(ARG_DEVICE_ID);
            mChoiceItems = getArguments().getStringArray(ARG_CHOICE_ITEMS);
        } else {
            mChoiceItems = new CharSequence[0];
        }
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //
        //
        builder.setTitle(R.string.title_select_action);
        builder.setSingleChoiceItems(mChoiceItems, -1, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User wants to proceed. Let's do it.
                // Get the selected item index
                int selectedItem = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                // Delegate back to the owning activity through our handy dandy callback interface
                mListener.onActionDialogFragmentInteraction(mDeviceId, mChoiceItems[selectedItem].toString());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // If running on version < Marshmallow, then the listener has to be initialized this way
        if (Build.VERSION.SDK_INT < 23) {
            Context context = getActivity();
            if (context instanceof ActionDialogFragment.OnActionDialogFragmentInteractionListener) {
                mListener = (ActionDialogFragment.OnActionDialogFragmentInteractionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnFragmentInteractionListener");
            }
        }
        return builder.create();
    }

    /**
     * Fragment is attaching. Make sure the Context implements the correct
     * interface(s).
     *
     * @param context The Context, most likely the owning Activity, but not
     *                necessarily.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnActionDialogFragmentInteractionListener) {
            mListener = (OnActionDialogFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnActionDialogFragmentInteractionListener");
        }
    }

    /**
     * Cleanup, aisle 5
     */
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
    public interface OnActionDialogFragmentInteractionListener {

        /**
         * Tell the owning Activity that the user has chosen an action
         *
         * @param deviceId       The deviceId of the device on which the action is to be taken
         * @param selectedAction The action that was chosen
         */
        void onActionDialogFragmentInteraction(String deviceId, String selectedAction);

    }
}
