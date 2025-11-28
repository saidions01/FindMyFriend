package ons.saidi.findmyfriend;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import ons.saidi.findmyfriend.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get coordinates from intent
        if (getIntent() != null) {
            longitude = getIntent().getDoubleExtra("longitude", 0.0);
            latitude = getIntent().getDoubleExtra("latitude", 0.0);
        }

        // Validate coordinates
        if (isValidCoordinate(latitude) && isValidCoordinate(longitude)) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } else {
            Toast.makeText(this, "Invalid location coordinates", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean isValidCoordinate(double coordinate) {
        return coordinate != 0.0 && coordinate >= -180 && coordinate <= 180;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (isValidCoordinate(latitude) && isValidCoordinate(longitude)) {
            LatLng friendLocation = new LatLng(latitude, longitude);

            // Add marker with better styling
            mMap.addMarker(new MarkerOptions()
                    .position(friendLocation)
                    .title("Friend's Location")
                    .snippet(String.format("Lat: %.6f, Lng: %.6f", latitude, longitude)));

            // Move camera with animation
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(friendLocation, 15f));

            // Enable zoom controls
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

        } else {
            Toast.makeText(this, "Unable to display location", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}