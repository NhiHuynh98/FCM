package com.example.fcm;

import android.app.PendingIntent;
import android.media.RingtoneManager;
import android.app.Notification;
import android.net.Uri;
import android.graphics.BitmapFactory;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.webkit.CookieManager;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainService extends Service {
    MqttAndroidClient client;
    String clientId;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public android.os.IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initFore();
    }
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.project_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.mobile_missed_call_icon)
                        .setColor(0x33cc5a)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.mobile_missed_call_icon))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationManager.IMPORTANCE_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Phone Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, notificationBuilder.build());
    }
    private void initFore() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("ufnsyiiv");
        options.setPassword("H3PS6--VX_3T".toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setConnectionTimeout(10000);

        String data = getCookie("https://chat.chek.agency", "device_token");

        if(data == null) {
            clientId = MqttClient.generateClientId();
            CookieManager.getInstance().setCookie("https://chat.chek.agency", "device_token=" + clientId);
        } else {
            clientId = data;
        }

        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://driver.cloudmqtt.com:18604", clientId);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {}

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                sendNotification(mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
        });

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    String topic = clientId;
                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {}

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {}
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {}
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private String getCookie(String siteName,String cookieName){
        String CookieValue = null;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if(cookies != null) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp ){
                if(ar1.contains(cookieName)){
                    String[] temp1=ar1.split("=");
                    CookieValue = temp1[1];
                    break;
                }
            }
        };
        return CookieValue;
    }
}

