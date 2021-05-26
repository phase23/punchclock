package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class deviceSetupcompleted extends AppCompatActivity {

    Button continueon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setupcompleted);

        continueon = (Button)findViewById(R.id.continuethis);

        continueon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent connectedgood = new Intent(deviceSetupcompleted.this, MainActivity.class);
                startActivity(connectedgood);


            }

            });




    }







}