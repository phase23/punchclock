package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class aidHelp extends AppCompatActivity {
    TextView helpbx;
    Button rrhome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid_help);

        String thismydevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        helpbx = (TextView) findViewById(R.id.devicebx);
        rrhome = (Button)findViewById(R.id.returnhome);

        helpbx.setText(thismydevice);


        rrhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent returnhelp = new Intent(aidHelp.this, MainActivity.class);
                startActivity(returnhelp);


            }

        });


    }
}