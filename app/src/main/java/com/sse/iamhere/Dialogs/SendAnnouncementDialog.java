package com.sse.iamhere.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sse.iamhere.R;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.Constants;

import static com.sse.iamhere.Utils.Constants.DEBUG_MODE;
import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class SendAnnouncementDialog extends AppCompatDialogFragment {
    private View view;
    private TextInputEditText bodyEt;
    private ActionProcessButton sendBtn;
    private ActionProcessButton cancelBtn;
    private Button doneBtn;

    private int eventId;
    private int preventBackLimit = 2;
    private boolean isSending = false;

    public SendAnnouncementDialog(int eventId) {
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.dialog_send_announcement, null);

        if (savedInstanceState!=null) {
            eventId = savedInstanceState.getInt("eventId", eventId);
            isSending = savedInstanceState.getBoolean("isSending", isSending);
            preventBackLimit = savedInstanceState.getInt("preventBackLimit", preventBackLimit);
        }

        setupUI();
        return createDialog();
    }

    private AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(getString(R.string.action_announcement_label))
                .setView(view);

        builder.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (isSending && preventBackLimit>0) {
                    showInfoToast(getString(R.string.send_ann_wait_msg), Toast.LENGTH_LONG);
                    preventBackLimit--;
                    return false;

                } else {
                    dialog.dismiss();
                }
            }
            return true;
        });

        AlertDialog dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);

        return dlg;
    }

    private void setupUI() {
        TextInputLayout bodyLy = view.findViewById(R.id.send_ann_bodyLy);
        bodyEt = view.findViewById(R.id.send_ann_bodyEt);

        sendBtn = view.findViewById(R.id.send_ann_sendBtn);
        sendBtn.setMode(ActionProcessButton.Mode.ENDLESS);
        sendBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!isSending) {
                    String msg = bodyEt.getText().toString();
                    if (!TextUtils.isEmpty(msg)) {
                        sendAnnouncement(msg);
                        sendBtn.setProgress(5);
                        bodyLy.setError(null);
                        bodyEt.setEnabled(false);

                    } else {
                        sendBtn.setProgress(-1);
                        bodyLy.setError("Invalid announcement message");

                        new Handler().postDelayed(() -> sendBtn.setProgress(0), 2500);
                    }
                } else {
                    showInfoToast(getString(R.string.send_ann_wait_msg), Toast.LENGTH_LONG);
                }
            }
        });
        if (isSending) {
            sendBtn.setProgress(5);
        }

        cancelBtn = view.findViewById(R.id.send_ann_cancelBtn);
        cancelBtn.setOnClickListener(v -> {
            if (isSending && preventBackLimit>0) {
                showInfoToast(getString(R.string.send_ann_wait_msg), Toast.LENGTH_LONG);
                preventBackLimit--;

            } else {
                dismiss();
            }
        });

        doneBtn = view.findViewById(R.id.send_ann_doneBtn);
        doneBtn.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void sendAnnouncement(String msgBody) {
        isSending = true;
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onHostSendAnnouncementSuccess(String string) {
                    super.onHostSendAnnouncementSuccess(string);

                    if (DEBUG_MODE) {// todo remove temp
                        Log.d("ANNOUNCEMENT", string);
                    }
                }

                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    isSending = false;

                    if (failed) {
                        bodyEt.setEnabled(true);
                        sendBtn.setProgress(-1);
                        new Handler().postDelayed(() -> sendBtn.setProgress(0), 2500);

                        if (failCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }

                    } else {
                        sendBtn.setVisibility(View.GONE);
                        cancelBtn.setVisibility(View.GONE);
                        doneBtn.setVisibility(View.VISIBLE);
                        sendBtn.setProgress(100);
                        showInfoSnackbar(getString(R.string.send_ann_success_msg), 5000);
                    }
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);
        rb.callRequest(() -> rb.hostSendAnnouncement(eventId, msgBody));
    }

    private void showInfoToast(String msg, int duration) {
        if (getActivity()!=null) {
            if (getActivity().getWindow().getDecorView().isShown()) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getActivity(), msg, duration).show();
                }
            }
        }
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getActivity()!=null) {
            if (getActivity().getWindow().getDecorView().isShown()) {
                if (!TextUtils.isEmpty(msg)) {
                    Snackbar.make(view.findViewById(R.id.invite_code_snackbarLy), msg, duration).show();
                }
            }
        }
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("eventId", eventId);
        outState.putBoolean("isSending", isSending);
        outState.putInt("preventBackLimit", preventBackLimit);
    }
}
