package ai.axcess.timelogabam;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
//import static ai.axcess.readdeviceid.MainActivity.isPlugged;

public class Myservice extends Service {

    TextView textView;
    private final int THIRTY_MINUTES = 5000;
    private final int TWENTY_SECONDS = 2000;
   private final int FIFTEENMINUTES = 900000;
    //private final int FIFTEENMINUTES = 2000;
    public Handler handler;
    String thedevice;
    String responseBody;

    LocationManager locationManager;
    Context mContext;


    @Override
    public void onCreate() {

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                //.setSmallIcon(R.mipmap.app_icon)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startMyOwnForeground();
        }else {
            startForeground(1337, notification);
        }

        mContext=this;




    }


    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)

                .setContentTitle("ReadID")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }




    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //do something
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

        }




        thedevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.e("nfc ID", thedevice);

        batteryLevel(getApplicationContext(), thedevice );

        scheduledevicestate();
        // scheduledeviceuptime();


    }





    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }





    public void scheduledevicestate() {

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                batteryLevel(getApplicationContext(), thedevice );        // this method will contain your almost-finished HTTP calls
                handler.postDelayed(this, FIFTEENMINUTES);
            }
        }, FIFTEENMINUTES);
    }



    public void scheduledeviceuptime() {

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                boolean online = hostAvailable("www.google.com", 80);
                if(online) {

                    gettimestamp();
                }
                // this method will contain your almost-finished HTTP calls
                handler.postDelayed(this, TWENTY_SECONDS);
            }
        }, TWENTY_SECONDS);
    }


    public void pingserver(int percentage, String chargestate){

        String getdeviceid = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Long tsLong = System.currentTimeMillis()/1000;





    }


    public void  batteryLevel(Context context, String Device)
    {
        Intent intent  = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int    level   = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int    scale   = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int    percent = (level*100)/scale;
        //textView.setText(Device + " Battery: " + percent + " %" );

        Log.d("percentage", " is :" + percent);
        boolean online = hostAvailable("www.google.com", 80);

        if(online) {

            Boolean charging = isPlugged(getApplicationContext());
            String chargestautus;

            if(charging){
                chargestautus = "Charging";
            }else {
                chargestautus = "Not Charging";
            }

            Log.d("percent internet", " Charging :" + chargestautus);

            senddevicestate( percent, chargestautus );









        }//end if online

        //return String.valueOf(percent) + "%";
    }




    public void sendlocation(Double lati , Double longi ) {

        String getdeviceid = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = "https://punchclock.ai/devicelocation.php?latitude="+lati + "&longitude="+ longi + "&deviceid=" + getdeviceid;

        Log.i("action url",url);

        OkHttpClient client = new OkHttpClient();


        // String contentType = fileSource.toURL().openConnection().getContentType();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("device",getdeviceid )
                .build();
        Request request = new Request.Builder()

                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.i("SUCC",""+response.message());

            }
            String resp = response.message();
            responseBody =  response.body().string();
            Log.i("respBody",responseBody);



            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }






    }

    public  void gettimestamp(){


        String getdeviceid = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Long tsLong = System.currentTimeMillis()/1000;
        //String ts = tsLong.toString();

        // long timestamp = Calendar.getInstance().getTime() / 1000;
        // long unixTime = System.currentTimeMillis() / 1000L;


        String url = "https://punchclock.ai/isrunning.php?timestamp="+tsLong + "&deviceid=" + getdeviceid;


        Log.i("action url",url);

        OkHttpClient client = new OkHttpClient();


        // String contentType = fileSource.toURL().openConnection().getContentType();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("device",getdeviceid )
                .build();
        Request request = new Request.Builder()

                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.i("SUCC",""+response.message());

            }
            String resp = response.message();
            responseBody =  response.body().string();
            Log.i("respBody",responseBody);



            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }








    public String senddevicestate( int percentage, String chargestate ) {

        //double longitude ;
        //double latitude ;


        String getdeviceid = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Long tsLong = System.currentTimeMillis()/1000;



        String url = "https://punchclock.ai/devicestate.php?percentage="+percentage + "&charge="+ chargestate + "&deviceid=" + getdeviceid + "&timestamp="+tsLong;



        Log.i("action url",url);

        OkHttpClient client = new OkHttpClient();


        // String contentType = fileSource.toURL().openConnection().getContentType();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("charge",chargestate )
                .build();
        Request request = new Request.Builder()

                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.i("SUCC",""+response.message());

            }
            String resp = response.message();
            responseBody =  response.body().string();
            Log.i("respBody",responseBody);



            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }





        return responseBody;
    }






    public static boolean isPlugged(Context context) {
        boolean isPlugged= false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        return isPlugged;
    }



    public boolean hostAvailable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2000);
            return true;
        } catch (IOException e) {
            // Either we have a timeout or unreachable host or failed DNS lookup
            System.out.println(e);
            return false;
        }
    }




}

