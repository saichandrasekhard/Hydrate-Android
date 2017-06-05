package com.underdog.hydrate;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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

import com.underdog.hydrate.database.HydrateDAO;

import java.util.Calendar;

public class SetupActivity extends AppCompatActivity {

    private HydrateDAO hydrateDAO;
    private SharedPreferences sharedPreferences;
    private boolean mlSelected = true;

    private Button startTime;
    private Button endTime;
    private Spinner cupSpinner;
    private Button interval;
    private EditText targetEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        hydrateDAO = HydrateDAO.getHydrateDAO();

        targetEdit = (EditText) findViewById(R.id.setupTargetEdit);


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
                setInterval();
            }
        });


        startTime = (Button) findViewById(R.id.setupStartTimeEdit);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = startTime.getText().toString();
                int[] hoursMins = getHoursAndMins(value);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SetupActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTime.setText(hourOfDay + ":" + minute);
                        setInterval();
                    }
                }, hoursMins[0], hoursMins[1], true);
                timePickerDialog.show();
            }
        });

        endTime = (Button) findViewById(R.id.setupEndTimeEdit);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = endTime.getText().toString();
                int[] hoursMins = getHoursAndMins(value);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SetupActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTime.setText(hourOfDay + ":" + minute);
                        setInterval();
                    }
                }, hoursMins[0], hoursMins[1], true);
                timePickerDialog.show();
            }
        });

        cupSpinner = (Spinner) findViewById(R.id.setupCupEdit);
        setCupOptions();
        cupSpinner.setSelection(3);
        cupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setInterval();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        interval = (Button) findViewById(R.id.setupIntervalEdit);
        interval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SetupActivity.this, R.string.interval_in_settings, Toast.LENGTH_SHORT).show();
            }
        });

        Button save = (Button) findViewById(R.id.setupSubmit);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new applyChanges().execute();
            }
        });
    }

    public void onMetricSelected(View view) {

        TextView targetView = (TextView) findViewById(R.id.setupTarget);

        if (view.getId() == R.id.radio_ml && !mlSelected) {
            mlSelected = true;
            targetView.setText(R.string.targetQuantityTitleMl);
            targetEdit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            targetEdit.setText(hydrateDAO.getTodayTarget(getApplicationContext()) / 1000 + "");
//            Toast.makeText(this, "ML", Toast.LENGTH_SHORT).show();
        } else {
            mlSelected = false;
            targetView.setText(R.string.targetQuantityTitleOz);
            targetEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
            targetEdit.setText((int) Math.round(hydrateDAO.getTodayTarget(getApplicationContext()) * 0.033814) + "");
        }
        setCupOptions();
        setInterval();
    }

    private int[] getHoursAndMins(String value) {
        String hourMins[] = value.split(":");
        int hour = Integer.parseInt(hourMins[0]);
        int mins = Integer.parseInt(hourMins[1]);
        return new int[]{hour, mins};
    }

    private void setInterval() {
        int[] selectedStartTime = getHoursAndMins(startTime.getText().toString());
        int[] selectedEndTime = getHoursAndMins(endTime.getText().toString());

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
        Toast.makeText(this, toastMessage + " - " + intervalInMins + " " + mins, Toast.LENGTH_SHORT).show();
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

    public class applyChanges extends AsyncTask<String, Void, Void> {

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
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
