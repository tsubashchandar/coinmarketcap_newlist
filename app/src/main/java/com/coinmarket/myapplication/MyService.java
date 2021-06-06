package com.coinmarket.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class MyService extends Service {
    CountDownTimer cdt = null;
    int time;
    String lastCoin = "";
    Network net;
    Receiver receiver;
    Uri notification;
    MediaPlayer mp;
    AlertDialog alert;

    public MyService() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("subash", "destroy");
        if (cdt != null) {
            cdt.cancel();
        }
        unregisterReceiver(receiver);
        if (mp.isPlaying()) {
            mp.stop();
        }
        mp.release();
        stopForeground(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground();
        receiver = new Receiver();
        net = new Network();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForeground() {
        NotificationChannel chan = new NotificationChannel("Token Check", "Poll Coins", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "Token Check")
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Token Check")
                    .setContentText("Polling for new coins...")
                    .setContentIntent(pendingIntent).build();
        }

        startForeground(1337, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("subash", "start");
        time = 0;
        if (intent != null) {
            time = intent.getIntExtra("timer", 0);
        }
        InitializeSound();
        IntentFilter filter = new IntentFilter(Network.LAST_COIN_KEY);
        filter.addAction(Network.INTENT_ACTION_KEY);
        registerReceiver(receiver, filter);
        if (time > 0) {
            cdt = new CountDownTimer(time * 1000, time * 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    net.PostRequest(getApplicationContext());
                }
            }.start();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            String newCoin = arg1.getExtras().getString(Network.LAST_COIN_KEY);
            String newCoinPlatform = arg1.getExtras().getString(Network.LAST_COIN_PLATFORM_KEY);
            cdt.start();
            if ((lastCoin != "") && (lastCoin.compareTo(newCoin) != 0)) {
                if (newCoinPlatform.compareTo("BNB") == 0 || newCoinPlatform.compareTo("error") == 0) {
                    notifyNewToken();
                }
            }
            lastCoin = newCoin;
            Log.e("subash", "Received broadcast " + newCoin + lastCoin);
        }
    }

    private void InitializeSound() {
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mp = MediaPlayer.create(getApplicationContext(), notification);
    }

    private void notifyNewToken() {

          /*  mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mp.prepareAsync();*/
        mp.start();
        showAlertDialog();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Token Check");
        builder.setMessage("Token : " + Network.dataSet.get(0).name);
        builder.setPositiveButton(
                "Buy",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       Intent intent = getPackageManager().
                                getLaunchIntentForPackage("com.coinmarket.myapplication");
                     //  intent.
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        alert.show();
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                if (mp.isPlaying())
                    mp.stop();
            }
        });

        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (mp.isPlaying())
                    mp.stop();
            }
        });
    }
}
