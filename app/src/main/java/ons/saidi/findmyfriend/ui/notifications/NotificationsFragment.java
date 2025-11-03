package ons.saidi.findmyfriend.ui.notifications;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.util.HashMap;

import ons.saidi.findmyfriend.Config;
import ons.saidi.findmyfriend.JSONParser;
import ons.saidi.findmyfriend.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnAddLocation.setOnClickListener(view -> {
            String pseudo = binding.etPseudo.getText().toString().trim();
            String numero = binding.etNumero.getText().toString().trim();
            String longitude = binding.etLongitude.getText().toString().trim();
            String latitude = binding.etLatitude.getText().toString().trim();

            if (pseudo.isEmpty() || numero.isEmpty() || longitude.isEmpty() || latitude.isEmpty()) {
                Toast.makeText(requireActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                new AddPositionTask(pseudo, numero, longitude, latitude).execute();
            }
        });

        return root;
    }

    class AddPositionTask extends AsyncTask<Void, Void, JSONObject> {
        private AlertDialog alert;
        private final String pseudo;
        private final String numero;
        private final String longitude;
        private final String latitude;

        public AddPositionTask(String pseudo, String numero, String longitude, String latitude) {
            this.pseudo = pseudo;
            this.numero = numero;
            this.longitude = longitude;
            this.latitude = latitude;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder dialog = new AlertDialog.Builder(requireActivity());
            dialog.setTitle("Adding...");
            dialog.setMessage("Please wait...");
            alert = dialog.create();
            alert.show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                HashMap<String, String> data = new HashMap<>();
                data.put("pseudo", pseudo);
                data.put("numero", numero);
                data.put("longitude", longitude);
                data.put("latitude", latitude);

                JSONParser parser = new JSONParser();
                return parser.makeHttpRequest(Config.url_add_position, "POST", data);
            } catch (Exception e) {
                Log.e("AddPositionTask", "Error during request", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);

            if (alert != null && alert.isShowing()) {
                alert.dismiss();
            }

            if (response == null) {
                Toast.makeText(requireActivity(), "No response from server", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int success = response.getInt("success");
                String message = response.getString("message");

                if (success == 1) {
                    Toast.makeText(requireActivity(), "✅ " + message, Toast.LENGTH_SHORT).show();
                    binding.etPseudo.setText("");
                    binding.etNumero.setText("");
                    binding.etLongitude.setText("");
                    binding.etLatitude.setText("");
                } else {
                    Toast.makeText(requireActivity(), "❌ " + message, Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e("AddPositionTask", "Invalid JSON or error: " + e.getMessage());
                Toast.makeText(requireActivity(), "Error processing server response", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
