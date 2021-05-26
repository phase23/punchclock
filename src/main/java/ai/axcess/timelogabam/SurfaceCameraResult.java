package ai.axcess.timelogabam;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ai.axcess.timelogabam.R;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SurfaceCameraResult extends AppCompatActivity {

    TextView textView;
    TextView statresult;
    TextView task;
    private ImageView imageView;

    Button button;
    Button button2;
    Intent intent = getIntent();
    String cunqrez;
    String cunq;
    public Handler handler;
    private long mLastClickTime = 0;
    //String responsethis = intent.getStringExtra("passthis");

    //Bundle extras = getIntent().getExtras();
    //String responsethis = extras.getString("passthis");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_camera_result);

        textView = (TextView)findViewById(R.id.textView);
        statresult = (TextView)findViewById(R.id.statusresult);
        task = (TextView)findViewById(R.id.taskid);
        button = (Button)findViewById(R.id.retake);
        button2 = (Button)findViewById(R.id.finish);
        imageView = (ImageView)findViewById(R.id.imageView);


        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // TODO: Your application init goes here.
                Intent mInHome = new Intent(SurfaceCameraResult.this, MainActivity.class);
                SurfaceCameraResult.this.startActivity(mInHome);
                SurfaceCameraResult.this.finish();
            }
        }, 60000);


        String responsethis = getIntent().getExtras().getString("passthis");
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



            new DownloadImageTask(imageView)
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

                Intent intent = new Intent(SurfaceCameraResult.this, SurfaceCamera.class);
                intent.putExtra("cunq",cunq);
                startActivity(intent);
                ((Activity) SurfaceCameraResult.this).finish();
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

                Intent intent = new Intent(SurfaceCameraResult.this, MainActivity.class);

                startActivity(intent);
                ((Activity) SurfaceCameraResult.this).finish();
                handler.removeCallbacksAndMessages(null);
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
