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

public class Accountsetup extends AppCompatActivity {
    Button btncancel;
    Button btncreate;
    EditText fname;
    EditText lname;
    EditText bname;
    EditText inemail;
    EditText inpasswrd;
    EditText cinpasswrd;
    String responseLocation;
    TextView deviceidtxt;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsetup);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        btncancel = (Button)findViewById(R.id.cancelcc);
        btncreate = (Button)findViewById(R.id.presscreate);

        fname = (EditText)findViewById(R.id.fname);
        inemail = (EditText)findViewById(R.id.inemail);
        lname = (EditText)findViewById(R.id.lname);
        bname = (EditText)findViewById(R.id.bname);
        inemail = (EditText)findViewById(R.id.thisemail);
        inpasswrd = (EditText)findViewById(R.id.inpasswrd);
        cinpasswrd = (EditText)findViewById(R.id.cinpasswd);

       /* deviceidtxt = (TextView) findViewById(R.id.deviceids);
        deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        deviceidtxt.setText(deviceId);
*/
        btncreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String thisfname = fname.getText().toString();
                String thislname = lname.getText().toString();

                String thisbname = bname.getText().toString();
                String thisinemail = inemail.getText().toString();

                String thisinpasswrd = inpasswrd.getText().toString();
                String thiscinpasswrd = cinpasswrd.getText().toString();


                if (thisfname.matches("") || thislname.matches("") || thisbname.matches("") || thisinemail.matches("") ||thisinpasswrd.matches("") ||thiscinpasswrd.matches("")   ) {
                    Toast.makeText(getApplicationContext(), "All field are mandatory. Please check and try again", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (!isEmailValid(thisinemail)){
                    Toast.makeText(getApplicationContext(), "You did not a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!isValidPassword(thisinpasswrd)){
                    Toast.makeText(getApplicationContext(), "Minimum eight characters, at least one letter and one number", Toast.LENGTH_SHORT).show();

                    return;
                }

                if(!thisinpasswrd.equals(thiscinpasswrd)){

                    Toast.makeText(getApplicationContext(), "Your password do not match", Toast.LENGTH_SHORT).show();
                    return;

                }


                String addUser = postCreate(thisfname, thislname, thisbname, thisinemail,thisinpasswrd);
                addUser = addUser.trim();
                Log.i("[print]",addUser);

                if(addUser.equals("emailinuse")){
                    Toast.makeText(getApplicationContext(), "This email address is already in use", Toast.LENGTH_LONG).show();
                    return;
                } else {

                    Intent deviceup = new Intent(Accountsetup.this, deviceSetupcompleted.class);
                    startActivity(deviceup);



                }




            }
        });




        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gosetup = new Intent(Accountsetup.this, MainActivity.class);
                startActivity(gosetup);



            }
        });





    }



    public String postCreate( String thisfname, String thislname, String thisbname, String thisinemail, String thisinpasswrd ) {

        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        //String url = "https://punchclock.ai/devicesetup.php?action=adduser&token="+thisdevice + "&thisfname=" + thisfname + "&thislname=" +thislname + "&thisbname="+ thisbname + "&thisinemail="+thisinemail + "&thisinpasswrd="+thisinpasswrd;
        String url = "https://punchclock.ai/devicesetup.php?action=adduser";

        Log.i("action url",url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token",thisdevice )
                .addFormDataPart("thisfname",thisfname )
                .addFormDataPart("thislname",thislname )
                .addFormDataPart("thisbname",thisbname )
                .addFormDataPart("thisinemail",thisinemail )
                .addFormDataPart("thisinpasswrd",thisinpasswrd )



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




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }


    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        //final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

        final String PASSWORD_PATTERN ="^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=\\S+$)" +           //no white spaces
                ".{8,}" +               //at least 8 characters
                "$";

        pattern = Pattern.compile(PASSWORD_PATTERN);

        matcher = pattern.matcher(password);

        return matcher.matches();

    }


}