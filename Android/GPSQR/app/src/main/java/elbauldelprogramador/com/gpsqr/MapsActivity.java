package elbauldelprogramador.com.gpsqr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.software.shell.fab.ActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
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
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;
    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
    /**
     * UI things
     */
    private Activity mAct;
    private GoogleMap mMap;
    /**
     * The coordinates readed from the QR code
     */
    private double[] mCoord = new double[2]; // 0:lat,1:lng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAct = this;

        final ActionButton ab = (ActionButton) findViewById(R.id.action_button);

        ab.setImageResource(R.drawable.fab_plus_icon);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Matcher m = pat.matcher(result.getContents());
                int i = 0;
                while (m.find()) {
                    mCoord[i++] = Double.parseDouble(m.group(1));
                }
                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(mCoord[0], mCoord[1]);
                mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12.0f));

                GoogleDirection.withServerKey(getString(R.string.google_maps_server_key))
                        .from(new LatLng(37.2011816, -3.6167042))
                        .to(new LatLng(mCoord[0], mCoord[1]))
                        .transportMode(TransportMode.WALKING)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction) {
                                if (direction.isOK()) {
                                    Toast.makeText(getApplicationContext(), "DIRECTION KOK", Toast.LENGTH_LONG).show();
                                    ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                                    mMap.addPolyline(polylineOptions);


                                    List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
                                    ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(getApplicationContext(), stepList, 5, Color.CYAN, 3, Color.BLUE);
                                    for (PolylineOptions polylineOption : polylineOptionList) {
                                        mMap.addPolyline(polylineOption);
                                    }
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
    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}