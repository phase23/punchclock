package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class deviceSetupcompleted extends AppCompatActivity {

    Button continueon;
    TextView usere;
    TextView helpbx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setupcompleted);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        continueon = (Button)findViewById(R.id.continuethis);
        String email = getIntent().getExtras().getString("email");

        String thismydevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        usere = (TextView) findViewById(R.id.acce);
        helpbx = (TextView) findViewById(R.id.helpbox);
        usere.setText(email);

        helpbx.setText("If you are having issues contact support with ID" + thismydevice );



        continueon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent connectedgood = new Intent(deviceSetupcompleted.this, MainActivity.class);
                startActivity(connectedgood);


            }

            });




    }







}