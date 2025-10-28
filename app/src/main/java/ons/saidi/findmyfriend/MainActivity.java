package ons.saidi.findmyfriend;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import ons.saidi.findmyfriend.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

private ActivityMainBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     binding = ActivityMainBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                },
                1
        );
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "FindMyFreinds_ChannelID",
                    "FindMyFreinds Location Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for location sharing");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

        if(requestCode == 1){
            if(grantResults.length > 0){
                if(grantResults[0] == PackageManager.PERMISSION_DENIED ||
                        grantResults[3] == PackageManager.PERMISSION_DENIED ) {

                    // If any permission is denied, close the app
                    finish();
                }
            } else {

                // Permissions denied
                finish();
            }
        }
    }
    }

