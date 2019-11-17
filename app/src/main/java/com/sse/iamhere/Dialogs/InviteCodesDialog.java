package com.sse.iamhere.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sse.iamhere.Adapters.InviteCodeAdapter;
import com.sse.iamhere.R;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Utils.Constants;

import java.util.ArrayList;

public class InviteCodesDialog extends AppCompatDialogFragment {
    private ArrayList<String> inviteCodes = new ArrayList<>();
    private boolean dialogInputInviteCodesDismissed = true;
    private Constants.Role role;

    private EmptySupportedRecyclerView recyclerView;
    private TextView placeHolderView;
    private InviteCodeAdapter adapter;


    private InviteCodesDialogListener inviteCodesDialogListener;
    public interface InviteCodesDialogListener {
        void onPositiveButton(ArrayList<String> individuals);
        void onDismiss();
    }

    /*
     * DO NOT USE THIS CONSTRUCTOR, it's here for a technical reason
     * */
    public InviteCodesDialog() { }

    public InviteCodesDialog(Constants.Role role, InviteCodesDialogListener listener) {
        this.role = role;
        this.inviteCodesDialogListener = listener;
    }

    private InputInviteCodeDialog.InputIndividualDialogListener inputInviteCodeDialogListener =
        new InputInviteCodeDialog.InputIndividualDialogListener() {
            @Override
            public void onPositiveButton(String name) {
                inviteCodes.add(name);
                adapter.setInviteCodes(inviteCodes);
            }

            @Override
            public void onDismiss() {
                dialogInputInviteCodesDismissed = true;
            }
        };


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState!=null) {
            inviteCodes = savedInstanceState.getStringArrayList("inviteCodes");
            role = Constants.Role.values()[savedInstanceState.getInt("role")];
        }

        View view = View.inflate(getActivity(), R.layout.dialog_invite_code, null);

        recyclerView = view.findViewById(R.id.invite_code_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new InviteCodeAdapter();
        adapter.setInviteCodes(inviteCodes);
        recyclerView.setAdapter(adapter);
        placeHolderView = view.findViewById(R.id.invite_code_empty_view);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            private final Drawable icon =
                    ContextCompat.getDrawable(getActivity(), R.drawable.ic_trash_white_24dp);
            private final ColorDrawable background =
                    new ColorDrawable(Color.parseColor("#D32F2F"));
            private boolean vibrated = false;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (getActivity() == null) {
                    Log.e("InviteCodesDialog", "Activity is null onViewCreated:onSwiped");
                    return;
                }

                int pos = viewHolder.getAdapterPosition();
                adapter.removeInviteCodeAt(pos);

                if (inviteCodes.isEmpty()) {
                    recyclerView.setPlaceHolderView(placeHolderView);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

                if (dX > 0) { // Swiping to the right
                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                            itemView.getBottom());
                } else if (dX < 0) { // Swiping to the left
                    background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else { // view is unSwiped
                    background.setBounds(0, 0, 0, 0);
                }
                background.draw(c);

                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + icon.getIntrinsicHeight();

                if (dX > 0) { // Swiping to the right
                    int iconLeft = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                    if (!vibrated && dX>iconLeft) {
                        vibrated = true;
                        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);  // <<--- Depreciated, refers to this,
                        v.vibrate(75);
                    }
                    int iconRight = itemView.getLeft() + iconMargin;
                    icon.setBounds(iconRight, iconTop, iconLeft, iconBottom);


                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                            itemView.getBottom());
                } else if (dX < 0) { // Swiping to the left
                    int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else { // view is unSwiped
                    background.setBounds(0, 0, 0, 0);
                    vibrated = false;
                }

                background.draw(c);
                icon.draw(c);
            }
        }).attachToRecyclerView(recyclerView);

        @Nullable
        TextView infoTv = view.findViewById(R.id.invite_code_infoTv);
        if (infoTv!=null) { //infoTv can be null in landscape mode
            if (role == Constants.Role.ATTENDEE) {
                infoTv.setText(getString(R.string.invite_code_infoTv_attendee_label));

            } else if (role == Constants.Role.HOST) {
                infoTv.setText(getString(R.string.invite_code_infoTv_host_label));

            } else {
                throw new RuntimeException("Invite Code Dialog can only be used for Roles: Attendee, Host");
            }
        }

        Button addBtn = view.findViewById(R.id.invite_code_addBtn);
        addBtn.setOnClickListener(v -> showInputInviteCodeDialog());

        // Display dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle("Invite Codes")
            .setView(view)
            .setPositiveButton("OK", (dialog, which) -> {
                inviteCodesDialogListener.onPositiveButton(inviteCodes);
            })
            .setNegativeButton("Cancel",null)
            .setOnDismissListener(dialog -> {
                inviteCodesDialogListener.onDismiss();
            });

        return builder.create();
    }

    private void loadInviteCodes() {
        if (role==Constants.Role.ATTENDEE) {
//            AsyncTask.execute(() -> new AuthRequestBuilder(getActivity())
//                .attachToken(Constants.TOKEN_ACCESS)
//                .setCallback(new RequestsCallback() {
//                    @Override
//                    public void onAttendeePartiesListSuccess(Set<String> codeWords) {
//                        adapter.setInviteCodes(codeWords);
//                        if (codeWords.isEmpty()) {
//
//                            recyclerView.setPlaceHolderView(placeHolderView);
//                        } else {
//
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(int errorCode) {
//
//                    }
//                }))
//                .attendeeGetCodeWords();

        } else if (role==Constants.Role.MANAGER) {


        } else {
            throw new RuntimeException("Invite Code Dialog can only be used for Roles: Attendee, Host");
        }
    }

    private void showInputInviteCodeDialog() {
        if (getActivity() == null) {
            Log.e("InviteCodesDialog", "Activity is null showInputInviteCodeDialog");
            return;
        }
        InputInviteCodeDialog inputInviteCodeDialog = new InputInviteCodeDialog();
        inputInviteCodeDialog.setListener(inputInviteCodeDialogListener);
        inputInviteCodeDialog.show(getActivity().getSupportFragmentManager(),
                        getString(R.string.fragment_input_invite_code_dialog_tag));

        dialogInputInviteCodesDismissed = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity()==null) {
            Log.e("InviteCodesDialog", "Activity is null onResume");

            return;
        }

        if (!dialogInputInviteCodesDismissed) {
            // re-set the listener, (in case screen was rotated, (listeners can't be saved into instance state and are lost on screen rotate))
            Fragment frag = getActivity().getSupportFragmentManager()
                    .findFragmentByTag(getString(R.string.fragment_input_invite_code_dialog_tag));
            if (frag!=null) {
                ((InputInviteCodeDialog)frag).setListener(inputInviteCodeDialogListener);
            }
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList("inviteCodes", inviteCodes);
        outState.putInt("role", role.toIdx());
        super.onSaveInstanceState(outState);
    }
}
