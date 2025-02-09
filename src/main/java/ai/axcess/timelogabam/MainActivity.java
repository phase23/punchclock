package ai.axcess.timelogabam;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    ImageView facelog;
    ImageView nfcread;
    ImageView away;
    ImageView wrenhr;
    ImageView pinpad;
    TextView textView;
    TextView locationplace;
    TextView justwait;
    String responseBody;
    String responseLocation;
    String deviceId;
    public Handler handler;
    String returndevice;
    View thisview;
    ImageView adminlevel;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_STORAGE_REQUEST_CODE = 101;
    int ALL_PERMISSIONS = 102;
    private WifiManager wifiManager;
    WifiConfiguration currentConfig;
    WifiManager.LocalOnlyHotspotReservation hotspotReservation;
    String locationnow;
    String thelocation;
    Handler handler2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final ProgressBar pb = (ProgressBar) findViewById(R.id.progress_loader);
        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        // ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);

        justwait = (TextView) findViewById(R.id.wait);
        thisview = (View) findViewById(R.id.barup);
        adminlevel = (ImageView) findViewById(R.id.gear);
        away = (ImageView) findViewById(R.id.ontravel);
        pinpad = (ImageView) findViewById(R.id.dailpad);
        wrenhr = (ImageView) findViewById(R.id.wrench);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfig = new WifiConfiguration();

        handler2 = new Handler(Looper.getMainLooper());
        deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


       createfile();

        showWritePermissionSettings();
        setWifiEnabled(wifiConfig, false); // Disable the WiFi hotspot

        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else {
            connected = false;
        }


        if (!connected) {
            Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
            Intent nointernet = new Intent(MainActivity.this, Nointernet.class);
            startActivity(nointernet);

        } else {

            Intent i = new Intent(this, Myservice.class);
            this.startService(i);

            adminlevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    justwait.setVisibility(View.VISIBLE);
                    Intent goadmin = new Intent(MainActivity.this, Adminpanel.class);
                    startActivity(goadmin);


                }
            });

            wrenhr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    justwait.setVisibility(View.VISIBLE);
                    Intent goadmin = new Intent(MainActivity.this, aidHelp.class);
                    startActivity(goadmin);


                }
            });



        /*
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // TODO: Your application init goes here.
                Intent mInHome = new Intent(MainActivity.this, Sleepscreen.class);
                MainActivity.this.startActivity(mInHome);
                MainActivity.this.finish();
            }
        }, 30000);
*/


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);


            //returndevice = isregistered();

            try {
                devisregistered(" https://punchclock.ai/devicesetup.php?action=checkdevice&token=" + deviceId);

            } catch (IOException e) {
                e.printStackTrace();
            }



            facelog = (ImageView) findViewById(R.id.facial);
            nfcread = (ImageView) findViewById(R.id.nfcfob);
            textView = (TextView) findViewById(R.id.setinternet);
            locationplace = (TextView) findViewById(R.id.location);

            //FullScreencall();


            // lcheckinternet();


            boolean online = isOnline();
            if (!online) {

                Log.i("Online Status", "Connected check....");
                textView.setText("No Internet Connection");
            } else {
                textView.setText("");
            }



            //String thelocation = getdevicelocation(deviceId);
            //locationplace.setText("" + thelocation + " > Choose Option");

            try {
                getdeviceloc("https://punchclock.ai/getdevicelocation.php?token=" + deviceId);

            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.i("log device", deviceId);

            /*
            facelog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    //pb.setVisibility(visible);
                    //thisview.setVisibility(View.INVISIBLE);
                    justwait.setVisibility(View.VISIBLE);

                    boolean online = isOnline() ;
                    if(online) {
                        String cunq = readFile();
                        justwait.setVisibility(View.VISIBLE);
                        Log.i("log owner", cunq);

                        Log.i("log owner", cunq);
                        Intent intent = new Intent(MainActivity.this, SurfaceCamera.class);
                        intent.putExtra("cunq", cunq);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivityForResult(intent, 0);
                        overridePendingTransition(0, 0); //0 for no animation
                        startActivity(intent);

                    } else {

                        Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
                        Intent errorpunch = new Intent(MainActivity.this, Nointernet.class);
                        startActivity(errorpunch);


                    }


                }

            });

*/

            facelog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new camTask().execute();
                }
            });

            away.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //thisview.setVisibility(View.INVISIBLE);
                    justwait.setVisibility(View.VISIBLE);

                    boolean online = isOnline() ;
                    if(online) {
                        String cunq = readFile();
                        justwait.setVisibility(View.VISIBLE);
                        Log.i("log owner", cunq);


                    Log.i("log owner", cunq);

                    Intent intent = new Intent(MainActivity.this, SurfaceCameraTravel.class);
                    intent.putExtra("cunq", cunq);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivityForResult(intent, 0);
                    overridePendingTransition(0, 0); //0 for no animation

                    startActivity(intent);

                    } else {

                        Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
                        Intent errorpunch = new Intent(MainActivity.this, Nointernet.class);
                        startActivity(errorpunch);
                    }


                }

            });


            pinpad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new pinpadTask().execute();
                }
            });

            /*

            pinpad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    boolean online = isOnline() ;
                    if(online) {
                        String cunq = readFile();
                        justwait.setVisibility(View.VISIBLE);
                        Log.i("log owner", cunq);

                        Intent intent = new Intent(MainActivity.this, Pinpad.class);
                        intent.putExtra("cunq", cunq);
                        startActivity(intent);


                            } else {

                    Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
                    Intent errorpunch = new Intent(MainActivity.this, Nointernet.class);
                    startActivity(errorpunch);


                 }


                }

            });

            */

            nfcread.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean online = isOnline() ;
                    if(online) {
                        String cunq = readFile();
                        justwait.setVisibility(View.VISIBLE);
                        Log.i("log owner", cunq);

                        Intent intent = new Intent(MainActivity.this, fobOptions.class);
                        intent.putExtra("cunq", cunq);
                        startActivity(intent);

                    } else {

                        Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
                        Intent errorpunch = new Intent(MainActivity.this, Nointernet.class);
                        startActivity(errorpunch);


                    }


                }

            });


            if (!hasPermissions(this, permissions)) {
                ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
            }


                        /*
                          if (checkPermission()) {
                            //main logic or main code
                            // . write your main code to execute, It will execute if the permission is already given.

                            } else {
                            requestPermission();
                                }
                         */


        }//end else


        /*
        if (Build.VERSION.SDK_INT >= 23) {


            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_REQUEST_CODE);
            }


            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            }

        }


         */

    }


    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }



    public String readFile() {
        String fileName = "base.txt";
        StringBuilder stringBuilder = new StringBuilder();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            fis = openFileInput(fileName);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }

            locationnow = stringBuilder.toString();
            // Use the file contents as needed
            // Uncomment the line below to display a toast message with the content
            // Toast.makeText(getApplicationContext(), "Serlat: " + locationnow, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Error reading file
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return  locationnow;
    }


    public void createfile() {



        String fileName = "base.txt";
        String content = "";

        File file = new File(getFilesDir(), fileName);

        if (!file.exists()) {
            FileOutputStream fos = null;
            try {
                fos = openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(content.getBytes());
                // File written successfully
            } catch (IOException e) {
                e.printStackTrace();
                // Error writing file
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // File already exists, handle accordingly
        }


    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                //Intent nopermission = new Intent(Register.this, Nopermission.class);
                //startActivity(nopermission);

            }
        }


        if (requestCode == MY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "storage permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "storage permission denied", Toast.LENGTH_LONG).show();
                //Intent nopermission = new Intent(Register.this, Nopermission.class);
                //startActivity(nopermission);

            }
        }


    }


    public  void sendhotspot(){


        String getdeviceid = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Long tsLong = System.currentTimeMillis()/1000;

        String url = "https://punchclock.ai/api/hotspot.php?timestamp="+tsLong + "&deviceid=" + getdeviceid;

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

    public void FullScreencall() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);

        /*
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_STORAGE_REQUEST_CODE);
*/


    }


    private void lcheckinternet() {

        ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                //we have WIFI
                //textView.setVisibility(View.VISIBLE);
                //textView.setText("please wait...");
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                textView.setVisibility(View.VISIBLE);
                //textView.setText("please wait...");
            }
        } else {
            //we have no connection :(
            Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
            textView.setVisibility(View.VISIBLE);
            textView.setText("No Internet Connection");
        }
    }


    private void checkinternet() {
        boolean online = isOnline() ;


        if(online) {
            //do nothing
        } else {
            //we have no connection :(
            Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
            textView.setVisibility(View.VISIBLE);
            textView.setText("No Internet Connection");
        }
    }


    /*

    private void checkinternet() {

        ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                //we have WIFI
                //textView.setVisibility(View.VISIBLE);
                //textView.setText("please wait...");
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                //textView.setVisibility(View.VISIBLE);
                //textView.setText("please wait...");
            }
        } else {
            //we have no connection :(
            Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
            textView.setVisibility(View.VISIBLE);
            textView.setText("No Internet Connection");
        }
    }


*/



    void getdeviceloc(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.i("ddevice",url);
        OkHttpClient client = new OkHttpClient();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        Log.i("ddevice","errot"); // Error

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {


                        thelocation = response.body().string();
                        Log.i("ddevice",thelocation);

                        handler2.post(new Runnable() {
                            @Override
                            public void run() {

                                locationplace.setText("" + thelocation + " > Choose Option");


                            }
                        });


                    }//end if




                });

    }


    public String getdevicelocation(String deviceid) {

        String url = "https://punchclock.ai/getdevicelocation.php?token=" + deviceid;


        Log.i("action url", url);

        OkHttpClient client = new OkHttpClient();


        // String contentType = fileSource.toURL().openConnection().getContentType();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("deviceid", deviceid)
                .build();
        Request request = new Request.Builder()

                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.i("SUCC", "" + response.message());

            }
            String resp = response.message();
            responseLocation = response.body().string();
            Log.i("respBody:main", responseLocation);


            Log.i("MSG", resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseLocation;

    }//emd



    void devisregistered(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.i("ddevice",url);
        OkHttpClient client = new OkHttpClient();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        Log.i("ddevice","errot"); // Error

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {


                        thelocation = response.body().string();
                        Log.i("ddevice",thelocation);

                        handler2.post(new Runnable() {
                            @Override
                            public void run() {

                                returndevice = thelocation.trim();

                                if (returndevice.equals("not found")) {
                                    Intent devicesetup = new Intent(MainActivity.this, Startup.class);
                                    startActivity(devicesetup);
                                }


                            }
                        });


                    }//end if




                });

    }


    public String isregistered() {


        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = "https://punchclock.ai/devicesetup.php?action=checkdevice&token=" + thisdevice;
        Log.i("action url", url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("deviceid", thisdevice)
                .build();
        Request request = new Request.Builder()
                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.i("SUCC", "" + response.message());
            }
            String resp = response.message();
            responseLocation = response.body().string();
            Log.i("respBody:main", responseLocation);
            Log.i("MSG", resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseLocation;

    }//emd


    private boolean showWritePermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (!Settings.System.canWrite(this)) {
                Log.v("DANG", " " + !Settings.System.canWrite(this));
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                return false;
            }
        }
        return true; //Permission already given
    }


    public boolean setWifiEnabled(WifiConfiguration wifiConfig, boolean enabled) {

        try {
            if (enabled) { //disables wifi hotspot if it's already enabled
                wifiManager.setWifiEnabled(false);
                Log.i("Hotspot Disabed", "Connected turn it off....");
                Toast.makeText(getApplicationContext(), "ILLEGAL ACTION DECTECTED! Hotspot NOT available", Toast.LENGTH_LONG).show();

                sendhotspot();
            }

            Method method = wifiManager.getClass()
                    .getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }


    public String getdeviceowner( String deviceid ) {

        String url = "https://punchclock.ai/getdeviceinfo.php?token="+deviceid;


        Log.i("action url",url);

        OkHttpClient client = new OkHttpClient();


        // String contentType = fileSource.toURL().openConnection().getContentType();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("deviceid",deviceid )
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
            Log.i("respBody:main",responseBody);



            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseBody;

    }//emd



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


    @Override
    public void onBackPressed()
    {

        //thats it
    }

    private class pinpadTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return isOnline();
        }

        @Override
        protected void onPostExecute(Boolean online) {
            if (online) {
                String cunq = readFile();
                justwait.setVisibility(View.VISIBLE);
                Log.i("log owner", cunq);

                Intent intent = new Intent(MainActivity.this, Pinpad.class);
                intent.putExtra("cunq", cunq);
                startActivity(intent);

                finish(); // Finish the current Activity

            } else {
                Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
                Intent errorpunch = new Intent(MainActivity.this, Nointernet.class);
                startActivity(errorpunch);
            }
        }


    }


    private class camTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return isOnline();
        }

        @Override
        protected void onPostExecute(Boolean online) {
            if (online) {
                String cunq = readFile();
                justwait.setVisibility(View.VISIBLE);
                Log.i("log owner", cunq);

                Intent intent = new Intent(MainActivity.this, SurfaceCamera.class);
                intent.putExtra("cunq", cunq);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent, 0);
                overridePendingTransition(0, 0); //0 for no animation
                startActivity(intent);

                finish(); // Finish the current Activity

            } else {
                Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
                Intent errorpunch = new Intent(MainActivity.this, Nointernet.class);
                startActivity(errorpunch);
            }
        }


    }





}




