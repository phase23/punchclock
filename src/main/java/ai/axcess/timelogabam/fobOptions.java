package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class fobOptions extends AppCompatActivity {

    public Handler handler;
    public String camaction;
    public String cunq;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fob_options);

        cunq = getIntent().getExtras().getString("cunq");


        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // TODO: Your application init goes here.
                Intent mInHome = new Intent(fobOptions.this, MainActivity.class);
                fobOptions.this.startActivity(mInHome);
                fobOptions.this.finish();
            }
        },60000);



        final Button btnfobin = (Button) findViewById(R.id.btnfobin);
        final Button btnfobout = (Button) findViewById(R.id.btnfobout);
        final Button btnClockincancel = (Button) findViewById(R.id.btnClockincancel);



        btnClockincancel.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Log.d("ckil", "button click");

                btnClockincancel.setEnabled(false);

                btnfobout.setEnabled(false);

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }

                Intent intent = new Intent(fobOptions.this, MainActivity.class);
                startActivity(intent);
                handler.removeCallbacksAndMessages(null);

            }
        });

        btnfobin.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                btnfobin.setEnabled(false);


                btnfobout.setEnabled(false);

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }


                Log.d("ckil", "button click");
                camaction = "1";

                handler.removeCallbacksAndMessages(null);
                Intent intent = new Intent(fobOptions.this, Requestnfc.class);
                intent.putExtra("cunq",cunq);
                intent.putExtra("camaction",camaction);
                startActivity(intent);

            }
        });

        btnfobout.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Log.d("ckil", "button click");
                camaction = "2";

                btnfobout.setEnabled(false);

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }

                mLastClickTime = SystemClock.elapsedRealtime();

                handler.removeCallbacksAndMessages(null);
                Intent intent = new Intent(fobOptions.this, Requestnfc.class);
                intent.putExtra("cunq",cunq);
                intent.putExtra("camaction",camaction);
                startActivity(intent);


            }
        });





    }






}