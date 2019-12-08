package com.sse.iamhere.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.sse.iamhere.R;


public class InputInviteCodeDialog extends AppCompatDialogFragment {
    private EditText inputEt;
    private String name = "";

    public InputInviteCodeDialog() { }

    private InputIndividualDialogListener inputIndividualDialogListener;
    public interface InputIndividualDialogListener {
        void onPositiveButton(String name);
        void onDismiss();
    }

    public void setListener(InputIndividualDialogListener iidl) {
        this.inputIndividualDialogListener = iidl;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState!=null) {
            name = savedInstanceState.getString("name");
        }

        View inputInvolvedView = View.inflate(getActivity(), R.layout.dialog_invite_code_input, null);

        inputEt = inputInvolvedView.findViewById(R.id.input_invite_code_nameEt);
        inputEt.setText(name);
        inputEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) {
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        inputEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onOkPressed();
                dismiss();
            }
            return false;
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.input_invite_code_title))
            .setView(inputInvolvedView)
            .setPositiveButton(getString(R.string.input_invite_code_okBtn), (dialog, which) -> {
                onOkPressed();
            })
            .setNegativeButton(getString(R.string.input_invite_code_cancelBtn),null)
            .setOnDismissListener(dialog -> {
                inputIndividualDialogListener.onDismiss();
            }
        );

        return builder.create();
    }

    private void onOkPressed() {
        if (!inputEt.getText().toString().isEmpty()) {
            if (getActivity()!=null) {
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
            inputIndividualDialogListener.onPositiveButton(inputEt.getText().toString());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("name", inputEt.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
