package com.underdog.hydrate;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.DateUtil;
import com.underdog.hydrate.util.Log;

import java.util.Calendar;

public class SetupActivity extends AppCompatActivity {

    private HydrateDAO hydrateDAO;
    private SharedPreferences sharedPreferences;
    private boolean mlSelected = true;

//    private SetupActivity.ApplyChanges applyChanges;

    private Button startTime;
    private Button endTime;
    private Spinner cupSpinner;
    private Button interval;
    private EditText targetEdit;
    private EditText username;
    private Button save;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        hydrateDAO = HydrateDAO.getInstance();

        username = (EditText) findViewById(R.id.setupUsernameEdit);
        targetEdit = (EditText) findViewById(R.id.setupTargetEdit);
        save = (Button) findViewById(R.id.setupSubmit);


        targetEdit.setText(hydrateDAO.getTodayTarget(getApplicationContext()) / 1000 + "");
        targetEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    save.setEnabled(false);
                    save.animate().alpha(0.6f);
                } else {
                    save.setEnabled(true);
                    save.animate().alpha(1f);
                    setInterval();
                }
            }
        });


        startTime = (Button) findViewById(R.id.setupStartTimeEdit);
        startTime.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                String target = targetEdit.getText().toString();
                if (target.isEmpty()) {
                    Toast.makeText(SetupActivity.this, R.string.target_not_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                String value = startTime.getText().toString();
                int[] hoursMins = DateUtil.getInstance().getHoursAndMins(value);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SetupActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTime.setText((hourOfDay > 9 ? hourOfDay : ("0" + hourOfDay)) + ":" + (minute > 9 ? minute : ("0" + minute)));
                        setInterval();
                    }
                }, hoursMins[0], hoursMins[1], true);
                timePickerDialog.show();
            }
        });

        endTime = (Button) findViewById(R.id.setupEndTimeEdit);
        endTime.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                String target = targetEdit.getText().toString();
                if (target.isEmpty()) {
                    Toast.makeText(SetupActivity.this, R.string.target_not_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                String value = endTime.getText().toString();
                int[] hoursMins = DateUtil.getInstance().getHoursAndMins(value);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SetupActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTime.setText((hourOfDay > 9 ? hourOfDay : ("0" + hourOfDay)) + ":" + (minute > 9 ? minute : ("0" + minute)));
                        setInterval();
                    }
                }, hoursMins[0], hoursMins[1], true);
                timePickerDialog.show();
            }
        });

        cupSpinner = (Spinner)

                findViewById(R.id.setupCupEdit);

        setCupOptions();
        cupSpinner.setSelection(3);
        cupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                setInterval();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        interval = (Button)

                findViewById(R.id.setupIntervalEdit);
        interval.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Toast.makeText(SetupActivity.this, R.string.interval_in_settings, Toast.LENGTH_SHORT).show();
            }
        });

        save.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                SetupActivity.ApplyChanges applyChanges = new SetupActivity.ApplyChanges();
                applyChanges.execute(username.getText().toString(), mlSelected,
                        targetEdit.getText().toString(),
                        startTime.getText().toString(), endTime.getText().toString(),
                        cupSpinner.getSelectedItem().toString(), interval.getText().toString());
            }
        });

//        Button skip = (Button) findViewById(R.id.setupSkip);
//        skip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            }
//        });
    }

    @Override
    protected void onDestroy() {
//        if (applyChanges != null) {
//            applyChanges.cancel(true);
//        }
        super.onDestroy();
    }

    public void onMetricSelected(View view) {

        TextView targetView = (TextView) findViewById(R.id.setupTarget);

        if (view.getId() == R.id.radio_ml && !mlSelected) {
            mlSelected = true;
            targetView.setText(R.string.targetQuantityTitleMl);
            targetEdit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            targetEdit.setText(hydrateDAO.getTodayTarget(getApplicationContext()) / 1000 + "");
        } else {
            mlSelected = false;
            targetView.setText(R.string.targetQuantityTitleOz);
            targetEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
            targetEdit.setText((int) Math.round(hydrateDAO.getTodayTarget(getApplicationContext()) * 0.033814) + "");
        }
        setCupOptions();
        setInterval();
    }

    private void setInterval() {
        int[] selectedStartTime = DateUtil.getInstance().getHoursAndMins(startTime.getText().toString());
        int[] selectedEndTime = DateUtil.getInstance().getHoursAndMins(endTime.getText().toString());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedEndTime[0]);
        calendar.set(Calendar.MINUTE, selectedEndTime[1]);
        long endTimeInMillis = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, selectedStartTime[0]);
        calendar.set(Calendar.MINUTE, selectedStartTime[1]);
        long startTimeInMillis = calendar.getTimeInMillis();

        long timeAlive = endTimeInMillis - startTimeInMillis;

        String quantity = (String) cupSpinner.getSelectedItem();
        double cupSelected = 250;
        if (quantity != null) {
            cupSelected = Double.valueOf(quantity);
        }
        double target = Double.valueOf(targetEdit.getText().toString());
        if (mlSelected)
            target *= 1000;

        long intervalInMins = (long) ((timeAlive * (cupSelected) / target) / 60000);
        String toastMessage = getResources().getString(R.string.interval_set_toast);
        String mins = getResources().getString(R.string.mins);
//        Toast.makeText(this, toastMessage + " - " + intervalInMins + " " + mins, Toast.LENGTH_SHORT).show();
        interval.setText(intervalInMins + " " + mins);

    }

    private void setCupOptions() {
        ArrayAdapter<CharSequence> adapter = null;

        if (mlSelected)
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.glass_options_ml, android.R.layout.simple_spinner_item);
        else
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.glass_options_oz, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        cupSpinner.setAdapter(adapter);
    }

    public class ApplyChanges extends AsyncTask<Object, Void, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SetupActivity.this);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        @Override
        protected Void doInBackground(Object... params) {
            Log.i("ApplyChanges", params[0] + ", " + params[1] + ", " + params[2] + ", " + params[3] + ", " + params[4] + ", " + params[5] + ", " + params[6]);
            String username = params[0].toString();
            boolean isML = (boolean) params[1];
            Double target = Double.valueOf(params[2].toString());
            String startTimeString = params[3].toString();
            int[] startTime = DateUtil.getInstance().getHoursAndMins(startTimeString);
            String endTimeString = params[4].toString();
            int[] endTime = DateUtil.getInstance().getHoursAndMins(endTimeString);
            String cupQuantity = params[5].toString();
            int interval = Integer.parseInt(params[6].toString().split(" ")[0]);
            SharedPreferences sharedPreferences;

            if (isCancelled())
                return null;

            if (isML)
                target *= 1000;

            if (HydrateDAO.getInstance().applyInitialSetupChanges(target, startTime[0], startTime[1], endTime[0], endTime[1], interval, isML, getApplicationContext())) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.FIRST_RUN, false);
                if (isML)
                    editor.putString(getResources().getString(R.string.key_metric), getResources().getString(R.string.milliliter));
                else
                    editor.putString(getResources().getString(R.string.key_metric), getResources().getString(R.string.us_oz));
                editor.putString(getResources().getString(R.string.key_glass_quantity), cupQuantity);
                editor.putString(getResources().getString(R.string.key_user_name), username);
                editor.apply();

                AlarmReceiver alarmReceiver = new AlarmReceiver();
                alarmReceiver.setNextAlarm(getApplicationContext());
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }
}
