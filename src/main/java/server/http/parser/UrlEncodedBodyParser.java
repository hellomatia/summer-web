package server.http.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class UrlEncodedBodyParser {
    private UrlEncodedBodyParser() {
    }

    public static Map<String, String> parse(String body) {
        Map<String, String> parameters = new HashMap<>();
        if (body != null && !body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1) {
                    try {
                        String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                        String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                        parameters.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                    }
                }
            }
        }
        return parameters;
    }
}
