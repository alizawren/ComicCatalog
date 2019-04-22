package com.alizawren.comiccatalog;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alizawren.comiccatalog.util.Consumer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class ScannerFragment extends Fragment {
    String[] appPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
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

    private String currentRecordUrl;
    private String currentIsbn;
    private String currentTitle;
    private String currentImageUrl;

    //private FirebaseAuth mAuth;
    User user;

    Context context = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceBundle) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_scanner, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceBundle) {
        if(!checkAndRequestPermissions()) {
            Toast.makeText(getActivity(),"We need permissions for our app to work!",Toast.LENGTH_SHORT).show();
        }

        FirebaseApp.initializeApp(getActivity());

        myImageView = getView().findViewById(R.id.imgview);
        txtView = getView().findViewById(R.id.txtview);

        Button btnProcess = getView().findViewById(R.id.button_process);
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBarcodesWithFirebase();
            }
        });

        Button btnCapture = getView().findViewById(R.id.button_capture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(getActivity().getApplicationContext(),"Device has no camera!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void scanBarcodesWithFirebase() {

        if (imageBitmap == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Please take a picture first.", Toast.LENGTH_SHORT).show();
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

                        //processBarcodes(barcodes);

                        if (barcodes.size() < 1) {
                            txtView.setText("No barcodes detected. Please try again!");
                            return;
                        }
                        if (barcodes.size() > 1) {
                            txtView.setText("Too many barcodes in the same photo. (Support for multiple barcodes is a future feature.) Please try again!");
                            return;
                        }

                        FirebaseVisionBarcode barcode = barcodes.get(0);
                        String rawValue = barcode.getRawValue();
                        String isbn = rawValue;

                        new ScannerFragment.GetJSONTask(new Consumer<JSONObject>() {
                            @Override
                            public void accept(JSONObject bookObject) {
                                Log.d(TAG, "Response From Asynchronous task: " + bookObject.toString());
                                openComicDialog(bookObject);
                            }
                        }).execute("http://openlibrary.org/api/volumes/brief/isbn/"+isbn+".json");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        Toast.makeText(getActivity().getApplicationContext(), "The detector didn't work with error: " + e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // not being used
    private void processBarcodes(List<FirebaseVisionBarcode> barcodes) {
        String text = "Number of barcodes detected: " + barcodes.size() + "\n";

        String isbn = null;

        for (int index = 0; index < barcodes.size(); index++) {
            FirebaseVisionBarcode barcode = barcodes.get(index);
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();

            String rawValue = barcode.getRawValue();
            isbn = rawValue;
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

    private void openComicDialog(JSONObject bookObject) {
        try {
            currentRecordUrl = bookObject.getString("recordURL");
            currentIsbn = bookObject.getJSONArray("isbns").getString(0);
            currentTitle = bookObject.getJSONObject("data").getString("title");
            currentImageUrl = bookObject.getJSONObject("cover").getString("small");
            //currentImageUrl = bookObject.getJSONObject("details").getString("thumbnail_url");

        }
        catch (Exception e) {
            Toast.makeText(context, "Can't parse JSON: " + e,Toast.LENGTH_SHORT).show();
        }

        ImageView cover = new ImageView(getActivity());
        cover.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        new ScannerFragment.DownloadImageTask(cover).execute(currentImageUrl);

        String message = "Title: " + currentTitle + "\nISBN: " + currentIsbn +
                "\nRecord URL: " + currentRecordUrl +
                "\n\nWould you like to save this book to your library?";

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(cover);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Save to Library", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // yes was pressed
                // create a comic book object with data

                ComicBook book = new ComicBook(currentIsbn, currentTitle, currentRecordUrl, "none");
                // Save the comic into the database
                FirebaseUtil.addComicBook(user, book);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // no was pressed
            }

        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }

    private void scanBarcodesWithGoogleAPI() {
        BarcodeDetector detector =
                new BarcodeDetector.Builder(getActivity().getApplicationContext())
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();
        if(!detector.isOperational()){
            txtView.setText("Could not set up the detector!");
            return;
        }

        if (imageBitmap == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Please take a picture first.", Toast.LENGTH_SHORT).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
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
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong when taking a picture!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void DeletePicTaken(){
        final String[] imageColumns = { BaseColumns._ID, MediaStore.MediaColumns.DATA };
        final String imageOrderBy = BaseColumns._ID + " DESC";
        Cursor imageCursor = getActivity().managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
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
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
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
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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
            if(ContextCompat.checkSelfPermission(getActivity(),perm) != PackageManager.PERMISSION_GRANTED){
                //add them to the list if not already granted
                listPermissionsNeeded.add(perm);
            }
        }
        if(!listPermissionsNeeded.isEmpty()){
            //request remaining permissions
            ActivityCompat.requestPermissions(getActivity(),
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE);
            return false;
        }
        //if everything is already requested, continue
        Toast.makeText(getActivity(),"All Permissions Granted",Toast.LENGTH_SHORT).show();
        return true;
    }

    // ------------- private classes for async tasks --------------

    private class GetJSONTask extends AsyncTask<String, Void, String> {
        private Consumer<JSONObject> bookResponse = null;
        private ProgressDialog pd;

        // constructor
        public GetJSONTask(Consumer<JSONObject> asyncResponse) {
            bookResponse = asyncResponse;//Assigning call back interfacethrough constructor
        }

        @Override
        protected void onPreExecute(){
            pd = ProgressDialog.show(getActivity(),"","Loading",true,false);
        }
        @Override
        protected String doInBackground(String... urls){
//            String url = "http://openlibrary.org/api/volumes/brief/isbn/"+isbnCode+".json";
            StringBuffer response = null;
            String url = urls[0];
            int responseCode = 0;
            try{
                URL openlibrary = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) openlibrary.openConnection();
                connection.setRequestMethod("GET");
                responseCode = connection.getResponseCode();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                String inputLine;

                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } catch (Exception e) {
                return("Error when retrieving data, response code:" + responseCode + "\nerror message: " + e);
            }

            return response.toString();
        }
        @Override
        protected void onPostExecute(String result){
            pd.dismiss();
            //do something with the result...
            try{
                JSONObject book = new JSONObject(result);
                JSONObject bookObject = book.getJSONObject("records").getJSONObject(book.getJSONObject("records").keys().next());
                String placeHolder = "Record URL: "+bookObject.getString("recordURL")+"\n"+
                        "ISBN: "+bookObject.getJSONArray("isbns").getString(0)+"\n"+
                        "Title: "+bookObject.getJSONObject("data").getString("title");
                //txtView.setText(placeHolder);
                bookResponse.accept(bookObject);

            } catch (Exception e){
                //txtView.setText("Can't parse JSON: " + e);
                Toast.makeText(context, "Can't parse JSON: " + e,Toast.LENGTH_LONG).show();
            }

        }
    }

    // Using this: https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
    // Note, we may be able to combine our asynctasks or do something else
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error getting image", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
