package com.sse.iamhere;


import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sse.iamhere.Adapters.HostSlimAdapter;
import com.sse.iamhere.Adapters.PartySlimAdapter;
import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.impl.ResourcesTimeFormat;
import org.ocpsoft.prettytime.units.Day;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;
import static com.sse.iamhere.Utils.TextFormatter.formatAsHTML;

public class EventDetailsFrag extends Fragment {
    private @Nullable SubjectData eventData;

    private RecyclerView hostsRv;
    private RecyclerView partiesRv;
    private TextView hostsTv;
    private TextView partiesTv;

    private HostSlimAdapter hostSlimAdapter;
    private PartySlimAdapter partySlimAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState!=null){
            eventData = savedInstanceState.getParcelable("eventData");

        } else {
            Bundle extras = getArguments();
            if (extras!=null) {
                eventData = extras.getParcelable("eventData");
            }
        }

        setupUI();
        populateEventDetails();
    }

    private void setupUI() {
        hostsTv = getActivity().findViewById(R.id.event_details_hostsTv);
        hostsTv.setOnClickListener(new OnSingleClickListener() {
            boolean isCollapsed = false;
            @Override
            public void onSingleClick(View v) {
                if (isCollapsed) expandSection(hostsRv, hostsTv);
                else collapseSection(hostsRv, hostsTv);
                isCollapsed = !isCollapsed;
            }
        });

        partiesTv = getActivity().findViewById(R.id.event_details_partiesTv);
        partiesTv.setOnClickListener(new OnSingleClickListener() {
            boolean isCollapsed = false;
            @Override
            public void onSingleClick(View v) {
                if (isCollapsed) expandSection(partiesRv, partiesTv);
                else collapseSection(partiesRv, partiesTv);
                isCollapsed = !isCollapsed;
            }
        });

        // Initial setup of recycler view's
        hostsRv = getActivity().findViewById(R.id.event_details_hostsRv);
        hostsRv.setLayoutManager(new LinearLayoutManager(getContext()));

        hostSlimAdapter = new HostSlimAdapter(this::showUserDetailsDialog);
        hostsRv.setAdapter(hostSlimAdapter);

        partiesRv = getActivity().findViewById(R.id.event_details_partiesRv);
        partiesRv.setLayoutManager(new LinearLayoutManager(getContext()));

        partySlimAdapter = new PartySlimAdapter(partyId -> {
//            showPartyInfoDialog(); //todo;
        });
        partiesRv.setAdapter(partySlimAdapter);
    }

    private void populateEventDetails() {
        if (getActivity()==null) {
            Log.e("EventDetailsFrag", "Activity is null populateEventDetails");
            return;
        }
        TextView startDateTv = getActivity().findViewById(R.id.event_details_startDateTv);
        TextView finishDateTv = getActivity().findViewById(R.id.event_details_finishDateTv);
        TextView inviteCodeTv = getActivity().findViewById(R.id.event_details_inviteCodeTv);

        if (eventData !=null) {
            startDateTv.setText(formatAsHTML(
                    String.format("<b>%s:</b><br/>%s <i>(%s)</i>",
                                  getString(R.string.event_details_startDateTv_label),
                                  formatDate(eventData.getStartDate()),
                                  getRelativeDays(eventData.getStartDate()))),
                                TextView.BufferType.SPANNABLE);

            finishDateTv.setText(formatAsHTML(
                    String.format("<b>%s:</b><br/>%s <i>(%s)</i>",
                                  getString(R.string.event_details_finishDateTv_label),
                                  formatDate(eventData.getFinishDate()),
                                  getRelativeDays(eventData.getFinishDate()))),
                                 TextView.BufferType.SPANNABLE);

            hostsTv.setText(getString(R.string.event_details_hostsTv_label));
            hostSlimAdapter.setHosts(eventData.getHosts());

            partiesTv.setText(getString(R.string.event_details_partiesTv_label));
            partySlimAdapter.setParties(eventData.getParties());

            inviteCodeTv.setText(formatAsHTML(
                    String.format("<b>%s:</b> %s",
                                  getString(R.string.event_details_inviteCodeTv_label),
                                  eventData.getCodeWord())),
                                 TextView.BufferType.SPANNABLE);
        }
    }

    private String formatDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        return sdf.format(new Date(date));
    }
    private String getRelativeDays(long date) {
        PrettyTime p = new PrettyTime(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        p.clearUnits();
        p.registerUnit(new Day(), new ResourcesTimeFormat(new Day()));
        return  p.format(new Date(date));
    }

    public void onParentRefresh(SubjectData eventData) {
        this.eventData = eventData;
        populateEventDetails();
    }

    private void showUserDetailsDialog(int hostId) {
        RequestBuilder rb = new RequestBuilder()
                .setCallback(new RequestsCallback() {
                    @Override
                    public void onGetUser(CredentialData userData) {
                        super.onGetUser(userData);

                        String name = userData.getName();
                        String email = userData.getEmail();

                        if (!TextUtils.isEmpty(name) && !name.equals("null")) {
                            String msg = String.format("<b>%s:</b> %s", getString(R.string.user_details_name), name);

                            if (!TextUtils.isEmpty(email) && !email.equals("null")) {
                                msg += String.format("<br/><b>%s:</b> %s", getString(R.string.user_details_email), email);
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(getString(R.string.user_details_title));
                            builder.setMessage(formatAsHTML(msg));
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.show();
                        }
                    }
                });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);

        Constants.Role role = PreferencesUtil.getRole(getActivity(), Constants.Role.NONE);
        switch (role) {
            case ATTENDEE: rb.callRequest(() -> { rb.attendeeGetUserById(hostId, "ACCOUNT_HOST"); }); break;
            case HOST: rb.callRequest(() -> { rb.hostGetUserById(hostId, "ACCOUNT_HOST"); }); break;
            default:
                throw new RuntimeException("Invite Code Dialog can only be used for Roles: Attendee, Host");
        }
    }

    private void expandSection(final View v, final TextView headerTv) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 2.5dp/ms
        a.setDuration(((int)((targetHeight / v.getContext().getResources().getDisplayMetrics().density)* 2.5)));
        v.startAnimation(a);

        // animate arrow head
        for(Drawable drawable: headerTv.getCompoundDrawables()) {
            if(drawable == null) continue;
            ObjectAnimator anim = ObjectAnimator.ofInt(drawable, "level", 10000, 0);
            anim.start();
            break;
        }
    }

    private void collapseSection(final View v, final TextView headerTv) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 2.5dp/ms
        a.setDuration(((int)((initialHeight / v.getContext().getResources().getDisplayMetrics().density)* 2.5)));
        v.startAnimation(a);

        // animate arrow head
        for(Drawable drawable: headerTv.getCompoundDrawables()) {
            if(drawable == null) continue;
            ObjectAnimator anim = ObjectAnimator.ofInt(drawable, "level", 0, 10000);
            anim.start();
            break;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("eventData", eventData);
    }
}
