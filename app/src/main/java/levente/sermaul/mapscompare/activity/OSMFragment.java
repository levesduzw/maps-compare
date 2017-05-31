package levente.sermaul.mapscompare.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import levente.sermaul.mapscompare.R;
import levente.sermaul.mapscompare.bingmaps.BingMapTileSource;
import levente.sermaul.mapscompare.heremaps.HEREWeGoTileSource;

/**
 * Created by Levente on 2016.10.26..
 */
public class OSMFragment extends Fragment {
    public MapView mapView;
    LinearLayout zoomControls;
    RelativeLayout crosshairBlack;
    RelativeLayout crosshairWhite;
    double[] lastCenterCoords;
    int lastZoomLevel;
    float lastBearing;
    private TileProvider tileProvider;

    Context context;


    String mapType;
    public enum TileProvider {
        MAPNIK,
        MAPBOX, MAPBOX_SAT,
        HEREWEGO, HEREWEGO_SAT,
        BING, BING_SAT
    }

    public void setTileProvider(TileProvider tp) {
        tileProvider = tp;
        if (mapView == null) return;
        if (mapView.getTileProvider() != null) mapView.getTileProvider().clearTileCache();
        switch (tp) {
            case MAPNIK:
                final ITileSource mapnikTileSource = TileSourceFactory.MAPNIK;
                mapView.setTileSource(mapnikTileSource);
                mapType = "OpenStreetMaps";
                break;
            case MAPBOX:
                final MapBoxTileSource mapboxTileSource = new MapBoxTileSource();
                mapboxTileSource.setAccessToken("pk.eyJ1IjoibGV2ZXNkdXp3IiwiYSI6ImNpdjhqeGkwYzAwMWMydHBmcnc3MmdwYWMifQ.3kFmZHgdB3CNA9yN1XJrRA");
                mapboxTileSource.setMapboxMapid("mapbox.streets");
                mapView.setTileSource(mapboxTileSource);
                mapType = "Mapbox";
                break;
            case MAPBOX_SAT:
                final MapBoxTileSource mapboxSatTileSource = new MapBoxTileSource();
                mapboxSatTileSource.retrieveAccessToken(context);
                mapboxSatTileSource.setMapboxMapid("mapbox.streets-satellite");
                mapView.setTileSource(mapboxSatTileSource);
                mapType = "Mapbox";
                break;
            case HEREWEGO:
                final ITileSource tileSource = new HEREWeGoTileSource(context);
                ((HEREWeGoTileSource) tileSource).setAppCode("nk-J0pHKR1urxEhlv58Xsg");
                ((HEREWeGoTileSource) tileSource).setAppId("2JXgMmZ5DVoR6yTHJwcg");
                mapView.setTileSource(tileSource);
                mapType = "Here Maps";
                break;
            case HEREWEGO_SAT:
                final ITileSource tileSource2 = new HEREWeGoTileSource(context);
                ((HEREWeGoTileSource) tileSource2).setHereWeGoMapid("hybrid.day");
                ((HEREWeGoTileSource) tileSource2).setAppCode("nk-J0pHKR1urxEhlv58Xsg");
                ((HEREWeGoTileSource) tileSource2).setAppId("2JXgMmZ5DVoR6yTHJwcg");
                mapView.setTileSource(tileSource2);
                mapType = "Here Maps";
                break;
            case BING:
                BingMapTileSource.setBingKey("AurJVD33OZvQRlQJR1RDXP7lY9dRXrUBS9cqFikiShZSQze3ScNqjOtkDZVw8ue3");
                BingMapTileSource bingTileSource = new levente.sermaul.mapscompare.bingmaps.BingMapTileSource(null, BingMapTileSource.IMAGERYSET_ROAD);
                bingTileSource.initMetaData();
                bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
                mapView.setTileSource(bingTileSource);
                mapType = "Bing Maps";
                break;
            case BING_SAT:
                BingMapTileSource.setBingKey("AurJVD33OZvQRlQJR1RDXP7lY9dRXrUBS9cqFikiShZSQze3ScNqjOtkDZVw8ue3");
                BingMapTileSource bingSatTileSource = new levente.sermaul.mapscompare.bingmaps.BingMapTileSource(null, BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
                bingSatTileSource.initMetaData();
                bingSatTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
                mapView.setTileSource(bingSatTileSource);
                mapType = "Bing Maps";
                break;
        }
    }
    public TileProvider getTileProvider() {
        return tileProvider;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout) layoutInflater.inflate(R.layout.fragment_osm, container, false);
        super.onCreateView(layoutInflater, container, savedInstanceState);  // sima super hívás, nem return-öl az értékével

        mapView = (MapView) layout.findViewById(R.id.osmMapView);
        zoomControls = (LinearLayout) layout.findViewById(R.id.zoom_controls);
        crosshairBlack = (RelativeLayout) layout.findViewById(R.id.crossbar_black);
        crosshairWhite = (RelativeLayout) layout.findViewById(R.id.crossbar_white);

        lastCenterCoords = new double[2];

        return layout;
    }

}
