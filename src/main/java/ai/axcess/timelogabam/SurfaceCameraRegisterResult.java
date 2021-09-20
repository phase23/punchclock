package ai.axcess.timelogabam;



import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
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

public class SurfaceCameraRegisterResult extends AppCompatActivity {
    TextView textView;
    private ImageView imageView;

    Button retakebtn;
    Button finishbtn;
    Intent intent = getIntent();
    String respid;
    public String passsthis;
    public String responsethis;
    public String timeowner;
    public Handler handler;
    //public String userid;
    //String responsethis = intent.getStringExtra("passthis");

    //Bundle extras = getIntent().getExtras();
    //String responsethis = extras.getString("passthis");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_camera_register_result);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        textView = (TextView)findViewById(R.id.textViewres);
        retakebtn = (Button)findViewById(R.id.retakeres);
        finishbtn = (Button)findViewById(R.id.finishres);
        imageView = (ImageView)findViewById(R.id.imageViewres);


        responsethis = getIntent().getExtras().getString("passthis");
        String userid = getIntent().getExtras().getString("userid");
        timeowner = getIntent().getExtras().getString("timeowner");

        Log.i("thispass",responsethis);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // TODO: Your application init goes here.
                Intent mInHome = new Intent(SurfaceCameraRegisterResult.this, MainActivity.class);
                SurfaceCameraRegisterResult.this.startActivity(mInHome);
                SurfaceCameraRegisterResult.this.finish();
            }
        }, 60000);



        String[] separated = responsethis.split("~");
        String rekstat = separated[0];
        int myNum = 0;

        try {
            myNum = Integer.parseInt(rekstat);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }


        if(myNum == 1) {

            String fname = separated[1];
            String lname = separated[2];
            String jobtype = separated[3];
            String url = separated[4];
            String datein = separated[5];
            String timein = separated[6];
            String putall = "" + fname + "\n" + lname + "\n" + jobtype + "\n" + datein + "\n" + timein;
            textView.setText(putall);
            retakebtn.setVisibility(View.INVISIBLE);


            new DownloadImageTask(imageView)
                    .execute("https://punchclock.ai/" + url);

        }else{


            String errorrez = separated[1];
            String respid = separated[2];
            String putall = "" + errorrez + "\n";
            textView.setText(putall);
            finishbtn.setVisibility(View.INVISIBLE);


        }

        retakebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SurfaceCameraRegisterResult.this, SurfaceCameraRegister.class);
                intent.putExtra("userid", respid);
                startActivity(intent);
                //((Activity) SurfaceCameraRegisterResult.this).finish();
                handler.removeCallbacksAndMessages(null);
            }
        });


        finishbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SurfaceCameraRegisterResult.this, Listworkforce.class);
                intent.putExtra("position","regisiter");
                intent.putExtra("passthis",responsethis);
                intent.putExtra("timeowner",timeowner);
                startActivity(intent);
                handler.removeCallbacksAndMessages(null);
                //((Activity) SurfaceCameraRegisterResult.this).finish();

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