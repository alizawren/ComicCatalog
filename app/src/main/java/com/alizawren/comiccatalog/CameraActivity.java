package com.alizawren.comiccatalog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class CameraActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // Autofocus mode is supported
            // set the focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            // set Camera parameters
            mCamera.setParameters(params);
            Log.d(TAG, "Setting autofocus parameter. Current focus mode: "+ params.getFocusMode());
        }
        else {
            Log.d(TAG, "Autofocus not supported!");
        }

        mCamera.autoFocus(null);

        // start the process of detecting barcodes? Put this here? Idk
        detectBarcodes();

        // set orientation???????

        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d(TAG, "Camera found");
                cameraId = i;
                break;
            }
        }

        setCameraDisplayOrientation((Activity) this, cameraId, mCamera);

        //mCamera.setDisplayOrientation(0);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get an image from the camera
                //mCamera.takePicture(null, null, picture);
                //mCamera.cancelAutoFocus();

                mCamera.release();
                finish();

            }
        });
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera not available: " + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    // Ideally would be barcode-detecting code. See https://developers.google.com/vision/android/multi-tracker-tutorial
    private void detectBarcodes() {

        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();
        if(!detector.isOperational()){
            Log.d(TAG, "Could not set up the detector!");
            return;
        }
        //BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay);
        //BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory();

        // Note, we just create an instance of MultiProcessor.Factory<Barcode>
        // We can replace this with BarcodeTrackerFactory, where BarcodeTrackerFactory implements
        // MultiProcessor.Factory<Barcode> (see Github code from tutorial)
        MultiProcessor.Factory<Barcode> barcodeFactory = new MultiProcessor.Factory<Barcode>() {
            @Override
            public Tracker<Barcode> create(Barcode barcode) {
                return null;
            }
        };

        detector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        CameraSource mCameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(15.0f)
                .build();

        // May need some code similar to below to add graphic overlay with bounding boxes to the view
        //mPreview.start(mCameraSource, mGraphicOverlay);
    }


    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }



}
