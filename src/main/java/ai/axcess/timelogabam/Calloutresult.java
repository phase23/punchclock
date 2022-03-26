package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Calloutresult extends AppCompatActivity {

    Button callfinito;
    private long mLastClickTime = 0;
    public Handler handler;
    TextView cname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calloutresult);

        cname = (TextView)findViewById(R.id.emp);
        callfinito = (Button)findViewById(R.id.calloutfinish);

        String responsethis = getIntent().getExtras().getString("passthis");
        final String cunq = getIntent().getExtras().getString("cunq");

        responsethis = responsethis.trim();
        String[] separated = responsethis.split("~");
        String fname = separated[1];

        cname.setText("Hi " + fname);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // TODO: Your application init goes here.
                Intent mInHome = new Intent(Calloutresult.this, MainActivity.class);
                Calloutresult.this.startActivity(mInHome);
                Calloutresult.this.finish();
            }
        }, 60000);



        callfinito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callfinito.setEnabled(false);

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }

                mLastClickTime = SystemClock.elapsedRealtime();
                handler.removeCallbacksAndMessages(null);
                Intent intent = new Intent(Calloutresult.this, MainActivity.class);

                startActivity(intent);
                ((Activity) Calloutresult.this).finish();

            }
        });



    }





}