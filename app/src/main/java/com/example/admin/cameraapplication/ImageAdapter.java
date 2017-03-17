package com.example.admin.cameraapplication;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by admin on 2017/03/07.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private File[] imagesFile;
    private Bitmap placeHolder;
    private static RecyclerViewClick pPositionInterface;



    private static class AsyncDrawable extends BitmapDrawable {
        final WeakReference<BitmapWorkerTask> taskReference;

        public AsyncDrawable(Resources recources, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(recources, bitmap);
            taskReference = new WeakReference(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return taskReference.get();
        }
    }

    public ImageAdapter(File[] folderFile, RecyclerViewClick positionInterface) {
        pPositionInterface = positionInterface;
        imagesFile = folderFile;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.images_gallery, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        File imageFile = imagesFile[position];
        // Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        // holder.getImageView().setImageBitmap(imageBitmap);
        //  BitmapWorkerTask workerTask = new BitmapWorkerTask(holder.getImageView());
        // workerTask.execute(imageFile);
        Bitmap bitmap = MainActivity.getBitmapFromMemoryCache(imageFile.getName());
        if (bitmap != null) {
            holder.getImageView().setImageBitmap(bitmap);
        } else if (checkBitmapWorkerTask(imageFile, holder.getImageView())) {
            BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(holder.getImageView());
            AsyncDrawable asyncDrawable = new AsyncDrawable(holder.getImageView().getResources(), placeHolder, bitmapWorkerTask);
            holder.getImageView().setImageDrawable(asyncDrawable);
            bitmapWorkerTask.execute(imageFile);
        }
    }

    @Override
    public int getItemCount() {
        return imagesFile.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        private ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            imageView = (ImageView) view.findViewById(R.id.capturedPhoto);
            imageView.setRotation((float)90);
        }

        public ImageView getImageView() {

            return imageView;

        }

        @Override
        public void onClick(View v) {
            pPositionInterface.getRecyclerViewAdapter(this.getAdapterPosition());

        }
    }

    public static boolean checkBitmapWorkerTask(File imageFile, ImageView imageView) {

        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final File workFile = bitmapWorkerTask.getImageFile();
            if (workFile != null) {
                if (workFile != imageFile) {
                    bitmapWorkerTask.cancel(true);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        Drawable drawableObj = imageView.getDrawable();
        if (drawableObj instanceof AsyncDrawable) {
            AsyncDrawable asyncDrawable = (AsyncDrawable) drawableObj;

            return asyncDrawable.getBitmapWorkerTask();
        }

        return null;
    }

}
