package ai.axcess.timelogabam;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import ai.axcess.timelogabam.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SurfaceCameraRegister extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    Integer cameraId;
    public String userid;
    public String timeowner;
    TextView campause;

    //ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_surface_camera_register);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        userid = getIntent().getExtras().getString("userid");
        timeowner = getIntent().getExtras().getString("timeowner");
        campause = (TextView) findViewById(R.id.camwait);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mCamera = getCameraInstance();


        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_viewreg);
        preview.addView(mPreview);

        // mCamera.setDisplayOrientation(90);


        //imageView = (ImageView)findViewById(R.id.imageView);
        // imageView.addView(mPreview);


        final Button btnCamera = (Button) findViewById(R.id.btnCameraReg);
        Button btnCameracancel = (Button) findViewById(R.id.btnCameraRegcancel);

        btnCamera.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Log.d("ckil", "button click");
                campause.setVisibility(View.VISIBLE);
                btnCamera.setText("Please wait...");
                mCamera.takePicture(null, null, mPicture);


            }
        });


        btnCameracancel.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Log.d("ckil", "button click");
                Log.d("action: clix",timeowner);
                Intent intent = new Intent(SurfaceCameraRegister.this, Listworkforce.class);
                //intent.putExtra("position","regisiter");
                intent.putExtra("timeowner",timeowner);

                startActivity(intent);


            }
        });





        // And the previewCamera method :
    }





    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        releaseCameraAndPreview();


    }

    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;

        }

    }
    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
        releaseCamera();

    }


    @Override
    protected void onPause() {
        super.onPause();
        //releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_viewreg);
        preview.removeView(mPreview);
        releaseCamera();



// release the camera immediately on pause event
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;
        private boolean isPreviewRunning;
        // private Integer height;
        //private Integer width;


        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }





        public void previewCamera() {

            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }


            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                isPreviewRunning = true;
            } catch(Exception e) {
                //Log.d(APP_CLASS, "Cannot start preview", e);
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                //Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
            mCamera.stopPreview();
            isPreviewRunning = false;
            mCamera.release();
        }







        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // If your preview can change or rotate, take care of those events here.
            if (isPreviewRunning) {
                mCamera.stopPreview();
            }

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.set("jpeg-quality", 30);
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();



            /*
            for(Camera.Size elem : previewSizes){
                System.out.println(elem+" ");
            }


            List sizes = parameters.getSupportedPictureSizes();
            for (int i=0;i<sizes.size();i++) {
                Log.i("PictureSize", "Supported Size: " +sizes.get(i).width + "height : " + sizes.get(i).height);
            }
            */

            // Camera.Parameters parameters = mCamera.getParameters();
/*

            Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();


            if(display.getRotation() == Surface.ROTATION_0) {
                parameters.setPreviewSize(640, 480);
                mCamera.setDisplayOrientation(270);
            }

            if(display.getRotation() == Surface.ROTATION_90) {
                parameters.setPreviewSize(640, 480);
                mCamera.setDisplayOrientation(270);
            }

            if(display.getRotation() == Surface.ROTATION_180) {
                parameters.setPreviewSize(640, 480);
                mCamera.setDisplayOrientation(270);
            }

            if(display.getRotation() == Surface.ROTATION_270) {
                parameters.setPreviewSize(640, 480);
                mCamera.setDisplayOrientation(270);
            }



*/
            mCamera.setParameters(parameters);
            previewCamera();





        }
    }




    Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);


            if (pictureFile == null) {
                //Log.d(TAG, "Error creating media file, check storage permissions");
                Log.i("MSG","Error creating media file, check storage permissions");
                return;
            }

            try {

                //Log.d(TAG, "pic taken: ");
                Log.i("MSG","pic taken:");
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();


                String filepath = pictureFile.getAbsolutePath();
                Log.d("picpath : to /",  filepath);

                Bitmap myBitmap = BitmapFactory.decodeFile(filepath);

                saveBitmapToFile(pictureFile);
                //uploadMultipart(filepath);
                uploadFile(filepath, userid);



                MediaStore.Images.Media.insertImage(getContentResolver(), myBitmap, "PhotoTest", "taken with intent camera");
                MediaScannerConnection.scanFile(SurfaceCameraRegister.this, new String[]{filepath}, null, null);

                deleteImage(pictureFile);

            } catch (FileNotFoundException e) {
                // Log.d(TAG, "File not found: " + e.getMessage());
                System.out.print( "File not found: " + e.getMessage() );
            } catch (IOException e) {
                //Log.d(TAG, "Error accessing file: " + e.getMessage());
                System.out.print( "Error accessing file: " + e.getMessage() );
            }
        }
    };


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MTN_Camera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");

        } else {
            return null;
        }

        return mediaFile;
    }



    private void deleteImage(File file) {
        // Set up the projection (we only need the ID)
        String[] projection = {MediaStore.Images.Media._ID};

        // Match on the file path
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};

        // Query for the ID of the media matching the file path
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            // We found the ID. Deleting the item via the content provider will also remove the file
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
        } else {
            // File not found in media store DB
        }
        c.close();
    }



    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 5;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=60;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }



    public int uploadFile(String imagePath, String userid) {

        String url = "https://punchclock.ai/captureRegister.php?user="+userid + "&rotate=no";

        Log.i("PATH",imagePath);
        OkHttpClient client = new OkHttpClient();

        File fileSource = new File(imagePath);
        // String contentType = fileSource.toURL().openConnection().getContentType();


        if (fileSource.isFile()){
            Log.i("EXIST","exist");
        }else {
            Log.i("NOT EXIST","not exist");
        }
        final MediaType MEDIA_TYPE;
        String imageType;
        if (imagePath.endsWith("png")){
            MEDIA_TYPE = MediaType.parse("image/png");
            imageType = ".png";
        }else {
            MEDIA_TYPE = MediaType.parse("image/jpeg");
            imageType = ".jpg";
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "upload"+timeStamp+imageType;
        Log.d("fileName : to /",  fileName);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file",fileName,RequestBody.create(MEDIA_TYPE,fileSource))
                .addFormDataPart("ruserid", userid)
                .addFormDataPart("result", "my_image")
                .build();
        Request request = new Request.Builder()

                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.i("SUCC",""+response.message());

            }
            String resp = response.message();
            responseBody =  response.body().string();
            Log.i("photo:respBody",responseBody);

            // alertbox(responseBody);

            Intent intent = new Intent(SurfaceCameraRegister.this, SurfaceCameraRegisterResult.class);
            intent.putExtra("passthis",responseBody);
            intent.putExtra("userid",userid);
            intent.putExtra("timeowner",timeowner);
            startActivity(intent);


            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  0;
    }






    public void alertbox(String whatever){


        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(whatever);

        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();


    }






}

