package levente.sermaul.mapscompare;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Levente on 2016.12.03..
 */
public class FavoritePlacesAdapter extends RecyclerView.Adapter<FavoritePlacesAdapter.ViewHolder> {
    ArrayList<FavoritePlace> favoritePlaces;
    Context context;
    private final LayoutInflater layoutInflater;
    OnFavoriteItemSelectedListener listener;

    public FavoritePlacesAdapter(ArrayList<FavoritePlace> _favoritePlaces, Context _context, OnFavoriteItemSelectedListener _listener) {
        favoritePlaces = _favoritePlaces;
        context = _context;
        listener = _listener;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public FavoritePlacesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.favorites_list_item, parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FavoritePlacesAdapter.ViewHolder holder, int position) {
        FavoritePlace place = favoritePlaces.get(position);

        holder.title.setText(place.getName());
        holder.coords.setText(place.getCoords());
        holder.addedDate.setText(place.getDate());
    }

    @Override
    public int getItemCount() {
        return favoritePlaces.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView coords;
        public TextView addedDate;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            coords = (TextView) itemView.findViewById(R.id.item_coords);
            addedDate = (TextView) itemView.findViewById(R.id.added_date);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onFavoriteItemSelected(v, getLayoutPosition());
                }
            });
        }
    }
}
