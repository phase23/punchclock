package ai.axcess.timelogabam;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_STORAGE_REQUEST_CODE = 101;
    private static final int ALL_PERMISSIONS = 102;

    // UI Components
    private ImageView facelog, nfcread, away, wrenhr, pinpad, adminlevel;
    private TextView textView, locationplace, justwait;
    private View thisview;

    // Core variables
    private String deviceId;
    private Handler mainHandler;
    private WifiManager wifiManager;
    private String locationnow;
    private String thelocation;
    private String returndevice;

    // Network and threading
    private ExecutorService backgroundExecutor;
    private OkHttpClient httpClient;
    private volatile boolean isDestroyed = false;

    // Cache for quick access
    private Boolean isOnlineCache = null;
    private long lastOnlineCheck = 0;
    private static final long ONLINE_CHECK_CACHE_DURATION = 5000; // 5 seconds

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize core components first
        initializeCoreComponents();

        // Setup UI immediately (no blocking operations)
        setupUIComponents();

        // Setup device policy and lock task
        setupDevicePolicy();

        // Check permissions (non-blocking)
        checkAndRequestPermissions();

        // Initialize background operations
        initializeBackgroundOperations();

        // Start all network operations in background
        startBackgroundTasks();
    }

    private void initializeCoreComponents() {
        mainHandler = new Handler(Looper.getMainLooper());
        backgroundExecutor = Executors.newFixedThreadPool(3); // Limit concurrent operations

        // Initialize HTTP client with optimized settings
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void setupUIComponents() {
        // Initialize all UI components
        justwait = findViewById(R.id.wait);
        thisview = findViewById(R.id.barup);
        adminlevel = findViewById(R.id.gear);
        away = findViewById(R.id.ontravel);
        pinpad = findViewById(R.id.dailpad);
        wrenhr = findViewById(R.id.wrench);
        facelog = findViewById(R.id.facial);
        nfcread = findViewById(R.id.nfcfob);
        textView = findViewById(R.id.setinternet);
        locationplace = findViewById(R.id.location);

        // Set initial UI state
        locationplace.setText("Loading location...");
        textView.setText("Checking connection...");

        // Setup click listeners immediately (they'll handle their own connectivity checks)
        setupClickListeners();
    }

    private void setupClickListeners() {
        adminlevel.setOnClickListener(v -> {
            justwait.setVisibility(View.VISIBLE);
            startActivity(new Intent(MainActivity.this, Adminpanel.class));
        });

        wrenhr.setOnClickListener(v -> {
            justwait.setVisibility(View.VISIBLE);
            startActivity(new Intent(MainActivity.this, aidHelp.class));
        });

        facelog.setOnClickListener(v -> new OptimizedCamTask().executeOnExecutor(backgroundExecutor));

        away.setOnClickListener(v -> {
            justwait.setVisibility(View.VISIBLE);
            backgroundExecutor.execute(() -> {
                if (isOnlineFast()) {
                    String cunq = readFile();
                    mainHandler.post(() -> {
                        Intent intent = new Intent(MainActivity.this, SurfaceCameraTravel.class);
                        intent.putExtra("cunq", cunq);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    });
                } else {
                    showNoInternetError();
                }
            });
        });

        pinpad.setOnClickListener(v -> new OptimizedPinpadTask().executeOnExecutor(backgroundExecutor));

        nfcread.setOnClickListener(v -> {
            backgroundExecutor.execute(() -> {
                if (isOnlineFast()) {
                    String cunq = readFile();
                    mainHandler.post(() -> {
                        justwait.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(MainActivity.this, fobOptions.class);
                        intent.putExtra("cunq", cunq);
                        startActivity(intent);
                    });
                } else {
                    showNoInternetError();
                }
            });
        });
    }

    private void setupDevicePolicy() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(this, MyDeviceAdminReceiver.class);
            dpm.setLockTaskPackages(admin, new String[]{"ai.axcess.timelogabam"});
            startLockTask();
        } catch (Exception e) {
            Log.e("MainActivity", "Error setting up device policy", e);
        }
    }

    private void checkAndRequestPermissions() {
        final String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
        }
    }

    private void initializeBackgroundOperations() {
        // Create base file if needed
        backgroundExecutor.execute(this::createfile);

        // Setup WiFi hotspot protection
        backgroundExecutor.execute(() -> {
            try {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                setWifiEnabled(wifiConfig, false);
            } catch (Exception e) {
                Log.e("MainActivity", "Error disabling WiFi hotspot", e);
            }
        });

        // Show write permission settings if needed
        backgroundExecutor.execute(() -> {
            try {
                showWritePermissionSettings();
            } catch (Exception e) {
                Log.e("MainActivity", "Error checking write permissions", e);
            }
        });
    }

    private void startBackgroundTasks() {
        // Quick connectivity check first
        backgroundExecutor.execute(() -> {
            boolean connected = isConnectedQuick();

            mainHandler.post(() -> {
                if (!connected) {
                    textView.setText("No Internet Connection");
                    Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this, Nointernet.class));
                } else {
                    textView.setText("Connected");
                    // Start service only if connected
                    startService(new Intent(MainActivity.this, Myservice.class));
                }
            });

            if (connected) {
                // Start other background tasks in parallel
                backgroundExecutor.execute(this::checkDeviceRegistration);
                backgroundExecutor.execute(this::loadDeviceLocation);
            }
        });
    }

    private boolean isConnectedQuick() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isOnlineFast() {
        long currentTime = System.currentTimeMillis();

        // Use cached result if recent
        if (isOnlineCache != null && (currentTime - lastOnlineCheck) < ONLINE_CHECK_CACHE_DURATION) {
            return isOnlineCache;
        }

        // Quick connectivity check first
        if (!isConnectedQuick()) {
            isOnlineCache = false;
            lastOnlineCheck = currentTime;
            return false;
        }

        // Ping test (but with timeout)
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -W 2 8.8.8.8");
            boolean result = process.waitFor() == 0;
            isOnlineCache = result;
            lastOnlineCheck = currentTime;
            return result;
        } catch (Exception e) {
            isOnlineCache = false;
            lastOnlineCheck = currentTime;
            return false;
        }
    }

    private void checkDeviceRegistration() {
        if (isDestroyed) return;

        try {
            String url = "https://punchclock.ai/devicesetup.php?action=checkdevice&token=" + deviceId;
            Request request = new Request.Builder().url(url).build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("MainActivity", "Device registration check failed", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (isDestroyed) return;

                    String result = response.body().string();
                    Log.i("DeviceCheck", result);

                    mainHandler.post(() -> {
                        if (!isDestroyed && result.trim().equals("not found")) {
                            startActivity(new Intent(MainActivity.this, Startup.class));
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error checking device registration", e);
        }
    }

    private void loadDeviceLocation() {
        if (isDestroyed) return;

        try {
            String url = "https://punchclock.ai/getdevicelocation.php?token=" + deviceId;
            Request request = new Request.Builder().url(url).build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("MainActivity", "Location loading failed", e);
                    mainHandler.post(() -> {
                        if (!isDestroyed) {
                            locationplace.setText("Location unavailable > Choose Option");
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (isDestroyed) return;

                    String location = response.body().string();
                    Log.i("DeviceLocation", location);

                    mainHandler.post(() -> {
                        if (!isDestroyed) {
                            thelocation = location;
                            locationplace.setText(location + " > Choose Option");
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading device location", e);
        }
    }

    private void showNoInternetError() {
        mainHandler.post(() -> {
            if (!isDestroyed) {
                Toast.makeText(getApplicationContext(), "Check Internet & Restart App", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, Nointernet.class));
            }
        });
    }

    public String readFile() {
        String fileName = "base.txt";
        StringBuilder stringBuilder = new StringBuilder();

        try (FileInputStream fis = openFileInput(fileName);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            locationnow = stringBuilder.toString();

        } catch (IOException e) {
            Log.e("MainActivity", "Error reading file", e);
            locationnow = "";
        }

        return locationnow;
    }

    public void createfile() {
        String fileName = "base.txt";
        String content = "";

        File file = new File(getFilesDir(), fileName);
        if (!file.exists()) {
            try (FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE)) {
                fos.write(content.getBytes());
            } catch (IOException e) {
                Log.e("MainActivity", "Error creating file", e);
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == MY_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean showWritePermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                return false;
            }
        }
        return true;
    }

    public boolean setWifiEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        try {
            if (enabled) {
                wifiManager.setWifiEnabled(false);
                Log.i("MainActivity", "Hotspot disabled for security");
                Toast.makeText(getApplicationContext(), "ILLEGAL ACTION DETECTED! Hotspot NOT available", Toast.LENGTH_LONG).show();
                backgroundExecutor.execute(this::sendhotspot);
            }

            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            Log.e("MainActivity", "Error setting WiFi enabled state", e);
            return false;
        }
    }

    public void sendhotspot() {
        try {
            Long tsLong = System.currentTimeMillis() / 1000;
            String url = "https://punchclock.ai/api/hotspot.php?timestamp=" + tsLong + "&deviceid=" + deviceId;

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("device", deviceId)
                    .build();

            Request request = new Request.Builder().url(url).post(requestBody).build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("MainActivity", "Hotspot report failed", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i("MainActivity", "Hotspot reported: " + response.body().string());
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error sending hotspot report", e);
        }
    }

    @Override
    public void onBackPressed() {
        // Disabled for kiosk mode
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;

        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
            try {
                if (!backgroundExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    backgroundExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                backgroundExecutor.shutdownNow();
            }
        }

        super.onDestroy();
    }

    // Optimized AsyncTask classes
    private class OptimizedPinpadTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return isOnlineFast();
        }

        @Override
        protected void onPostExecute(Boolean online) {
            if (isDestroyed) return;

            if (online) {
                String cunq = readFile();
                justwait.setVisibility(View.VISIBLE);
                Intent intent = new Intent(MainActivity.this, Pinpad.class);
                intent.putExtra("cunq", cunq);
                startActivity(intent);
            } else {
                showNoInternetError();
            }
        }
    }

    private class OptimizedCamTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return isOnlineFast();
        }

        @Override
        protected void onPostExecute(Boolean online) {
            if (isDestroyed) return;

            if (online) {
                String cunq = readFile();
                justwait.setVisibility(View.VISIBLE);
                Intent intent = new Intent(MainActivity.this, SurfaceCamera.class);
                intent.putExtra("cunq", cunq);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else {
                showNoInternetError();
            }
        }
    }
}