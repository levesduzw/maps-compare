package levente.sermaul.mapscompare.heremaps;

import android.content.Context;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.ManifestUtil;
import android.content.Context;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.ManifestUtil;

public class HEREWeGoTileSource extends OnlineTileSourceBase {
    private static final String HEREWEGO_MAPID = "HEREWEGO_MAPID";
    private static final String HEREWEGO_APPID = "HEREWEGO_APPID";
    private static final String APPCODE = "HEREWEGO_APPCODE";
    private static final String HEREWEGO_DOMAIN_OVERRIDE = "HEREWEGO_OVERRIDE";
    private static final String[] mapBoxBaseUrl = new String[]{"http://1.{domain}/maptile/2.1/maptile/newest/", "http://2.{domain}/maptile/2.1/maptile/newest/", "http://3.{domain}/maptile/2.1/maptile/newest/", "http://4.{domain}/maptile/2.1/maptile/newest/"};
    private String herewegoMapId = "terrain.day";    // hybrid.day normal.day
    private String appId = "";
    private String appCode = "";
    private String domainOverride = "aerial.maps.cit.api.here.com";

    public HEREWeGoTileSource() {
        super("herewego", 1, 19, 256, ".png", mapBoxBaseUrl);
    }

    public HEREWeGoTileSource(Context ctx) {
        super("herewego", 1, 19, 256, ".png", mapBoxBaseUrl);
        this.mName = "herewego" + this.herewegoMapId;
    }

    private void retrieveDomainOverride(Context aContext) {
        String temp = ManifestUtil.retrieveKey(aContext, "HEREWEGO_OVERRIDE");
        if(temp != null && temp.length() > 0) {
            this.domainOverride = temp;
        }

    }

    public void setDomainOverride(String hostname) {
        this.domainOverride = hostname;
    }

    public HEREWeGoTileSource(String mapboxid, String accesstoken, String appCode) {
        super("herewego", 1, 19, 256, ".png", mapBoxBaseUrl);
        this.appId = accesstoken;
        this.herewegoMapId = mapboxid;
        this.appCode = appCode;
        this.mName = "herewego" + this.herewegoMapId;
    }

    public HEREWeGoTileSource(String name, int zoomMinLevel, int zoomMaxLevel, int tileSizePixels, String imageFilenameEnding) {
        super(name, zoomMinLevel, zoomMaxLevel, tileSizePixels, imageFilenameEnding, mapBoxBaseUrl);
    }

    public HEREWeGoTileSource(String name, int zoomMinLevel, int zoomMaxLevel, int tileSizePixels, String imageFilenameEnding, String mapBoxMapId, String mapBoxVersionBaseUrl) {
        super(name, zoomMinLevel, zoomMaxLevel, tileSizePixels, imageFilenameEnding, new String[]{mapBoxVersionBaseUrl});
    }

    public final void retrieveAppCode(Context aContext) {
        this.appCode = ManifestUtil.retrieveKey(aContext, "HEREWEGO_APPCODE");
    }

    public final void retrieveMapBoxMapId(Context aContext) {
        this.herewegoMapId = ManifestUtil.retrieveKey(aContext, "HEREWEGO_MAPID");
    }

    public final void retrieveAppId(Context aContext) {
        this.appId = ManifestUtil.retrieveKey(aContext, "HEREWEGO_APPID");
    }

    public void setHereWeGoMapid(String key) {
        this.herewegoMapId = key;
        this.mName = "herewego" + this.herewegoMapId;
    }

    public String getHerewegoMapId() {
        return this.herewegoMapId;
    }

    public String getTileURLString(MapTile aMapTile) {
        StringBuilder url = new StringBuilder(this.getBaseUrl().replace("{domain}", this.domainOverride));
        url.append(this.getHerewegoMapId());
        url.append("/");
        url.append(aMapTile.getZoomLevel());
        url.append("/");
        url.append(aMapTile.getX());
        url.append("/");
        url.append(aMapTile.getY());
        url.append("/").append(this.getTileSizePixels()).append("/png8?");
        url.append("app_id=").append(this.getAppId());
        url.append("&app_code=").append(this.getAppCode());
        url.append("&lg=pt-BR");
        String res = url.toString();
        return res;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String accessTokeninput) {
        this.appId = accessTokeninput;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
