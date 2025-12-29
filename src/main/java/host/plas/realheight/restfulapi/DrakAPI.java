package host.plas.realheight.restfulapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gg.drak.thebase.objects.SingleSet;
import kong.unirest.*;

import java.util.Map;

public class DrakAPI {
    public static final String BASE_URL = "https://api.drak.gg/api/v1/";
    public static final String ENDPOINT = "plugins/heights";
    public static final String URL = BASE_URL + ENDPOINT;

    public static SingleSet<Boolean, Double> getScale(String uuid) {
        JsonObject response = getResponse(URL, Map.of("uuid", uuid));
        boolean found = response.has("found") && response.get("found").getAsBoolean();
        double scale = found && response.has("scale") ? response.get("scale").getAsDouble() : -1.0;
        if (scale == -1.0) {
            found = false;
        }

        return new SingleSet<>(found, scale);
    }

    public static SingleSet<Boolean, Double> setScale(String uuid, double scale) {
        JsonObject response = postResponse(URL, Map.of("uuid", uuid, "scale", String.valueOf(scale)));
        boolean found = response.has("success") && response.get("success").getAsBoolean();
        double scaleReturned = found && response.has("scale") ? response.get("scale").getAsDouble() : -1.0;
        if (scaleReturned == -1.0) {
            found = false;
        }

        return new SingleSet<>(found, scaleReturned);
    }

    public static JsonObject getResponse(String url, Map<String, String> params) {
        GetRequest request = Unirest.get(url);

        return formatResponse(request, params);
    }

    public static JsonObject postResponse(String url, Map<String, String> params) {
        HttpRequestWithBody request = Unirest.post(url);

        return formatResponse(request, params);
    }

    public static JsonObject formatResponse(HttpRequest<?> request, Map<String, String> params) {
        request = request.header("accept", "application/json");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            request = request.queryString(entry.getKey(), entry.getValue());
        }

        HttpResponse<JsonNode> response = request.asJson();
        return JsonParser.parseString(response.getBody().toString()).getAsJsonObject();
    }
}
