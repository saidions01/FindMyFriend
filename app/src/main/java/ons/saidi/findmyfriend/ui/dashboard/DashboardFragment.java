package ons.saidi.findmyfriend.ui.dashboard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import ons.saidi.findmyfriend.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private static final int SMS_PERMISSION_CODE = 101;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = binding.edtPhone.getText().toString().trim();
                if (number.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check SMS permission
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                } else {
                    sendLocationRequest(number);
                }
            }
        });

        return root;
    }

    private void sendLocationRequest(String number) {
        try {
            SmsManager manager = SmsManager.getDefault();
            manager.sendTextMessage(number, null,
                    "FindMyFriends: Envoyer moi votre position",
                    null,
                    null);
            Toast.makeText(requireContext(), "Location request sent!", Toast.LENGTH_SHORT).show();
            binding.edtPhone.setText(""); // Clear the input
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String number = binding.edtPhone.getText().toString().trim();
                if (!number.isEmpty()) {
                    sendLocationRequest(number);
                }
            } else {
                Toast.makeText(requireContext(), "SMS permission is required to send location requests", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}