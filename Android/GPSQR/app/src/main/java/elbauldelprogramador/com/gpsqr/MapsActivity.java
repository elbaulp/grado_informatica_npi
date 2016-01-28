package elbauldelprogramador.com.gpsqr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.software.shell.fab.ActionButton;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Activity mAct;
    private GoogleMap mMap;

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



//        GoogleDirection.withServerKey(getString(R.string.google_maps_key))
//                .from(new LatLng(40.2085, -3.7136))
//                .to(new LatLng(37.1839719, -3.6017634))
//                .avoid(AvoidType.FERRIES)
//                .avoid(AvoidType.HIGHWAYS)
//                .execute(new DirectionCallback() {
//                    @Override
//                    public void onDirectionSuccess(Direction direction) {
//                        if (direction.isOK()) {
//                            Toast.makeText(getApplicationContext(), "DIRECTION KOK", Toast.LENGTH_LONG).show();
////                            direction.
//                        } else {
//                            Toast.makeText(getApplicationContext(), direction.getStatus(), Toast.LENGTH_LONG).show();
//                        }
//                    }
//
//                    @Override
//                    public void onDirectionFailure(Throwable t) {
//                        Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();
//                    }
//                });

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

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}