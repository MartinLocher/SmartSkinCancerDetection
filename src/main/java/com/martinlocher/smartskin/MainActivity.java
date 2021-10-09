package com.martinlocher.smartskin;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity {

    public static String Number1Code;
    public static String Number2Code;
    private Button btnTakePic;
    private Button btnSavePic;
    private Button btnHistoryPic;
    private ImageView imageView;
    private EditText msgView;
    private String pathtoFile;
    private TextView textView;
    private ImageClassifier classifier;

    final int CAMERA_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 1002;
    private static final int REQUEST_HISTORY_IMAGE = 1003;
    private static String log_tag = "SmartSkin";

    //captured picture uri

    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTakePic = findViewById(R.id.btnTakePic);
        btnSavePic = findViewById(R.id.btnSavePic);
        btnHistoryPic = findViewById (R.id.btnHistoryPic);


        imageView = findViewById(R.id.image);
        msgView = findViewById(R.id.editText);


        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCamPermissionGranted()){
                    pickImage();
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                }
            }
        });

        btnSavePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isWritePermissionGranted())
                {
                    writeImage();
                }else
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                }
                // dispatchPictureAktionTakerAction ()
        });

        btnHistoryPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             //   startActivityForResult(new Intent(MainActivity.this, ImageHistory.class), REQUEST_HISTORY_IMAGE);

            }
            // dispatchPictureAktionTakerAction ()
        });

    }
    public boolean writeImage ()
    {
        Bitmap bitmap;
        OutputStream output;
        boolean success;
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".jpg";


        // Retrieve the image from the res folder
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        bitmap = drawable.getBitmap();

        File filepath = getPublicAlbumStorageDir ("SmartSkin_Cropped");


        // Create a new folder in SD Card
       File dir = new File(filepath.getAbsolutePath());

        dir.mkdirs();

        // Create a name for the saved image
        File file = new File(dir, name);

        success=false;
        try {

            output = new FileOutputStream(file);

            // Compress into png format image from 0% - 100%

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();
            output.close();
            success = true;
            scanFile(file.getAbsolutePath());
        }

        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return success;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
     public void scanFile(String path) {

        MediaScannerConnection.scanFile(MainActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }
    static public File getPublicAlbumStorageDir(String albumName) {

        // This is for private
        /*
         File file = new File(context.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES), albumName);
         */
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(log_tag, "Directory not created");
        }
        return file;

    }


    public boolean isWritePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

            {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean isCamPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                return false;
            else {
                return true;
            }
        } else {
            return true;
        }
    }
    public void pickImage() {

        startActivityForResult(new Intent(this, ImagePickerActivity.class), REQUEST_PICK_IMAGE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        }
    }

    public void get_directory_content (List list) {
        String appDirectoryName = getResources().getString(R.string.app_name);
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getResources().getString(R.string.app_name));
        directory.mkdirs();
        File[] fList = directory.listFiles();
        int a = 1;
        for (int x = 0; x < fList.length; x++) {

            //txt.setText("You Have Capture " + String.valueOf(a) + " Photos");
            a++;
        }
        //get all the files from a directory
        /*
        for (File file : fList) {
            if (file.isFile()) {
                list.add(new ModelClass(file.getName(), file.getAbsolutePath()));
            }
        }*/
    }
    /*
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }*/

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();

        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;

        float scaleHeight = ((float) newHeight) / height;

// create a matrix for the manipulation

        Matrix matrix = new Matrix();

// resize the bit map

        matrix.postScale(scaleWidth, scaleHeight);

// recreate the new Bitmap

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    String imagePath = data.getStringExtra("image_path");
                    setImage(imagePath);



                    BitmapFactory.Options op = new BitmapFactory.Options();
                    op.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, op);
                    //imageView.setImageBitmap(bitmap);


                    try {
                        classifier = new ImageClassifier(this);

                        Bitmap bm = getResizedBitmap(bitmap,ImageClassifier.DIM_IMG_SIZE_X,ImageClassifier.DIM_IMG_SIZE_Y);

                        String textToShow = classifier.classifyFrame(bm);
                        msgView.setText(textToShow);

                        Log.d(log_tag, textToShow);
//                  bitmap.recycle();
                    } catch (IOException e) {
                        Log.d(log_tag, "Failed to initialize an image classifier.");
                    }

                    break;
            }
        } else {

            System.out.println("Failed to load image");
        }
    }

    private void setImage(String imagePath) {

        imageView.setImageBitmap(getImageFromStorage(imagePath));
    }

    private Bitmap getImageFromStorage(String path) {

        try {

            File f = new File(path);
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 512, 512);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return b;

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        return null;
    }
    private int calculateInSampleSize(

            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (requestCode == CAMERA_CAPTURE) {



                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inPreferredConfig = Bitmap.Config.ARGB_8888;

                Bitmap bitmap = BitmapFactory.decodeFile(pathtoFile, op);
                imageView.setImageBitmap(bitmap);


                try {
                    classifier = new ImageClassifier(this);

                   Bitmap bm = getResizedBitmap(bitmap,ImageClassifier.DIM_IMG_SIZE_X,ImageClassifier.DIM_IMG_SIZE_Y);

                    String textToShow = classifier.classifyFrame(bm);
                    msgView.setText(textToShow);

                    Log.d(log_tag, textToShow);
//                  bitmap.recycle();
                } catch (IOException e) {
                    Log.d(log_tag, "Failed to initialize an image classifier.");
                }
               
            }
            else {

            }

        }
    }*/





    private void dispatchPictureAktionTakerAction() {
        Intent takePic = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePic.resolveActivity(getPackageManager())!=null)
        {
            File photoFile = null;

            photoFile = createphotoFile ();

            if (photoFile != null) {
                pathtoFile = photoFile.getAbsolutePath();

              photoURI = FileProvider.getUriForFile(MainActivity.this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, CAMERA_CAPTURE);
            }
        }
    }

    private File createphotoFile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;

        try {
            image = File.createTempFile(
                    name,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            Log.d(log_tag,"createfailed"+e.toString());
        }

        return (image);
    }
}
