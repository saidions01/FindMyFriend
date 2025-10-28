package ons.saidi.findmyfriend;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
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
    private double receivedLatitude;
    private double receivedLongitude;
    private boolean hasValidLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get and validate the coordinates from the intent
        String longitudeStr = getIntent().getStringExtra("longitude");
        String latitudeStr = getIntent().getStringExtra("latitude");

        Log.d("MapsActivity", "Received coordinates - Lat: " + latitudeStr + ", Long: " + longitudeStr);

        if (longitudeStr != null && latitudeStr != null &&
                !longitudeStr.trim().isEmpty() && !latitudeStr.trim().isEmpty()) {
            try {
                receivedLatitude = Double.parseDouble(latitudeStr.trim());
                receivedLongitude = Double.parseDouble(longitudeStr.trim());
                hasValidLocation = true;
                Log.d("MapsActivity", "Parsed coordinates - Lat: " + receivedLatitude + ", Long: " + receivedLongitude);
            } catch (NumberFormatException e) {
                Log.e("MapsActivity", "Error parsing coordinates", e);
                Toast.makeText(this, "Invalid location coordinates received", Toast.LENGTH_LONG).show();
                hasValidLocation = false;
            }
        } else {
            Log.w("MapsActivity", "No valid coordinates received");
            Toast.makeText(this, "No location data received", Toast.LENGTH_LONG).show();
            hasValidLocation = false;
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double latitude, longitude;
        String title;

        if (hasValidLocation) {
            latitude = receivedLatitude;
            longitude = receivedLongitude;
            title = "Friend's Location";
            Toast.makeText(this, "Showing friend's location", Toast.LENGTH_SHORT).show();
        } else {
            // Default to a central location if no valid coordinates
            latitude = 31.6295; // Morocco center
            longitude = -7.9811;
            title = "Default Location (No valid coordinates received)";
        }

        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .snippet("Lat: " + latitude + ", Long: " + longitude));

        // Move camera with appropriate zoom level
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));

        // Enable zoom controls and location button if available
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        Log.d("MapsActivity", "Map ready and marker added at: " + latitude + ", " + longitude);
    }
}