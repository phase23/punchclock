package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class Adminpanel extends AppCompatActivity {
    Button ccancel;
    Button llogin;
    EditText pin;
    EditText email;
    String responseLocation;
    String cunq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpanel);

        ccancel = (Button)findViewById(R.id.login_cancel);
        llogin = (Button)findViewById(R.id.login_now);

        pin = (EditText)findViewById(R.id.innpasswrd);
        email = (EditText)findViewById(R.id.innemail);
        FullScreencall();

        ccancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gohome = new Intent(Adminpanel.this, MainActivity.class);
                startActivity(gohome);



            }
        });



        llogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String thispin = pin.getText().toString();
                String thisemail = email.getText().toString();


                if (thisemail.matches("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter a email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (thispin.matches("")) {
                    Toast.makeText(getApplicationContext(), "Enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }


/*
                if (!isEmailValid(thisemail)){
                    Toast.makeText(getApplicationContext(), "You did not a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }

*/
                String postaction = postLogin(thispin, thisemail);
                postaction = postaction.trim();
                Log.i("[print]",postaction);

                String[] separated = postaction.split("~");
                String dologin = separated[0];
                 cunq = separated[1];

                if(dologin.equals("noluck")){
                    Toast.makeText(getApplicationContext(), "Your password or email is incorrect", Toast.LENGTH_LONG).show();
                    return;
                }


                if(dologin.equals("sucess")){

                    Log.i("pass:unq -- ",cunq);
                   // Toast.makeText(getApplicationContext(), "Success "+ cunq, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Adminpanel.this, Admindashboard.class);
                    intent.putExtra("timeowner",cunq);
                    startActivity(intent);

                }

            }
        });





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


    public String postLogin( String thispin,  String thisemail ) {

        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = "https://punchclock.ai/deviceadmin.php?&token="+thisdevice;
        Log.i("action url",url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)

                .addFormDataPart("password",thispin )
                .addFormDataPart("thisemail",thisemail )

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

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }



}