package levente.sermaul.mapscompare.bingmaps.ImageryMetaData;

import org.json.JSONArray;
import org.json.JSONObject;
import levente.sermaul.mapscompare.bingmaps.ImageryMetaData.ImageryMetaDataResource;

public class ImageryMetaData {
    private static final String STATUS_CODE = "statusCode";
    private static final String AUTH_RESULT_CODE = "authenticationResultCode";
    private static final String AUTH_RESULT_CODE_VALID = "ValidCredentials";
    private static final String RESOURCE_SETS = "resourceSets";
    private static final String ESTIMATED_TOTAL = "estimatedTotal";
    private static final String RESOURCE = "resources";

    public ImageryMetaData() {
    }

    public static ImageryMetaDataResource getInstanceFromJSON(String a_jsonContent) throws Exception {
        if(a_jsonContent == null) {
            throw new Exception("JSON to parse is null");
        } else {
            JSONObject jsonResult = new JSONObject(a_jsonContent);
            int statusCode = jsonResult.getInt("statusCode");
            if(statusCode != 200) {
                throw new Exception("Status code = " + statusCode);
            } else if("ValidCredentials".compareToIgnoreCase(jsonResult.getString("authenticationResultCode")) != 0) {
                throw new Exception("authentication result code = " + jsonResult.getString("authenticationResultCode"));
            } else {
                JSONArray resultsSet = jsonResult.getJSONArray("resourceSets");
                if(resultsSet != null && resultsSet.length() >= 1) {
                    if(resultsSet.getJSONObject(0).getInt("estimatedTotal") <= 0) {
                        throw new Exception("No resource found in json response");
                    } else {
                        JSONObject resource = resultsSet.getJSONObject(0).getJSONArray("resources").getJSONObject(0);
                        return ImageryMetaDataResource.getInstanceFromJSON(resource);
                    }
                } else {
                    throw new Exception("No results set found in json response");
                }
            }
        }
    }
}