package ai.axcess.timelogabam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    ImageView facelog;
    ImageView nfcread;
    ImageView fingr;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final ProgressBar pb = (ProgressBar)findViewById(R.id.progress_loader);
        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
       // ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);

        justwait = (TextView) findViewById(R.id.wait);
        thisview = (View) findViewById(R.id.barup);
        adminlevel = (ImageView) findViewById(R.id.gear);
        fingr =  (ImageView) findViewById(R.id.fingrpr);
        pinpad = (ImageView) findViewById(R.id.dailpad);
        wrenhr = (ImageView) findViewById(R.id.wrench);


        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else {
            connected = false;
        }



        if(!connected) {
            Toast.makeText(getApplicationContext(),"Check Internet & Restart App",Toast.LENGTH_LONG).show();
            Intent nointernet = new Intent(MainActivity.this, Nointernet.class);
            startActivity(nointernet);

        }else {

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


            returndevice = isregistered();
            returndevice = returndevice.trim();

            if (returndevice.equals("not found")) {
                Intent devicesetup = new Intent(MainActivity.this, Startup.class);
                startActivity(devicesetup);
            }


            facelog = (ImageView) findViewById(R.id.facial);
            nfcread = (ImageView) findViewById(R.id.nfcfob);
            textView = (TextView) findViewById(R.id.setinternet);
            locationplace = (TextView) findViewById(R.id.location);

            //FullScreencall();


            // lcheckinternet();


            boolean online = hostAvailable("www.google.com", 80);
            if (!online) {

                Log.i("Online Status", "Connected check....");
                textView.setText("No Internet Connection");
            } else {
                textView.setText("");
            }

            deviceId = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            String thelocation = getdevicelocation(deviceId);
            locationplace.setText("" + thelocation + " > Choose Option");

            Log.i("log device", deviceId);

            facelog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //checkinternet();
/*
                boolean online = hostAvailable("www.google.com", 80);


                if(!online) {

                    Log.i("Online Status","Connected check....");
                    textView.setText("No Internet Connection");
                }else{

                    textView.setText("");
                }
*/

                    //pb.setVisibility(visible);
                    thisview.setVisibility(View.INVISIBLE);
                    justwait.setVisibility(View.VISIBLE);
                    String cunq = getdeviceowner(deviceId);

                    Log.i("log owner", cunq);

                    Intent intent = new Intent(MainActivity.this, SurfaceCamera.class);
                    intent.putExtra("cunq", cunq);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivityForResult(intent, 0);
                    overridePendingTransition(0,0); //0 for no animation

                    startActivity(intent);

                }

            });



            fingr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(getApplicationContext(),"This option is not available",Toast.LENGTH_LONG).show();

                }

            });


            pinpad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    checkinternet();
                    String cunq = getdeviceowner(deviceId);
                    justwait.setVisibility(View.VISIBLE);
                    Log.i("log owner", cunq);

                    Intent intent = new Intent(MainActivity.this, Pinpad.class);
                    intent.putExtra("cunq", cunq);
                    startActivity(intent);


                }

            });






            nfcread.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    checkinternet();
                    String cunq = getdeviceowner(deviceId);

                    Log.i("log owner", cunq);

                    Intent intent = new Intent(MainActivity.this, fobOptions.class);
                    intent.putExtra("cunq", cunq);
                    startActivity(intent);

                }

            });


            if(!hasPermissions(this, permissions)){
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



    public void FullScreencall() {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
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


    private  void lcheckinternet(){

        ConnectivityManager manager =(ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                //we have WIFI
                //textView.setVisibility(View.VISIBLE);
                //textView.setText("please wait...");
            }
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                textView.setVisibility(View.VISIBLE);
                //textView.setText("please wait...");
            }
        } else{
            //we have no connection :(
            Toast.makeText(getApplicationContext(),"Check Internet & Restart App",Toast.LENGTH_LONG).show();
            textView.setVisibility(View.VISIBLE);
            textView.setText("No Internet Connection");
        }
    }


    private  void checkinternet(){

        ConnectivityManager manager =(ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                //we have WIFI
                //textView.setVisibility(View.VISIBLE);
                //textView.setText("please wait...");
            }
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                //textView.setVisibility(View.VISIBLE);
                //textView.setText("please wait...");
            }
        } else{
            //we have no connection :(
            Toast.makeText(getApplicationContext(),"Check Internet & Restart App",Toast.LENGTH_LONG).show();
            textView.setVisibility(View.VISIBLE);
            textView.setText("No Internet Connection");
        }
    }


    public String getdevicelocation( String deviceid ) {

        String url = "https://punchclock.ai/getdevicelocation.php?token="+deviceid;


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
            responseLocation =  response.body().string();
            Log.i("respBody:main",responseLocation);



            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseLocation;

    }//emd



    public String isregistered() {


        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = "https://punchclock.ai/devicesetup.php?action=checkdevice&token="+thisdevice;
        Log.i("action url",url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("deviceid",thisdevice )
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
            responseLocation =  response.body().string();
            Log.i("respBody:main",responseLocation);
            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseLocation;

    }//emd



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



}





