package levente.sermaul.mapscompare.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import levente.sermaul.mapscompare.BuildConfig;
import levente.sermaul.mapscompare.FavoritePlace;
import levente.sermaul.mapscompare.NonClickableToolbar;
import levente.sermaul.mapscompare.R;
import levente.sermaul.mapscompare.SearchAdapter;
import levente.sermaul.mapscompare.SearchField;
import levente.sermaul.mapscompare.SearchObserver;
import levente.sermaul.mapscompare.bingmaps.BingMapTileSource;
import levente.sermaul.mapscompare.heremaps.HEREWeGoTileSource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final double START_LATITUDE = 47.504600;
    public static final double START_LONGITUDE = 19.050300;
    public static final int START_ZOOM_LEVEL = 14;

    private GoogleMapsFragment gmf;
    private OSMFragment osmf;
    private OSMFragment osmf2; // csak splitscreenview-hoz
    private ArrayList<Fragment> activeFragments;
    LinearLayout linearLayout;

    static Context context;
    static double[] currentCenterCoords;
    static int currentZoomLevel;
    static float currentBearing;
    //Fragment currentMapFragment;

    FloatingActionButton fab, fab2, fab3, fab4, fab5;
    FloatingActionButton[] fabs;
    Menu menu;
    SearchField autoCompleteTextView;
    SearchAdapter adapter;
    DrawerLayout drawer;

    int googleMapType = 0;
    int osmType;    // 0 = NORMAL, 1 = SATELLITE

    boolean fabLongClicked = false;
    boolean fabSwiped = false;
    boolean gmfOnTop = true;
    boolean splitscreen = false;
    boolean lockMapToGmf = false;
    boolean lockMapToOsmf = false;
    boolean lockMapToOsmf2 = false;
    boolean fullscreenLockMapToGmf = false;
    boolean fullscreenLockMapToOsmf = false;
    boolean isGmfInMotion;
    boolean isOsmfInMotion;
    boolean isOsmf2InMotion;

    String screenOrientation;
    private boolean screenOrientationLocked;
    private String fabBehavior;
    private boolean fabBehaviorReplaceOther;
    boolean rotationGesturesEnabled;
    boolean showCrosshair;
    private ArrayList<String> searchDropdownStringArray;

    boolean[] spinner_maps_selected = new boolean[2];
    int[] spinner_selection_positions = new int[2];
    String[] splitScreenFragments = new String[2];

    boolean spinner_map_selected;
    int spinner_selection_position;
    String fullScreenFragment = new String();

    public static ArrayList<FavoritePlace> favoritePlaces;    // todo oncreate-ben fájl beolvasás
    boolean spinner_fav_selected;
    private final String FILENAME = "favorite_places.csv";

    Fragment currentFullScreenVisibleFragment;
    Fragment prevFullScreenFragment;
    ArrayList<String> hiddenMaps;
    ArrayList<String> allMaps;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        favoritePlaces = new ArrayList<>();
        hiddenMaps = new ArrayList<String>(3);



        context = this;
        NonClickableToolbar toolbar = (NonClickableToolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_btn);

        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setItemIconTintList(null);

        //ImageView headerImage = new ImageView(MainActivity.this);
        //headerImage.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        //navigationView.addHeaderView(headerImage);

        //TextInputLayout search = (TextInputLayout) navigationView.findViewById(R.id.search_layout);

        menu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {        // TODO OnNavItemSelected osztály (implements NavigationView.OnNavigationItemSelectedListener() )
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.splitscreen_mode:
                        showSplitScreenMapSelectDialog();
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.fullscreen_mode:
                        showFullScreenMapSelectDialog();
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.googleMapsNormal:
                        item.setVisible(false);
                        menu.findItem(R.id.googleMapsSatellite).setVisible(true);
                        if (gmf != null) {
                            googleMapType = GoogleMap.MAP_TYPE_NORMAL;    // eltárolás
                            gmf.map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            if (showCrosshair) {                          // célkereszt beállítása
                                gmf.crosshairBlack.setVisibility(View.VISIBLE);
                                gmf.crosshairWhite.setVisibility(View.INVISIBLE);
                            }
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.googleMapsSatellite:
                        item.setVisible(false);
                        menu.findItem(R.id.googleMapsNormal).setVisible(true);
                        if (gmf != null) {
                            googleMapType = GoogleMap.MAP_TYPE_HYBRID;   // eltárolás
                            gmf.map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            if (showCrosshair) {                          // célkereszt beállítása
                                gmf.crosshairBlack.setVisibility(View.INVISIBLE);
                                gmf.crosshairWhite.setVisibility(View.VISIBLE);
                            }
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.mapboxNormal:
                        item.setVisible(false);
                        menu.findItem(R.id.mapboxSatellite).setVisible(true);
                        if (osmf != null && osmf.getTileProvider() == OSMFragment.TileProvider.MAPBOX_SAT) {
                            osmf.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                            if (showCrosshair) {
                                osmf.crosshairBlack.setVisibility(View.VISIBLE);
                                osmf.crosshairWhite.setVisibility(View.INVISIBLE);
                            }
                        }
                        if (osmf2 != null && osmf2.getTileProvider() == OSMFragment.TileProvider.MAPBOX_SAT) {
                            osmf2.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                            if (showCrosshair) {
                                osmf2.crosshairBlack.setVisibility(View.VISIBLE);
                                osmf2.crosshairWhite.setVisibility(View.INVISIBLE);
                            }
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.mapboxSatellite:
                        item.setVisible(false);
                        menu.findItem(R.id.mapboxNormal).setVisible(true);
                        if (osmf != null && osmf.getTileProvider() == OSMFragment.TileProvider.MAPBOX) {
                            osmf.setTileProvider(OSMFragment.TileProvider.MAPBOX_SAT);
                            if (showCrosshair) {
                                osmf.crosshairBlack.setVisibility(View.INVISIBLE);
                                osmf.crosshairWhite.setVisibility(View.VISIBLE);
                            }
                        }
                        if (osmf2 != null && osmf2.getTileProvider() == OSMFragment.TileProvider.MAPBOX) {
                            osmf2.setTileProvider(OSMFragment.TileProvider.MAPBOX_SAT);
                            if (showCrosshair) {
                                osmf2.crosshairBlack.setVisibility(View.INVISIBLE);
                                osmf2.crosshairWhite.setVisibility(View.VISIBLE);
                            }
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.hereMapsNormal:
                        item.setVisible(false);
                        menu.findItem(R.id.hereMapsSatellite).setVisible(true);
                        if (osmf != null && osmf.getTileProvider() == OSMFragment.TileProvider.HEREWEGO_SAT) {
                            osmf.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                            if (showCrosshair) {
                                osmf.crosshairBlack.setVisibility(View.VISIBLE);
                                osmf.crosshairWhite.setVisibility(View.INVISIBLE);
                            }
                        }
                        if (osmf2 != null && osmf2.getTileProvider() == OSMFragment.TileProvider.HEREWEGO_SAT) {
                            osmf2.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                            if (showCrosshair) {
                                osmf2.crosshairBlack.setVisibility(View.VISIBLE);
                                osmf2.crosshairWhite.setVisibility(View.INVISIBLE);
                            }
                        }

                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.hereMapsSatellite:
                        item.setVisible(false);
                        menu.findItem(R.id.hereMapsNormal).setVisible(true);
                        if (osmf != null && osmf.getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                            osmf.setTileProvider(OSMFragment.TileProvider.HEREWEGO_SAT);
                            if (showCrosshair) {
                                osmf.crosshairBlack.setVisibility(View.INVISIBLE);
                                osmf.crosshairWhite.setVisibility(View.VISIBLE);
                            }
                        }
                        if (osmf2 != null && osmf2.getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                            osmf2.setTileProvider(OSMFragment.TileProvider.HEREWEGO_SAT);
                            if (showCrosshair) {
                                osmf2.crosshairBlack.setVisibility(View.INVISIBLE);
                                osmf2.crosshairWhite.setVisibility(View.VISIBLE);
                            }
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.bingMapsNormal:
                        item.setVisible(false);
                        menu.findItem(R.id.bingMapsSatellite).setVisible(true);
                        if (osmf != null && osmf.getTileProvider() == OSMFragment.TileProvider.BING_SAT) {
                            osmf.setTileProvider(OSMFragment.TileProvider.BING);
                            if (showCrosshair) {
                                osmf.crosshairBlack.setVisibility(View.VISIBLE);
                                osmf.crosshairWhite.setVisibility(View.INVISIBLE);
                            }
                        }
                        if (osmf2 != null && osmf2.getTileProvider() == OSMFragment.TileProvider.BING_SAT) {
                            osmf2.setTileProvider(OSMFragment.TileProvider.BING);
                            if (showCrosshair) {
                                osmf2.crosshairBlack.setVisibility(View.VISIBLE);
                                osmf2.crosshairWhite.setVisibility(View.INVISIBLE);
                            }
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.bingMapsSatellite:
                        item.setVisible(false);
                        menu.findItem(R.id.bingMapsNormal).setVisible(true);
                        if (osmf != null && osmf.getTileProvider() == OSMFragment.TileProvider.BING) {
                            osmf.setTileProvider(OSMFragment.TileProvider.BING_SAT);
                            if (showCrosshair) {
                                osmf.crosshairBlack.setVisibility(View.INVISIBLE);
                                osmf.crosshairWhite.setVisibility(View.VISIBLE);
                            }
                        }
                        if (osmf2 != null && osmf2.getTileProvider() == OSMFragment.TileProvider.BING) {
                            osmf2.setTileProvider(OSMFragment.TileProvider.BING_SAT);
                            if (showCrosshair) {
                                osmf2.crosshairBlack.setVisibility(View.INVISIBLE);
                                osmf2.crosshairWhite.setVisibility(View.VISIBLE);
                            }
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.select_maps:
                        showSplitScreenMapSelectDialog();
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.add_favorite_item:
                        if (splitscreen) showSplitScreenFavoriteDialog();
                        saveToFavorites(currentFullScreenVisibleFragment);
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.favorite_places:
                        saveCurrentCenter();
                        startActivity(new Intent(MainActivity.this, FavoritePlacesActivity.class));
                        break;
                    case R.id.my_location:
                            if (mLastLocation != null) {
                                Intent intent = new Intent("SCROLL_MAP");
                                intent.putExtra("latitude", mLastLocation.getLatitude());
                                intent.putExtra("longitude", mLastLocation.getLongitude());
                                context.sendBroadcast(intent);
                            }
                        break;
                    case R.id.settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                    case R.id.about:

                        break;
                }

                return true;
            }
        });

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout.setWeightSum(1);

        activeFragments = new ArrayList<>();

        swapFullScreenFragment();
        //setUpGoogleMaps();
        //setUpOSM(osmf);

        //menu.findItem(R.id.activeMapsItem).getSubMenu().setGroupVisible(R.id.googleMapsGroup, false);
        //setUpSplitScreen();


        fab = (FloatingActionButton) findViewById(R.id.fab);    // a fő FAB
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);  // a mögötte levő rejtett FAB-ok
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab4 = (FloatingActionButton) findViewById(R.id.fab4);
        fab5 = (FloatingActionButton) findViewById(R.id.fab5);
        fabs = new FloatingActionButton[4];
        fabs[0] = fab2;     fabs[1] = fab3;     fabs[2] = fab4;     fabs[3] = fab5; // egy for ciklus miatt kell
        final Animation fabAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_ontouch);
        final Animation topleft = AnimationUtils.loadAnimation(this, R.anim.fab_translate_topleft);
        final Animation top = AnimationUtils.loadAnimation(this, R.anim.fab_translate_top);
        final Animation topright = AnimationUtils.loadAnimation(this, R.anim.fab_translate_topright);
        final Animation topleft_back = AnimationUtils.loadAnimation(this, R.anim.fab_translate_topleft_back);
        final Animation top_back = AnimationUtils.loadAnimation(this, R.anim.fab_translate_top_back);
        final Animation topright_back = AnimationUtils.loadAnimation(this, R.anim.fab_translate_topright_back);

        fab.setOnTouchListener(new View.OnTouchListener() {
            private float x1, x2, y1, y2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {               // nyomvatartás esete: felengedéskor
                    fabSwiped = true;
                    fab.setTranslationY(0.0f);
                    if (fabLongClicked) {
                        fabLongClicked = false;

                        x2 = event.getX();
                        y2 = event.getY();
                        int deltaX = Math.round(x2 - x1);
                        int deltaY = Math.round(y2 - y1);

                        int[] fabl = new int[2];    //"FAB L"ocation
//                        fab.getLocationOnScreen(fabl);
                        fab.getLocationInWindow(fabl);
                        Rect fabRect = new Rect(fabl[0] + deltaX, fabl[1] + deltaY, fabl[0] + fab2.getWidth() + deltaX, fabl[1] + fab2.getHeight() + deltaY);

                        // Mivel az animációknál a View maga nem mozog, csak egy másik mutált példánya, így szerzem meg az animáció utáni pozíciókat
                        // *1.5 nem tudom miért kell, de kell.

                        Rect fab2Rect = new Rect((int) (fabl[0] - 1.06 * fab.getWidth()), (int) (fabl[1] - 1.06 * fab.getHeight()),
                                (int) (fabl[0] - 1.06 * fab.getWidth()) + fab2.getWidth(), (int) (fabl[1] - 1.06 * fab.getHeight() + fab2.getHeight()*1.5));
                        Rect fab3Rect = new Rect(fabl[0], (int) (fabl[1] - 1.5 * fab.getHeight()),
                                fabl[0] + fab2.getWidth(), (int) (fabl[1] - 1.5 * fab.getHeight() + fab2.getHeight()*1.5));
                        Rect fab4Rect = new Rect((int) (fabl[0] + 1.06 * fab.getWidth()), (int) (fabl[1] - 1.06 * fab.getHeight()),
                                (int) (fabl[0] + 1.06 * fab.getWidth()) + fab2.getWidth(), (int) (fabl[1] - 1.06 * fab.getHeight() + fab2.getHeight()*1.5));

                        // középpont mentése
                        saveCurrentCenter();

                        // tartalmazás vizsgálata
                        if (fab2Rect.contains(fabl[0] + deltaX + fab.getHeight() / 2, fabl[1] + fab.getHeight() + deltaY)) {
                            Toast.makeText(MainActivity.this, hiddenMaps.get(0), Toast.LENGTH_SHORT).show();
                            newFullScreenFragment(hiddenMaps.get(0));
                        }
                        if (fab3Rect.contains(fabl[0] + deltaX + fab.getHeight() / 2, fabl[1] + fab.getHeight() + deltaY)) {
                            Toast.makeText(MainActivity.this, hiddenMaps.get(1), Toast.LENGTH_SHORT).show();
                            newFullScreenFragment(hiddenMaps.get(1));
                        }
                        if (fab4Rect.contains(fabl[0] + deltaX + fab.getHeight() / 2, fabl[1] + fab.getHeight() + deltaY)) {
                            Toast.makeText(MainActivity.this, hiddenMaps.get(2), Toast.LENGTH_SHORT).show();
                            newFullScreenFragment(hiddenMaps.get(2));
                        }

                        fabs[0].startAnimation(topleft_back);
                        fabs[1].startAnimation(top_back);
                        fabs[2].startAnimation(topright_back);
                        fabs[0].setVisibility(View.GONE);
                        fabs[1].setVisibility(View.GONE);
                        fabs[2].setVisibility(View.GONE);

                        return false;
                    } else {                                                // sima érintés esete: felengedéskor
                        fab.startAnimation(fabAnimation);

                        // középpont mentése
                        saveCurrentCenter();
                        swapFullScreenFragment();

                        return false;
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    fabSwiped = false;
                    x1 = event.getX();
                    y1 = event.getY();
                }
                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    fabSwiped = false;
                }
                return false;
            }
        });


        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!fabSwiped) {
                    fabLongClicked = true;

                    String map1 = currentFullScreenVisibleFragment instanceof GoogleMapsFragment ? "Google Maps" : ((OSMFragment)currentFullScreenVisibleFragment).getMapType();
                    String map2 = prevFullScreenFragment instanceof GoogleMapsFragment ? "Google Maps" : ((OSMFragment)prevFullScreenFragment).getMapType();
                    ArrayList<String> currentMaps = new ArrayList<String>();
                    currentMaps.add(map1);  currentMaps.add(map2);

                    String[] allmaps = {"Google Maps", "OpenStreetMaps", "Mapbox", "Here Maps", "Bing Maps"};
                    allMaps = new ArrayList<String>(Arrays.asList(allmaps));

                    allMaps.removeAll(currentMaps);
                    hiddenMaps = allMaps;   // a nem megnyitott térképek listája

                    for (int i=0; i<hiddenMaps.size(); i++) {
                        if (hiddenMaps.get(i).equals("Google Maps")) {
                            fabs[i].setImageResource(R.drawable.maps_icon);
                        } else if (hiddenMaps.get(i).equals("OpenStreetMaps")) {
                            fabs[i].setImageResource(R.drawable.osm_icon);
                        } else if (hiddenMaps.get(i).equals("Mapbox")) {
                            fabs[i].setImageResource(R.drawable.mapbox_icon);
                        } else if (hiddenMaps.get(i).equals("Here Maps")) {
                            fabs[i].setImageResource(R.drawable.here_icon);
                        } else if (hiddenMaps.get(i).equals("Bing Maps")) {
                            fabs[i].setImageResource(R.drawable.bing_icon);
                        }
                        fabs[i].setVisibility(View.VISIBLE);
                    }
                    fabs[0].startAnimation(topleft);
                    fabs[1].startAnimation(top);
                    fabs[2].startAnimation(topright);
                }
                return false;
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        settings.registerOnSharedPreferenceChangeListener(this);
        screenOrientation = settings.getString("screen_orientation", "portrait");
        screenOrientationLocked = (screenOrientation.equals("portrait") || screenOrientation.equals("landscape"));
        fabBehavior = settings.getString("fab_behavior", "replace current");
        fabBehaviorReplaceOther = fabBehavior.equals("replace other");
        rotationGesturesEnabled = settings.getBoolean("rotation_gestures", false);
        showCrosshair = settings.getBoolean("crosshair", false);

        autoCompleteTextView = (SearchField) navigationView.getHeaderView(0).findViewById(R.id.search_field);
        autoCompleteTextView.setHint("Search");
        autoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_black_24dp, 0, 0, 0);

        searchDropdownStringArray = new ArrayList<String>();
        adapter = new SearchAdapter(this, R.layout.dropdown_list_item, R.id.address, searchDropdownStringArray);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setObserver(new SearchObserver() {
            @Override
            public void callback(ArrayList<Address> addressList) {
                geocoderCallBack(addressList);
            }
        });

        registerReceiver(br, new IntentFilter("SCROLL_MAP"));

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    private void saveCurrentCenter() {
        // középpont mentése
        if (currentFullScreenVisibleFragment instanceof GoogleMapsFragment) {
            double[] coords = {gmf.map.getCameraPosition().target.latitude,
                    gmf.map.getCameraPosition().target.longitude};
            currentCenterCoords = coords;
            currentZoomLevel = (int) gmf.map.getCameraPosition().zoom;
        } else if (currentFullScreenVisibleFragment instanceof OSMFragment) {
            double[] coords = {((OSMFragment) currentFullScreenVisibleFragment).mapView.getMapCenter().getLatitude(),
                    ((OSMFragment) currentFullScreenVisibleFragment).mapView.getMapCenter().getLongitude()};
            currentCenterCoords = coords;
            currentZoomLevel = ((OSMFragment) currentFullScreenVisibleFragment).mapView.getZoomLevel();
        }
    }

    public void geocoderCallBack(ArrayList<Address> addressList) {
        searchDropdownStringArray.clear();
        adapter.notifyDataSetChanged();
        adapter.clear();
        adapter.notifyDataSetChanged();

        if (addressList.size() > 0)
            adapter.add(addressList.get(0).getAddressLine(0) + ", " + addressList.get(0).getCountryName());
        if (addressList.size() > 1)
            adapter.add(addressList.get(1).getAddressLine(0) + ", " + addressList.get(1).getCountryName());
        if (addressList.size() > 2)
            adapter.add(addressList.get(2).getAddressLine(0) + ", " + addressList.get(2).getCountryName());
        adapter.notifyDataSetChanged();
        autoCompleteTextView.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // nem kell remove! :DDD
        /*if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }*/

        // ez így rossz
        if (screenOrientation.equals("portrait")) {
            linearLayout.setOrientation(LinearLayout.VERTICAL);
        } else if (screenOrientation.equals("landscape")) {
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            if (linearLayout.getOrientation() == LinearLayout.VERTICAL) {
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                linearLayout.setOrientation(LinearLayout.VERTICAL);
            }
        }


    }

    public void crosshairPrefChanged(boolean newValue) {

        if (newValue) {    // ha a showCrosshair true
            if (activeFragments.contains(gmf)) {
                if (googleMapType == GoogleMap.MAP_TYPE_NORMAL) {
                    gmf.crosshairBlack.setVisibility(View.VISIBLE);
                    gmf.crosshairWhite.setVisibility(View.INVISIBLE);
                } else if (googleMapType == GoogleMap.MAP_TYPE_HYBRID) {
                    gmf.crosshairBlack.setVisibility(View.INVISIBLE);
                    gmf.crosshairWhite.setVisibility(View.VISIBLE);
                }
            }
            if (activeFragments.contains(osmf)) {
                if (osmf.getTileProvider() == OSMFragment.TileProvider.MAPNIK || osmf.getTileProvider() == OSMFragment.TileProvider.MAPBOX || osmf.getTileProvider() == OSMFragment.TileProvider.BING || osmf.getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                    osmf.crosshairBlack.setVisibility(View.VISIBLE);
                    osmf.crosshairWhite.setVisibility(View.INVISIBLE);
                } else {
                    osmf.crosshairBlack.setVisibility(View.INVISIBLE);
                    osmf.crosshairWhite.setVisibility(View.VISIBLE);
                }
            }
            if (activeFragments.contains(osmf2)) {
                if (osmf2.getTileProvider() == OSMFragment.TileProvider.MAPNIK || osmf2.getTileProvider() == OSMFragment.TileProvider.MAPBOX || osmf2.getTileProvider() == OSMFragment.TileProvider.BING || osmf.getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                    osmf2.crosshairBlack.setVisibility(View.VISIBLE);
                    osmf2.crosshairWhite.setVisibility(View.INVISIBLE);
                } else {
                    osmf2.crosshairBlack.setVisibility(View.INVISIBLE);
                    osmf2.crosshairWhite.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (activeFragments.contains(gmf)) {
                gmf.crosshairBlack.setVisibility(View.INVISIBLE);
                gmf.crosshairWhite.setVisibility(View.INVISIBLE);

            }
            if (activeFragments.contains(osmf)) {
                osmf.crosshairBlack.setVisibility(View.INVISIBLE);
                osmf.crosshairWhite.setVisibility(View.INVISIBLE);
            }
            if (activeFragments.contains(osmf2)) {
                osmf2.crosshairBlack.setVisibility(View.INVISIBLE);
                osmf2.crosshairWhite.setVisibility(View.INVISIBLE);
            }
        }


    }

    private void checkNextZoomLevel() {
        if (activeFragments.contains(gmf)) {
            if (gmf.map.getCameraPosition().zoom == 19.0f) {
                (gmf.zoomControls.findViewById(R.id.zoomInButton)).setEnabled(false);
            } else if (gmf.map.getCameraPosition().zoom == 2.0f/* && gmf.map.getCameraPosition().zoom < 2.01f*/) {
                (gmf.zoomControls.findViewById(R.id.zoomOutButton)).setEnabled(false);
            } else {
                (gmf.zoomControls.findViewById(R.id.zoomInButton)).setEnabled(true);
                (gmf.zoomControls.findViewById(R.id.zoomOutButton)).setEnabled(true);
            }
        }
        if (activeFragments.contains(osmf)) {
            if (osmf.mapView.getZoomLevel() == 19) {
                (osmf.zoomControls.findViewById(R.id.zoomInButton)).setEnabled(false);
            } else if (osmf.mapView.getZoomLevel() <= 2) {
                (osmf.zoomControls.findViewById(R.id.zoomOutButton)).setEnabled(false);
            } else {
                (osmf.zoomControls.findViewById(R.id.zoomInButton)).setEnabled(true);
                (osmf.zoomControls.findViewById(R.id.zoomOutButton)).setEnabled(true);
            }
        }
        if (activeFragments.contains(osmf2)) {
            if (osmf2.mapView.getZoomLevel() == 19) {
                (osmf2.zoomControls.findViewById(R.id.zoomInButton)).setEnabled(false);
            } else if (osmf2.mapView.getZoomLevel() <= 2) {
                (osmf2.zoomControls.findViewById(R.id.zoomOutButton)).setEnabled(false);
            } else {
                (osmf2.zoomControls.findViewById(R.id.zoomInButton)).setEnabled(true);
                (osmf2.zoomControls.findViewById(R.id.zoomOutButton)).setEnabled(true);
            }
        }
    }

    /// Google Maps setup
    public void setUpGoogleMaps() {
        // Fragment létrehozás, hozzáadás
        gmf = null;
        gmf = new GoogleMapsFragment();
        gmf.setRetainInstance(true);

        getSupportFragmentManager().beginTransaction().add(R.id.linearLayout, gmf).commit();    // lehet Tag-et is megadni
        getSupportFragmentManager().executePendingTransactions();
        activeFragments.add(gmf);

        // Új szálról kell hívni, különben a mapView null
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gmf.mapView.getMapAsync(MainActivity.this);
            }
        }, 50);
    }

    /// OSM setup
    public OSMFragment setUpOSM(OSMFragment targetFragment) {
        // Fragment létrehozás, hozzáadás
        targetFragment = new OSMFragment();



        getSupportFragmentManager().beginTransaction().add(R.id.linearLayout, targetFragment).commit();
        getSupportFragmentManager().executePendingTransactions();
        activeFragments.add(targetFragment);

        final OSMFragment temp = targetFragment;

        // Új szálról kell hívni, különben a mapView null
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                displayMap(temp);
            }
        }, 50);
        return temp;
    }

    // Google Maps nézet
    @Override
    public void onMapReady(final GoogleMap map) {
        gmf.map = map;

        Rect windowRect = new Rect();
        linearLayout.getWindowVisibleDisplayFrame(windowRect);
        int wWindow = windowRect.right - windowRect.left;
        int hWindow = windowRect.bottom - windowRect.top;
//        if (splitscreen) {
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(wWindow, hWindow/2, 1.0f);
//            ((FrameLayout) gmf.mapView.getParent()).setLayoutParams(params);
//        } else {
        //    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(wWindow, 0, 1.0f);
        //    ((FrameLayout) gmf.mapView.getParent()).setLayoutParams(params);
//        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ((FrameLayout) gmf.mapView.getParent()).getLayoutParams();
        params.weight = 1.0f;
        ((FrameLayout) gmf.mapView.getParent()).setLayoutParams(params);

        if (googleMapType == 0) {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMapType = GoogleMap.MAP_TYPE_NORMAL;
            menu.findItem(R.id.googleMapsSatellite).setVisible(true);
            menu.findItem(R.id.googleMapsNormal).setVisible(false);
            if (showCrosshair) {
                gmf.crosshairBlack.setVisibility(View.VISIBLE);
                gmf.crosshairWhite.setVisibility(View.INVISIBLE);
            }
        } else {
            map.setMapType(googleMapType);
            if (showCrosshair) {
                if (googleMapType == GoogleMap.MAP_TYPE_NORMAL) {
                    gmf.crosshairBlack.setVisibility(View.VISIBLE);
                    gmf.crosshairWhite.setVisibility(View.INVISIBLE);
                    menu.findItem(R.id.googleMapsSatellite).setVisible(true);
                    menu.findItem(R.id.googleMapsNormal).setVisible(false);
                } else if (googleMapType == GoogleMap.MAP_TYPE_HYBRID) {
                    gmf.crosshairBlack.setVisibility(View.INVISIBLE);
                    gmf.crosshairWhite.setVisibility(View.VISIBLE);
                    menu.findItem(R.id.googleMapsSatellite).setVisible(false);
                    menu.findItem(R.id.googleMapsNormal).setVisible(true);
                }

            }
        }
        map.setMaxZoomPreference(18.0f);    // ne tudjon OSM-nél jobban zoomolni
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.setBuildingsEnabled(true);
        setRotationGesturesEnabled(rotationGesturesEnabled);
        float bearing;

        if (currentBearing != 0.0f) bearing = currentBearing;
        else bearing = 0.0f;

        ImageButton lockButton = (ImageButton) gmf.zoomControls.getChildAt(0);
        lockButton.setSelected(fullscreenLockMapToGmf);

        if (currentCenterCoords != null) {
            LatLng startPoint = new LatLng(currentCenterCoords[0], currentCenterCoords[1]);
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, currentZoomLevel));
            CameraPosition cp = new CameraPosition.Builder()
                    .target(startPoint)
                    .zoom(currentZoomLevel)
                    .bearing(bearing)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
        } else {
            LatLng startPoint = new LatLng(START_LATITUDE, START_LONGITUDE);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, START_ZOOM_LEVEL));
        }

        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                isGmfInMotion = true;
            }
        });

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //isGmfInMotion = false;

                if (lockMapToGmf) {
                    // ha lock nézetben nem egész float a gmf zoom level: kerekítés
                    if (map.getCameraPosition().zoom % 1.0f != 0) {
                        map.animateCamera(CameraUpdateFactory.zoomTo(Math.round(map.getCameraPosition().zoom)));
                    }
                }

                //Log.e("Scale: ", map.getProjection());


                /*int w = gmf.mapView.getWidth()/3;
                LatLng loc1 = map.getProjection().fromScreenLocation(new Point(1*w,400));
                LatLng loc2 = map.getProjection().fromScreenLocation(new Point(2*w,400));*/

                //TODO scale bar

                DisplayMetrics dm = MainActivity.this.getResources().getDisplayMetrics();

                int xdpcm = (int) ((double) dm.xdpi / 2.54D);
                int xLen = (int) (2.54F * (float) xdpcm);

                LatLng loc1 = map.getProjection().fromScreenLocation(new Point(dm.widthPixels / 2 - xLen / 2, 10));
                LatLng loc2 = map.getProjection().fromScreenLocation(new Point(dm.widthPixels / 2 + xLen / 2, 10));

                float[] results = new float[1];
                Location.distanceBetween(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude, results);

                int meters = (int) results[0];

                String scaleBarText;
                if (meters >= 5000) {
                    scaleBarText = Integer.toString(meters / 1000) + " km";
                } else if (meters >= 200) {
                    scaleBarText = Double.toString((double) ((int) ((double) meters / 100.0D)) / 10.0D) + " km";
                } else {
                    scaleBarText = meters + " m";
                }

                //Toast.makeText(MainActivity.this, meters + " \n" + scaleBarText, Toast.LENGTH_SHORT).show();

            }
        });

    }

    // OSM nézet
    public void displayMap(OSMFragment targetFragment) {
        final org.osmdroid.views.MapView map = targetFragment.mapView;
        OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);

        Rect windowRect = new Rect();
        linearLayout.getWindowVisibleDisplayFrame(windowRect);

        // Súly állítása
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ((FrameLayout) targetFragment.mapView.getParent()).getLayoutParams();
        params.weight = 1.0f;
        ((FrameLayout) targetFragment.mapView.getParent()).setLayoutParams(params);

        // TileProvider beállítása
        targetFragment.setContext(MainActivity.this);

        // UI beállítások
        map.setMultiTouchControls(true);
        map.setTilesScaledToDpi(true);
        ImageButton lockButton = (ImageButton) targetFragment.zoomControls.getChildAt(0);
        lockButton.setSelected(fullscreenLockMapToOsmf);

        // célkereszt beállítása
        if (showCrosshair) {
            targetFragment.crosshairBlack.setVisibility(View.VISIBLE);
            targetFragment.crosshairWhite.setVisibility(View.INVISIBLE);
        }

        //TODO TileSystem.setTileSize(1500);
        // TileSystem.setTileSize((int) (aTileSource.getTileSizePixels() * density * 0.5));
        // http://stackoverflow.com/questions/7240568/osmdroid-display-tiles-bigger?rq=1

        final IMapController mapController = map.getController();
        final float bearing;
        final GeoPoint startPoint;

        if (currentBearing != 0.0f) bearing = currentBearing;
        else bearing = 0.0f;

        if (currentCenterCoords != null) {
            mapController.setZoom(currentZoomLevel);
            startPoint = new GeoPoint(currentCenterCoords[0], currentCenterCoords[1]);
        } else {
            mapController.setZoom(START_ZOOM_LEVEL);
            startPoint = new GeoPoint(START_LATITUDE, START_LONGITUDE);
        }

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(MainActivity.this), map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);

        final ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setLineWidth(3.5f);

        final OSMFragment temp = targetFragment;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                OSMFragment.TileProvider tp = temp.getTileProvider();
                if (tp == null) temp.mapView.setTileSource(TileSourceFactory.MAPNIK);
                switch (tp) {
                    case MAPNIK:
                        final ITileSource mapnikTileSource = TileSourceFactory.MAPNIK;
                        temp.mapView.setTileSource(mapnikTileSource);
                        temp.mapType = "OpenStreetMaps";
                        break;
                    case MAPBOX:
                        final MapBoxTileSource mapboxTileSource = new MapBoxTileSource();
                        mapboxTileSource.retrieveAccessToken(context);
                        mapboxTileSource.setMapboxMapid("mapbox.streets");
                        temp.mapView.setTileSource(mapboxTileSource);
                        temp.mapType = "Mapbox";
                        break;
                    case MAPBOX_SAT:
                        final MapBoxTileSource mapboxSatTileSource = new MapBoxTileSource();
                        mapboxSatTileSource.retrieveAccessToken(context);
                        mapboxSatTileSource.setMapboxMapid("mapbox.streets-satellite");
                        temp.mapView.setTileSource(mapboxSatTileSource);
                        temp.mapType = "Mapbox";
                        break;
                    case HEREWEGO:
                        final ITileSource tileSource = new HEREWeGoTileSource(context);
                        ((HEREWeGoTileSource) tileSource).setAppCode("nk-J0pHKR1urxEhlv58Xsg");
                        ((HEREWeGoTileSource) tileSource).setAppId("2JXgMmZ5DVoR6yTHJwcg");
                        temp.mapView.setTileSource(tileSource);
                        temp.mapType = "Here Maps";
                        break;
                    case HEREWEGO_SAT:
                        final ITileSource tileSource2 = new HEREWeGoTileSource(context);
                        ((HEREWeGoTileSource) tileSource2).setHereWeGoMapid("hybrid.day");
                        ((HEREWeGoTileSource) tileSource2).setAppCode("nk-J0pHKR1urxEhlv58Xsg");
                        ((HEREWeGoTileSource) tileSource2).setAppId("2JXgMmZ5DVoR6yTHJwcg");
                        temp.mapView.setTileSource(tileSource2);
                        temp.mapType = "Here Maps";
                        break;
                    case BING:
                        BingMapTileSource.retrieveBingKey(context);
                        BingMapTileSource bingTileSource = new levente.sermaul.mapscompare.bingmaps.BingMapTileSource(null, BingMapTileSource.IMAGERYSET_ROAD);
                        bingTileSource.initMetaData();
                        bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
                        temp.mapView.setTileSource(bingTileSource);
                        temp.mapType = "Bing Maps";
                        break;
                    case BING_SAT:
                        BingMapTileSource.retrieveBingKey(context);
                        BingMapTileSource bingSatTileSource = new levente.sermaul.mapscompare.bingmaps.BingMapTileSource(null, BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
                        bingSatTileSource.initMetaData();
                        bingSatTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
                        temp.mapView.setTileSource(bingSatTileSource);
                        temp.mapType = "Bing Maps";
                        break;
                }

                mapController.setCenter(startPoint);
                temp.mapView.setMapOrientation(360 - bearing);
                setRotationGesturesEnabled(rotationGesturesEnabled);
                mScaleBarOverlay.setScaleBarOffset(map.getWidth() / 2, 10);
                map.getOverlays().add(mScaleBarOverlay);
            }
        }, 50);

        map.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                if (temp == osmf) {
                    isOsmfInMotion = true;
                }
                if (temp == osmf2) {
                    isOsmf2InMotion = true;
                }
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                if (temp == osmf) {
                    isOsmfInMotion = true;
                }
                if (temp == osmf2) {
                    isOsmf2InMotion = true;
                }
                return true;
            }
        });
    }

    public void setRotationGesturesEnabled(boolean value) {
        // beállítás
        if (value) {
            // Gmf
            if (activeFragments.contains(gmf) && gmf.map != null) {
                gmf.map.getUiSettings().setRotateGesturesEnabled(true);
            }
            // Osmf
            if (activeFragments.contains(osmf) && osmf.mapView != null) {
                if (osmf.mapView.getOverlays().size() != 0) {
                    // csak 1 db RotationOverlay lehessen
                    for (int i = 0; i < osmf.mapView.getOverlays().size(); i++) {
                        Object o = osmf.mapView.getOverlays().get(i);
                        if (o instanceof RotationGestureOverlay) {
                            return;
                        }
                    }
                }
                RotationGestureOverlay rgo = new RotationGestureOverlay(osmf.mapView);
                rgo.setEnabled(true);
                osmf.mapView.getOverlays().add(rgo);
            }
            // Osmf2
            if (activeFragments.contains(osmf2) && osmf2.mapView != null) {
                if (osmf2.mapView.getOverlays().size() != 0) {
                    // csak 1 db RotationOverlay lehessen
                    for (int i = 0; i < osmf2.mapView.getOverlays().size(); i++) {
                        Object o = osmf2.mapView.getOverlays().get(i);
                        if (o instanceof RotationGestureOverlay) {
                            return;
                        }
                    }
                }
                RotationGestureOverlay rgo = new RotationGestureOverlay(osmf2.mapView);
                rgo.setEnabled(true);
                osmf2.mapView.getOverlays().add(rgo);
            }

            // letiltás
        } else {
            // Gmf
            if (activeFragments.contains(gmf) && gmf.map != null) {
                gmf.map.getUiSettings().setRotateGesturesEnabled(false);

                // visszaforgatás Észak felé
                CameraPosition cp = new CameraPosition.Builder()
                        .bearing(0)
                        .target(gmf.map.getCameraPosition().target)
                        .zoom(gmf.map.getCameraPosition().zoom)
                        .build();
                gmf.map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));

            }
            // Osmf
            if (activeFragments.contains(osmf) && osmf.mapView != null) {
                if (!osmf.mapView.getOverlayManager().isEmpty()) {
                    // az overlay-ek listájából kitöröljük a forgatás kezeléséért felelőset
                    for (int i = 0; i < osmf.mapView.getOverlays().size(); i++) {
                        Object o = osmf.mapView.getOverlays().get(i);
                        if (o instanceof RotationGestureOverlay) {
                            // visszaforgatás Észak felé
                            osmf.mapView.setMapOrientation(0);
                            osmf.mapView.getOverlays().remove(o);
                        }
                    }
                }

            }
            // Osmf2
            if (activeFragments.contains(osmf2) && osmf2.mapView != null) {
                if (!osmf2.mapView.getOverlayManager().isEmpty()) {
                    // az overlay-ek listájából kitöröljük a forgatás kezeléséért felelőset
                    for (int i = 0; i < osmf2.mapView.getOverlays().size(); i++) {
                        Object o = osmf2.mapView.getOverlays().get(i);
                        if (o instanceof RotationGestureOverlay) {
                            // visszaforgatás Észak felé
                            osmf2.mapView.setMapOrientation(0);
                            osmf2.mapView.getOverlays().remove(o);
                        }
                    }
                }

            }
        }
    }

    public void buttonClicked(View v) {
        // a Button-től referenciát szerzünk a gombokat tartalmazó LinearLayout objektumra
        LinearLayout currentZoomControls;
        if (v.getParent() instanceof LinearLayout)
            currentZoomControls = (LinearLayout) v.getParent();
        else currentZoomControls = (LinearLayout) v.getParent().getParent();

        // ezt a referenciát összehasonlítjuk a létező Fragment(ek) LinearLayout referenciáival
        // így megkapjuk azt a Fragment-et, amin a gomb lenyomódott (splitscreen módban van jelentősége)
        Fragment currentFragment = null;
        if (gmf != null && currentZoomControls == gmf.zoomControls)
            currentFragment = gmf;
        else if (osmf != null && currentZoomControls == osmf.zoomControls)
            currentFragment = osmf;
        else if (osmf2 != null && currentZoomControls == osmf2.zoomControls)
            currentFragment = osmf2;

        // switch-case a gombokra
        switch (v.getId()) {
            case R.id.lockButton:
                //if (gmf != null && osmf != null) {  // ha splitscreen módban vagyunk

                // ha splitscreen módban vagyunk
                if (activeFragments.size() == 2) {

                    // esetek: gmf-et követi osmf, [osmf-et követi gmf, osmf-et követi osmf2], osmf2-t követi osmf

                    if (currentFragment == gmf && !lockMapToGmf) {          // ha gmf-en nyomtunk lock-ot: az osmf is scrollozzon oda

                        if (lockMapToOsmf || lockMapToOsmf2) return;

                        v.setSelected(true);

                        lockMapToGmf = true;

                        GeoPoint newCenter = new GeoPoint(
                                gmf.map.getCameraPosition().target.latitude,
                                gmf.map.getCameraPosition().target.longitude);
                        osmf.mapView.getController().animateTo(newCenter);

                        int intZoomLevel = Math.round(gmf.map.getCameraPosition().zoom);  // gmf zoomlevel kerekítése:
                        osmf.mapView.getController().zoomTo(intZoomLevel);                // új zoomlevel osmf-nek
                        gmf.map.moveCamera(CameraUpdateFactory.zoomTo(intZoomLevel));     // és gmf-nek is

                        new MapUpdateTask().execute();

                    } else if (currentFragment == osmf && !lockMapToOsmf) { // ha osmf-en nyomtunk lock-ot

                        if (lockMapToGmf || lockMapToOsmf2) return;

                        v.setSelected(true);

                        lockMapToOsmf = true;

                        // lekérdezzük melyik a másik fragment todo ez hogy működik majd Fullscreenben 5 db activeFragmenttel? lul
                        Fragment otherFragment = new Fragment();
                        if (activeFragments.get(0) == currentFragment)
                            otherFragment = activeFragments.get(1);
                        if (activeFragments.get(1) == currentFragment)
                            otherFragment = activeFragments.get(0);

                        if (otherFragment instanceof GoogleMapsFragment) {
                            LatLng newCenter = new LatLng(osmf.mapView.getMapCenter().getLatitude(), osmf.mapView.getMapCenter().getLongitude());
                            int intZoomLevel = osmf.mapView.getZoomLevel();
                            gmf.map.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenter, intZoomLevel));
                        } else if (otherFragment instanceof OSMFragment) {
                            GeoPoint newCenter = new GeoPoint(osmf.mapView.getMapCenter().getLatitude(), osmf.mapView.getMapCenter().getLongitude());
                            int intZoomLevel = osmf.mapView.getZoomLevel();
                            osmf.mapView.getController().animateTo(newCenter);
                            osmf.mapView.getController().setZoom(intZoomLevel);
                        }

                        new MapUpdateTask().execute();

                    } else if (currentFragment == osmf2 && !lockMapToOsmf2) { // ha osmf2-n nyomtunk lockot

                        if (lockMapToGmf || lockMapToOsmf) return;

                        v.setSelected(true);

                        lockMapToOsmf2 = true;

                        GeoPoint newCenter = new GeoPoint(osmf2.mapView.getMapCenter().getLatitude(), osmf2.mapView.getMapCenter().getLongitude());
                        int intZoomLevel = osmf2.mapView.getZoomLevel();
                        osmf.mapView.getController().animateTo(newCenter);
                        osmf.mapView.getController().setZoom(intZoomLevel);

                        new MapUpdateTask().execute();
                    } else {
                        v.setSelected(false);
                        lockMapToGmf = false;
                        lockMapToOsmf = false;
                        lockMapToOsmf2 = false;
                    }
                    // ha teljes képernyős módban vagyunk todo ezt kitörölni / feltételt átírni (fullscreen = false)-ra -> működik így?
                } else if (activeFragments.size() == 1) {
                    if (activeFragments.get(0) == gmf && !fullscreenLockMapToGmf) {
                        if (fullscreenLockMapToOsmf) return;
                        v.setSelected(true);
                        fullscreenLockMapToGmf = true;

                    } else if (activeFragments.get(0) == osmf && !fullscreenLockMapToOsmf) {
                        if (fullscreenLockMapToGmf) return;
                        v.setSelected(true);
                        fullscreenLockMapToOsmf = true;
                    } else {
                        v.setSelected(false);
                        if (activeFragments.get(0) == gmf) fullscreenLockMapToGmf = false;
                        else fullscreenLockMapToOsmf = false;
                    }
                }
                break;

            case R.id.zoomInButton:
                if (currentFragment == gmf) {
                    if (lockMapToGmf) {
                        if (osmf.mapView.canZoomIn() && gmf.map.getCameraPosition().zoom < 19) {
                            gmf.map.animateCamera(CameraUpdateFactory.zoomIn());
                            osmf.mapView.getController().zoomIn();
                        }
                    } else {
                        gmf.map.animateCamera(CameraUpdateFactory.zoomIn());
                    }


                } else if (currentFragment == osmf) {
                    if (lockMapToOsmf) {
                        if (osmf.mapView.canZoomIn() && gmf.map.getCameraPosition().zoom < 19) {
                            gmf.map.animateCamera(CameraUpdateFactory.zoomIn());
                            osmf.mapView.getController().zoomIn();
                        }
                    } else {
                        osmf.mapView.getController().zoomIn();
                    }


                } else if (currentFragment == osmf2) {
                    if (lockMapToOsmf2) {
                        if (osmf2.mapView.canZoomIn() && osmf.mapView.canZoomIn()) {
                            osmf.mapView.getController().zoomIn();
                            osmf2.mapView.getController().zoomIn();
                        }
                    } else {
                        osmf2.mapView.getController().zoomIn();
                    }
                } else {
                    // ha lesz más map
                }
                break;
            case R.id.zoomOutButton:
                if (currentFragment == gmf) {
                    if (lockMapToGmf) {
                        if (osmf.mapView.canZoomOut() && gmf.map.getCameraPosition().zoom > 2) {
                            gmf.map.animateCamera(CameraUpdateFactory.zoomOut());
                            osmf.mapView.getController().zoomOut();
                        }
                    } else {
                        gmf.map.animateCamera(CameraUpdateFactory.zoomOut());
                    }

                } else if (currentFragment == osmf) {
                    if (lockMapToOsmf) {
                        if (osmf.mapView.canZoomOut() && gmf.map.getCameraPosition().zoom > 2) {
                            gmf.map.animateCamera(CameraUpdateFactory.zoomOut());
                            osmf.mapView.getController().zoomOut();
                        }
                    } else {
                        osmf.mapView.getController().zoomOut();
                    }
                } else if (currentFragment == osmf2) {
                    if (lockMapToOsmf2) {
                        if (osmf2.mapView.canZoomOut() && osmf.mapView.canZoomOut()) {
                            osmf2.mapView.getController().zoomOut();
                            osmf2.mapView.getController().zoomOut();
                        }
                    } else {
                        osmf2.mapView.getController().zoomOut();
                    }
                } else {
                    // ha lesz más map
                }
                break;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNextZoomLevel();
            }
        }, 500);
        //checkNextZoomLevel();
    }

    public void newFullScreenFragment(String mapType) {
        if (fabBehaviorReplaceOther) {
            // 1. verzió: a prev helyére kerül az új térkép, az aktuális lesz a prev
            getSupportFragmentManager().beginTransaction().hide(currentFullScreenVisibleFragment).commit();
            getSupportFragmentManager().beginTransaction().remove(prevFullScreenFragment).commit();
            activeFragments.remove(prevFullScreenFragment);

            if (prevFullScreenFragment == osmf && currentFullScreenVisibleFragment == osmf2) {
                osmf = osmf2;   // osmf példány felülírásaa
                osmf2 = null;
                currentFullScreenVisibleFragment = osmf;
            }

            // új érték
            prevFullScreenFragment = currentFullScreenVisibleFragment;

            // Lehetőségek: gmf -> osmf,  osmf -> gmf,    osmf2 -> gmf,
            //                            osmf -> osmf2,  osmf2 -> osmf

            // ha az új map gmf
            if (mapType.equals("Google Maps")) {
                //lockMapToGmf = false;   // lock elengedése, ha volt
                setUpGoogleMaps();
                currentFullScreenVisibleFragment = gmf;
                return;
            }

            if (prevFullScreenFragment == gmf) {                          // ha az új map osmf, gmf-ről váltunk
                lockMapToGmf = false;
                osmf = setUpOSM(osmf);
                if (mapType.equals("OpenStreetMaps")) {
                    osmf.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                }
                if (mapType.equals("Mapbox")) {
                    osmf.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                }
                if (mapType.equals("Here Maps")) {
                    osmf.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                }
                if (mapType.equals("Bing Maps")) {
                    osmf.setTileProvider(OSMFragment.TileProvider.BING);
                }
                currentFullScreenVisibleFragment = osmf;
            } else if (prevFullScreenFragment == osmf) {                        // ha az új map osmf2, osmf-ről váltunk
                lockMapToOsmf = false;
                lockMapToOsmf2 = false;
                osmf.zoomControls.getChildAt(0).setSelected(false);             // esetenként benyomódva marad a gomb
                osmf2 = setUpOSM(osmf2);
                if (mapType.equals("OpenStreetMaps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                }
                if (mapType.equals("Mapbox")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                }
                if (mapType.equals("Here Maps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                }
                if (mapType.equals("Bing Maps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.BING);
                }
                currentFullScreenVisibleFragment = osmf2;
            } else if (prevFullScreenFragment == osmf2) {                   // ha az új map osmf, osmf2-ről váltunk
                lockMapToOsmf = false;
                lockMapToOsmf2 = false;

                // marad a régi osmf példány
                osmf.zoomControls.getChildAt(0).setSelected(false);         // így benyomódva maradhat a gomb
                osmf2.zoomControls.getChildAt(0).setSelected(false);
                if (mapType.equals("OpenStreetMaps")) {
                    osmf.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                }
                if (mapType.equals("Mapbox")) {
                    osmf.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                }
                if (mapType.equals("Here Maps")) {
                    osmf.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                }
                if (mapType.equals("Bing Maps")) {
                    osmf.setTileProvider(OSMFragment.TileProvider.BING);
                }
                currentFullScreenVisibleFragment = osmf;
            }

            // Menü visibility logika
            setUpFullscreenDrawerGroups();

        } else {
            // 2. verzió: az aktuális térkép helyére kerül az új térkép, a prev marad
            getSupportFragmentManager().beginTransaction().remove(currentFullScreenVisibleFragment).commit();
            activeFragments.remove(currentFullScreenVisibleFragment);

            // Lehetőségek: gmf -> osmf,  osmf -> gmf,    osmf2 -> gmf,
            //                            osmf -> osmf2,  osmf2 -> osmf

            // ha az új map gmf
            if (mapType.equals("Google Maps")) {
                //lockMapToGmf = false;   // lock elengedése, ha volt
                setUpGoogleMaps();
                currentFullScreenVisibleFragment = gmf;
                return;
            }

            if (currentFullScreenVisibleFragment == gmf) {                          // ha az új map osmf, gmf-ről váltunk - itt a "current" a most kidobottat jelöli!
                lockMapToGmf = false;
                osmf2 = setUpOSM(osmf2);                                            // akkor a prev osmf, osmf2 kell!
                if (mapType.equals("OpenStreetMaps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                }
                if (mapType.equals("Mapbox")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                }
                if (mapType.equals("Here Maps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                }
                if (mapType.equals("Bing Maps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.BING);
                }

                currentFullScreenVisibleFragment = osmf2;
            } else if (currentFullScreenVisibleFragment == osmf) {                        // ha osmf-ről váltunk
                if (prevFullScreenFragment == gmf) {
                    lockMapToOsmf = false;
                    osmf = setUpOSM(osmf);
                    if (mapType.equals("OpenStreetMaps")) {
                        osmf.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                    }
                    if (mapType.equals("Mapbox")) {
                        osmf.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                    }
                    if (mapType.equals("Here Maps")) {
                        osmf.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                    }
                    if (mapType.equals("Bing Maps")) {
                        osmf.setTileProvider(OSMFragment.TileProvider.BING);
                    }

                    currentFullScreenVisibleFragment = osmf;
                } else {    // itt osmf2 volt (bug!)
                    lockMapToOsmf = false;
                    osmf = setUpOSM(osmf);
                    if (mapType.equals("OpenStreetMaps")) {
                        osmf.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                    }
                    if (mapType.equals("Mapbox")) {
                        osmf.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                    }
                    if (mapType.equals("Here Maps")) {
                        osmf.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                    }
                    if (mapType.equals("Bing Maps")) {
                        osmf.setTileProvider(OSMFragment.TileProvider.BING);
                    }

                    currentFullScreenVisibleFragment = osmf;
                }
            } else if (currentFullScreenVisibleFragment == osmf2) {                  // ha osmf2-ről váltunk
                // volt osmf
                lockMapToOsmf2 = false;
                osmf2 = setUpOSM(osmf2);
                if (mapType.equals("OpenStreetMaps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                }
                if (mapType.equals("Mapbox")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                }
                if (mapType.equals("Here Maps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                }
                if (mapType.equals("Bing Maps")) {
                    osmf2.setTileProvider(OSMFragment.TileProvider.BING);
                }

                currentFullScreenVisibleFragment = osmf2;
            }

            // Menü visibility logika
            setUpFullscreenDrawerGroups();
        }

    }

    public void swapFullScreenFragment() {
        // init alkalmazás induláskor
        if (getSupportFragmentManager().getFragments() == null) {
            osmf = setUpOSM(osmf);
            setUpGoogleMaps();
            currentFullScreenVisibleFragment = gmf;
            prevFullScreenFragment = osmf;
            osmf.setTileProvider(OSMFragment.TileProvider.MAPNIK);
            menu.findItem(R.id.fullscreen_mode).setVisible(false);
            MenuItem activeMapsItem = menu.findItem(R.id.activeMapsItem);
            activeMapsItem.getSubMenu().setGroupVisible(R.id.osmGroup, false);
            activeMapsItem.getSubMenu().setGroupVisible(R.id.mapboxGroup, false);
            activeMapsItem.getSubMenu().setGroupVisible(R.id.hereMapsGroup, false);
            activeMapsItem.getSubMenu().setGroupVisible(R.id.bingMapsGroup, false);
            return;
        }

        getSupportFragmentManager().beginTransaction().hide(currentFullScreenVisibleFragment).commit();
        getSupportFragmentManager().beginTransaction().show(prevFullScreenFragment).commit();

        Fragment temp = prevFullScreenFragment;
        prevFullScreenFragment = currentFullScreenVisibleFragment;
        currentFullScreenVisibleFragment = temp;

        // Menü visibility logika
        setUpFullscreenDrawerGroups();

    }

    private void showSplitScreenMapSelectDialog() {
        final String[] tempSplitScreenFragments = new String[2];

        View v = LayoutInflater.from(context).inflate(R.layout.map_select_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        builder.setTitle("Select maps");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                splitScreenFragments = tempSplitScreenFragments;
                // a fontos fv. hívás itt
                swapSplitScreenFragments();
                setUpSplitScreenDrawerGroups();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Copy-paste varázslat kezdete
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.szürke));

        final Spinner mapSpinner1 = (Spinner) v.findViewById(R.id.map_select_1);
        final Spinner mapSpinner2 = (Spinner) v.findViewById(R.id.map_select_2);

        final String[] array1 = {"Map #1", "Google Maps", "OpenStreetMaps", "Mapbox", "Here Maps", "Bing Maps"};
        final String[] array2 = {"Map #2", "Google Maps", "OpenStreetMaps", "Mapbox", "Here Maps", "Bing Maps"};
        ArrayList<String> list1 = new ArrayList<String>(Arrays.asList(array1));
        ArrayList<String> list2 = new ArrayList<String>(Arrays.asList(array2));

        ArrayAdapter<String> mapsAdapter1 = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, list1);
        ArrayAdapter<String> mapsAdapter2 = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, list2);
        mapsAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapsAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mapSpinner1.setAdapter(mapsAdapter1);
        mapSpinner2.setAdapter(mapsAdapter2);

        mapSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // az első elem csak default
                if (position == 0) spinner_maps_selected[0] = false;
                else {
                    spinner_maps_selected[0] = true;
                    spinner_selection_positions[0] = position;
                }
                // ha mindkét Spinner-en választottunk két különböző térképet
                if (spinner_maps_selected[0] && spinner_maps_selected[1] && spinner_selection_positions[0] != spinner_selection_positions[1]) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));

                    tempSplitScreenFragments[0] = array1[spinner_selection_positions[0]];
                    tempSplitScreenFragments[1] = array1[spinner_selection_positions[1]];
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.szürke));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mapSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // az első elem csak default
                if (position == 0) spinner_maps_selected[1] = false;
                else {
                    spinner_maps_selected[1] = true;
                    spinner_selection_positions[1] = position;
                }
                // ha mindkét Spinner-en választottunk két különböző térképet
                if (spinner_maps_selected[0] && spinner_maps_selected[1] && spinner_selection_positions[0] != spinner_selection_positions[1]) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));

                    tempSplitScreenFragments[0] = array1[spinner_selection_positions[0]];
                    tempSplitScreenFragments[1] = array1[spinner_selection_positions[1]];
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.szürke));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
        // Copy-paste varázslat vége
    }

    private void showFullScreenMapSelectDialog() {
        final String[] tempFullScreenFragment = new String[1];      // egy belső osztály kell írjon bele -> 1 elemű tömbként eléri és írhat bele

        View v = LayoutInflater.from(context).inflate(R.layout.map_select_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        builder.setTitle("Select map");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fullScreenFragment = tempFullScreenFragment[0];
                exitSplitScreen();
                setUpFullScreenMenuVisibilities();
            }

            // kicsit más, mint a másik fullscreen-es visibility függvény
            private void setUpFullScreenMenuVisibilities() {
                // módváltás visibility logika
                menu.findItem(R.id.splitscreen_mode).setVisible(true);
                menu.findItem(R.id.fullscreen_mode).setVisible(false);
                menu.findItem(R.id.select_maps).setVisible(false);

                // group visibility logika
                MenuItem activeMapsItem = menu.findItem(R.id.activeMapsItem);
                activeMapsItem.getSubMenu().setGroupVisible(R.id.googleMapsGroup, false);
                activeMapsItem.getSubMenu().setGroupVisible(R.id.osmGroup, false);
                activeMapsItem.getSubMenu().setGroupVisible(R.id.mapboxGroup, false);
                activeMapsItem.getSubMenu().setGroupVisible(R.id.hereMapsGroup, false);
                activeMapsItem.getSubMenu().setGroupVisible(R.id.bingMapsGroup, false);

                if (fullScreenFragment.equals("Google Maps")) {
                    activeMapsItem.getSubMenu().setGroupVisible(R.id.googleMapsGroup, true);
                    if (googleMapType == 0) menu.findItem(R.id.googleMapsNormal).setVisible(false);
                    else menu.findItem(R.id.googleMapsSatellite).setVisible(false);
                }
                if (fullScreenFragment.equals("OpenStreetMaps")) {
                    activeMapsItem.getSubMenu().setGroupVisible(R.id.osmGroup, true);
                }
                if (fullScreenFragment.equals("Mapbox")) {
                    activeMapsItem.getSubMenu().setGroupVisible(R.id.mapboxGroup, true);
                    if (osmf.getTileProvider() == OSMFragment.TileProvider.MAPBOX) {
                        menu.findItem(R.id.mapboxNormal).setVisible(false);
                    } else {
                        menu.findItem(R.id.mapboxSatellite).setVisible(false);
                    }
                }
                if (fullScreenFragment.equals("Here Maps")) {
                    activeMapsItem.getSubMenu().setGroupVisible(R.id.hereMapsGroup, true);
                    if (osmf.getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                        menu.findItem(R.id.hereMapsNormal).setVisible(false);
                    } else {
                        menu.findItem(R.id.hereMapsSatellite).setVisible(false);
                    }
                }
                if (fullScreenFragment.equals("Bing Maps")) {
                    activeMapsItem.getSubMenu().setGroupVisible(R.id.bingMapsGroup, true);
                    if (osmf.getTileProvider() == OSMFragment.TileProvider.BING) {
                        menu.findItem(R.id.bingMapsNormal).setVisible(false);
                    } else {
                        menu.findItem(R.id.bingMapsSatellite).setVisible(false);
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.szürke));

        final Spinner mapSpinner1 = (Spinner) v.findViewById(R.id.map_select_1);
        (v.findViewById(R.id.map_select_2)).setVisibility(View.GONE);   // 2. spinner elrejtése

        final String[] array1 = {"Select map", "Google Maps", "OpenStreetMaps", "Mapbox", "Here Maps", "Bing Maps"};
        ArrayList<String> list1 = new ArrayList<String>(Arrays.asList(array1));

        ArrayAdapter<String> mapsAdapter1 = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, list1);
        mapsAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mapSpinner1.setAdapter(mapsAdapter1);

        mapSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // az első elem csak default
                if (position == 0) spinner_map_selected = false;
                else {
                    spinner_map_selected = true;
                    spinner_selection_position = position;
                }
                // ha mindkét Spinner-en választottunk két különböző térképet
                if (spinner_map_selected) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));

                    tempFullScreenFragment[0] = array1[spinner_selection_position];
                    // mapok hozzáadása Ok gombra
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.szürke));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showSplitScreenFavoriteDialog() {
        final Integer[] favoriteIndex = new Integer[1];     // belső osztály kell írjon bele -> 1 elemű tömbként eléri és írhat bele

        View v = LayoutInflater.from(context).inflate(R.layout.map_select_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        builder.setTitle("Add to Favorite places");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveToFavorites(activeFragments.get(favoriteIndex[0]));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setMessage("Which map's position would you like to save?");
        alertDialog.show();

        // Copy-paste varázslat eleje
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.szürke));

        final Spinner mapSpinner1 = (Spinner) v.findViewById(R.id.map_select_1);
        (v.findViewById(R.id.map_select_2)).setVisibility(View.GONE);   // 2. spinner elrejtése

        String[] array = new String[3];
        if (activeFragments.get(0) instanceof GoogleMapsFragment) {
            array[0] = "Select map";
            array[1] = "Google Maps";
            array[2] = ((OSMFragment) activeFragments.get(1)).getMapType();
        }
        if (activeFragments.get(0) instanceof OSMFragment) {
            array[0] = "Select map";
            array[1] = ((OSMFragment) activeFragments.get(0)).getMapType();
            array[2] = (activeFragments.get(1) instanceof OSMFragment) ? ((OSMFragment) activeFragments.get(1)).getMapType() : "Google Maps";
        }
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(array));

        ArrayAdapter<String> mapsAdapter1 = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, list);
        mapsAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mapSpinner1.setAdapter(mapsAdapter1);

        mapSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // az első elem csak default
                if (position == 0) {
                    spinner_fav_selected = false;
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.szürke));
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));

                    favoriteIndex[0] = position - 1;  // a default szöveget nem számoljuk
                    // fav hozzáadása Ok gombra
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void saveToFavorites(Fragment source) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> reverseGeoPos;
        String name = new String();
        String coords = new String();
        String date = new String();
        if (source == null) return;
        try {
            if (source instanceof GoogleMapsFragment) {
                coords = String.format(Locale.getDefault(), "%.8f", gmf.map.getCameraPosition().target.latitude)
                        + ", "
                        + String.format(Locale.getDefault(), "%.8f", gmf.map.getCameraPosition().target.longitude);
                reverseGeoPos = geocoder.getFromLocation(gmf.map.getCameraPosition().target.latitude, gmf.map.getCameraPosition().target.longitude, 1);
                name = (!reverseGeoPos.isEmpty()) ? reverseGeoPos.get(0).getAddressLine(0) + ", " + reverseGeoPos.get(0).getCountryName() : coords;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
                date = formatter.format(Calendar.getInstance().getTime());

            } else if (source instanceof OSMFragment) {
                coords = String.format(Locale.getDefault(), "%.8f", ((OSMFragment) source).mapView.getMapCenter().getLatitude())
                        + ", "
                        + String.format(Locale.getDefault(), "%.8f", ((OSMFragment) source).mapView.getMapCenter().getLongitude());
                reverseGeoPos = geocoder.getFromLocation(((OSMFragment) source).mapView.getMapCenter().getLatitude(),
                        ((OSMFragment) source).mapView.getMapCenter().getLongitude(), 1);
                name = (!reverseGeoPos.isEmpty()) ? reverseGeoPos.get(0).getAddressLine(0) + ", " + reverseGeoPos.get(0).getCountryName() : coords;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
                date = formatter.format(Calendar.getInstance().getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String csvString = coords + "," + date + "," + name;    // név utoljára, mert lehet benne vessző
        FileOutputStream out;

        try {
            out = openFileOutput(FILENAME, Context.MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(out);
            osw.write(csvString);
            osw.write("\n");
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void exitSplitScreen() {
        splitscreen = false;
        linearLayout.setWeightSum(1);
        lockMapToGmf = false;
        lockMapToOsmf = false;
        lockMapToOsmf2 = false;
        fullscreenLockMapToGmf = false;
        fullscreenLockMapToOsmf = false;

        if (fullScreenFragment.equals("Google Maps")) {                                                 // Ha Google Maps-re váltunk
            if (activeFragments.get(0) == gmf) {                                                        // ha már van gmf (1. helyen)
                currentFullScreenVisibleFragment = gmf;
                prevFullScreenFragment = activeFragments.get(1);
                getSupportFragmentManager().beginTransaction().hide(activeFragments.get(1)).commit();   // a másikat elrejtjük
                activeFragments.remove(1);
                gmf.zoomControls.getChildAt(0).setSelected(false);                                      // lock (ami kikapcsol) esetén UI frissítése todo ezt osm-be is?
            } else if (activeFragments.get(1) == gmf) {                                                 // ha már van gmf (2. helyen)
                currentFullScreenVisibleFragment = gmf;
                prevFullScreenFragment = activeFragments.get(0);
                getSupportFragmentManager().beginTransaction().hide(activeFragments.get(0)).commit();
                activeFragments.remove(0);
                gmf.zoomControls.getChildAt(0).setSelected(false);                                      // lock UI frissítése
            } else {                                                                                    // ha nincs gmf
                double[] coords = {osmf.mapView.getMapCenter().getLatitude(), osmf.mapView.getMapCenter().getLongitude()};
                currentCenterCoords = coords;                                                           // pozíció elmentése (az 1. fragmentről)
                currentZoomLevel = osmf.mapView.getZoomLevel();
                prevFullScreenFragment = osmf;
                getSupportFragmentManager().beginTransaction().hide(osmf).commit();
                getSupportFragmentManager().beginTransaction().hide(osmf2).commit();
                activeFragments.remove(0);
                activeFragments.remove(0);
                setUpGoogleMaps();
                currentFullScreenVisibleFragment = gmf;
            }
            // ha nem volt gmf

            // az első két ágba: kell?
            /*double[] coords =  {gmf.map.getCameraPosition().target.latitude, gmf.map.getCameraPosition().target.longitude};
            currentCenterCoords = coords;
            currentZoomLevel = (int) gmf.map.getCameraPosition().zoom;*/

        } else {                                                                                        // Ha osmf típusú térképre váltunk - az egyik jelenlegi fragment biztosan osmf
            if (activeFragments.get(0) == osmf) {                                                       // ha az 1. fragment osmf
                currentFullScreenVisibleFragment = osmf;
                prevFullScreenFragment = activeFragments.get(1);
                getSupportFragmentManager().beginTransaction().hide(activeFragments.get(1)).commit();   // a másikat elrejtjük
                activeFragments.remove(1);
                if (!((OSMFragment) activeFragments.get(0)).getMapType().equals(fullScreenFragment)) {  // ha más típusú, mint a kívánt fs fragment
                    switch (fullScreenFragment) {
                        case "OpenStreetMaps":
                            osmf.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                            break;
                        case "Mapbox":
                            osmf.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                            break;
                        case "Here Maps":
                            osmf.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                            break;
                        case "Bing Maps":
                            osmf.setTileProvider(OSMFragment.TileProvider.BING);
                            break;
                    }
                }                                                                                       // ha azonos típusú, nem kell csinálni semmit
            } else if (activeFragments.get(1) == osmf) {                                                // ha a 2. fragment osmf
                currentFullScreenVisibleFragment = osmf;
                prevFullScreenFragment = activeFragments.get(0);
                getSupportFragmentManager().beginTransaction().hide(activeFragments.get(0)).commit();   // a másikat elrejtjük
                activeFragments.remove(0);
                if (!((OSMFragment) activeFragments.get(0)).getMapType().equals(fullScreenFragment)) {  // ha más típusú, mint a kívánt fs fragment
                    switch (fullScreenFragment) {
                        case "OpenStreetMaps":
                            osmf.setTileProvider(OSMFragment.TileProvider.MAPNIK);
                            break;
                        case "Mapbox":
                            osmf.setTileProvider(OSMFragment.TileProvider.MAPBOX);
                            break;
                        case "Here Maps":
                            osmf.setTileProvider(OSMFragment.TileProvider.HEREWEGO);
                            break;
                        case "Bing Maps":
                            osmf.setTileProvider(OSMFragment.TileProvider.BING);
                            break;
                    }
                }                                                                                       // ha azonos típusú, nem kell csinálni semmit
            }
        }


        fab.setVisibility(View.VISIBLE);
        menu.findItem(R.id.select_maps).setVisible(false);
    }

    public void swapSplitScreenFragments() {
        splitscreen = true;
        linearLayout.setWeightSum(2);
        lockMapToGmf = false;
        lockMapToOsmf = false;
        lockMapToOsmf2 = false;
        fullscreenLockMapToGmf = false;
        fullscreenLockMapToOsmf = false;
        fab.setVisibility(View.GONE);

        menu.findItem(R.id.select_maps).setVisible(true);

        // a beállítandó Fragmentek megadása, sorrendben
        final Fragment[] fragments = new Fragment[2];
        OSMFragment.TileProvider[] tileProviders = new OSMFragment.TileProvider[2];
        switch (splitScreenFragments[0]) {
            case "Google Maps":
                fragments[0] = gmf;
                break;
            case "OpenStreetMaps":
                fragments[0] = osmf;
                tileProviders[0] = OSMFragment.TileProvider.MAPNIK;
                break;
            case "Mapbox":
                fragments[0] = osmf;
                tileProviders[0] = OSMFragment.TileProvider.MAPBOX;
                break;
            case "Here Maps":
                fragments[0] = osmf;
                tileProviders[0] = OSMFragment.TileProvider.HEREWEGO;
                break;
            case "Bing Maps":
                fragments[0] = osmf;
                tileProviders[0] = OSMFragment.TileProvider.BING;
                break;
        }
        switch (splitScreenFragments[1]) {
            case "Google Maps":
                fragments[1] = gmf;
                break;
            case "OpenStreetMaps":
                fragments[1] = (fragments[0] == osmf) ? osmf2 : osmf;
                tileProviders[1] = OSMFragment.TileProvider.MAPNIK;
                break;
            case "Mapbox":
                fragments[1] = (fragments[0] == osmf) ? osmf2 : osmf;
                tileProviders[1] = OSMFragment.TileProvider.MAPBOX;
                break;
            case "Here Maps":
                fragments[1] = (fragments[0] == osmf) ? osmf2 : osmf;
                tileProviders[1] = OSMFragment.TileProvider.HEREWEGO;
                break;
            case "Bing Maps":
                fragments[1] = (fragments[0] == osmf) ? osmf2 : osmf;
                tileProviders[1] = OSMFragment.TileProvider.BING;
                break;
        }

        // ha fullscreen-ről váltottunk
        saveCurrentCenter();


        // activeFragments = a jelenlegi Fragmentek, sorrendben
        // fragments[2] = a beállítandó Fragmentek, sorrendben

        // ha nincs Fragment a képernyőn (most váltottunk Splitscreen módba) -------- full->split váltáskor van 2 !
        if (activeFragments.isEmpty()) {
            if (fragments[0] == gmf) setUpGoogleMaps();
            else setUpOSM(osmf);

            if (fragments[1] == gmf) setUpGoogleMaps();
            else {
                if (fragments[0] == gmf) setUpOSM(osmf);
                if (fragments[0] == osmf) setUpOSM(osmf2);  // ide még kell setTileProvider, nem?
            }
            return;
        }

        getSupportFragmentManager().beginTransaction().show(gmf).commit();
        getSupportFragmentManager().beginTransaction().show(osmf).commit();
        getSupportFragmentManager().executePendingTransactions();

        if (activeFragments.contains(gmf)) {
            getSupportFragmentManager().beginTransaction().remove(gmf).commit();
            activeFragments.remove(gmf);
        }
        if (activeFragments.contains(osmf)) {
            getSupportFragmentManager().beginTransaction().remove(osmf).commit();
            activeFragments.remove(osmf);
        }
        if (activeFragments.contains(osmf2)) {
            getSupportFragmentManager().beginTransaction().remove(osmf2).commit();
            activeFragments.remove(osmf2);
        }

        if (fragments[0] == gmf) {
            setUpGoogleMaps();
        }
        if (fragments[0] == osmf) {
            osmf = setUpOSM(osmf);
            osmf.setTileProvider(tileProviders[0]);
        }
        if (fragments[1] == gmf) {
            setUpGoogleMaps();
        }
        if (fragments[1] == osmf) {
            osmf = setUpOSM(osmf);
            osmf.setTileProvider(tileProviders[1]);
        }
        if (fragments[1] == osmf2) {
            osmf2 = setUpOSM(osmf2);
            osmf2.setTileProvider(tileProviders[1]);
        }
    }

    // Menü visibility logika
    private void setUpFullscreenDrawerGroups() {
        // használja: newFullScreenFragment(mapType), swapFullScreenFragment()
        MenuItem activeMapsItem = menu.findItem(R.id.activeMapsItem);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.googleMapsGroup, false);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.osmGroup, false);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.mapboxGroup, false);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.hereMapsGroup, false);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.bingMapsGroup, false);

        if (currentFullScreenVisibleFragment instanceof GoogleMapsFragment) {
            activeMapsItem.getSubMenu().setGroupVisible(R.id.googleMapsGroup, true);
            if (googleMapType == 0) menu.findItem(R.id.googleMapsSatellite).setVisible(false);
            else menu.findItem(R.id.googleMapsNormal).setVisible(false);
        } else {
            OSMFragment currentOSMFragment = ((OSMFragment)currentFullScreenVisibleFragment);   // rövidebb kód végett

            if (currentOSMFragment.getMapType().equals("OpenStreetMaps")) {
                activeMapsItem.getSubMenu().setGroupVisible(R.id.osmGroup, true);
            }
            if (currentOSMFragment.getMapType().equals("Mapbox")) {
                activeMapsItem.getSubMenu().setGroupVisible(R.id.mapboxGroup, true);
                if (currentOSMFragment.getTileProvider() == OSMFragment.TileProvider.MAPBOX) {
                    menu.findItem(R.id.mapboxNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.mapboxSatellite).setVisible(false);
                }
            }
            if (currentOSMFragment.getMapType().equals("Here Maps")) {
                activeMapsItem.getSubMenu().setGroupVisible(R.id.hereMapsGroup, true);
                if (currentOSMFragment.getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                    menu.findItem(R.id.mapboxNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.mapboxSatellite).setVisible(false);
                }
            }
            if (currentOSMFragment.getMapType().equals("Bing Maps")) {
                activeMapsItem.getSubMenu().setGroupVisible(R.id.bingMapsGroup, true);
                if (currentOSMFragment.getTileProvider() == OSMFragment.TileProvider.BING) {
                    menu.findItem(R.id.mapboxNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.mapboxSatellite).setVisible(false);
                }
            }
        }
    }

    // Menü visibility logika
    private void setUpSplitScreenDrawerGroups() {
        // használja: showSplitScreenMapSelectDialog()
        // Visibility logika
        menu.findItem(R.id.splitscreen_mode).setVisible(false);
        menu.findItem(R.id.fullscreen_mode).setVisible(true);
        menu.findItem(R.id.select_maps).setVisible(true);

        // RONDA group visibility logika
        MenuItem activeMapsItem = menu.findItem(R.id.activeMapsItem);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.googleMapsGroup, false);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.osmGroup, false);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.mapboxGroup, false);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.hereMapsGroup, false);
        activeMapsItem.getSubMenu().setGroupVisible(R.id.bingMapsGroup, false);

        // ha van gmf
        if (splitScreenFragments[0].equals("Google Maps") || splitScreenFragments[1].equals("Google Maps")) {
            activeMapsItem.getSubMenu().setGroupVisible(R.id.googleMapsGroup, true);
            if (googleMapType == 0)
                menu.findItem(R.id.googleMapsSatellite).setVisible(false);
            else menu.findItem(R.id.googleMapsNormal).setVisible(false);
        }
        if (splitScreenFragments[0].equals("OpenStreetMaps") || splitScreenFragments[1].equals("OpenStreetMaps")) {
            activeMapsItem.getSubMenu().setGroupVisible(R.id.osmGroup, true);
        }
        if (splitScreenFragments[0].equals("Mapbox") || splitScreenFragments[1].equals("Mapbox")) {
            activeMapsItem.getSubMenu().setGroupVisible(R.id.mapboxGroup, true);
            // ha az első fragment gmf
            if (activeFragments.get(0) == gmf) {
                if (((OSMFragment) activeFragments.get(1)).getTileProvider() == OSMFragment.TileProvider.MAPBOX) {
                    menu.findItem(R.id.mapboxNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.mapboxSatellite).setVisible(false);
                }
            }
            // ha a második fragment gmf
            if (activeFragments.get(1) == gmf) {
                if (((OSMFragment) activeFragments.get(0)).getTileProvider() == OSMFragment.TileProvider.MAPBOX) {
                    menu.findItem(R.id.mapboxNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.mapboxSatellite).setVisible(false);
                }
            }
            // ha egyik fragment sem gmf
            if (splitScreenFragments[0].equals("Mapbox")) {
                if (((OSMFragment) activeFragments.get(0)).getTileProvider() == OSMFragment.TileProvider.MAPBOX) {
                    menu.findItem(R.id.mapboxNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.mapboxSatellite).setVisible(false);
                }
            } else if (splitScreenFragments[1].equals("Mapbox")) {
                if (((OSMFragment) activeFragments.get(1)).getTileProvider() == OSMFragment.TileProvider.MAPBOX) {
                    menu.findItem(R.id.mapboxNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.mapboxSatellite).setVisible(false);
                }
            }

        }
        if (splitScreenFragments[0].equals("Here Maps") || splitScreenFragments[1].equals("Here Maps")) {
            activeMapsItem.getSubMenu().setGroupVisible(R.id.hereMapsGroup, true);

            // ha az első fragment gmf
            if (activeFragments.get(0) == gmf) {
                if (((OSMFragment) activeFragments.get(1)).getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                    menu.findItem(R.id.hereMapsNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.hereMapsSatellite).setVisible(false);
                }
            }
            // ha a második fragment gmf
            if (activeFragments.get(1) == gmf) {
                if (((OSMFragment) activeFragments.get(0)).getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                    menu.findItem(R.id.hereMapsNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.hereMapsSatellite).setVisible(false);
                }
            }
            // ha egyik fragment sem gmf
            if (splitScreenFragments[0].equals("Here Maps")) {
                if (((OSMFragment) activeFragments.get(0)).getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                    menu.findItem(R.id.hereMapsNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.hereMapsSatellite).setVisible(false);
                }
            } else if (splitScreenFragments[1].equals("Here Maps")) {
                if (((OSMFragment) activeFragments.get(1)).getTileProvider() == OSMFragment.TileProvider.HEREWEGO) {
                    menu.findItem(R.id.hereMapsNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.hereMapsSatellite).setVisible(false);
                }
            }
        }
        if (splitScreenFragments[0].equals("Bing Maps") || splitScreenFragments[1].equals("Bing Maps")) {
            activeMapsItem.getSubMenu().setGroupVisible(R.id.bingMapsGroup, true);

            // ha az első fragment gmf
            if (activeFragments.get(0) == gmf) {
                if (((OSMFragment) activeFragments.get(1)).getTileProvider() == OSMFragment.TileProvider.BING) {
                    menu.findItem(R.id.bingMapsNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.bingMapsSatellite).setVisible(false);
                }
            }
            // ha a második fragment gmf
            if (activeFragments.get(1) == gmf) {
                if (((OSMFragment) activeFragments.get(0)).getTileProvider() == OSMFragment.TileProvider.BING) {
                    menu.findItem(R.id.bingMapsNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.bingMapsSatellite).setVisible(false);
                }
            }
            // ha egyik fragment sem gmf
            if (splitScreenFragments[0].equals("Bing Maps")) {
                if (((OSMFragment) activeFragments.get(0)).getTileProvider() == OSMFragment.TileProvider.BING) {
                    menu.findItem(R.id.bingMapsNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.bingMapsSatellite).setVisible(false);
                }
            } else if (splitScreenFragments[1].equals("Bing Maps")) {
                if (((OSMFragment) activeFragments.get(1)).getTileProvider() == OSMFragment.TileProvider.BING) {
                    menu.findItem(R.id.bingMapsNormal).setVisible(false);
                } else {
                    menu.findItem(R.id.bingMapsSatellite).setVisible(false);
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "rotation_gestures":
                rotationGesturesEnabled = sharedPreferences.getBoolean(key, false);
                setRotationGesturesEnabled(rotationGesturesEnabled);
                break;
            case "screen_rotation":
                String value = sharedPreferences.getString(key, "portrait");
                screenOrientation = value;
                screenOrientationLocked = (value.equals("portrait") || value.equals("landscape"));
                onConfigurationChanged(null);
                break;
            case "crosshair":
                showCrosshair = sharedPreferences.getBoolean(key, false);
                crosshairPrefChanged(showCrosshair);
                break;
            case "fab_behavior":
                String value2 = sharedPreferences.getString(key, "replace current");
                fabBehavior = value2;
                fabBehaviorReplaceOther = value2.equals("replace other");
                break;

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
             && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Helyzetmeghatározás
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    private class MapUpdateTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                while(lockMapToGmf || lockMapToOsmf || lockMapToOsmf2) {
                    if (isGmfInMotion  && lockMapToGmf) {
                        Thread.sleep(120);
                        publishProgress();
                    }
                    if (isOsmfInMotion  && lockMapToOsmf) {
                        Thread.sleep(120);
                        publishProgress();
                    }
                    if (isOsmf2InMotion  && lockMapToOsmf2) {
                        Thread.sleep(120);
                        publishProgress();
                    }
                }
            } catch (InterruptedException e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            // Lehetséges párosítások: gmf-t követi osmf, osmf-t követi gmf, osmf-t követi osmf2, osmf2-t követi osmf

            if (isGmfInMotion && lockMapToGmf) {                            // A gmf mozgását követi az osmf
                GeoPoint newCenter = new GeoPoint(
                        gmf.map.getCameraPosition().target.latitude,
                        gmf.map.getCameraPosition().target.longitude);
                osmf.mapView.getController().animateTo(newCenter);
                // A zoom szintjét is
                osmf.mapView.getController().zoomTo((int)gmf.map.getCameraPosition().zoom);
                // A bearing fokát is
                osmf.mapView.setMapOrientation(360-gmf.map.getCameraPosition().bearing);
            }
            if (isOsmfInMotion && lockMapToOsmf) {                          // Az osmf mozgását követi vagy:...

                if (activeFragments.contains(gmf)) {                        // ...a gmf, vagy...
                    // A koordinátákat
                    LatLng newCenter = new LatLng(
                            osmf.mapView.getMapCenter().getLatitude(),
                            osmf.mapView.getMapCenter().getLongitude());
                    // A zoom szintjét is
                    int intZoomLevel = osmf.mapView.getZoomLevel();
                    // A bearing fokát is (gmf)
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(newCenter)
                            .zoom(intZoomLevel)
                            .bearing(360 - osmf.mapView.getMapOrientation())
                            .build();
                    gmf.map.animateCamera(CameraUpdateFactory.
                            newCameraPosition(cameraPosition), 120, null);
                } else if (activeFragments.contains(osmf2)) {               // ...az osmf2.
                    // A koordinátákat
                    osmf2.mapView.getController().animateTo(
                            new GeoPoint(osmf.mapView.getMapCenter().getLatitude(),
                                         osmf.mapView.getMapCenter().getLongitude()));
                    // A zoom szintjét is
                    osmf2.mapView.getController().zoomTo(osmf.mapView.getZoomLevel());
                    // A bearing fokát is
                    osmf2.mapView.setMapOrientation(osmf.mapView.getMapOrientation());
                }
            }
            if (isOsmf2InMotion && lockMapToOsmf2) {                        // Az osmf2 mozgását követi az osmf
                // A koordinátákat
                osmf.mapView.getController().animateTo(
                        new GeoPoint(osmf2.mapView.getMapCenter().getLatitude(),
                                     osmf2.mapView.getMapCenter().getLongitude()));
                // A zoom szintjét is
                osmf.mapView.getController().zoomTo(osmf2.mapView.getZoomLevel());
                // A bearing fokát is
                osmf.mapView.setMapOrientation(osmf2.mapView.getMapOrientation());
            }

        }
    }

    BroadcastReceiver br = new BroadcastReceiver() {        // a Geocoder és a FavoritesActivity küld ide Intent-et
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()=="SCROLL_MAP") {
                if (activeFragments.contains(gmf)) {
                    LatLng newCenter = new LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0));
                    CameraPosition cp = new CameraPosition.Builder()
                            .target(newCenter)
                            .zoom(gmf.map.getCameraPosition().zoom)
                            .bearing(currentBearing)
                            .build();
                    gmf.map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
                }
                if (activeFragments.contains(osmf)) {
                    GeoPoint newCenter = new GeoPoint(
                            intent.getDoubleExtra("latitude", 0.0),
                            intent.getDoubleExtra("longitude", 0.0));
                    osmf.mapView.getController().setCenter(newCenter);
                }
                if (activeFragments.contains(osmf2)) {
                    GeoPoint newCenter = new GeoPoint(
                            intent.getDoubleExtra("latitude", 0.0),
                            intent.getDoubleExtra("longitude", 0.0));
                    osmf2.mapView.getController().setCenter(newCenter);
                }

                autoCompleteTextView.setText("");
                drawer.closeDrawer(GravityCompat.START);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
                            }
        }
    };

}
