package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Admindashboard extends AppCompatActivity {
    Button llogout;
    ImageView facer;
    ImageView chiper;
    String cunq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);

         cunq = getIntent().getExtras().getString("timeowner");

        FullScreencall();

        llogout = (Button)findViewById(R.id.logoutbtn);
        facer = (ImageView) findViewById(R.id.rface);
        chiper = (ImageView) findViewById(R.id.rchip);

        llogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gohome = new Intent(Admindashboard.this, Adminpanel.class);
                startActivity(gohome);



            }
        });

        facer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Admindashboard.this, Listworkforce.class);
                intent.putExtra("timeowner",cunq);
                startActivity(intent);



            }
        });

        chiper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Admindashboard.this, Listworkforce_rfid.class);
                intent.putExtra("timeowner",cunq);
                startActivity(intent);



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

}