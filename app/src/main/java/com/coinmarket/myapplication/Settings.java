package com.coinmarket.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity implements Network.NetworkResponse, View.OnClickListener {

    private TextView curKey, usage;
    private Button addKey;
    private EditText apiKey;
    private Network net;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        curKey = (TextView)findViewById(R.id.curKey);
        usage = (TextView)findViewById(R.id.usage);
        addKey= (Button)findViewById(R.id.addKey);
        apiKey=(EditText)findViewById(R.id.newKey);
        addKey.setOnClickListener(this);
        net = new Network(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        curKey.setText(Network.Key);
        net.setRequestType(Network.RequestType.KEY);
        net.PostRequest(getApplicationContext());
        Log.e("asdasd", "onresume");
    }

    @Override
    public void onResponse() {
        usage.setText(Network.key_usage);
    }

    private void saveKey(String key)
    {
        Context context = this.getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                Network.API_KEY_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Network.API_KEY_SHARED_PREFERENCE, key);
        editor.apply();
        Network.Key = key;
    }

    @Override
    public void onClick(View v) {
        if (v == addKey) {
            String btnStr = addKey.getText().toString();

            if (btnStr.compareTo("Key") == 0) {
                Log.e("subash", "button key");
                apiKey.setVisibility(View.VISIBLE);
                addKey.setText("Save");

            } else {
                if (Network.keySet.contains(apiKey.getText().toString())) {
                    saveKey(apiKey.getText().toString());
                    Toast.makeText(this, "API Key added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Add valid API Key", Toast.LENGTH_SHORT).show();
                }
                addKey.setText("Key");
                apiKey.setVisibility(View.INVISIBLE);
            }
        }
    }
}
