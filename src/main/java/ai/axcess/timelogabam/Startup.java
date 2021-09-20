package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Startup extends AppCompatActivity {

    Button setup;

    EditText pin;
    EditText email;
    EditText passwrd;
    EditText cpasswrd;
    String responseLocation;
    TextView accup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setup = (Button)findViewById(R.id.login_now);
        pin = (EditText)findViewById(R.id.passwrd);
        email = (EditText)findViewById(R.id.inemail);
        accup = (TextView) findViewById(R.id.ccacc);


        accup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gosetup = new Intent(Startup.this, Accountsetup.class);
                startActivity(gosetup);



            }
        });


                setup.setOnClickListener(new View.OnClickListener() {
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


               String addDevice = postStartup(thispin, thisemail);
                addDevice = addDevice.trim();
                Log.i("[print]",addDevice);

                if(addDevice.equals("noluck")){
                    Toast.makeText(getApplicationContext(), "Your password or email is incorrect", Toast.LENGTH_LONG).show();
                    return;
                }


                if(addDevice.equals("alreadyadded")){
                    Toast.makeText(getApplicationContext(), "This device has already been added", Toast.LENGTH_LONG).show();
                    return;
                }


                if(addDevice.equals("newdevice")){

                    Intent deviceup = new Intent(Startup.this, deviceSetupcompleted.class);
                    startActivity(deviceup);

                }




            }

        });





    } //end create bundle

    public String postStartup( String thispin,  String thisemail ) {

        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = "https://punchclock.ai/devicesetup.php?action=createaccount&token="+thisdevice;
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

    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        //final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

        final String PASSWORD_PATTERN ="^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                 "(?=\\S+$)" +           //no white spaces
                ".{8,}" +               //at least 8 characters
                "$";

        pattern = Pattern.compile(PASSWORD_PATTERN);

        matcher = pattern.matcher(password);

        return matcher.matches();

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