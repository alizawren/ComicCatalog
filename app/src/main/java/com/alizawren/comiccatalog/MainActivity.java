package com.alizawren.comiccatalog;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //this will store whether or not we were granted camera permissions
    String[] appPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private int PERMISSIONS_REQUEST_CODE = 1;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String TAG = "MainActivity";

    ImageView myImageView;
    Bitmap imageBitmap;
    TextView txtView;

    ContentValues values;
    private Uri imageUri;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;

        if(!checkAndRequestPermissions()) {
            Toast.makeText(this,"We need permissions for our app to work!",Toast.LENGTH_SHORT).show();
        }

        FirebaseApp.initializeApp(this);

        myImageView = (ImageView) findViewById(R.id.imgview);
        txtView = (TextView) findViewById(R.id.txtContent);

        // https://codelabs.developers.google.com/codelabs/bar-codes/#5
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

        scanBarcodesWithFirebase();

            }
        });

        // Opens the camera activity
        Button openCameraButton = (Button) findViewById(R.id.open_camera_button);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the camera exists first
                if (checkCameraHardware(context)) {
                    //if the camera exists, then we request for permission to use camera
                    if(checkAndRequestPermissions()) {
                        dispatchTakePictureIntent();
                    }
                        //more old code
                        /*values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "New Picture");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                        imageUri = getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }*/
                        //old code
                        //final Intent intent = new Intent(context, CameraActivity.class);
                        //startActivityForResult(intent, 1);

                }
                else {
                    Toast.makeText(getApplicationContext(),"Device has no camera!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void scanBarcodesWithFirebase() {

        if (imageBitmap == null) {
            Toast.makeText(getApplicationContext(), "Please take a picture first.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                        .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        Log.d(TAG, "New FirebaseVisionImage from image. Current photo path is: " + currentPhotoPath);

        FirebaseVisionBarcodeDetector newDetector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);


        Task<List<FirebaseVisionBarcode>> result = newDetector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully
                        // ...

                        String text = "Number of barcodes detected: " + barcodes.size() + "\n";

                        for (int index = 0; index < barcodes.size(); index++) {
                            FirebaseVisionBarcode barcode = barcodes.get(index);
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            text = text + "Barcode " + (index+1) + ": " + rawValue + "\n";

                            int valueType = barcode.getValueType();
                            // See API reference for complete list of supported types
                            switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_PRODUCT:

                                    break;
                                case FirebaseVisionBarcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    break;
                            }
                        }

                        txtView.setText(text);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });
    }

    private void scanBarcodesWithGoogleAPI() {
        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();
        if(!detector.isOperational()){
            txtView.setText("Could not set up the detector!");
            return;
        }

        if (imageBitmap == null) {
            Toast.makeText(getApplicationContext(), "Please take a picture first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        String text = "Number of barcodes detected: " + barcodes.size() + "\n";
        for (int i = 0; i < barcodes.size(); i++) {
            Barcode barcode = barcodes.valueAt(i);
            text = text + i + ": " + barcode.rawValue + "\n";

            int valueType = barcode.format;
            Log.d(TAG, "" + valueType);
        }
        txtView.setText(text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //imageBitmap = (Bitmap) extras.get("data");

            //String imageurl = getRealPathFromURI(imageUri);

            Log.d(TAG, "Current photo path is: " + currentPhotoPath);
            imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
            //myImageView.setImageBitmap(imageBitmap);

            if(imageBitmap != null && myImageView != null) {
                myImageView.setImageBitmap(imageBitmap);
                txtView.setText("New image.");

                DeletePicTaken();
            }
            else {
                Toast.makeText(getApplicationContext(), "Something went wrong when taking a picture!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void DeletePicTaken(){
        final String[] imageColumns = { BaseColumns._ID, MediaStore.MediaColumns.DATA };
        final String imageOrderBy = BaseColumns._ID + " DESC";
        Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
        if(imageCursor.moveToFirst()){
            //int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            String fullPath = currentPhotoPath;
            //imageCursor.close();
            File file = new File(fullPath);
            file.delete();
        }


    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.d(TAG, "Error occurred when creating the image file: " + ex);

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.alizawren.comiccatalog.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // note that by calling CreateTempFile we are saving to the cache
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public boolean checkAndRequestPermissions(){
        //keep track of which permissions are still needed
        List<String> listPermissionsNeeded = new ArrayList<>();
        //loop through needed permissions
        for (String perm : appPermissions){
            if(ContextCompat.checkSelfPermission(this,perm) != PackageManager.PERMISSION_GRANTED){
                //add them to the list if not already granted
                listPermissionsNeeded.add(perm);
            }
        }
        if(!listPermissionsNeeded.isEmpty()){
            //request remaining permissions
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE);
            return false;
        }
        //if everything is already requested, continue
        Toast.makeText(this,"All Permissions Granted",Toast.LENGTH_SHORT).show();
        return true;
    }
}
