package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.telephony.SignalStrength;
import android.telephony.PhoneStateListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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

    // Add flag to track if we're in searching state
    private boolean isSearchingNetwork = false;
    private long searchStartTime = 0;
    private static final long SEARCH_TIMEOUT = 10000; // 10 seconds timeout
    private Handler searchHandler = new Handler();

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if we have an actual network connection before clearing search state
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

    // Runnable to handle search timeout
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
            Intent returnhelp = new Intent(aidHelp.this, MainActivity.class);
            startActivity(returnhelp);
        });

        btnapn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopLockTask();
            }
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

        btnwifi.setOnClickListener(v -> {
            Intent intent = new Intent(aidHelp.this, WifiControlActivity.class);
            startActivity(intent);
        });

        btndata.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopLockTask();
            }
            Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
            // Set searching state
            isSearchingNetwork = true;
            searchStartTime = System.currentTimeMillis();
            statusText.setText("Searching network...");

            // Start timeout handler
            searchHandler.postDelayed(searchTimeoutRunnable, SEARCH_TIMEOUT);

            wifiManager.setWifiEnabled(isChecked);
            String msg = isChecked ? "Enabling Wi-Fi..." : "Switching to Mobile Data...";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

            if (isChecked && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.reconnect();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PHONE_STATE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            listenForSignalStrength();
            updateNetworkStatus();
        }
    }

    private void listenForSignalStrength() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                signalStrengthDbm = getSignalStrengthDbm(signalStrength);
                if (!isSearchingNetwork) {
                    updateNetworkStatus();
                }
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

    private void updateSwitchText() {
        if (isSearchingNetwork) return; // Don't update switch text while searching

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
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        toggleSwitch.setText("Switch to Mobile Data");
                    } else {
                        toggleSwitch.setText("Switch to Wi-Fi");
                    }
                } else {
                    toggleSwitch.setText("Switch to Wi-Fi");
                }
            }
        }
    }

    private void updateNetworkStatus() {
        if (isSearchingNetwork) return; // Don't update status while searching
        statusText.setText(getCurrentNetworkStatus());
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

    private String getCurrentNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo != null ? wifiInfo.getSSID() : "Unknown SSID";
                        int rssi = wifiInfo.getRssi();
                        String bars = getSignalBars(rssi, true);
                        return "Currently using: Wi-Fi (" + ssid + ", Signal: " + rssi + " dBm " + bars + ")";
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        String carrierName = getCarrierName(tm);
                        String networkType = getNetworkType(tm);
                        String bars = getSignalBars(signalStrengthDbm, false);
                        return "Currently using: Mobile Data (" + carrierName + ", " + networkType + ", Signal: " + signalStrengthDbm + " dBm " + bars + ")";
                    } else {
                        return "No active network";
                    }
                } else {
                    return "No active network";
                }
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo != null ? wifiInfo.getSSID() : "Unknown SSID";
                        int rssi = wifiInfo.getRssi();
                        String bars = getSignalBars(rssi, true);
                        return "Currently using: Wi-Fi (" + ssid + ", Signal: " + rssi + " dBm " + bars + ")";
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        String carrierName = getCarrierName(tm);
                        String networkType = getNetworkType(tm);
                        String bars = getSignalBars(signalStrengthDbm, false);
                        return "Currently using: Mobile Data (" + carrierName + ", " + networkType + ", Signal: " + signalStrengthDbm + " dBm " + bars + ")";
                    } else {
                        return "Connected to: Other Network";
                    }
                } else {
                    return "No active network";
                }
            }
        }
        return "No active network";
    }

    private String getCarrierName(TelephonyManager tm) {
        if (tm == null) {
            return "Unknown Carrier";
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return "Carrier Info Unavailable";
        }

        try {
            String carrierName = tm.getSimOperatorName();
            return (carrierName != null && !carrierName.isEmpty()) ? carrierName : "Unknown Carrier";
        } catch (SecurityException e) {
            return "Carrier Info Unavailable";
        } catch (Exception e) {
            return "Unknown Carrier";
        }
    }

    private String getNetworkType(TelephonyManager tm) {
        if (tm == null) return "Unknown";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return "Unknown";
        }

        try {
            switch (tm.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_NR:
                    return "5G";
                default:
                    return "Unknown";
            }
        } catch (SecurityException e) {
            return "Unknown";
        }
    }

    private int getSignalStrengthDbm(SignalStrength signalStrength) {
        if (signalStrength == null) return -1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return signalStrength.getCellSignalStrengths().isEmpty() ? -1 :
                    signalStrength.getCellSignalStrengths().get(0).getDbm();
        } else {
            try {
                java.lang.reflect.Method method = SignalStrength.class.getMethod("getDbm");
                Object result = method.invoke(signalStrength);
                return (int) result;
            } catch (Exception e) {
                return -1;
            }
        }
    }
}