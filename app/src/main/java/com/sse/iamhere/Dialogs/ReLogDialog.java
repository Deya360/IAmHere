package com.sse.iamhere.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sse.iamhere.AuthenticationActivity;
import com.sse.iamhere.HomeActivity;
import com.sse.iamhere.R;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import static android.app.Activity.RESULT_OK;
import static com.sse.iamhere.Utils.Constants.AUTHENTICATION_RELOG_RQ;


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
        builder.setPositiveButton(getString(R.string.auth_re_log_authenticate), (dialog, which) -> {
            startAuthenticationActivity();
        });
        builder.setNegativeButton("Exit", (dialog, which) -> {
            dialog.dismiss();
            getActivity().finish();
        });
        return builder.create();
    }

    private void startAuthenticationActivity() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser!=null) {
            if (getActivity()!=null) {
                Constants.Role role;
                if ((role= PreferencesUtil.getRole(getActivity(), Constants.Role.NONE))!= Constants.Role.NONE) {
                    Intent intent;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isRegistered", isRegistered);
                    bundle.putString("phone", currentUser.getPhoneNumber());
                    bundle.putString("phoneFormatted", currentUser.getPhoneNumber()); //TODO: format properly
                    bundle.putBoolean("showAsDialog", true);
                    bundle.putInt("forceRole", role.toIdx());
                    bundle.putInt("activityOrientation", this.getResources().getConfiguration().orientation);
                    bundle.putBoolean("disallowCancel", true);
                    bundle.putInt("returnRequestCode", AUTHENTICATION_RELOG_RQ);

                    String description = "";
                    if (isRegistered) {
                        description = getString(R.string.auth_subtitleTv_remote_login_label_prefix) + " " +
                                getString(role.toStringRes()) + " " + getString(R.string.auth_subtitleTv_remote_login_label_suffix);

                    } else {
                        switch (role) {
                            case HOST:
                            case MANAGER:
                                description = getString(R.string.auth_subtitleTv_remote_registration_label_prefix) + " " +
                                        getString(role.toStringRes()) + " "
                                        + getString(R.string.auth_subtitleTv_remote_registration_label_suffix);
                                break;

                            case ATTENDEE:
                                description = getString(R.string.auth_subtitleTv_remote_registration_label_prefix) +
                                        getString(R.string.auth_subtitleTv_remote_registration_label_prefix_attendee_extra) + " " +
                                        getString(role.toStringRes()) + " "
                                        + getString(R.string.auth_subtitleTv_remote_registration_label_suffix);
                                break;
                        }
                    }
                    bundle.putString("customDescription", description);

                    intent = new Intent(getActivity(), AuthenticationActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, AUTHENTICATION_RELOG_RQ);

                } else {
                    //Todo: implement
                }
            } else {
                //Todo: implement
            }
        } else {
            //Todo: implement
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==AUTHENTICATION_RELOG_RQ) {
            if (resultCode==RESULT_OK) {
                if (getDialog()!=null) {
                    getDialog().dismiss();
                }
            }
        }

    }
}
