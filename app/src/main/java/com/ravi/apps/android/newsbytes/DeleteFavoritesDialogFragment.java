/*
 * Copyright (C) 2016 Ravi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ravi.apps.android.newsbytes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Hosts the alert dialog that confirms deleting all favorite news stories.
 */
public class DeleteFavoritesDialogFragment extends DialogFragment {

    // Hold reference to hosting activity that implements the dialog listener interface.
    DeleteFavoritesDialogListener mDeleteFavoritesDialogListener;

    /**
     * Callback interface to pass dialog events back to hosting activity.
     */
    public interface DeleteFavoritesDialogListener {
        // Positive button clicked.
        void onDialogPositiveClick(DialogFragment dialog);
        // Negative button clicked.
        void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Store reference to hosting activity ONLY if it implements the dialog listener interface.
        try {
            mDeleteFavoritesDialogListener = (DeleteFavoritesDialogListener) activity;
        } catch(ClassCastException e) {
            // Throw an exception that the hosting activity does not implement the dialog listener.
            throw new ClassCastException(activity.toString()
                    + getString(R.string.dialog_delete_favorites_no_listener));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Instantiate an alert dialog builder.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Construct the alert dialog builder.
        builder.setTitle(R.string.dialog_delete_favorites_title)
                .setMessage(R.string.dialog_delete_favorites_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.dialog_delete_favorites_delete,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Pass the event back to hosting activity.
                                mDeleteFavoritesDialogListener
                                        .onDialogPositiveClick(DeleteFavoritesDialogFragment.this);
                            }
                        })
                .setNegativeButton(R.string.dialog_delete_favorites_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Pass the event back to hosting activity.
                                mDeleteFavoritesDialogListener
                                        .onDialogNegativeClick(DeleteFavoritesDialogFragment.this);
                            }
                        });

        // Create and return the alert dialog.
        return builder.create();
    }
}
