package levente.sermaul.mapscompare.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import levente.sermaul.mapscompare.FavoritePlace;
import levente.sermaul.mapscompare.FavoritePlacesAdapter;
import levente.sermaul.mapscompare.OnFavoriteItemSelectedListener;
import levente.sermaul.mapscompare.R;

public class FavoritePlacesActivity extends AppCompatActivity implements OnFavoriteItemSelectedListener {
    private final String FILENAME = "favorite_places.csv";
    private final String FILENAME_TEMP = "favorite_places_temp.csv";

    static Activity favoritePlacesActivity;
    RecyclerView recyclerView;
    View emptyListView;

    ArrayList<FavoritePlace> favoritePlaces;
    FavoritePlacesAdapter favoritePlacesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_places);
        Toolbar toolbar = (Toolbar)findViewById(R.id.favorite_places_toolbar);
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favoritePlacesActivity = this;

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        emptyListView = findViewById(R.id.empty_list_view);
        favoritePlaces = new ArrayList<>();

        csvToArrayList();

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        favoritePlacesAdapter = new FavoritePlacesAdapter(favoritePlaces, this.getApplicationContext(), this);
        recyclerView.setAdapter(favoritePlacesAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                favoritePlaces.remove(viewHolder.getAdapterPosition());
                csvRemove(viewHolder.getAdapterPosition());
                recyclerView.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.invalidate();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }



    private void csvToArrayList() {
        FileInputStream in;
        BufferedReader reader;
        String line;

        try {
            in = openFileInput(FILENAME);
            reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");   // [lat, long], dátum, [hely, ország]      ha a helyben nincs vessző: size(values) = 5
                FavoritePlace fp = new FavoritePlace();
                fp.setCoords(values[0]+", "+values[1]);
                fp.setDate(values[2]);
                if (values.length==5) {             // ha nincs vessző a címben
                    fp.setName(values[3]+", "+values[4]);
                } else {                            // ha vannak vesszők a címben
                    StringBuilder name = new StringBuilder(values[3]);
                    for (int i=0; i<values.length - 4; i++){
                        name.append(", "+values[4+i]);
                    }
                    fp.setName(name.toString());
                }
                favoritePlaces.add(fp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (favoritePlaces.size() != 0) {
            emptyListView.setVisibility(View.GONE);
        } else {
            emptyListView.setVisibility(View.VISIBLE);
        }
    }

    private void csvRemove(int index) {     // http://stackoverflow.com/a/1377322
        File in, out;
        BufferedReader reader;
        BufferedWriter writer;
        String line;
        int i = 0;

        try {
            in = new File(getFilesDir(), FILENAME);     // http://stackoverflow.com/a/13803870  -  getFilesDir() kell! (internal storage, FileInputStream nélkül)
            out = new File(getFilesDir(), FILENAME_TEMP);

            reader = new BufferedReader(new FileReader(in));
            writer = new BufferedWriter(new FileWriter(out));


            while ((line = reader.readLine()) != null) {
                if (i == index) {
                    i++;
                    continue;
                } else {
                    i++;
                    writer.write(line + System.getProperty("line.separator"));      // ez mi?
                }
            }
            writer.close();
            reader.close();
            boolean successful = out.renameTo(in);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFavoriteItemSelected(View v, int position) {
        CharSequence text = ((TextView) v.findViewById(R.id.item_coords)).getText();
        String[] coords = text.toString().split(",");
        Intent intent = new Intent("SCROLL_MAP");
        intent.putExtra("latitude", Double.valueOf(coords[0]));
        intent.putExtra("longitude", Double.valueOf(coords[1]));
        MainActivity.context.sendBroadcast(intent);
        //favoritePlacesActivity.finish();
        //NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }
}
