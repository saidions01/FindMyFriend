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
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent() != null) {
            // read as doubles (must match what the receiver sends)
            longitude = getIntent().getDoubleExtra("longitude", 0.0);
            latitude = getIntent().getDoubleExtra("latitude", 0.0);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (latitude != 0.0 || longitude != 0.0) {
            LatLng friendLocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(friendLocation).title("Friend's Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLocation, 15f));
        } else {
            // optionally show a default location or a toast
        }
    }
}