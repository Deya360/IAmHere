package com.sse.iamhere.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.InviteCodeAdapter;
import com.sse.iamhere.HomeActivity;
import com.sse.iamhere.R;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.CustomSwipeToRefresh;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class InviteCodesDialog extends AppCompatDialogFragment {
    private CustomSwipeToRefresh mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;
    private ShimmerFrameLayout placeholderLy;
    private FrameLayout contentLy;
    private InviteCodeAdapter adapter;
    private View view;

    private Parcelable savedRecyclerLayoutState;
    private boolean dialogInputInviteCodesDismissed = true;
    private boolean isDataRefreshing = false;
    private boolean dataChanged = false;

    public InviteCodesDialog() { }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (!(getActivity() instanceof HomeActivity)) {
            throw new RuntimeException("InviteCodesDialog is setup to only run when called from HomeActivity");
        }

        view = View.inflate(getActivity(), R.layout.dialog_invite_code, null);

        if (savedInstanceState!=null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");
            dialogInputInviteCodesDismissed = savedInstanceState.getBoolean("dialogInputInviteCodesDismissed");
            dataChanged = savedInstanceState.getBoolean("dataChanged", dataChanged);
        }

        setupUI();
        return createDialog();
    }

    private void setupUI() {
        placeholderLy = view.findViewById(R.id.invite_code_placeholderLy);
        contentLy = view.findViewById(R.id.invite_code_contentLy);

        // Setup of swipe to refresh layout
        mSwipeRefreshLayout = view.findViewById(R.id.invite_code_swipe_refreshLy);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isDataRefreshing) {
                isDataRefreshing = true;
                loadInviteCodes();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        //Setup of recycler view
        recyclerView = view.findViewById(R.id.invite_code_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new InviteCodeAdapter();
        recyclerView.setAdapter(adapter);

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
                removeInviteCode(adapter.getInviteCodes().get(pos));
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
            Constants.Role role = PreferencesUtil.getRole(getActivity(), Constants.Role.NONE);
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
    }

    private AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle(getString(R.string.nav_invite_codes_title))
            .setView(view)
            .setPositiveButton(getString(R.string.invite_code_doneBtn_label), (dialog, which) -> {
                finishDialog();
            })
            .setOnDismissListener(dialog -> {
                finishDialog();
            });

        return builder.create();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadInviteCodes();
    }

    private void loadInviteCodes() {
        showShimmer();
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onGetCodeWordsSuccess(List<String> inviteCodes) {
                    super.onGetCodeWordsSuccess(inviteCodes);
                    Collections.sort(inviteCodes);
                    adapter.setInviteCodes(inviteCodes);
//                    measureSTRLHeight(inviteCodes.size());
                    if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);

                    if (inviteCodes.isEmpty()) {
                        recyclerView.setPlaceHolderView(view.findViewById(R.id.invite_code_empty_view));
                    }
                }

                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    isDataRefreshing = false;
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (failed) {
                        stopShimmer();
                        if (failCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }

                    } else {
                        hideShimmer();
                    }
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);

        Constants.Role role = PreferencesUtil.getRole(getActivity(), Constants.Role.NONE);
        switch (role) {
            case ATTENDEE: rb.callRequest(rb::attendeeGetCodeWords); break;
            case HOST: rb.callRequest(rb::hostGetCodeWords); break;
            default:
                throw new RuntimeException("Invite Code Dialog can only be used for Roles: Attendee, Host");
        }
    }

    private void addInviteCode(String code) {
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    if (failed) {
                        if (failCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }

                    } else {
                        dataChanged = true;
                        showInfoSnackbar(getString(R.string.msg_invite_code_added), 3000);
                        loadInviteCodes();
                    }
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);

        Constants.Role role = PreferencesUtil.getRole(getActivity(), Constants.Role.NONE);
        switch (role) {
            case ATTENDEE: rb.callRequest(() -> rb.attendeeSetCodeWords(new ArrayList<>(Collections.singletonList(code)))); break;
            case HOST: rb.callRequest(() -> rb.hostSetCodeWords(new ArrayList<>(Collections.singletonList(code)))); break;
            default:
                throw new RuntimeException("Invite Code Dialog can only be used for Roles: Attendee, Host");
        }
    }

    private void removeInviteCode(String code) {
        RequestBuilder rb = new RequestBuilder()
                .setCallback(new RequestsCallback() {
                    @Override
                    public void onComplete(boolean failed, Integer failCode) {
                        if (failed) {
                            if (failCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                                showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                            } else {
                                showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                            }
                        } else {
                            showInfoSnackbar(getString(R.string.msg_invite_code_removed), 3000);
                            loadInviteCodes();
                            dataChanged = true;
                        }
                    }
                });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);

        Constants.Role role = PreferencesUtil.getRole(getActivity(), Constants.Role.NONE);
        switch (role) {
            case ATTENDEE: rb.callRequest(() -> rb.attendeeRemoveCodeWords(new ArrayList<>(Collections.singletonList(code)))); break;
            case HOST: rb.callRequest(() -> rb.hostRemoveCodeWords(new ArrayList<>(Collections.singletonList(code)))); break;
            default:
                throw new RuntimeException("Invite Code Dialog can only be used for Roles: Attendee, Host");
        }
    }

//    private int measureSTRLHeight(int itemCount) {
//        int minHeight = dpToPx(120);
//        int itemHeight = dpToPx(36);
//        mSwipeRefreshLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        int maxHeight = mSwipeRefreshLayout.getMeasuredHeight();
//
//        int height = minHeight;
//        while (height < maxHeight && itemCount>0) {
//            height += itemHeight;
//            itemCount--;
//        }
//
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mSwipeRefreshLayout.getLayoutParams();
//        params.height = height;
//        mSwipeRefreshLayout.setLayoutParams(params);
//        return minHeight;
//    }
//
//    private int dpToPx(int dp) {
//        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
//        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//    }

    private void showShimmer() {
        placeholderLy.startShimmer();
        placeholderLy.setVisibility(View.VISIBLE);
        contentLy.setVisibility(View.GONE);
    }

    private void stopShimmer() {
        new Handler().postDelayed(placeholderLy::stopShimmer,2500);
    }

    private void hideShimmer() {
        placeholderLy.setVisibility(View.GONE);
        contentLy.setVisibility(View.VISIBLE);
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

    private InputInviteCodeDialog.InputIndividualDialogListener inputInviteCodeDialogListener =
            new InputInviteCodeDialog.InputIndividualDialogListener() {
                @Override
                public void onPositiveButton(String name) {
                    if (!adapter.getInviteCodes().contains(name)) {
                        addInviteCode(name);

                    } else {
                        showInfoSnackbar(getString(R.string.msg_invite_code_duplicate), 4000);
                    }
                }

                @Override
                public void onDismiss() {
                    dialogInputInviteCodesDismissed = true;
                }
            };

    private void finishDialog() {
        if (getActivity()!=null && getActivity() instanceof HomeActivity) {
            ((HomeActivity)getActivity()).onInviteCodeDialogDismissed(dataChanged);

        } else {
            Log.e("InviteCodesDialog", "getActivity() is null finishDialog");
        }
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
        super.onSaveInstanceState(outState);

        RecyclerView.LayoutManager mListState = recyclerView.getLayoutManager();
        if (mListState!=null) {
            outState.putParcelable("mListState", mListState.onSaveInstanceState());
        }
        outState.putBoolean("dialogInputInviteCodesDismissed",dialogInputInviteCodesDismissed);
        outState.putBoolean("dataChanged", dataChanged);
    }
}
