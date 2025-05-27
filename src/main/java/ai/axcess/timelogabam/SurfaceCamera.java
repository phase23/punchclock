package ai.axcess.timelogabam;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SurfaceCamera extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private String cunq;
    private Handler mainHandler;
    private String camaction;
    private long mLastClickTime = 0;
    private TextView campause;
    private String deviceId;

    // Performance optimizations
    private ExecutorService backgroundExecutor;
    private OkHttpClient httpClient;
    private volatile boolean isDestroyed = false;
    private boolean isCameraReady = false;

    // UI components
    private Button btnCamera, btnCameraout, btnClockincancel;
    private FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_camera);

        // Initialize core components first
        initializeCoreComponents();

        // Setup UI immediately
        setupUIComponents();

        // Initialize camera in background
        initializeCameraAsync();

        // Setup auto-return timer
        setupAutoReturnTimer();
    }

    private void initializeCoreComponents() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        FullScreencall();

        // Initialize threading and networking
        mainHandler = new Handler(Looper.getMainLooper());
        backgroundExecutor = Executors.newFixedThreadPool(2);

        // Optimized HTTP client
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        // Get device info
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        cunq = getIntent().getExtras().getString("cunq");

        // Allow network on main thread (but we'll use background threads)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void setupUIComponents() {
        // Initialize UI components
        btnCamera = findViewById(R.id.btnCamera);
        btnCameraout = findViewById(R.id.btnCameraout);
        btnClockincancel = findViewById(R.id.btnClockincancel);
        campause = findViewById(R.id.camwait);
        preview = findViewById(R.id.camera_view);

        // Setup click listeners immediately (they'll check camera readiness)
        setupClickListeners();

        // Show loading state
        campause.setText("Initializing camera...");
        campause.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> {
            if (!isCameraReady) {
                Toast.makeText(this, "Camera not ready, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            campause.setVisibility(View.VISIBLE);
            btnCamera.setEnabled(false);
            camaction = "1";
            btnCamera.setText("Please wait / por favor espera..");

            // Take picture in background
            backgroundExecutor.execute(() -> {
                try {
                    mCamera.takePicture(null, null, mPicture);
                } catch (Exception e) {
                    Log.e("Camera", "Error taking picture", e);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Camera error occurred", Toast.LENGTH_SHORT).show();
                        resetCameraButton();
                    });
                }
            });
        });

        btnCameraout.setOnClickListener(v -> {
            if (!isCameraReady) {
                Toast.makeText(this, "Camera not ready, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            campause.setVisibility(View.VISIBLE);
            btnCameraout.setEnabled(false);
            camaction = "2";
            btnCameraout.setText("Please wait / por favor espera..");

            // Take picture in background
            backgroundExecutor.execute(() -> {
                try {
                    mCamera.takePicture(null, null, mPicture);
                } catch (Exception e) {
                    Log.e("Camera", "Error taking picture", e);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Camera error occurred", Toast.LENGTH_SHORT).show();
                        resetCameraOutButton();
                    });
                }
            });
        });

        btnClockincancel.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            btnClockincancel.setEnabled(false);
            returnToMain();
        });
    }

    private void initializeCameraAsync() {
        backgroundExecutor.execute(() -> {
            try {
                // Initialize camera
                mCamera = getCameraInstance();

                if (mCamera != null) {
                    mainHandler.post(() -> {
                        // Create and setup camera preview on main thread
                        mPreview = new CameraPreview(this, mCamera);
                        preview.addView(mPreview);

                        isCameraReady = true;
                        campause.setVisibility(View.GONE);

                        Log.i("Camera", "Camera initialized successfully");
                    });
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Camera unavailable", Toast.LENGTH_LONG).show();
                        campause.setText("Camera unavailable");
                    });
                }
            } catch (Exception e) {
                Log.e("Camera", "Error initializing camera", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_LONG).show();
                    campause.setText("Camera error");
                });
            }
        });
    }

    private void setupAutoReturnTimer() {
        mainHandler.postDelayed(() -> {
            if (!isDestroyed) {
                returnToMain();
            }
        }, 60000); // 60 seconds
    }

    private void returnToMain() {
        Intent intent = new Intent(SurfaceCamera.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void resetCameraButton() {
        btnCamera.setEnabled(true);
        btnCamera.setText("Clock In");
        campause.setVisibility(View.GONE);
    }

    private void resetCameraOutButton() {
        btnCameraout.setEnabled(true);
        btnCameraout.setText("Clock Out");
        campause.setVisibility(View.GONE);
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(1); // Front camera
        } catch (Exception e) {
            Log.e("Camera", "Error opening camera", e);
            // Try default camera if front camera fails
            try {
                c = Camera.open();
            } catch (Exception e2) {
                Log.e("Camera", "Error opening default camera", e2);
            }
        }
        return c;
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        mainHandler.removeCallbacksAndMessages(null);

        // Shutdown background executor
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

        releaseCameraAndPreview();
        super.onDestroy();
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                isCameraReady = false;
            } catch (Exception e) {
                Log.e("Camera", "Error releasing camera", e);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null && mPreview != null) {
            preview.removeView(mPreview);
        }
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.release();
                mCamera = null;
                isCameraReady = false;
            } catch (Exception e) {
                Log.e("Camera", "Error releasing camera in onPause", e);
            }
        }
    }

    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;
        private boolean isPreviewRunning;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void previewCamera() {
            try {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();
                    isPreviewRunning = true;
                }
            } catch (Exception e) {
                Log.e("CameraPreview", "Error starting preview", e);
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error setting camera preview: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Error setting camera preview", Toast.LENGTH_SHORT).show();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    isPreviewRunning = false;
                }
            } catch (Exception e) {
                Log.e("CameraPreview", "Error in surfaceDestroyed", e);
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mCamera == null) return;

            try {
                if (isPreviewRunning) {
                    mCamera.stopPreview();
                }

                Camera.Parameters parameters = mCamera.getParameters();
                parameters.set("jpeg-quality", 70); // Increased quality slightly

                List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
                if (previewSizes != null && !previewSizes.isEmpty()) {
                    // Set optimal preview size
                    Camera.Size optimalSize = getOptimalPreviewSize(previewSizes, width, height);
                    if (optimalSize != null) {
                        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                    }
                }

                if (parameters.isAutoExposureLockSupported()) {
                    parameters.setAutoExposureLock(false);
                }

                mCamera.setParameters(parameters);
                previewCamera();

            } catch (Exception e) {
                Log.e("CameraPreview", "Error in surfaceChanged", e);
            }
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) w / h;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }

            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - h) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - h);
                    }
                }
            }
            return optimalSize;
        }
    }

    Camera.PictureCallback mPicture = (data, camera) -> {
        backgroundExecutor.execute(() -> {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            if (pictureFile == null) {
                Log.e("Camera", "Error creating media file, check storage permissions");
                mainHandler.post(() -> {
                    Toast.makeText(this, "Storage error", Toast.LENGTH_SHORT).show();
                    resetButtons();
                });
                return;
            }

            try {
                // Save image
                try (FileOutputStream fos = new FileOutputStream(pictureFile)) {
                    fos.write(data);
                }

                String filepath = pictureFile.getAbsolutePath();
                Log.d("Camera", "Picture saved: " + filepath);

                // Compress and upload
                File compressedFile = compressImage(pictureFile);
                uploadFileAsync(compressedFile != null ? compressedFile.getAbsolutePath() : filepath);

            } catch (Exception e) {
                Log.e("Camera", "Error saving picture", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Error saving picture", Toast.LENGTH_SHORT).show();
                    resetButtons();
                });
            }
        });
    };

    private void resetButtons() {
        if (camaction.equals("1")) {
            resetCameraButton();
        } else {
            resetCameraOutButton();
        }
    }

    public static final int MEDIA_TYPE_IMAGE = 1;

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MTN_Camera");

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.e("Camera", "Failed to create directory");
            return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        }
        return null;
    }

    private File compressImage(File originalFile) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 800, 600);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            if (bitmap == null) return originalFile;

            // Compress and save
            try (FileOutputStream fos = new FileOutputStream(originalFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            }

            bitmap.recycle();
            return originalFile;

        } catch (Exception e) {
            Log.e("Camera", "Error compressing image", e);
            return originalFile;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void uploadFileAsync(String imagePath) {
        try {
            String url = "https://punchclock.ai/capture.php?cunq=" + cunq.trim() +
                    "&getdevice=" + deviceId + "&choice=" + camaction +
                    "&capturetype=options&test=yes";

            Log.i("Upload", "URL: " + url);
            Log.i("Upload", "Path: " + imagePath);

            File fileSource = new File(imagePath);
            if (!fileSource.exists()) {
                Log.e("Upload", "File does not exist");
                mainHandler.post(() -> {
                    Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                    resetButtons();
                });
                return;
            }

            MediaType mediaType = MediaType.parse("image/jpeg");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String fileName = "Capture_" + timeStamp + ".jpg";

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, RequestBody.create(mediaType, fileSource))
                    .addFormDataPart("name", "moredata")
                    .addFormDataPart("result", "my_image")
                    .addFormDataPart("thisdevice", deviceId)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("Upload", "Upload failed", e);
                    mainHandler.post(() -> {
                        Toast.makeText(SurfaceCamera.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        resetButtons();
                    });

                    // Clean up file
                    cleanupFile(fileSource);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.i("Upload", "Response: " + responseBody);

                    // Clean up file
                    cleanupFile(fileSource);

                    mainHandler.post(() -> handleUploadResponse(responseBody));
                }
            });

        } catch (Exception e) {
            Log.e("Upload", "Error starting upload", e);
            mainHandler.post(() -> {
                Toast.makeText(this, "Upload error", Toast.LENGTH_SHORT).show();
                resetButtons();
            });
        }
    }

    private void cleanupFile(File file) {
        try {
            if (file.exists() && file.delete()) {
                Log.d("Cleanup", "File deleted: " + file.getName());
                // Update media scanner
                MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
            }
        } catch (Exception e) {
            Log.e("Cleanup", "Error deleting file", e);
        }
    }

    private void handleUploadResponse(String responseBody) {
        try {
            responseBody = responseBody.trim();
            String[] separated = responseBody.split("~");
            String rekstat = separated[0];

            int statusCode = 0;
            try {
                statusCode = Integer.parseInt(rekstat);
            } catch (NumberFormatException e) {
                Log.e("Response", "Invalid status code: " + rekstat);
            }

            if (statusCode == 99) {
                // Error case
                String report_en = separated.length > 1 ? separated[1] : "Error occurred";
                String report_es = separated.length > 2 ? separated[2] : "Error occurred";
                String errorMsg = "<h3>" + report_en + "</h3><b><h3>" + report_es + "</h3>";

                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(Html.fromHtml(errorMsg))
                        .setPositiveButton("Close/Cerrar", (dialog, which) -> {
                            dialog.dismiss();
                            returnToMain();
                        })
                        .setCancelable(false)
                        .show();
            } else {
                // Success case
                Intent intent = new Intent(SurfaceCamera.this, SurfaceCameraResult.class);
                intent.putExtra("passthis", responseBody);
                intent.putExtra("cunq", cunq);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e("Response", "Error handling response", e);
            Toast.makeText(this, "Response processing error", Toast.LENGTH_SHORT).show();
            resetButtons();
        }
    }

    public void FullScreencall() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}