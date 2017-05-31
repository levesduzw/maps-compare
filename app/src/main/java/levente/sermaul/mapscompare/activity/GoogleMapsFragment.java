package levente.sermaul.mapscompare.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;

import levente.sermaul.mapscompare.R;

/**
 * Created by Levente on 2016.10.26..
 */
public class GoogleMapsFragment extends Fragment {
    public MapView mapView;
    public GoogleMap map;
    LinearLayout zoomControls;
    RelativeLayout crosshairBlack;
    RelativeLayout crosshairWhite;

    double[] lastCenterCoords;
    int lastZoomLevel;
    float lastBearing;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout) layoutInflater.inflate(R.layout.fragment_maps_google, container, false);
        super.onCreateView(layoutInflater, container, savedInstanceState);  // sima super hívás, nem return-öl az értékével!
        mapView = (MapView) layout.findViewById(R.id.googleMapView);
        mapView.onCreate(savedInstanceState);

        zoomControls = (LinearLayout) layout.findViewById(R.id.zoom_controls);
        crosshairBlack = (RelativeLayout) layout.findViewById(R.id.crossbar_black);
        crosshairWhite = (RelativeLayout) layout.findViewById(R.id.crossbar_white);

        lastCenterCoords = new double[2];

        return layout;
    }

    // Fragment lifecycle callback metódusok
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mapView != null) {
            try {
                //SupportMapFragment fragment = ((SupportMapFragment) getFragmentManager().get;
                mapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("TAG", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            //mapView.onSaveInstanceState(outState);
        }
    }
}
