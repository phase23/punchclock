package ai.axcess.timelogabam;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class WifiControlActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SavedWifiPrefs";
    private static final String KEY_SAVED_SSID = "SavedSSID";
    private static final String KEY_SAVED_PASSWORD = "SavedPassword";
    private static final long SCAN_TIMEOUT = 10000;

    private WifiManager wifiManager;
    private BroadcastReceiver scanReceiver;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<ScanResult> scanResults;
    private boolean isScanning = false;
    private Handler scanHandler = new Handler();

    private Runnable scanTimeoutRunnable = () -> {
        if (isScanning) {
            isScanning = false;
            adapter.clear();
            adapter.add("Scan timeout - Tap 'Scan' to retry");
            Toast.makeText(WifiControlActivity.this, "Network scan timed out", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_control);

        Button backButton = findViewById(R.id.back_to_settings_button);
        backButton.setOnClickListener(v -> startActivity(new Intent(WifiControlActivity.this, aidHelp.class)));

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        listView = findViewById(R.id.wifi_list);
        Switch wifiToggle = findViewById(R.id.wifi_toggle);
        Button scanButton = findViewById(R.id.scan_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        scanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isScanning) return;
                isScanning = false;
                scanHandler.removeCallbacks(scanTimeoutRunnable);
                updateNetworkListWithInternetStatus();
            }
        };
        registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        wifiToggle.setChecked(wifiManager.isWifiEnabled());
        wifiToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            wifiManager.setWifiEnabled(isChecked);
            if (isChecked) {
                scanHandler.postDelayed(this::scanNetworks, 2000);
            } else {
                isScanning = false;
                scanHandler.removeCallbacks(scanTimeoutRunnable);
                adapter.clear();
                adapter.add("WiFi is disabled - Enable WiFi to scan");
            }
        });

        scanButton.setOnClickListener(v -> scanNetworks());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (isScanning || scanResults == null || position >= scanResults.size()) {
                Toast.makeText(this, "Please wait for scan to complete", Toast.LENGTH_SHORT).show();
                return;
            }

            String ssid = scanResults.get(position).SSID;
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String savedSsid = prefs.getString(KEY_SAVED_SSID, null);
            String savedPassword = prefs.getString(KEY_SAVED_PASSWORD, null);

            if (savedSsid != null && savedSsid.equals(ssid)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    connectToNetworkModern(savedSsid, savedPassword);
                } else {
                    connectToNetworkLegacy(savedSsid, savedPassword);
                }
            } else {
                promptForPasswordAndConnect(ssid);
            }
        });

        if (wifiManager.isWifiEnabled()) {
            scanNetworks();
        } else {
            adapter.add("WiFi is disabled - Enable WiFi to scan");
        }
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
            // For older versions, use getActiveNetworkInfo
            if (cm != null) {
                android.net.NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    private void updateNetworkListWithInternetStatus() {
        scanResults = wifiManager.getScanResults();
        adapter.clear();

        WifiInfo currentConnection = wifiManager.getConnectionInfo();
        String currentSsid = currentConnection != null ? currentConnection.getSSID().replace("\"", "") : "";
        boolean connectedHasInternet = hasInternetAccess();

        scanResults.sort((a, b) -> {
            boolean aIsConnected = a.SSID.equals(currentSsid);
            boolean bIsConnected = b.SSID.equals(currentSsid);
            if (aIsConnected && !bIsConnected) return -1;
            if (!aIsConnected && bIsConnected) return 1;
            return Integer.compare(b.level, a.level);
        });

        for (ScanResult result : scanResults) {
            int signalLevel = WifiManager.calculateSignalLevel(result.level, 5);
            StringBuilder barsBuilder = new StringBuilder();
            for (int i = 0; i < signalLevel; i++) {
                barsBuilder.append("\u00A6");
            }
            String bars = barsBuilder.toString();
            String colorPrefix = (signalLevel >= 4) ? "[Strong] " : (signalLevel >= 3) ? "[Good] " : (signalLevel >= 2) ? "[Fair] " : "[Weak] ";
            String displayName = result.SSID + " " + colorPrefix + "[" + bars + "]";
            if (result.SSID.equals(currentSsid)) {
                displayName += connectedHasInternet ? " (Connected - Internet)" : " (Connected - No Internet)";
            }
            adapter.add(displayName);
        }

        Toast.makeText(WifiControlActivity.this, "Found " + scanResults.size() + " networks", Toast.LENGTH_SHORT).show();
    }

    private void promptForPasswordAndConnect(String ssid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter password for " + ssid);

        final EditText input = new EditText(this);
        input.setHint("Password (leave blank for open network)");
        builder.setView(input);

        builder.setPositiveButton("Connect", (dialog, which) -> {
            String password = input.getText().toString();
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString(KEY_SAVED_SSID, ssid)
                    .putString(KEY_SAVED_PASSWORD, password)
                    .apply();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToNetworkModern(ssid, password);
            } else {
                connectToNetworkLegacy(ssid, password);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Forget", (dialog, which) -> {
            forgetNetwork(ssid);
            Toast.makeText(this, "Forgot " + ssid, Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void forgetNetwork(String ssid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Toast.makeText(this, "Forget network not supported on Android 10+", Toast.LENGTH_SHORT).show();
            return;
        }

        List<WifiConfiguration> configuredNetworks = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                configuredNetworks = wifiManager.getConfiguredNetworks();
            } catch (SecurityException e) {
                Toast.makeText(this, "Permission denied to access configured networks", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location permission required to manage networks", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.remove(KEY_SAVED_SSID);
        editor.remove(KEY_SAVED_PASSWORD);
        editor.apply();

        scanHandler.postDelayed(this::scanNetworks, 2000);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectToNetworkModern(String ssid, String password) {
        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder().setSsid(ssid);
        if (!password.isEmpty()) {
            builder.setWpa2Passphrase(password);
        }
        WifiNetworkSpecifier specifier = builder.build();

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                cm.bindProcessToNetwork(network);
                runOnUiThread(() -> {
                    Toast.makeText(WifiControlActivity.this, "Connected to " + ssid, Toast.LENGTH_SHORT).show();
                    scanNetworks();
                });
            }

            @Override
            public void onUnavailable() {
                runOnUiThread(() -> {
                    Toast.makeText(WifiControlActivity.this, "Failed to connect to " + ssid, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void connectToNetworkLegacy(String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = String.format("\"%s\"", ssid);
        if (password.isEmpty()) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            config.preSharedKey = String.format("\"%s\"", password);
        }

        int netId = wifiManager.addNetwork(config);
        if (netId != -1) {
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            runOnUiThread(() -> {
                Toast.makeText(this, "Connected to " + ssid, Toast.LENGTH_SHORT).show();
                scanHandler.postDelayed(this::scanNetworks, 3000);
            });
        } else {
            Toast.makeText(this, "Failed to connect to " + ssid, Toast.LENGTH_SHORT).show();
        }
    }

    private void scanNetworks() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Please enable Wi-Fi first", Toast.LENGTH_SHORT).show();
            adapter.clear();
            adapter.add("WiFi is disabled - Enable WiFi to scan");
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            Toast.makeText(this, "Location services must be enabled", Toast.LENGTH_LONG).show();
            adapter.clear();
            adapter.add("Location services required - Enable location to scan");
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            adapter.clear();
            adapter.add("Location permission required - Grant permission to scan");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        isScanning = true;
        adapter.clear();
        adapter.add("Searching for networks...");
        scanHandler.postDelayed(scanTimeoutRunnable, SCAN_TIMEOUT);

        boolean scanStarted = wifiManager.startScan();
        if (!scanStarted) {
            isScanning = false;
            scanHandler.removeCallbacks(scanTimeoutRunnable);
            adapter.clear();
            adapter.add("Scan failed - Tap 'Scan' to retry");
            Toast.makeText(this, "Failed to start network scan", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Scanning for networks...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanReceiver != null) {
            unregisterReceiver(scanReceiver);
        }
        scanHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted. Scanning networks...", Toast.LENGTH_SHORT).show();
            scanNetworks();
        } else {
            Toast.makeText(this, "Location permission is required to scan Wi-Fi", Toast.LENGTH_LONG).show();
            adapter.clear();
            adapter.add("Location permission required - Grant permission to scan");
        }
    }
}
