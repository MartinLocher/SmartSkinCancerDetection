package com.martinlocher.smartskin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImagePickerActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_IMAGE = 2365;
    private static final int REQUEST_CROP_IMAGE = 2342;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivityForResult(getPickImageChooserIntent(), REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == REQUEST_PICK_IMAGE) {
                Intent intent = new Intent(this, CropImageActivity.class);
                Uri imageUri = getPickImageResultUri(data);

                writeImage(imageUri);

                intent.putExtra(CropImageActivity.EXTRA_IMAGE_URI, imageUri.toString());
                startActivityForResult(intent, REQUEST_CROP_IMAGE);

            }
            else if(requestCode == REQUEST_CROP_IMAGE) {
                System.out.println("Image crop success :"+data.getStringExtra(CropImageActivity.CROPPED_IMAGE_PATH));
                String imagePath = new File(data.getStringExtra(CropImageActivity.CROPPED_IMAGE_PATH), "image.jpg").getAbsolutePath();
                Intent result = new Intent();
                result.putExtra("image_path", imagePath);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        }
        else {
            System.out.println("Image crop failed");
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }


    private void savefile(URI sourceuri)
    {
        String sourceFilename= sourceuri.getPath();
        String destinantionFilename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".jpg";

        File filepath = MainActivity.getPublicAlbumStorageDir ("SmartSkin");
        File dir = new File(filepath.getAbsolutePath());

        dir.mkdirs();

        // Create a name for the saved image
        File file = new File(dir, destinantionFilename);

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileOutputStream fo;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            fo = new FileOutputStream(file);
            bos = new BufferedOutputStream(fo);
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
                scanFile(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

     public void scanFile(String path) {

        MediaScannerConnection.scanFile(ImagePickerActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    public boolean writeImage (Uri file_uri)
    {
        Bitmap bitmap;
        OutputStream output;
        boolean success = true;

        String sourceFilename= file_uri.getPath();
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".jpg";


        File filepath = MainActivity.getPublicAlbumStorageDir ("SmartSkin");


        // Create a new folder in SD Card
        File dir = new File(filepath.getAbsolutePath());

        dir.mkdirs();

        // Create a name for the saved image
        File file = new File(dir, name);

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;


        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();

                scanFile(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list, so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get URI to image received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }


}
