package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.numpad.NumPad;
import com.example.numpad.NumPadClick;
import com.example.numpad.numPadClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Pinpad extends AppCompatActivity {
    EditText setpin;
    public Handler handler;
    public String cunq;
    private long mLastClickTime = 0;
    String pincode;
    String postaction;
    String responseLocation;
    String putall;
    TextView waitpin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinpad);
        setpin = (EditText)findViewById(R.id.pinpass);
        setpin.setEnabled(false);
        waitpin = (TextView) findViewById(R.id.pinwait);
        cunq = getIntent().getExtras().getString("cunq");


        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // TODO: Your application init goes here.
                Intent mInHome = new Intent(Pinpad.this, MainActivity.class);
                Pinpad.this.startActivity(mInHome);
                Pinpad.this.finish();
            }
        },60000);




        NumPad numPad = findViewById(R.id.padpin);

        numPad.setOnNumPadClickListener(new NumPadClick(new numPadClickListener() {
            @Override
            public void onNumpadClicked(ArrayList<Integer> nums) {
                Log.d("MYTAG", "onNumpadClicked: " + nums);

                String listout  = Arrays.toString(nums.toArray());

                StringBuilder sb = new StringBuilder();

                for (Integer pinpress : nums) {
                    sb.append(pinpress);
                    Log.d("MYTAG", "press: " + pinpress);
                }

                setpin.setText(sb);

            }
        }));



        final Button btnClockincancel = (Button) findViewById(R.id.pinCancel);
        final Button btnpinin = (Button) findViewById(R.id.pinIn);
        final Button btnpinout = (Button) findViewById(R.id.pinOut);



        btnpinin.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Log.d("ckil", "button click");

                btnClockincancel.setEnabled(false);
                waitpin.setVisibility(View.VISIBLE);

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }

                pincode = setpin.getText().toString();


                if (pincode.matches("")  ) {
                    Toast.makeText(getApplicationContext(), "Your pin cannot be blank", Toast.LENGTH_SHORT).show();
                    waitpin.setVisibility(View.INVISIBLE);
                    btnClockincancel.setEnabled(true);
                    return;
                }

                String pinback = checkPin(  pincode,  cunq, "1");
                pinback = pinback.trim();

                String[] separated = pinback.split("~");
                String thisresult = separated[0].trim();

                int myNum = 0;
                try {
                    myNum = Integer.parseInt(thisresult);
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }


                if(myNum == 2){
                    Toast.makeText(getApplicationContext(), "Your pin is not recognised", Toast.LENGTH_SHORT).show();
                    waitpin.setVisibility(View.INVISIBLE);
                    btnClockincancel.setEnabled(true);
                }


                if(myNum == 1) {
                    handler.removeCallbacksAndMessages(null);
                    String fname = separated[1];
                    String lname = separated[2];
                    String jobtype = separated[3];
                    String datein = separated[4];
                    String timein = separated[5];
                    String clockstat = separated[6];
                    String onthejob = separated[7];
                    String tasklist = separated[8];
                    tasklist = tasklist.trim();


                    String taskis;
                    if(tasklist.equals("***")){
                        //if(tasklist == '***'){
                        taskis = "";
                    }else {
                        taskis = "<h3>Today's task: " + tasklist +"</h3>";

                    }


                    putall = "" + fname + "<br>" + lname
                            + "<br>" + jobtype
                            + "<br>" + datein
                            + "<br>" + timein
                            + "<br><b>" + clockstat +"</b>"
                            + "<br>" + onthejob + "<br>";

                    Intent intent3 = new Intent(Pinpad.this, pinPadresult.class);
                    intent3.putExtra("outbag",putall);
                    intent3.putExtra("uid",pincode);
                    intent3.putExtra("clockstat",clockstat);
                    intent3.putExtra("taskis",taskis);

                    startActivity(intent3);



                }






                if(myNum == 99) {
                    btnClockincancel.setEnabled(true);
                    waitpin.setVisibility(View.INVISIBLE);
                    String report_en = separated[1];
                    String report_es = separated[2];
                    String errormsg = "<h3>" + report_en + "</h3> <b> <h3>" + report_es + "</h3>";

                    AlertDialog.Builder builder = new AlertDialog.Builder(Pinpad.this);
                    builder.setTitle("Error");

                    //builder.setMessage(putall);
                    builder.setMessage(Html.fromHtml(errormsg));


                    builder.setPositiveButton("Close/Cerrar", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                             
                        }
                    });


                    AlertDialog alert = builder.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();
                }











            }




        });













        btnpinout.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Log.d("ckil", "button click");

                btnClockincancel.setEnabled(false);
                waitpin.setVisibility(View.VISIBLE);

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }

                pincode = setpin.getText().toString();


                if (pincode.matches("")  ) {
                    Toast.makeText(getApplicationContext(), "Your pin cannot be blank", Toast.LENGTH_SHORT).show();
                    waitpin.setVisibility(View.INVISIBLE);
                    btnClockincancel.setEnabled(true);
                    return;
                }

                String pinback = checkPin(  pincode,  cunq, "2");
                pinback = pinback.trim();

                String[] separated = pinback.split("~");
                String thisresult = separated[0].trim();

                int myNum = 0;
                try {
                    myNum = Integer.parseInt(thisresult);
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }


                if(myNum == 2){
                    waitpin.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Your pin is not recognised", Toast.LENGTH_SHORT).show();
                    btnClockincancel.setEnabled(true);
                }



                if(myNum == 1) {
                    handler.removeCallbacksAndMessages(null);
                    String fname = separated[1];
                    String lname = separated[2];
                    String jobtype = separated[3];
                    String datein = separated[4];
                    String timein = separated[5];
                    String clockstat = separated[6];
                    String onthejob = separated[7];
                    String tasklist = separated[8];
                    tasklist = tasklist.trim();


                    String taskis;
                    if(tasklist.equals("***")){
                        //if(tasklist == '***'){
                        taskis = "";
                    }else {
                        taskis = "<h3>Today's task: " + tasklist +"</h3>";

                    }


                    putall = "" + fname + "<br>" + lname
                            + "<br>" + jobtype
                            + "<br>" + datein
                            + "<br>" + timein
                            + "<br><b>" + clockstat +"</b>"
                            + "<br>" + onthejob + "<br>";

                    Intent intent3 = new Intent(Pinpad.this, pinPadresult.class);
                    intent3.putExtra("outbag",putall);
                    intent3.putExtra("uid",pincode);
                    intent3.putExtra("clockstat",clockstat);
                    intent3.putExtra("taskis",taskis);

                    startActivity(intent3);



                }






                if(myNum == 99) {
                    btnClockincancel.setEnabled(true);
                    waitpin.setVisibility(View.INVISIBLE);
                    String report_en = separated[1];
                    String report_es = separated[2];
                    String errormsg = "<h3>" + report_en + "</h3> <b> <h3>" + report_es + "</h3>";

                    AlertDialog.Builder builder = new AlertDialog.Builder(Pinpad.this);
                    builder.setTitle("Error");

                    //builder.setMessage(putall);
                    builder.setMessage(Html.fromHtml(errormsg));


                    builder.setPositiveButton("Close/Cerrar", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    });


                    AlertDialog alert = builder.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();
                }











            }




        });





        btnClockincancel.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Log.d("ckil", "button click");

                btnClockincancel.setEnabled(false);

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }

                Intent intent = new Intent(Pinpad.this, MainActivity.class);
                startActivity(intent);
                handler.removeCallbacksAndMessages(null);

                finish(); // Finish the current Activity

            }
        });



    }




    public String checkPin( String pin, String cunq, String option) {

        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        //String url = "https://punchclock.ai/devicesetup.php?action=adduser&token="+thisdevice + "&thisfname=" + thisfname + "&thislname=" +thislname + "&thisbname="+ thisbname + "&thisinemail="+thisinemail + "&thisinpasswrd="+thisinpasswrd;
        String pinurl = "https://punchclock.ai/capturePin.php?getdevice=" + thisdevice + "&pin=" + pin + "&cunq="+cunq + "&capturetype=options&choice="+option;
        String url = "https://punchclock.ai/capturePin.php";

        Log.i("action url",pinurl);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("getdevice",thisdevice )
                .addFormDataPart("pin",pin )
                .addFormDataPart("cunq",cunq )
                .addFormDataPart("capturetype","options" )
                .addFormDataPart("choice",option )

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