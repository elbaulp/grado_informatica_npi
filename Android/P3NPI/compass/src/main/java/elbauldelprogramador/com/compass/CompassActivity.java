/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elbauldelprogramador.com.compass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CompassActivity extends Activity {

    private static final int REQUEST_RECOGNIZE = 100;
    private static final int REQUEST_TTS = 101;

    protected final Handler mHandlerCompass = new Handler();
    View mCompassView;
    CompassView mPointer;
    CompassView mUserHint;
    TextView mLocationTextView;
    LinearLayout mDirectionLayout;
    LinearLayout mAngleLayout;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private Context mCtx;
    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private Sensor mAccelerometer;
    private float mDirection;
    private float mHeadedDirection = -1;
    private float mTargetDirection;
    private AccelerateInterpolator mInterpolator;
    private boolean mStopDrawing;
    private TextToSpeech mTts;
    private boolean mKeepStraight = false;
    protected Runnable mCompassViewUpdater = new Runnable() {
        @Override
        public void run() {
            if (mPointer != null && !mStopDrawing) {
                if (mDirection != mTargetDirection) {

                    // calculate the short routine
                    float to = mTargetDirection;
                    if (to - mDirection > 180) {
                        to -= 360;
                    } else if (to - mDirection < -180) {
                        to += 360;
                    }

                    // limit the max speed to MAX_ROTATE_DEGREE
                    float distance = to - mDirection;
                    float MAX_ROATE_DEGREE = 1.0f;
                    if (Math.abs(distance) > MAX_ROATE_DEGREE) {
                        distance = distance > 0 ? MAX_ROATE_DEGREE : (-1.0f * MAX_ROATE_DEGREE);
                    }

                    // need to slow down if the distance is short
                    mDirection = normalizeDegree(mDirection
                            + ((to - mDirection) * mInterpolator.getInterpolation(Math
                            .abs(distance) > MAX_ROATE_DEGREE ? 0.4f : 0.3f)));
                    mPointer.updateDirection(mDirection);

                    if (mHeadedDirection != -1) {
                        mUserHint.updateDirection(mDirection + mHeadedDirection);
                    }
                }
                updateDirection();
                mHandlerCompass.postDelayed(mCompassViewUpdater, 20);
            }
        }
    };
    private SensorEventListener mMagneticSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor == mMagneticSensor) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
            } else if (event.sensor == mAccelerometer) {
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mLastAccelerometerSet = true;
            }

            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(mR, mOrientation);
                float azimuthInRadians = mOrientation[0];
                float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

                mTargetDirection = -azimuthInDegress;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass_main);
        initResources();
        initServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMagneticSensor != null) {
            mSensorManager.registerListener(mMagneticSensorEventListener, mMagneticSensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        if (mAccelerometer != null) {
            mSensorManager.registerListener(mMagneticSensorEventListener, mAccelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        mStopDrawing = false;
        mHandlerCompass.postDelayed(mCompassViewUpdater, 20);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mStopDrawing = true;
        if (mMagneticSensor != null) {
            mSensorManager.unregisterListener(mMagneticSensorEventListener);
        }
        if (mAccelerometer != null) {
            mSensorManager.unregisterListener(mMagneticSensorEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }

    private void initResources() {
        mDirection = 0.0f;
        mTargetDirection = 0.0f;
        mInterpolator = new AccelerateInterpolator();
        mStopDrawing = true;

        mCompassView = findViewById(R.id.view_compass);
        mPointer = (CompassView) findViewById(R.id.compass_pointer);
        mUserHint = (CompassView) findViewById(R.id.user_direction);
        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        mDirectionLayout = (LinearLayout) findViewById(R.id.layout_direction);
        mAngleLayout = (LinearLayout) findViewById(R.id.layout_angle);

        mPointer.setImageResource(R.drawable.compass);

        mCtx = this;
        mLocationTextView.setText(R.string.default_direction);

        mUserHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListening();
            }
        });
    }

    private void initServices() {
        // sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, REQUEST_TTS);

        startListening();
    }

    private void updateDirection() {
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        mDirectionLayout.removeAllViews();
        mAngleLayout.removeAllViews();

        ImageView east = null;
        ImageView west = null;
        ImageView south = null;
        ImageView north = null;

        float direction = normalizeDegree(mTargetDirection * -1.0f);
        if (direction > 22.5f && direction < 157.5f) {
            // east
            east = new ImageView(this);
            east.setImageResource(R.drawable.e);
            east.setLayoutParams(lp);
        } else if (direction > 202.5f && direction < 337.5f) {
            // west
            west = new ImageView(this);
            west.setImageResource(R.drawable.w);
            west.setLayoutParams(lp);
        }

        if (direction > 112.5f && direction < 247.5f) {
            // south
            south = new ImageView(this);
            south.setImageResource(R.drawable.s);
            south.setLayoutParams(lp);
        } else if (direction < 67.5 || direction > 292.5f) {
            // north
            north = new ImageView(this);
            north.setImageResource(R.drawable.n);
            north.setLayoutParams(lp);
        }


        // north/south should be before east/west
        if (south != null) {
            mDirectionLayout.addView(south);
        }
        if (north != null) {
            mDirectionLayout.addView(north);
        }
        if (east != null) {
            mDirectionLayout.addView(east);
        }
        if (west != null) {
            mDirectionLayout.addView(west);
        }

        int direction2 = (int) direction;

        float thresholdlow = -.05f * mHeadedDirection + mHeadedDirection;
        float thresholdup = .05f * mHeadedDirection + mHeadedDirection;

        if (thresholdlow <= direction && direction <= thresholdup) {
            if (!mTts.isSpeaking() && !mKeepStraight) {
                mTts.speak("Sigue en esta dirección", TextToSpeech.QUEUE_FLUSH, null);
                mKeepStraight = true;
            }
        } else if (thresholdlow <= direction && !mTts.isSpeaking()) {
            mTts.speak("Gira a la izquierda", TextToSpeech.QUEUE_FLUSH, null);
            mKeepStraight = false;
        } else if (direction <= thresholdup && !mTts.isSpeaking()) {
            mTts.speak("Gira a la derecha", TextToSpeech.QUEUE_FLUSH, null);
            mKeepStraight = false;
        }

        boolean show = false;
        if (direction2 >= 100) {
            mAngleLayout.addView(getNumberImage(direction2 / 100));
            direction2 %= 100;
            show = true;
        }
        if (direction2 >= 10 || show) {
            mAngleLayout.addView(getNumberImage(direction2 / 10));
            direction2 %= 10;
        }
        mAngleLayout.addView(getNumberImage(direction2));

        ImageView degreeImageView = new ImageView(this);
        degreeImageView.setImageResource(R.drawable.degree);
        degreeImageView.setLayoutParams(lp);
        mAngleLayout.addView(degreeImageView);
    }

    private ImageView getNumberImage(int number) {
        ImageView image = new ImageView(this);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        switch (number) {
            case 0:
                image.setImageResource(R.drawable.number_0);
                break;
            case 1:
                image.setImageResource(R.drawable.number_1);
                break;
            case 2:
                image.setImageResource(R.drawable.number_2);
                break;
            case 3:
                image.setImageResource(R.drawable.number_3);
                break;
            case 4:
                image.setImageResource(R.drawable.number_4);
                break;
            case 5:
                image.setImageResource(R.drawable.number_5);
                break;
            case 6:
                image.setImageResource(R.drawable.number_6);
                break;
            case 7:
                image.setImageResource(R.drawable.number_7);
                break;
            case 8:
                image.setImageResource(R.drawable.number_8);
                break;
            case 9:
                image.setImageResource(R.drawable.number_9);
                break;
        }
        image.setLayoutParams(lp);
        return image;
    }

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

    /**
     * Starts listening for any user input.
     * When it recognizes something, the <code>processAsrResult</code> method is invoked.
     * If there is any error, the <code>processAsrError</code> method is invoked.
     */
    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.asr_prompt));
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);

        try {
            startActivityForResult(intent, REQUEST_RECOGNIZE);
        } catch (ActivityNotFoundException e) {
            //If no recognizer exists, download from Google Play
            showDownloadDialog();
        }
    }

    private void showDownloadDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.asr_download_title);
        builder.setMessage(R.string.asr_download_msg);
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        //Download, for example, Google Voice Search
                        Intent marketIntent =
                                new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(
                                Uri.parse("market://details?"
                                        + "id=com.google.android.voicesearch"));
                    }
                });
        builder.setNegativeButton(android.R.string.no, null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RECOGNIZE &&
                resultCode == Activity.RESULT_OK) {
            ArrayList<String> matches =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String[] tokens = matches.get(0).split(" ");

            if (tokens.length == 2) {
                mHeadedDirection = Float.parseFloat(tokens[1]);
                mLocationTextView.setText(String.format(getString(R.string.heading_text), matches.get(0)));

                switch (tokens[0].toLowerCase()) {
                    case "este":
                        mHeadedDirection += 90;
                        break;
                    case "sur":
                    case "surf":
                        mHeadedDirection += 180;
                        break;
                    case "oeste":
                        mHeadedDirection += 270;
                        break;
                }

                Toast.makeText(this, R.string.asr_ask_again,
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, R.string.asr_error,
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_TTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {

                // Create a TextToSpeech instance
                mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            // Display Toast
                            Toast.makeText(getApplicationContext(), "TTS initialized", Toast.LENGTH_LONG).show();
//
//                            // Set language to US English if it is available
//                            if (mTts.isLanguageAvailable(Locale.US) >= 0)
//                                mTts.setLanguage(Locale.US);
                        }
                    }
                });
            } else {
                // Install missing data
                PackageManager pm = getPackageManager();
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                ResolveInfo resolveInfo = pm.resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);

                if (resolveInfo == null) {
                    Toast.makeText(this, "There is no TTS installed, please download it from Google Play", Toast.LENGTH_LONG).show();
                } else {
                    startActivity(installIntent);
                }
            }
        }

    }
}