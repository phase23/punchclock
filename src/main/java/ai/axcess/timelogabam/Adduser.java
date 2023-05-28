package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Adduser extends AppCompatActivity {
    Button btnccancel;
    Button btnccreate;
    EditText ffname;
    EditText llname;
    String responseLocation;
    String cunq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adduser);

        cunq = getIntent().getExtras().getString("timeowner");

        btnccancel = (Button)findViewById(R.id.ccancel);
        btnccreate = (Button)findViewById(R.id.ccreate);
        ffname = (EditText)findViewById(R.id.ffname);
        llname = (EditText)findViewById(R.id.llname);



        btnccancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gosetup = new Intent(Adduser.this, Admindashboard.class);
                gosetup.putExtra("timeowner",cunq);
                startActivity(gosetup);



            }
        });



        btnccreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String thisfname = ffname.getText().toString();
                String thislname = llname.getText().toString();


                if (thisfname.matches("") || thislname.matches("")  ) {
                    Toast.makeText(getApplicationContext(), "All field are mandatory. Please check and try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                String addUser = postCreate(thisfname, thislname);
                addUser = addUser.trim();

                if(addUser.equals("good")) {

                    Intent gosetup = new Intent(Adduser.this, Admindashboard.class);
                    gosetup.putExtra("timeowner",cunq);
                    startActivity(gosetup);

                    int toastDurationInSeconds = 7; // the duration in seconds
                    final Toast toast = Toast.makeText(getApplicationContext(), "New user added", Toast.LENGTH_LONG);

                    int timeInMilliseconds = toastDurationInSeconds * 1000;
                    CountDownTimer toastCountDown;
                    toastCountDown = new CountDownTimer(timeInMilliseconds, 1000 /*Tick duration*/) {
                        public void onTick(long millisUntilFinished) {
                            toast.show();
                        }

                        public void onFinish() {
                            toast.cancel();
                        }
                    };

                    toast.show();
                    toastCountDown.start();






                }else{


                    int toastDurationInSeconds = 7; // the duration in seconds
                    final Toast toast = Toast.makeText(getApplicationContext(), "There was an error", Toast.LENGTH_LONG);

                    int timeInMilliseconds = toastDurationInSeconds * 1000;
                    CountDownTimer toastCountDown;
                    toastCountDown = new CountDownTimer(timeInMilliseconds, 1000 /*Tick duration*/) {
                        public void onTick(long millisUntilFinished) {
                            toast.show();
                        }

                        public void onFinish() {
                            toast.cancel();
                        }
                    };

                    toast.show();
                    toastCountDown.start();



                }



            }
        });


    }





    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }


    public String postCreate( String thisfname, String thislname) {

        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        //String url = "https://punchclock.ai/devicesetup.php?action=adduser&token="+thisdevice + "&thisfname=" + thisfname + "&thislname=" +thislname + "&thisbname="+ thisbname + "&thisinemail="+thisinemail + "&thisinpasswrd="+thisinpasswrd;
        String url = "https://punchclock.ai/usersetup.php?action=adduser";

        Log.i("action url",url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("thisfname",thisfname )
                .addFormDataPart("thislname",thislname )
                .addFormDataPart("cunq",cunq )
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
    }










}