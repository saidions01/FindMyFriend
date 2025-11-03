package ons.saidi.findmyfriend.ui.dashboard;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import ons.saidi.findmyfriend.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

private FragmentDashboardBinding binding;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = binding.edtPhone.getText().toString().trim();
                if (number.isEmpty()) return;

                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage(number, null,
                        "FindMyFriends: Envoyer moi votre position",
                        null,
                        null);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}