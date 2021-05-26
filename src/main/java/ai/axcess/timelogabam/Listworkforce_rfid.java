package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Listworkforce_rfid extends AppCompatActivity {

    String responseBody;
    String fname;
    String lname;
    String timeowner;
    TextView textView;
    String returnfname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listworkforce);
        textView = (TextView) findViewById(R.id.msgscanned);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



        FullScreencall();



        timeowner = getIntent().getExtras().getString("timeowner");

        String returnworkers = getworkfore( timeowner );

        Log.i("action workers", returnworkers);


        try {






            String[] dishout = returnworkers.split(Pattern.quote("*"));
            System.out.println("number tickets: " + Arrays.toString(dishout));


            LinearLayout layout = (LinearLayout) findViewById(R.id.scnf);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER|Gravity.TOP);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            // params.gravity = Gravity.TOP;

            params.setMargins(10, 5, 0, 30);


            int btnid = 250;
            Button logout = new Button(this);
            logout.setId(btnid);
            //btn.setTag( token);
            final int id_ = logout.getId();
            logout.setText("Return to dashboard ");
            logout.setLayoutParams(params);

            logout.setPadding(20, 5, 20, 5 );
            layout.addView(logout);


            TextView panel = new TextView(this);
            panel.setText("Register RFID card");
            panel.setLayoutParams(params);
            panel.setPadding(20, 5, 20, 5 );
            panel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            panel.setTypeface(null, Typeface.BOLD);
            panel.setGravity(Gravity.CENTER);
            layout.addView(panel);




            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(Listworkforce_rfid.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("Return");
                    dialog.setMessage("Are you sure you want to return to the dashboard?" );
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            Intent intent = new Intent(Listworkforce_rfid.this, Admindashboard.class);
                            intent.putExtra("timeowner",timeowner);
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



            createLayoutDynamically(returnworkers);

        } catch(ArrayIndexOutOfBoundsException e) {

            textView.setText(Html.fromHtml("No Workers  added "));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            textView.setTypeface(null, Typeface.BOLD);



        }






    }





    public String getworkfore( String working ) {


        String url = "https://punchclock.ai/viewwf_rfid.php?cunq="+working;


        Log.i("action url",url);

        OkHttpClient client = new OkHttpClient();


        // String contentType = fileSource.toURL().openConnection().getContentType();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ttoken",working )
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





        return responseBody;
    }


    private void createLayoutDynamically( String scantext) {

        LinearLayout layout = (LinearLayout) findViewById(R.id.scnf);
        layout.setOrientation(LinearLayout.VERTICAL);


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        //params.gravity = Gravity.TOP;
        layout.setGravity(Gravity.CENTER|Gravity.TOP);

        params.setMargins(10, 5, 0, 30);

        System.out.println("number scantxt : "+ scantext );
        // String[] separated = scantext.split(Pattern.quote("|"));

        String[] dishout = scantext.split(Pattern.quote("*"));

        int makebtn = dishout.length ;
        String tline;
        String uid;
        //String fname;
        //String lname;
        String imgx;

        String printwforce = "<br>"
                + makebtn + " Staff listed";

        /*
        textView.setText(Html.fromHtml(printwforce));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);
        */

        TextView newtxt = new TextView(this);
        newtxt.setText(Html.fromHtml(printwforce));
        newtxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        newtxt.setTypeface(null, Typeface.BOLD);
        newtxt.setGravity(Gravity.CENTER);
        layout.addView(newtxt);


        System.out.println(makebtn + "number buttons: " + Arrays.toString(dishout));
        for (int i = 0; i < makebtn; i++) {


            tline = dishout[i] ;
            String[] sbtns = tline.split("~");
            uid = sbtns[0];
            fname = sbtns[1];
            lname = sbtns[2];
            imgx = sbtns[3];
            imgx = imgx.trim();
            System.out.println(makebtn + "action listed: " +  printwforce + "col:  " +  imgx );

            Button btn = new Button(this);





            btn.setId(i);
            btn.setTag(uid);
            final int id_ = btn.getId();
            btn.setText(" " + fname +  " - " + lname + " ");
            params.width = 300;
            btn.setLayoutParams(params);
            btn.setPadding(5, 5, 5, 5 );
            btn.setBackgroundColor(Color.rgb(249, 249, 249));
            layout.addView(btn);

            if(!imgx.equals("nn")) {
                Log.i("action we got", imgx);
                btn.setBackgroundColor(Color.GREEN);
            }

            btn = ((Button) findViewById(id_));


            btn.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {

                    final String tagname = (String)view.getTag();

                    Log.i("action tag", tagname);

                    String outres = getsingle(tagname);
                    String[] dishout = outres.split("~");

                    String ruid = dishout[0];
                    String rfname = dishout[1];
                     returnfname = dishout[1];
                    String rlname = dishout[2];


                    Log.i("action tagsingle", outres);

                    AlertDialog.Builder builder = new AlertDialog.Builder(Listworkforce_rfid.this);
                    builder.setTitle("Confirm");

                    builder.setMessage(Html.fromHtml("Confirm Registration for <br><br>" + rfname + " "+rlname));

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing, but close the dialog
                            dialog.dismiss();
                            System.out.println("action numbers tag "+ tagname);
                            String releaseid = tagname;
                            // Button btn = (Button)findViewById(id_);
                            //btn.setBackgroundColor(Color.GREEN);
                            //scanconfirm( releaseid );


                            Intent intent = new Intent(Listworkforce_rfid.this, Requestnfc_register.class);
                            intent.putExtra("userid",releaseid);
                            intent.putExtra("timeowner",timeowner);
                            intent.putExtra("fname",returnfname);
                            startActivity(intent);



                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();





                }



            });






        }//end make buttons






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


    public String getsingle( String wunq ) {


        String url = "https://punchclock.ai/viewwf_single.php?wunq="+wunq;


        Log.i("action url",url);

        OkHttpClient client = new OkHttpClient();


        // String contentType = fileSource.toURL().openConnection().getContentType();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ttoken",wunq )
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





        return responseBody;
    }





}
