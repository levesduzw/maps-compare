package levente.sermaul.mapscompare.bingmaps.ImageryMetaData;

import org.json.JSONArray;
import org.json.JSONObject;

public class ImageryMetaDataResource {
    private static final String IMAGE_WIDTH = "imageWidth";
    private static final String IMAGE_HEIGHT = "imageHeight";
    private static final String IMAGE_URL = "imageUrl";
    private static final String IMAGE_URL_SUBDOMAINS = "imageUrlSubdomains";
    private static final String ZOOM_MIN = "ZoomMin";
    private static final String ZOOM_MAX = "ZoomMax";
    public int m_imageHeight = 256;
    public int m_imageWidth = 256;
    public String m_imageUrl;
    public String[] m_imageUrlSubdomains;
    public int m_zoomMax = 22;
    public int m_zoomMin = 1;
    public boolean m_isInitialised = false;
    private int m_subdomainsCounter = 0;

    public ImageryMetaDataResource() {
    }

    public static ImageryMetaDataResource getDefaultInstance() {
        return new ImageryMetaDataResource();
    }

    public static ImageryMetaDataResource getInstanceFromJSON(JSONObject a_jsonObject) throws Exception {
        ImageryMetaDataResource result = new ImageryMetaDataResource();
        if(a_jsonObject == null) {
            throw new Exception("JSON to parse is null");
        } else {
            if(a_jsonObject.has("imageHeight")) {
                result.m_imageHeight = a_jsonObject.getInt("imageHeight");
            }

            if(a_jsonObject.has("imageWidth")) {
                result.m_imageWidth = a_jsonObject.getInt("imageWidth");
            }

            if(a_jsonObject.has("ZoomMin")) {
                result.m_zoomMin = a_jsonObject.getInt("ZoomMin");
            }

            if(a_jsonObject.has("ZoomMax")) {
                result.m_zoomMax = a_jsonObject.getInt("ZoomMax");
            }

            result.m_imageUrl = a_jsonObject.getString("imageUrl");
            if(result.m_imageUrl != null && result.m_imageUrl.matches(".*?\\{.*?\\}.*?")) {
                result.m_imageUrl = result.m_imageUrl.replaceAll("\\{.*?\\}", "%s");
            }

            JSONArray subdomains = a_jsonObject.getJSONArray("imageUrlSubdomains");
            if(subdomains != null && subdomains.length() >= 1) {
                result.m_imageUrlSubdomains = new String[subdomains.length()];

                for(int i = 0; i < subdomains.length(); ++i) {
                    result.m_imageUrlSubdomains[i] = subdomains.getString(i);
                }
            }

            result.m_isInitialised = true;
            return result;
        }
    }

    public synchronized String getSubDomain() {
        if(this.m_imageUrlSubdomains != null && this.m_imageUrlSubdomains.length > 0) {
            String result = this.m_imageUrlSubdomains[this.m_subdomainsCounter];
            if(this.m_subdomainsCounter < this.m_imageUrlSubdomains.length - 1) {
                ++this.m_subdomainsCounter;
            } else {
                this.m_subdomainsCounter = 0;
            }

            return result;
        } else {
            return null;
        }
    }
}