package com.pbd.ifttw.ui.main;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pbd.ifttw.MainActivity;
import com.pbd.ifttw.NewActionModuleActivity;
import com.pbd.ifttw.NewConditionModuleActivity;
import com.pbd.ifttw.R;
import com.pbd.ifttw.service.SensorBackgroundService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NewRoutineFragment extends Fragment {
    private String LOG_TAG = "New Routine";
    public static final int CONDITION_REQUEST = 1;
    public static final int ACTION_REQUEST = 2;
    public static final String CONDITION_TYPE = "condition_type";
    public static final String CONDITION_VALUE = "condition_value";
    public static final String ACTION_TYPE = "action_type";
    public static final String ACTION_VALUE = "action_value";
    private String condition_type = "NONE";
    private String action_type = "NONE";
    private Integer condition_value = null;
    private Integer action_value = null;
    private String routine_name = null;

    private View root;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_new_routine, container, false);
        Button thisButton = root.findViewById(R.id.thisButton);
        thisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNewConditionModuleActivity(v);
            }
        });
        Button whatButton = root.findViewById(R.id.whatButton);
        whatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNewActionModuleActivity(v);
            }
        });
        FloatingActionButton addRoutine = root.findViewById(R.id.addRoutine);
        addRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRoutine(v);
            }
        });
        MainActivity parent = (MainActivity) getActivity();
        setButtonText(parent);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity parent = (MainActivity) getActivity();
        setButtonText(parent);
    }

    private void launchNewConditionModuleActivity(@Nullable View view) {
        Log.d(LOG_TAG, "This button Clicked!");
        Intent intent = new Intent(getActivity(), NewConditionModuleActivity.class);
        Activity parent = getActivity();
        if (parent != null) {
            parent.startActivityForResult(intent, CONDITION_REQUEST);
        } else {
            Log.d("New Routine Fragment", "Can't getActivity()");
        }
    }

    private void launchNewActionModuleActivity(@Nullable View view) {
        Log.d(LOG_TAG, "What button Clicked!");
        Intent intent = new Intent(getActivity(), NewActionModuleActivity.class);
        Activity parent = getActivity();
        if (parent != null) {
            parent.startActivityForResult(intent, ACTION_REQUEST);
        } else {
            Log.d("New Routine Fragment", "Can't getActivity()");
        }
    }

    private void setButtonText(@Nullable MainActivity parent) {
        // Set text of main activity
        if (parent != null) {
            // Handle THIS button text
            Bundle condition_bundle = parent.getCondition_bundle();
            Button thisButton = root.findViewById(R.id.thisButton);
            if (condition_bundle != null) {
                condition_type = condition_bundle.getString(CONDITION_TYPE, "NONE");
                if (!condition_type.equals("NONE")) {
                    // Handle Proximity module
                    if (condition_type.equals("proximity")) {
                        String valString;
                        condition_value = condition_bundle.getInt(CONDITION_VALUE, -1);
                        switch (condition_value) {
                            case 0:
                                valString = "Proximity is Near";
                                break;
                            case 1:
                                valString = "Proximity is Far";
                                break;
                            default:
                                valString = "SOMETHING IS WRONG";
                        }
                        thisButton.setText(valString);
                    }
                }
            }
            // Handle WHAT Button
            Bundle action_bundle = parent.getAction_bundle();
            Button whatButton = root.findViewById(R.id.whatButton);
            if (action_bundle != null) {
                action_type = action_bundle.getString(ACTION_TYPE, "NONE");
                if (!action_type.equals("NONE")) {
                    //Handle WiFi module
                    if (action_type.equals("wifi")) {
                        String valString;
                        action_value = action_bundle.getInt(ACTION_VALUE, -1);
                        switch (action_value) {
                            case 0:
                                valString = "Turn Wifi Off";
                                break;
                            case 1:
                                valString = "Turn Wifi On";
                                break;
                            default:
                                valString = "SOMETHING IS WRONG";
                        }
                        whatButton.setText(valString);
                    }
                }
            }
        } else {
            Log.d("New Routine Fragment", "Parent is Null!");
        }
    }

    private void addRoutine(View v) {
        if (!condition_type.equals("NONE") && !action_type.equals("NONE")) {
            if (condition_type.equals("proximity")) {
                addSensorAlarmManager(v);
            }
        }
    }

    private void addSensorAlarmManager(View v) {
        if (getActivity() != null) {
            // get scheduler and prepare intent
            AlarmManager scheduler = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            // add some extras for config
            Bundle args = new Bundle();
            args.putString(CONDITION_TYPE, condition_type);
            args.putString(ACTION_TYPE, action_type);
            args.putInt(CONDITION_VALUE, condition_value);
            args.putInt(ACTION_VALUE, action_value);
            if (condition_value == 0) {
                args.putFloat(SensorBackgroundService.KEY_THRESHOLD_MIN_VALUE, 1);
            } else {
                args.putFloat(SensorBackgroundService.KEY_THRESHOLD_MAX_VALUE, 7);
            }

            if (action_type.equals("wifi")) {
                Intent intent = new Intent(getActivity().getApplicationContext(), SensorBackgroundService.class);
                intent.putExtras(args);
                // try getting interval option
                long interval;
                interval = 1000L;

                PendingIntent scheduledIntent = PendingIntent.getService(getActivity().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // start the service
                scheduler.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval, scheduledIntent);
            }
        }
    }
}
