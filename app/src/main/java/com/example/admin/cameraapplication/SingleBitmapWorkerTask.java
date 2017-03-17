package com.example.admin.cameraapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by admin on 2017/03/07.
 */
public class SingleBitmapWorkerTask extends AsyncTask<File,Void,Bitmap> {

    WeakReference<ImageView> pImageViewReferences;
    final  int TARGET_IMAGE_VIEW_WIDTH ;
    final  int TARGET_IMAGE_VIEW_HEIGHT;
    private File pImageFile;


    public SingleBitmapWorkerTask(ImageView imageView, int width, int height){
        pImageViewReferences = new WeakReference<ImageView>(imageView);
        TARGET_IMAGE_VIEW_WIDTH = width;
        TARGET_IMAGE_VIEW_HEIGHT = height;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        pImageFile = params[0];
        Bitmap bitmap = decodeBitmapFromFile(pImageFile);

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null && pImageViewReferences != null){
            ImageView imageView = pImageViewReferences.get();
            if( imageView != null){
                imageView.setImageBitmap(bitmap);

            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options bmOptions){
        final int photoWidth = bmOptions.outWidth;
        final int photoHeight = bmOptions.outHeight;

        int scaleFactor = 1;

        if (photoWidth > TARGET_IMAGE_VIEW_WIDTH || photoHeight> TARGET_IMAGE_VIEW_HEIGHT){
            final int halfPhotoWidth = photoWidth/2;
            final int halfPhotoHeight= photoHeight/2;
            while(halfPhotoWidth/scaleFactor>TARGET_IMAGE_VIEW_WIDTH || halfPhotoHeight/scaleFactor>TARGET_IMAGE_VIEW_HEIGHT){
                scaleFactor *=2;
            }
        }
        return scaleFactor;
    }
    private Bitmap decodeBitmapFromFile(File imageFile){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
        bmOptions.inSampleSize = calculateInSampleSize(bmOptions);
        bmOptions.inJustDecodeBounds = false;
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.HONEYCOMB_MR1){
            addBitmapOptions(bmOptions);
        }
        return BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bmOptions);
    }

    public File getImageFile(){
        return pImageFile;
    }

    private static void addBitmapOptions(BitmapFactory.Options options){
        options.inMutable = true;
        Bitmap bitmap = MainActivity.getBitmapReuseable(options);
        if (bitmap != null){
            options.inBitmap = bitmap;
        }

    }
}

