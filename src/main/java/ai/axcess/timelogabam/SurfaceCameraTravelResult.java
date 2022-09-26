package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SurfaceCameraTravelResult extends AppCompatActivity {
    TextView textView;
    TextView statresult;
    TextView task;
    private ImageView imageView;

    Button button;
    Button button2;
    Button callout;
    Intent intent = getIntent();
    String cunqrez;
    String cunq;
    public String responsethis;
    String postaction;
    public Handler handler;
    private long mLastClickTime = 0;
    String wunq;
    //String responsethis = intent.getStringExtra("passthis");

    //Bundle extras = getIntent().getExtras();
    //String responsethis = extras.getString("passthis");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_camera_travel_result);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        textView = (TextView)findViewById(R.id.textView);
        statresult = (TextView)findViewById(R.id.statusresult);
        task = (TextView)findViewById(R.id.taskid);
        button = (Button)findViewById(R.id.retake);
        button2 = (Button)findViewById(R.id.finish);
        callout = (Button)findViewById(R.id.callout);
        imageView = (ImageView)findViewById(R.id.imageView);

        callout.setVisibility(View.INVISIBLE);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // TODO: Your application init goes here.
                Intent mInHome = new Intent(SurfaceCameraTravelResult.this, MainActivity.class);
                SurfaceCameraTravelResult.this.startActivity(mInHome);
                SurfaceCameraTravelResult.this.finish();
            }
        }, 60000);


        responsethis = getIntent().getExtras().getString("passthis");
        final String cunq = getIntent().getExtras().getString("cunq");

        Log.i("pass:respBody:surresult",responsethis);

        responsethis = responsethis.trim();
        String[] separated = responsethis.split("~");
        String rekstat = separated[0];
        int myNum = 0;

        try {
            myNum = Integer.parseInt(rekstat);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }

        System.out.println("numner ="+ myNum);
        if(myNum == 1) {
            String fname = separated[1];
            String lname = separated[2];
            String jobtype = separated[3];
            String url = separated[4];
            String datein = separated[5];
            String timein = separated[6];

            String clockstat = separated[8];
            String onthejob = separated[9];
            String tasklist = separated[13];
            wunq = separated[14];
            String iscallout = separated[15];
            String calloutjob = separated[16];

            tasklist = tasklist.trim();
            String putall = "" + fname + "\n" +  lname + "\n" +  jobtype + "\n" + datein
                    + "\n" + timein
                    + "\n" + clockstat
                    + "\n" + onthejob;

            textView.setText(putall);
            statresult.setText(clockstat + " " + timein);

            clockstat = clockstat.trim();

            if(clockstat.equals("Clocked In")){
                statresult.setTextColor(Color.parseColor("#04743e"));
                iscallout = iscallout.trim();

                if(iscallout.equals("yescallout")){
                    callout.setVisibility(View.VISIBLE);
                }


            }else {

                statresult.setTextColor(Color.parseColor("#CD0811"));
            }

            String taskis;
            String itlookempty = "***";
            Log.i("tsk",tasklist);
            if(tasklist.equals("***")){
                //if(tasklist == '***'){
                taskis = "";
            }else {
                taskis = "<b>Today's task</b><br>" + tasklist;
            }


            task.setText((Html.fromHtml(taskis)));


            button.setVisibility(View.INVISIBLE);



            new SurfaceCameraTravelResult.DownloadImageTask(imageView)
                    .execute("https://punchclock.ai/" + url);

        } else {

            String error = separated[1];
            String cunqrez = separated[2];
            String putall = "" + error + "\n" + cunqrez;
            textView.setText(putall);
            statresult.setText("No action recorded");
            button2.setVisibility(View.VISIBLE);

        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);


                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }

                mLastClickTime = SystemClock.elapsedRealtime();

                Intent intent = new Intent(SurfaceCameraTravelResult.this, SurfaceCamera.class);
                intent.putExtra("cunq",cunq);
                startActivity(intent);
                ((Activity) SurfaceCameraTravelResult.this).finish();
                handler.removeCallbacksAndMessages(null);

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button2.setEnabled(false);

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }

                mLastClickTime = SystemClock.elapsedRealtime();

                Intent intent = new Intent(SurfaceCameraTravelResult.this, MainActivity.class);

                startActivity(intent);
                ((Activity) SurfaceCameraTravelResult.this).finish();
                handler.removeCallbacksAndMessages(null);
            }
        });


        callout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(SurfaceCameraTravelResult.this);
                dialog.setCancelable(false);
                dialog.setTitle("Confirm Callout");
                dialog.setMessage("Please confirm this is a call out" );
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {


                        try {


                            Log.i("[print]", "https://punchclock.ai/sendcallout.php?call=" + wunq );
                            sendCallout("https://punchclock.ai/sendcallout.php?call=" + wunq);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }



                        Intent intent = new Intent(SurfaceCameraTravelResult.this, Calloutresult.class);
                        intent.putExtra("passthis", responsethis);
                        intent.putExtra("cunq", cunq);
                        startActivity(intent);


                        // finishAffinity();
                        //System.exit(0);

                        //Action for "Delete".
                    }
                })
                        .setNegativeButton("No ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Action for "Cancel".
                            }
                        });

                final AlertDialog alert = dialog.create();
                alert.show();









            }

        });




    }


    void sendCallout(String url) throws IOException {
        Log.i("[print]","url " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                                Log.i("[print]","error" + e);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        postaction = response.body().string();
                        Log.i("assyn url",postaction);
                        // Do something with the response


                        Log.i("[print]",postaction);
                        postaction = postaction.trim();


                    }
                });
    }






    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {

            final OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(urls[0])
                    .build();

            Response response = null;
            Bitmap mIcon11 = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response.isSuccessful()) {
                try {
                    mIcon11 = BitmapFactory.decodeStream(response.body().byteStream());
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
