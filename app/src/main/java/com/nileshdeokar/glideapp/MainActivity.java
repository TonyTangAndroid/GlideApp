package com.nileshdeokar.glideapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    public static final String URL = "https://cdn-images-1.medium.com/max/1200/1*hcfIq_37pabmAOnw3rhvGA.png";
    private static final String TAG = MainActivity.class.getName().toString();
    private int count;
    private long lastModified;
    private EditText etUrl;
    private ImageView imageView;
    private TextView tvStatus;

    private Bitmap mBitmap;

    public static boolean isSdPresent() {

        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lastModified = Calendar.getInstance().getTimeInMillis() - 100;
        imageView = findViewById(R.id.imageView);
        tvStatus = findViewById(R.id.tvStatus);
        etUrl = findViewById(R.id.editText);

        imageView.setTag(R.id.imageView, 33);

        etUrl.setText(URL);
    }

    /*
     *   We are keeping track of refresh count in count variable
     *   on 1st call if no image url is specified we are using default img url specified in strings.xml
     *
     *   on 3rd refresh we are updating the lastModified time which in result loads image from server
     *
     * */

    private void loadImage(String url) {
        GlideApp.with(this)
                .load(url)
                .signature(new ObjectKey(String.valueOf(lastModified)))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(R.color.chart_grey)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        tvStatus.setText("Count :" + count + " isFromCache : " + isFirstResource);
                        return false;
                    }
                })
                .placeholder(R.color.colorPrimary)
                .into(imageView);
    }

    public void reload(View view) {

        String url = etUrl.getText().toString().trim();
        if (count == 0 && url.isEmpty()) {
            etUrl.setText(getString(R.string.img_url_default));
            loadImage(getString(R.string.img_url_default));
        }
        if (count == 3) {
            count = 0;
            lastModified = Calendar.getInstance().getTimeInMillis() - 100;
        }

        if (!url.isEmpty()) {
            loadImage(url);
        }

        count++;
    }

    /*
     * get Tag from imageView
     *
     */
    public void getTag(View view) {
        Toast.makeText(this, "Tag is : " + view.getTag(R.id.imageView), Toast.LENGTH_SHORT).show();
    }

    /*
     *  save image to external / internal storage using Glide
     *
     */
    public void saveImage(View view) {
        String url = etUrl.getText().toString().trim();

        GlideApp.with(this)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.d("Size ", "width :" + resource.getWidth() + " height :" + resource.getHeight());
                        imageView.setImageBitmap(resource);
                        storeImage(resource);
                    }
                });

    }

    private void storeImage(Bitmap image) {
        mBitmap = image;
        File pictureFile = getOutputMediaFile();
        try {
            if (pictureFile == null) {

                if (checkWriteExternalPermission()) {

                    FileOutputStream fos = getBaseContext().openFileOutput("file_name" + ".jps", Context.MODE_PRIVATE);
                    image.compress(Bitmap.CompressFormat.PNG, 90, fos);
                    fos.close();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
                return;
            }

            if (checkWriteExternalPermission()) {

                FileOutputStream fos = new FileOutputStream(pictureFile);
                image.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
            Toast.makeText(this, "Image stored in : " + pictureFile, Toast.LENGTH_SHORT).show();

            // image stored now set the url to the image path so we can test it using LOAD IMAGE Button
            etUrl.setText(pictureFile.getPath());
            Log.d(TAG, "img dir: " + pictureFile);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        if (isSdPresent()) {
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                    + "/Android/data/"
                    + getApplicationContext().getPackageName()
                    + "/Files");


            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            File mediaFile;
            Random generator = new Random();
            int n = 1000;
            n = generator.nextInt(n);
            String mImageName = "Image-" + n + ".jpg";

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
            return mediaFile;
        } else {
            Toast.makeText(this, "SD Card not present", Toast.LENGTH_SHORT).show();
        }
        return null;

    }

    private boolean checkWriteExternalPermission() {
        int res = this.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storeImage(mBitmap);

                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to write your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
