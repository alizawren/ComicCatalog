package com.alizawren.comiccatalog;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity {
    //this will store whether or not we were granted camera permissions
    private int STORAGE_PERMISSION_CODE = 1;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String TAG = "MainActivity";

    ImageView myImageView;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;
        myImageView = (ImageView) findViewById(R.id.imgview);

        // NOTE: code here just processes an image of a QR code in the directory

        // https://codelabs.developers.google.com/codelabs/bar-codes/#5
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtView = (TextView) findViewById(R.id.txtContent);

                /*Bitmap myBitmap = BitmapFactory.decodeResource(
                        getApplicationContext().getResources(),
                        R.drawable.puppy);
                myImageView.setImageBitmap(myBitmap);*/

                BarcodeDetector detector =
                        new BarcodeDetector.Builder(getApplicationContext())
                                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
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

                Barcode thisCode = barcodes.valueAt(0);
                txtView.setText(thisCode.rawValue);
            }
        });

        // Opens the camera activity
        Button openCameraButton = (Button) findViewById(R.id.open_camera_button);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the camera exists first
                if (checkCameraHardware(context)) {
                    //if the camera exists, then we request for permission
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        //in this case, permission is already granted
                        //and you can proceed with starting the camera
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                        //old code
                        //final Intent intent = new Intent(context, CameraActivity.class);
                        //startActivityForResult(intent, 1);
                    } else{
                        //otherwise, request for permission, function down below
                        requestStoragePermission();
                    }

                }
                else {
                    Toast.makeText(getApplicationContext(),"Device has no camera!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            if(imageBitmap != null) {
                myImageView.setImageBitmap(imageBitmap);
            }
            else {
                Toast.makeText(getApplicationContext(), "Something went wrong when taking a picture!", Toast.LENGTH_SHORT).show();
            }

        }
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
    //requests for permission to use camera
    private void requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            //this is if the user denies the first request for permission
            //and gives an explanation of what we're using their camera for
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("We need to use the camera")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener(){
                        //if the user is okay with us asking again,
                        //we request for permission
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                        //if the user does not want us to ask again,
                        //we leave them alone
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            //if they grant us permission, change the permission status
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
        }
    }
    //once the permision status changes (we are granted permission),
    //then we make a dialog box indicating so

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
