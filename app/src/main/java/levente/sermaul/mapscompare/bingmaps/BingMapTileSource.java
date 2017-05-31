package levente.sermaul.mapscompare.bingmaps;

import android.content.Context;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.IStyledTileSource;
import org.osmdroid.tileprovider.tilesource.QuadTreeTileSource;
import levente.sermaul.mapscompare.bingmaps.ImageryMetaData.ImageryMetaData;
import levente.sermaul.mapscompare.bingmaps.ImageryMetaData.ImageryMetaDataResource;
import org.osmdroid.tileprovider.util.ManifestUtil;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.TileSystem;

public class BingMapTileSource extends QuadTreeTileSource implements IStyledTileSource<String> {
    private static final String BING_KEY = "BING_KEY";
    public static final String IMAGERYSET_AERIAL = "Aerial";
    public static final String IMAGERYSET_AERIALWITHLABELS = "AerialWithLabels";
    public static final String IMAGERYSET_ROAD = "Road";
    private static final String FILENAME_ENDING = ".jpeg";
    private static final String BASE_URL_PATTERN = "http://dev.virtualearth.net/REST/V1/Imagery/Metadata/%s?mapVersion=v1&output=json&key=%s";
    private static String mBingMapKey = "";
    private String mStyle = "Road";
    private ImageryMetaDataResource mImageryData = ImageryMetaDataResource.getDefaultInstance();
    private String mLocale;
    private String mBaseUrl;
    private String mUrl;



    public BingMapTileSource(String aLocale, String pStyle) {
        super("BingMaps", 0, 19, 256, ".jpeg", (String[])null);
        this.mLocale = aLocale;
        if(this.mLocale == null) {
            this.mLocale = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        }
        mStyle = pStyle;

    }

    public static void retrieveBingKey(Context aContext) {
        mBingMapKey = ManifestUtil.retrieveKey(aContext, "BING_KEY");
    }

    public static String getBingKey() {
        return mBingMapKey;
    }

    public static void setBingKey(String key) {
        mBingMapKey = key;
    }

    protected String getBaseUrl() {
        if(!this.mImageryData.m_isInitialised) {
            this.initMetaData();
        }

        return this.mBaseUrl;
    }

    public String getTileURLString(MapTile pTile) {
        if(!this.mImageryData.m_isInitialised) {
            this.initMetaData();
        }

        return String.format(this.mUrl, this.quadTree(pTile));
    }

    public int getMinimumZoomLevel() {
        return this.mImageryData.m_zoomMin;
    }

    public int getMaximumZoomLevel() {
        return this.mImageryData.m_zoomMax;
    }

    public int getTileSizePixels() {
        return this.mImageryData.m_imageHeight;
    }

    public String pathBase() {
        return this.mName + this.mStyle;
    }

    public void setStyle(String pStyle) {
        if(!pStyle.equals(this.mStyle)) {
            String var2 = this.mStyle;
            synchronized(mStyle) {
                this.mUrl = null;
                this.mBaseUrl = null;
                this.mImageryData.m_isInitialised = false;
            }
        }

        this.mStyle = pStyle;
    }

    public String getStyle() {
        return this.mStyle;
    }

    public ImageryMetaDataResource initMetaData() {
        if(!this.mImageryData.m_isInitialised) {
            synchronized(this) {
                if(!this.mImageryData.m_isInitialised) {
                    ImageryMetaDataResource imageryData = this.getMetaData();
                    if(imageryData != null) {
                        this.mImageryData = imageryData;
                        //TileSystem.setTileSize(this.getTileSizePixels());     ez a sor kód KIBASZ az Északi-tengerre ????
                        //TileSystem.setTileSize(512);                          ez meg Indonéziába
                        this.updateBaseUrl();
                    }
                }
            }
        }

        return this.mImageryData;
    }

    private ImageryMetaDataResource getMetaData() {
        Log.d("OsmDroid3rdParty", "getMetaData");
        HttpURLConnection client = null;

        InputStream e;
        try {
            client = (HttpURLConnection)((HttpURLConnection)(new URL(String.format("http://dev.virtualearth.net/REST/V1/Imagery/Metadata/%s?mapVersion=v1&output=json&key=%s", this.mStyle, mBingMapKey))).openConnection());
            Log.d("OsmDroid3rdParty", "make request " + client.getURL().toString().toString());
            client.setRequestProperty(OpenStreetMapTileProviderConstants.USER_AGENT, OpenStreetMapTileProviderConstants.getUserAgentValue());
            client.connect();
            if(client.getResponseCode() == 200) {
                e = client.getInputStream();
                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                BufferedOutputStream out = new BufferedOutputStream(dataStream, 8192);
                StreamUtils.copy(e, out);
                out.flush();
                ImageryMetaDataResource var5 = ImageryMetaData.getInstanceFromJSON(dataStream.toString());
                return var5;
            }

            Log.e("OsmDroid3rdParty", "Cannot get response for url " + client.getURL().toString() + " " + client.getResponseMessage());
            e = null;
        } catch (Exception var16) {
            Log.e("OsmDroid3rdParty", "Error getting imagery meta data", var16);
            return null;
        } finally {
            try {
                if(client != null) {
                    client.disconnect();
                }
            } catch (Exception var15) {
                ;
            }

            Log.d("OsmDroid3rdParty", "end getMetaData");
        }

        return null;
    }

    protected void updateBaseUrl() {
        Log.d("OsmDroid3rdParty", "updateBaseUrl");
        String subDomain = this.mImageryData.getSubDomain();
        int idx = this.mImageryData.m_imageUrl.lastIndexOf("/");
        if(idx > 0) {
            this.mBaseUrl = this.mImageryData.m_imageUrl.substring(0, idx);
        } else {
            this.mBaseUrl = this.mImageryData.m_imageUrl;
        }

        this.mUrl = this.mImageryData.m_imageUrl;
        if(subDomain != null) {
            this.mBaseUrl = String.format(this.mBaseUrl, subDomain);
            this.mUrl = String.format(this.mUrl, subDomain, "%s", this.mLocale);
        }

        Log.d("OsmDroid3rdParty", "updated url = " + this.mUrl);
        Log.d("OsmDroid3rdParty", "end updateBaseUrl");
    }
}