package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Pattern;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import ai.axcess.timelogabam.R;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.content.ContentValues.TAG;

public class Requestpass extends AppCompatActivity {


    private NfcAdapter nfcAdapter;
    private TextView promt;
    private byte password[] = { (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
    public List<ai.axcess.timelogabam.Nfc> list;
    private ListView listView;
    private Intent intents;
    public static String nfcTagSerialNum;

    private boolean isnews = true;
    private PendingIntent pendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private ai.axcess.timelogabam.Nfc mynfc;
    private String dataString;
    String responseBody;
    String putall;
    public Handler handler;
    TextView prompt;

    MediaPlayer playerNFC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requestnfc);


        prompt = (TextView)findViewById(R.id.promt);




        FullScreencall();




        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            promt.setText("Device can not support NFC��");
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            promt.setText("Please open NFC in system setting��");
            finish();
            return;
        }


        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        ndef.addCategory("*/*");
        mFilters = new IntentFilter[] { ndef };// ������
        mTechLists = new String[][] {
                new String[] { MifareClassic.class.getName() },
                new String[] { NfcA.class.getName() } };// ����ɨ��ı�ǩ����

        playerNFC = MediaPlayer.create(getApplicationContext(), R.raw.success);







        final Button bntcncel = (Button) findViewById(R.id.cancelnfc);

        bntcncel.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Log.d("ckil", "button click");

                Intent intent = new Intent(Requestpass.this, Adminpanel.class);
                startActivity(intent);

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        // �õ��Ƿ��⵽ACTION_TECH_DISCOVERED����
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters,
                mTechLists);
        if (isnews) {
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent()
                    .getAction())) {
                // �����intent
                processIntent2(getIntent());
                // processIntent1(getIntent());
                intents = getIntent();
                isnews = false;
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        // �õ��Ƿ��⵽ACTION_TECH_DISCOVERED����
        // nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters,
        // mTechLists);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            // �����intent
            // processIntent1(intent);
            processIntent2(intent);
            intents = intent;
        }

    }

    public void processIntent2(Intent intent) {

        read(intent);// ������

        //write(intent);// д����

        super.onNewIntent(intent);

    }


    public static String reverseHex(String originalHex) {
        // TODO: Validation that the length is even
        int lengthInBytes = originalHex.length() / 2;
        char[] chars = new char[lengthInBytes * 2];
        for (int index = 0; index < lengthInBytes; index++) {
            int reversedIndex = lengthInBytes - 1 - index;
            chars[reversedIndex * 2] = originalHex.charAt(index * 2);
            chars[reversedIndex * 2 + 1] = originalHex.charAt(index * 2 + 1);
        }
        return new String(chars);
    }


    public String capturetag( String uid ) {

        String deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        //String url = "https://aaxcess.com/getrfid.php?rfid="+uid + "&getdevice="+deviceId;;
        String url = "https://punchclock.ai/registerapp_raw.php?keyfob="+uid + "&getdevice="+deviceId;;



        Log.i("uidurl",url);
        OkHttpClient client = new OkHttpClient();


        // String contentType = fileSource.toURL().openConnection().getContentType();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("swipfob",uid )
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

        return  responseBody;
    }


    public void read(Intent intent) {

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[]  uuid = tag.getId();



        StringBuilder sb = new StringBuilder();
        for (byte b : uuid) {
            sb.append(String.format("%02X", b));
        };

        nfcTagSerialNum = sb.toString();
        Log.e("nfc ID", nfcTagSerialNum);

        String littleedian = reverseHex(nfcTagSerialNum);
        Log.d("UID/",  littleedian);
        BigInteger decimal = new BigInteger(littleedian, 16);
        System.out.println("UID/: " + decimal);

        String struid = decimal.toString();
        Log.d("UID str/",  struid);

        int uidl = struid.length();

        if(uidl < 10){
            struid = '0'+struid;
        }

        Log.d("UID adj str/",  struid);

        String showres = capturetag(struid);

        FullScreencall();


        Log.i("action responseserver", showres);

        String[] separated = showres.split(Pattern.quote("|"));

        String rekstat = separated[0];
        System.out.println("out number " + rekstat);
        int myNum = 0;

        try {
            myNum = Integer.parseInt(rekstat);
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }


        if (myNum == 2) {

            //nopass.setVisibility(View.VISIBLE);
            //wait.setVisibility(View.INVISIBLE);

            Intent intent2 = new Intent(Requestpass.this, Adminpanel.class);
            startActivity(intent2);
        }


        if (myNum == 1) {
           // handler.removeCallbacksAndMessages(null);

            String timeowner = separated[1];
            Log.i("action wtoken", timeowner);

            Intent intent2 = new Intent(Requestpass.this, Admindashboard.class);
            intent2.putExtra("timeowner",timeowner);
            startActivity(intent2);


        }





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
