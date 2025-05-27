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
import android.widget.CompoundButton;
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
    private static final long SCAN_TIMEOUT = 10000; // 10 seconds timeout

    private WifiManager wifiManager;
    private BroadcastReceiver scanReceiver;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<ScanResult> scanResults;
    private boolean isScanning = false;
    private Handler scanHandler = new Handler();

    // Timeout runnable for scan operations
    private Runnable scanTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (isScanning) {
                isScanning = false;
                adapter.clear();
                adapter.add("Scan timeout - Tap 'Scan' to retry");
                Toast.makeText(WifiControlActivity.this, "Network scan timed out", Toast.LENGTH_SHORT).show();
            }
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

        // Initialize adapter with searching message
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        scanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isScanning) return; // Ignore if we're not actively scanning

                // Clear scanning state
                isScanning = false;
                scanHandler.removeCallbacks(scanTimeoutRunnable);

                scanResults = wifiManager.getScanResults();
                adapter.clear();

                if (scanResults == null || scanResults.isEmpty()) {
                    adapter.add("No networks found - Tap 'Scan' to retry");
                    return;
                }

                // Refresh current connection info after scan
                WifiInfo updatedConnection = wifiManager.getConnectionInfo();
                String currentSsid = updatedConnection != null ? updatedConnection.getSSID().replace("\"", "") : "";

                scanResults.sort((a, b) -> {
                    boolean aIsConnected = a.SSID.equals(currentSsid);
                    boolean bIsConnected = b.SSID.equals(currentSsid);
                    if (aIsConnected && !bIsConnected) return -1;
                    if (!aIsConnected && bIsConnected) return 1;
                    return Integer.compare(b.level, a.level); // Sort by signal strength
                });

                for (ScanResult result : scanResults) {
                    int signalLevel = WifiManager.calculateSignalLevel(result.level, 5);
                    StringBuilder barsBuilder = new StringBuilder();
                    for (int i = 0; i < signalLevel; i++) {
                        barsBuilder.append("¦");
                    }
                    String bars = barsBuilder.toString(); // Unicode bar blocks
                    String colorPrefix;
                    if (signalLevel >= 4) {
                        colorPrefix = "[Strong] ";
                    } else if (signalLevel >= 3) {
                        colorPrefix = "[Good] ";
                    } else if (signalLevel >= 2) {
                        colorPrefix = "[Fair] ";
                    } else {
                        colorPrefix = "[Weak] ";
                    }

                    String displayName = result.SSID + " " + colorPrefix + "[" + bars + "]";
                    if (result.SSID.equals(currentSsid)) {
                        displayName += " (Connected)";
                    }
                    adapter.add(displayName);
                }

                // Show scan complete message
                Toast.makeText(WifiControlActivity.this, "Found " + scanResults.size() + " networks", Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        wifiToggle.setChecked(wifiManager.isWifiEnabled());
        wifiToggle.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            wifiManager.setWifiEnabled(isChecked);
            if (isChecked) {
                // Auto-scan when WiFi is enabled
                scanHandler.postDelayed(() -> scanNetworks(), 2000); // Wait 2 seconds for WiFi to stabilize
            } else {
                // Clear list when WiFi is disabled
                isScanning = false;
                scanHandler.removeCallbacks(scanTimeoutRunnable);
                adapter.clear();
                adapter.add("WiFi is disabled - Enable WiFi to scan");
            }
        });

        scanButton.setOnClickListener((View v) -> scanNetworks());

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

        // Start initial scan if WiFi is enabled
        if (wifiManager.isWifiEnabled()) {
            scanNetworks();
        } else {
            adapter.add("WiFi is disabled - Enable WiFi to scan");
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

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            adapter.clear();
            adapter.add("Location permission required - Grant permission to scan");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Set scanning state
        isScanning = true;
        adapter.clear();
        adapter.add("Searching for networks...");

        // Start timeout handler
        scanHandler.postDelayed(scanTimeoutRunnable, SCAN_TIMEOUT);

        // Start the actual scan
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

    private void promptForPasswordAndConnect(String ssid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter password for " + ssid);

        final EditText input = new EditText(this);
        input.setHint("Password (leave blank for open network)");
        builder.setView(input);

        builder.setPositiveButton("Connect", (DialogInterface dialog, int which) -> {
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
        builder.show();
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
                    // Rescan to update the list with connection status
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
                // Rescan to update the list with connection status
                scanHandler.postDelayed(() -> scanNetworks(), 3000); // Wait 3 seconds for connection to establish
            });
        } else {
            Toast.makeText(this, "Failed to connect to " + ssid, Toast.LENGTH_SHORT).show();
        }
    }
}