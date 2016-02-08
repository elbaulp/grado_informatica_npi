/*
 * Copyright 2016 Alejandro Alcalde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elbauldelprogramador.photogesture.camera;

import com.elbauldelprogramador.photogesture.R;
import com.elbauldelprogramador.photogesture.util.MediaScannerWrapper;
import com.elbauldelprogramador.photogesture.util.ToastUtils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Alejandro Alcalde (elbauldelprogramador.com) on 2/8/16.
 */
public class MakePhotoActivity extends Activity {

    public final static String DEBUG_TAG = "MakePhotoActivity";
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String TAG = MakePhotoActivity.class.getSimpleName();
    @Bind(R.id.countDownText)
    TextView mCountDown;
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PhotoGesture");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.count_down_activity);

        ButterKnife.bind(this);


        if (!checkCameraHardware(this)) {
            ToastUtils.show(R.string.no_camera, this);
            finish();
        }

        // Create an instance of Camera
        mCamera = getCameraInstance();
        Camera.Parameters params = mCamera.getParameters();

        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // set the focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            // set Camera parameters
            mCamera.setParameters(params);
        }


        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);


        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions: ");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();

                    new MediaScannerWrapper(getApplicationContext(), pictureFile.getPath(), "image/jpeg").scan();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }

                finish();
            }
        };

        new CountDownTimer(4000, 1000) {

            public void onTick(long millisUntilFinished) {
                mCountDown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                ToastUtils.show(R.string.make_photo, Toast.LENGTH_SHORT, getApplicationContext());
                mCamera.takePicture(null, null, mPicture);
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


}