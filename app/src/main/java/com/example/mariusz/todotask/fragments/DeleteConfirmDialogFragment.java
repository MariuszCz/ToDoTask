package com.example.mariusz.todotask.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.mariusz.todotask.Activities.TasksListActivity;
import com.example.mariusz.todotask.R;


public class DeleteConfirmDialogFragment extends DialogFragment {
    private Context context;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.delete_message))
                .setPositiveButton(getString(R.string.delete_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = getArguments().getInt(getString(R.string.positionExtra));
                        OnPositiveDelete onPositiveDelete = (TasksListActivity)context;
                        onPositiveDelete.positiveDelete(position);
                    }
                })
                .setNegativeButton(getString(R.string.delete_negative_button), null).create();
    }

    public interface OnPositiveDelete {
        void positiveDelete(int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }
}