package com.alizawren.comiccatalog;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

        detectBarcodes();

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
                mCamera.release();
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



}
