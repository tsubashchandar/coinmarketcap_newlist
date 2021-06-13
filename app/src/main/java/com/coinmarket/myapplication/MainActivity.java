package com.coinmarket.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        Network.NetworkResponse {


    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    public ProgressBar spin;
    public static RecyclerView recyclerView;
    Network net;

    private Button search, addKey;
    private TextView timerText;
    private EditText timeValue, apiKey;
    private Switch timerSwitch;
    private ImageView setting;

    private int setTime;
    private boolean isSwitchEnabled;

    static final String SET_TIME = "setTime";
    static final String IS_SWITCH_ENABLED = "isSwitchEnabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(this);
/*        addKey = (Button) findViewById(R.id.addKey);
        addKey.setOnClickListener(this);
        apiKey = (EditText) findViewById(R.id.key);*/
        spin = (ProgressBar) findViewById(R.id.progressBar);
        timerText = (TextView) findViewById(R.id.timerText);
        timeValue = (EditText) findViewById(R.id.timeValue);
        timerSwitch = (Switch) findViewById(R.id.timerswitch);
        setting = (ImageView)findViewById(R.id.setting);
        setting.setOnClickListener(this);
        timerSwitch.setOnCheckedChangeListener(this);
        net = new Network(this);
        loadKey();
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        setTime = 0;
        if (savedInstanceState != null) {
            Log.e("hhhh", "loadsave");
            setTime = savedInstanceState.getInt(SET_TIME);
            isSwitchEnabled = savedInstanceState.getBoolean(IS_SWITCH_ENABLED);
        }
        else
        {
            Log.e("subash ", " oncreate savestate null" );
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back Press is disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.e("onsave", "asdasd");
        outState.putInt(SET_TIME, setTime);
        outState.putBoolean(IS_SWITCH_ENABLED, isSwitchEnabled);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Log.e("onrestore", "loadsave");
            setTime = savedInstanceState.getInt(SET_TIME);
            isSwitchEnabled = savedInstanceState.getBoolean(IS_SWITCH_ENABLED);
        }
        else
        {
            Log.e("onrestore", " null");
        }
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("subash", "resume");
        postReq();
        loadPref();
    }

    @Override
    public void onClick(View v) {
        if (v == search) {
            postReq();
        }
        if(v == setting)
        {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
        Log.e("subash", "onDestroy");
    }

    private void postReq() {
        if (!spin.isShown())
            spin.setVisibility(View.VISIBLE);
        recyclerView.removeAllViewsInLayout();
        net.PostRequest(getBaseContext());
    }

    @Override
    public void onResponse() {
        Log.e("subash","asdasdasda");
        if (spin.isShown()) {
            spin.setVisibility(View.INVISIBLE);
        }
        setAdapter();
    }

    public void setAdapter() {
        adapter = new CustomAdapter(net.getDataSet());
        recyclerView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            int time = 0;
            String lastCoin = "";
            if (!Network.dataSet.isEmpty()) {
                lastCoin = Network.dataSet.get(0).name;
            }
            String timestr = timeValue.getText().toString();

            if (!timestr.isEmpty()) {
                time = Integer.parseInt(timeValue.getText().toString());
            }
            if (time == 0) {
                timerSwitch.setChecked(false);
                timerText.setText("");
                Toast.makeText(this, "Enter valid time in s", Toast.LENGTH_SHORT).show();
            } else {
                timeValue.setEnabled(false);
                timerText.setText("Timer : " + timestr + " s");
                if (!isServiceRunning()) {
                    Intent mIntent = new Intent(this, MyService.class);
                    mIntent.putExtra("timer", time);
                    mIntent.putExtra("lastCoin", lastCoin);
                    startService(mIntent);
                }
            }
            setTime = time;

        } else {

            timeValue.setEnabled(true);
            timerText.setText("");
            stopService();
        }

        isSwitchEnabled = isChecked;
        Log.e("s", String.valueOf(isSwitchEnabled));

    }

    private void stopService() {
        if (isServiceRunning()) {
            stopService(new Intent(this, MyService.class));
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MyService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void loadPref() {
        Log.e("loadPref", String.valueOf(setTime));
        if (isSwitchEnabled) {
            timerSwitch.setChecked(true);
            if (setTime > 0) {
                timerText.setText("Timer : " + String.valueOf(setTime) + " s");
                timeValue.setText(String.valueOf(setTime));
            }
            timeValue.setEnabled(false);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void saveKey(String key)
    {
        Context context = this.getBaseContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                Network.API_KEY_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Network.API_KEY_SHARED_PREFERENCE, key);
        editor.apply();
        Network.Key = key;
    }

    private void loadKey()
    {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                Network.API_KEY_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        Network.Key = sharedPref.getString(Network.API_KEY_SHARED_PREFERENCE, "defaultValue");
    }

    public static void copyToClipboard(String address, Context context)
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(address);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", address);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(context, "Copied address " + address, Toast.LENGTH_SHORT).show();

    }
}
