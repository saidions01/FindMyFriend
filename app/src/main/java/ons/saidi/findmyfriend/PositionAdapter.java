package ons.saidi.findmyfriend;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private List<Position> positionsList;

    public PositionAdapter(List<Position> positionsList) {
        this.positionsList = positionsList;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.position_item, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position currentPosition = positionsList.get(position);

        holder.tvName.setText(currentPosition.getPseudo());
        holder.tvPhone.setText(currentPosition.getNumero());
        holder.tvCoordinates.setText(currentPosition.getLatitude() + ", " + currentPosition.getLongitude());

        // You can calculate distance here later
        holder.tvDistance.setText("Nearby");
        holder.tvTime.setText("Active now");

        holder.btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle navigation button click
                // You can implement map navigation here
            }
        });
    }

    @Override
    public int getItemCount() {
        return positionsList.size();
    }

    public static class PositionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvCoordinates, tvDistance, tvTime;
        ImageButton btnNavigate;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvCoordinates = itemView.findViewById(R.id.tv_coordinates);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnNavigate = itemView.findViewById(R.id.btn_navigate);
        }
    }

    public void updateData(List<Position> newPositions) {
        this.positionsList = newPositions;
        notifyDataSetChanged();
    }
}
