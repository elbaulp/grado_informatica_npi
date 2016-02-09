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

package elbauldelprogramador.com.gpsqr;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.software.shell.fab.ActionButton;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by:
 *
 * Alejandro Alcalde (elbauldelprogramador.com)
 * Cristina Heredia
 *
 * on 2/9/16.
 *
 * This file is part of GPSQR
 */
public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        StreetViewPanorama.OnStreetViewPanoramaChangeListener,
        OnStreetViewPanoramaReadyCallback {


    protected static final String TAG = MapsActivity.class.getSimpleName();

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Regex to extract Lat, Lng from strings like this: LATITUD_37.19735641547103_LONGITUD_-3.623774830675075
     */
    private static final Pattern pat = Pattern.compile("[A-Z]+_(-?\\d+\\.\\d+)");

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Represents a geographical location.
     */
    private LatLng mCurrentLocation;

    /**
     * Last known location, used for drawing path between two Locations
     */
    private LatLng mPreviousLocation;

    /**
     * Used to restore the map state when restoring from a configuration change
     */
    private ArrayList<LatLng> mLocationsList;

    /**
     * UI things
     */
    private Activity mAct;
    private GoogleMap mMap;
    private StreetViewPanorama mStreetViewPanorama;

    /**
     * The coordinates readed from the QR code
     */
    private double[] mCoord = new double[2]; // 0:lat,1:lng

    /**
     * Broadcast Receiver to receive location updates
     */
    private BroadcastReceiver mLocationReceiver;

    /**
     * Intent for launch the Service
     */
    private Intent mRequestLocationIntent;

    /**
     * Change the current location in the map. Draws a line between the last location and the
     * current to show the path taken.
     */
    private void updateMap() {
        if (mPreviousLocation != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(mCurrentLocation)
                    .add(mPreviousLocation)
                    .color(Color.RED)
                    .width(5);
            mMap.addPolyline(polylineOptions);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 21f));
        if (mStreetViewPanorama != null) {
            mStreetViewPanorama.setPosition(mCurrentLocation);
        }
    }

    /**
     * Overloaded updateMap method. Used when the activity is restored from a saved instance state.
     *
     * @param loc: All the locations in which the user have been.
     */
    private void updateMap(ArrayList<LatLng> loc) {

        LatLng current;
        LatLng previous;

        for (int i = 0; i < loc.size() - 1; i++) {
            current = loc.get(i);
            previous = loc.get(i + 1);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(current)
                    .add(previous)
                    .color(Color.RED)
                    .width(5);
            mMap.addPolyline(polylineOptions);

            mMap.addMarker(new MarkerOptions().position(current).title("TITLE"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 21f));
        }

        if (loc.size() != 0 && mStreetViewPanorama != null) {
            mStreetViewPanorama.setPosition(loc.get(loc.size() - 1));
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        mAct = this;
        mRequestingLocationUpdates = true;

        final ActionButton ab = (ActionButton) findViewById(R.id.action_button);

        ab.setImageResource(R.drawable.ic_qr);
        ab.setShowAnimation(ActionButton.Animations.JUMP_FROM_DOWN);
        ab.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
        ab.setButtonColor(getResources().getColor(R.color.fab_material_amber_500));
        ab.setButtonColorPressed(getResources().getColor(R.color.fab_material_amber_900));

        ab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ab.hide();
                new IntentIntegrator(mAct).initiateScan();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ab.show();
                    }
                }, 1000);
            }
        });

        mLocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPreviousLocation = mCurrentLocation;
                mCurrentLocation = intent.getParcelableExtra(LocationUpdaterService.COPA_MESSAGE);
                updateMap();
                mLocationsList.add(mCurrentLocation);

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "LocationList size: " + mLocationsList.size());
                }
            }
        };

        mRequestLocationIntent = new Intent(this, LocationUpdaterService.class);
        startService(mRequestLocationIntent);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Updating values from bundle");
        }
        if (savedInstanceState != null) {
            // Update the value of mLocationsList from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mLocationsList
                // is not null.
                mLocationsList = savedInstanceState.getParcelableArrayList(LOCATION_KEY);
                mCurrentLocation = mLocationsList.get(mLocationsList.size() - 1);
                if (mLocationsList.size() >= 2) {
                    mPreviousLocation = mLocationsList.get(mLocationsList.size() - 2);
                }
            }
        } else {
            mLocationsList = new ArrayList<>(2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Cancelled scan");
                }
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Scanned");
                }
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Matcher m = pat.matcher(result.getContents());
                int i = 0;
                while (m.find()) {
                    mCoord[i++] = Double.parseDouble(m.group(1));
                }
                // Add a marker and move the camera
                LatLng firstLocation = new LatLng(mCoord[0], mCoord[1]);
                mMap.addMarker(new MarkerOptions().position(firstLocation).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(firstLocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 21.0f));

                GoogleDirection.withServerKey(getString(R.string.google_maps_server_key))
                        .from(mCurrentLocation)
                        .to(new LatLng(mCoord[0], mCoord[1]))
                        .transportMode(TransportMode.WALKING)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction) {
                                if (direction.isOK()) {
                                    Toast.makeText(getApplicationContext(), "DIRECTION KOK", Toast.LENGTH_LONG).show();
                                    ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.BLUE);
                                    mMap.addPolyline(polylineOptions);
                                } else {
                                    Toast.makeText(getApplicationContext(), "NOT OK" + direction.getStatus(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {
                                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();
                            }
                        });

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        if (mLocationsList != null) {
            updateMap(mLocationsList);
        }
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-1, -1), 12.0f));
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        mStreetViewPanorama = panorama;
        mStreetViewPanorama.setOnStreetViewPanoramaChangeListener(this);
        mStreetViewPanorama.setStreetNamesEnabled(true);

        // Only need to set the position once as the streetview fragment will maintain
        // its state.
        if (mCurrentLocation != null) {
            mStreetViewPanorama.setPosition(mCurrentLocation);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver, new IntentFilter(LocationUpdaterService.COPA_RESULT));
    }

    @Override
    protected void onDestroy() {
        stopService(mRequestLocationIntent);

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);

        savedInstanceState.putParcelableArrayList(LOCATION_KEY, mLocationsList);
//        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation location) {
        if (mCurrentLocation != null) {
            mStreetViewPanorama.setPosition(mCurrentLocation);
        }
    }
}