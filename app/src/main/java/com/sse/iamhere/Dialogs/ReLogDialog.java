package com.sse.iamhere.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.sse.iamhere.HomeActivity;
import com.sse.iamhere.R;


/*
* This is a housing dialog that launches the Authentication Activity with in it, with extra intent
* for a more personalized behaviour
* */
public class ReLogDialog extends AppCompatDialogFragment {
    private boolean isRegistered;
    public ReLogDialog(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (!(getActivity() instanceof HomeActivity)) {
            throw new RuntimeException("ReLogDialog is setup to only run when called from HomeActivity");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Authentication required");
        builder.setMessage(getString(R.string.auth_re_log_authenticate_msg));
        builder.setPositiveButton(getString(R.string.auth_re_log_authenticate), (dialog, which) -> onAuthenticate());
        builder.setNegativeButton("Exit", (dialog, which) -> {
            getActivity().finish();
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            int attempts = 1;
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if (attempts>0) {
                        Toast.makeText(getActivity(), "Press back again to exit", Toast.LENGTH_SHORT).show();
                        attempts--;
                        return false;

                    } else {
                        if (getActivity()!=null) {
                            getActivity().finish();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }
                return true;
            }
        });


        Dialog dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);

        return dlg;
    }

    private void onAuthenticate() {
        if (getActivity()!=null && getActivity() instanceof HomeActivity) {
            ((HomeActivity)getActivity()).onReLogDialogAuthenticate(isRegistered);

        } else {
            Log.e("ReLogDialog", "getActivity() is null onAuthenticate");
        }
    }
}
