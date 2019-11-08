package com.sse.iamhere.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;

import com.sse.iamhere.AuthenticationActivity;
import com.sse.iamhere.OnboardActivity;
import com.sse.iamhere.R;
import com.sse.iamhere.Utils.Constants;

public class RoleSetupDialog_depreciated extends AppCompatDialogFragment {
    private Constants.Role selectedRole;

    public RoleSetupDialog_depreciated(Constants.Role selectedRole) {
        this.selectedRole = selectedRole;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedRole = Constants.Role.valueOf(savedInstanceState.getString("selectedRole"));
        }

        if (!(getActivity() instanceof AuthenticationActivity)) {
            throw new RuntimeException("RoleSetupDialog_depreciated is setup to only run when called from AuthenticationActivity");
        }

        View view = View.inflate(getActivity(), R.layout.dialog_setup_depreciated, null);
        initViews(view);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle("Roles");
        builder.setPositiveButton("Done", (dialog, which) -> {
            if (getActivity() != null) {
                ((AuthenticationActivity)getActivity()).onRoleSetupDismiss(selectedRole);
            }
        });
        return builder.create();

    }

    private void initViews(View view) {
        Button helpMeChoose = view.findViewById(R.id.setup_helpBtn);
        helpMeChoose.setOnClickListener(v -> {
            showOnboardActivity();
        });

        LinearLayout setup_managerLy = view.findViewById(R.id.setup_managerLy);
        LinearLayout setup_hostLy = view.findViewById(R.id.setup_hostLy);
        LinearLayout setup_attendeeLy = view.findViewById(R.id.setup_attendeeLy);


        setup_managerLy.setOnClickListener(v -> {
            selectedRole = Constants.Role.MANAGER;
            setBackgroundColor(v, R.drawable.backg_rounded_rect_card_green);
            setBackgroundColor(setup_hostLy, R.drawable.backg_rounded_rect_card_grey);
            setBackgroundColor(setup_attendeeLy, R.drawable.backg_rounded_rect_card_grey);
        });

        setup_hostLy.setOnClickListener(v -> {
            selectedRole = Constants.Role.HOST;
            setBackgroundColor(v, R.drawable.backg_rounded_rect_card_green);
            setBackgroundColor(setup_managerLy, R.drawable.backg_rounded_rect_card_grey);
            setBackgroundColor(setup_attendeeLy, R.drawable.backg_rounded_rect_card_grey);
        });

        setup_attendeeLy.setOnClickListener(v -> {
            selectedRole = Constants.Role.ATTENDEE;
            setBackgroundColor(v, R.drawable.backg_rounded_rect_card_green);
            setBackgroundColor(setup_managerLy, R.drawable.backg_rounded_rect_card_grey);
            setBackgroundColor(setup_hostLy, R.drawable.backg_rounded_rect_card_grey);
        });
    }

    private void setBackgroundColor(View v, int color) {
        if (getActivity()==null) {
            Log.e("RoleSetupDialog", "getActivity() is null setBackgroundColor");
            return;
        }

        v.setBackground(ContextCompat.getDrawable(getActivity(), color));
    }

    private void showOnboardActivity() {
        Intent intent = new Intent(getActivity(), OnboardActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("selectedRole", selectedRole.name());
        super.onSaveInstanceState(outState);
    }
}
