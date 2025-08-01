package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class aidHelp extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 1001;

    TextView helpbx;
    Button rrhome;
    Button btnwifi;
    Button btndata;
    Button btnapn;

    private WifiManager wifiManager;
    private TextView statusText;
    private Switch toggleSwitch;
    private int signalStrengthDbm = -1;
    private boolean isSearchingNetwork = false;
    private long searchStartTime = 0;
    private static final long SEARCH_TIMEOUT = 10000;
    private Handler searchHandler = new Handler();

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isSearchingNetwork && isNetworkConnected()) {
                isSearchingNetwork = false;
                searchHandler.removeCallbacksAndMessages(null);
            }

            if (!isSearchingNetwork) {
                updateNetworkStatus();
                updateSwitchText();
            }
        }
    };

    private Runnable searchTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (isSearchingNetwork) {
                isSearchingNetwork = false;
                updateNetworkStatus();
                updateSwitchText();
                Toast.makeText(aidHelp.this, "Network search timed out", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid_help);

        String thismydevice = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        helpbx = findViewById(R.id.devicebx);
        rrhome = findViewById(R.id.returnhome);
        btnwifi = findViewById(R.id.wificon);
        btndata = findViewById(R.id.data);
        btnapn = findViewById(R.id.apn);
        helpbx.setText(thismydevice);


          rrhome.setOnClickListener(v -> {

                 /*
              Intent intent = new Intent(aidHelp.this, MainActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
              startActivity(intent);
              finish();

                  */

              Intent intent = new Intent(aidHelp.this, MainActivity.class);
              startActivity(intent);
              ((Activity) aidHelp.this).finish();

           });

        btnapn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) stopLockTask();
            try {
                Intent apnIntent = new Intent(Intent.ACTION_MAIN);
                apnIntent.setClassName("com.android.settings", "com.android.settings.ApnSettings");
                startActivity(apnIntent);
            } catch (Exception e) {
                Intent fallback = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(fallback);
                Toast.makeText(getApplicationContext(), "Click on Access Point Names", Toast.LENGTH_LONG).show();
            }
        });

        btnwifi.setOnClickListener(v -> startActivity(new Intent(aidHelp.this, WifiControlActivity.class)));
        btndata.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) stopLockTask();
            startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        statusText = findViewById(R.id.network_status);
        toggleSwitch = findViewById(R.id.toggle_network_switch);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            listenForSignalStrength();
        }

        updateNetworkStatus();
        toggleSwitch.setChecked(wifiManager.isWifiEnabled());
        updateSwitchText();

        toggleSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            isSearchingNetwork = true;
            searchStartTime = System.currentTimeMillis();
            statusText.setText("Searching network...");
            searchHandler.postDelayed(searchTimeoutRunnable, SEARCH_TIMEOUT);

            wifiManager.setWifiEnabled(isChecked);
            String msg = isChecked ? "Enabling Wi-Fi..." : "Switching to Mobile Data...";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

            if (isChecked && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) wifiManager.reconnect();
        });
    }

    private boolean hasInternetAccess() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork != null) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
                    return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }

    private String getCurrentNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        boolean internet = hasInternetAccess();
        String internetNote = internet ? " [Internet OK]" : " [No Internet]";

        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo != null ? wifiInfo.getSSID() : "Unknown SSID";
                        int rssi = wifiInfo.getRssi();
                        String bars = getSignalBars(rssi, true);
                        return "Currently using: Wi-Fi (" + ssid + ", Signal: " + rssi + " dBm " + bars + ")" + internetNote;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        String carrierName = getCarrierName(tm);
                        String networkType = getNetworkType(tm);
                        String bars = getSignalBars(signalStrengthDbm, false);
                        return "Currently using: Mobile Data (" + carrierName + ", " + networkType + ", Signal: " + signalStrengthDbm + " dBm " + bars + ")" + internetNote;
                    } else {
                        return "No active network" + internetNote;
                    }
                }
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo != null ? wifiInfo.getSSID() : "Unknown SSID";
                        int rssi = wifiInfo.getRssi();
                        String bars = getSignalBars(rssi, true);
                        return "Currently using: Wi-Fi (" + ssid + ", Signal: " + rssi + " dBm " + bars + ")" + internetNote;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        String carrierName = getCarrierName(tm);
                        String networkType = getNetworkType(tm);
                        String bars = getSignalBars(signalStrengthDbm, false);
                        return "Currently using: Mobile Data (" + carrierName + ", " + networkType + ", Signal: " + signalStrengthDbm + " dBm " + bars + ")" + internetNote;
                    } else {
                        return "Connected to: Other Network" + internetNote;
                    }
                }
            }
        }
        return "No active network" + internetNote;
    }

    private void updateNetworkStatus() {
        if (!isSearchingNetwork) statusText.setText(getCurrentNetworkStatus());
    }

    private void updateSwitchText() {
        if (isSearchingNetwork) return;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    toggleSwitch.setText("Switch to Mobile Data");
                } else {
                    toggleSwitch.setText("Switch to Wi-Fi");
                }
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    toggleSwitch.setText(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ? "Switch to Mobile Data" : "Switch to Wi-Fi");
                } else {
                    toggleSwitch.setText("Switch to Wi-Fi");
                }
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

    private void listenForSignalStrength() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                signalStrengthDbm = getSignalStrengthDbm(signalStrength);
                if (!isSearchingNetwork) updateNetworkStatus();
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private String getSignalBars(int dBm, boolean isWifi) {
        if (isWifi) {
            if (dBm >= -50) return "★★★★★";
            else if (dBm >= -60) return "★★★★☆";
            else if (dBm >= -70) return "★★★☆☆";
            else if (dBm >= -80) return "★★☆☆☆";
            else return "★☆☆☆☆";
        } else {
            if (dBm >= -85) return "★★★★★";
            else if (dBm >= -95) return "★★★★☆";
            else if (dBm >= -105) return "★★★☆☆";
            else if (dBm >= -115) return "★★☆☆☆";
            else return "★☆☆☆☆";
        }
    }

    private String getCarrierName(TelephonyManager tm) {
        if (tm == null) return "Unknown Carrier";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) return "Carrier Info Unavailable";
        try {
            String carrierName = tm.getSimOperatorName();
            return (carrierName != null && !carrierName.isEmpty()) ? carrierName : "Unknown Carrier";
        } catch (Exception e) {
            return "Unknown Carrier";
        }
    }

    private String getNetworkType(TelephonyManager tm) {
        if (tm == null) return "Unknown";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) return "Unknown";
        try {
            switch (tm.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_LTE: return "4G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP: return "3G";
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS: return "2G";
                case TelephonyManager.NETWORK_TYPE_NR: return "5G";
                default: return "Unknown";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private int getSignalStrengthDbm(SignalStrength signalStrength) {
        if (signalStrength == null) return -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return signalStrength.getCellSignalStrengths().isEmpty() ? -1 : signalStrength.getCellSignalStrengths().get(0).getDbm();
        } else {
            try {
                java.lang.reflect.Method method = SignalStrength.class.getMethod("getDbm");
                return (int) method.invoke(signalStrength);
            } catch (Exception e) {
                return -1;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
        searchHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PHONE_STATE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            listenForSignalStrength();
            updateNetworkStatus();
        }
    }
}
