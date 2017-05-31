package levente.sermaul.mapscompare.mapbox;

/**
 * Created by Levente on 2016.12.05..
 */
import android.content.Context;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.ManifestUtil;

public class MyMapBoxTileSource extends OnlineTileSourceBase {
    private static final String MAPBOX_MAPID = "MAPBOX_MAPID";
    private static final String ACCESS_TOKEN = "MAPBOX_ACCESS_TOKEN";
    private static final String[] mapBoxBaseUrl = new String[]{"http://api.tiles.mapbox.com/v4/"};
    private String mapBoxMapId = "";
    private String accessToken;

    public MyMapBoxTileSource() {
        super("mapbox", 1, 19, 256, ".png", mapBoxBaseUrl);
    }

    public MyMapBoxTileSource(Context ctx) {
        super("mapbox", 1, 19, 256, ".png", mapBoxBaseUrl);
        this.retrieveAccessToken(ctx);
        //this.retrieveMapBoxMapId(ctx);
        this.mName = "mapbox" + this.mapBoxMapId;
    }

    public MyMapBoxTileSource(String mapboxid, String accesstoken) {
        super("mapbox", 1, 19, 256, ".png", mapBoxBaseUrl);
        this.accessToken = accesstoken;
        this.mapBoxMapId = mapboxid;
        this.mName = "mapbox" + this.mapBoxMapId;
    }

    public MyMapBoxTileSource(String name, int zoomMinLevel, int zoomMaxLevel, int tileSizePixels, String imageFilenameEnding) {
        super(name, zoomMinLevel, zoomMaxLevel, tileSizePixels, imageFilenameEnding, mapBoxBaseUrl);
    }

    public MyMapBoxTileSource(String name, int zoomMinLevel, int zoomMaxLevel, int tileSizePixels, String imageFilenameEnding, String mapBoxMapId, String mapBoxVersionBaseUrl) {
        super(name, zoomMinLevel, zoomMaxLevel, tileSizePixels, imageFilenameEnding, new String[]{mapBoxVersionBaseUrl});
    }

    //  Megváltoztatva
    //public final void retrieveMapBoxMapId(Context aContext) {
    //    this.mapBoxMapId = ManifestUtil.retrieveKey(aContext, "MAPBOX_MAPID");
    //}
    public final void setMapBoxMapId(String mapId) {
        this.mapBoxMapId = mapId;
    }

    public final void retrieveAccessToken(Context aContext) {
        this.accessToken = ManifestUtil.retrieveKey(aContext, "MAPBOX_ACCESS_TOKEN");
    }

    public void setMapboxMapid(String key) {
        this.mapBoxMapId = key;
        this.mName = "mapbox" + this.mapBoxMapId;
    }

    public String getMapBoxMapId() {
        return this.mapBoxMapId;
    }

    public String getTileURLString(MapTile aMapTile) {
        StringBuilder url = new StringBuilder(this.getBaseUrl());
        url.append(this.getMapBoxMapId());
        url.append("/");
        url.append(aMapTile.getZoomLevel());
        url.append("/");
        url.append(aMapTile.getX());
        url.append("/");
        url.append(aMapTile.getY());
        url.append(".png");
        url.append("?access_token=").append(this.getAccessToken());
        String res = url.toString();
        return res;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessTokeninput) {
        this.accessToken = accessTokeninput;
    }
}
