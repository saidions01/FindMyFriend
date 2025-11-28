package ons.saidi.findmyfriend.ui.home;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ons.saidi.findmyfriend.JSONParser;
import ons.saidi.findmyfriend.Config;
import ons.saidi.findmyfriend.Position;
import ons.saidi.findmyfriend.R;
import ons.saidi.findmyfriend.databinding.FragmentHomeBinding;
import ons.saidi.findmyfriend.PositionAdapter;


public class HomeFragment extends Fragment {

    ArrayList<Position> data = new ArrayList<Position>();
    private FragmentHomeBinding binding;
    private PositionAdapter adapter;
    private RecyclerView recyclerViewPositions;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Use findViewById instead of data binding
        recyclerViewPositions = root.findViewById(R.id.recyclerViewPositions);

        // Setup RecyclerView
        recyclerViewPositions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PositionAdapter(data);
        recyclerViewPositions.setAdapter(adapter);

        binding.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Download d = new Download();
                d.execute();
            }
        });

        return root;
    }

    class Download extends AsyncTask<Void, Void, Void> {
        AlertDialog alert;

        @Override
        protected Void doInBackground(Void... voids) { //second thread
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeHttpRequest(Config.url_get_position, "GET", null);
            Log.e("response", response.toString());

            try {
                int success = response.getInt("success");
                if (success == 1) {
                    data.clear();
                    JSONArray tableau = response.getJSONArray("positions");
                    for (int i = 0; i < tableau.length(); i++) {
                        JSONObject ligne = tableau.getJSONObject(i);
                        int idposition = ligne.getInt("idposition");
                        String pseudo = ligne.getString("pseudo");
                        String numero = ligne.getString("numero");
                        String longitude = ligne.getString("longitude");
                        String latitude = ligne.getString("latitude");
                        data.add(new Position(idposition, pseudo, numero, longitude, latitude));
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(HomeFragment.this.getActivity());
            dialog.setTitle("Downloading");
            dialog.setMessage("Please wait...");
            alert = dialog.create();
            alert.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            alert.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}