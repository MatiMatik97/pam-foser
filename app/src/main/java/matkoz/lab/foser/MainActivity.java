package matkoz.lab.foser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button buttonStart, buttonStop, buttonRestart;
    private TextView textInfoService, textInfoSettings;
    private String message, seconds;
    private Boolean show_time, work, work_double, save_counter;
    private int timerValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonRestart = (Button) findViewById(R.id.buttonRestart);
        textInfoService = (TextView) findViewById(R.id.textInfoServiceState);
        textInfoSettings = (TextView) findViewById(R.id.textInfoSettings);

        // START
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickStart(view);
            }
        });

        // STOP
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickStop(view);
            }
        });

        // RESTART
        buttonRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRestart(view);
            }
        });

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.itemExit:
                finishAndRemoveTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void clickStart(View view) {
        MyForegroundService.counter = timerValue;

        getPreferences();

        Intent startIntent = new Intent(this, MyForegroundService.class);
        startIntent.putExtra(MyForegroundService.MESSAGE, message);
        startIntent.putExtra(MyForegroundService.TIME, show_time);
        startIntent.putExtra(MyForegroundService.WORK, work);
        startIntent.putExtra(MyForegroundService.WORK_DOUBLE, work_double);
        startIntent.putExtra(MyForegroundService.REFRESH_FREQ, seconds);
        startIntent.putExtra(MyForegroundService.SAVE_COUNTER, save_counter);

        ContextCompat.startForegroundService(this, startIntent);

        updateUI();
    }

    public void clickStop(View view) {
        if (save_counter) {
            timerValue = MyForegroundService.counter;
        } else {
            timerValue = 0;
        }

        Intent stopIntent = new Intent(this, MyForegroundService.class);
        stopService(stopIntent);

        updateUI();
    }

    public void clickRestart(View view) {
        clickStop(view);
        clickStart(view);
    }

    private String getPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        message = sharedPreferences.getString("message", "ForSer");
        show_time = sharedPreferences.getBoolean("show_time", true);
        work = sharedPreferences.getBoolean("sync", true);
        work_double = sharedPreferences.getBoolean("double", false);
        seconds = sharedPreferences.getString("refresh_freq", "2");
        save_counter = sharedPreferences.getBoolean("save_counter", false);

        return "Message: " + message + "\n"
                + "show_time: " + show_time.toString() + "\n"
                + "work: " + work.toString() + "\n"
                + "double: " + work_double.toString() + "\n"
                + "refresh_freq: " + seconds + "\n"
                + "save_counter: " + save_counter;
    }

    @SuppressWarnings("deprecation")
    private boolean isMyForegroundServiceRunning() {
        String myServiceName = MyForegroundService.class.getName();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            String runningServiceName = runningService.service.getClassName();
            if (runningServiceName.equals(myServiceName)) {
                return true;
            }
        }
        return false;
    }

    private void updateUI() {
        if (isMyForegroundServiceRunning()) {
            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);
            buttonRestart.setEnabled(true);
            textInfoService.setText(getString(R.string.info_service_running));
        } else {
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
            buttonRestart.setEnabled(false);
            textInfoService.setText(getString(R.string.info_service_not_running));
        }

        textInfoSettings.setText(getPreferences());
    }
}