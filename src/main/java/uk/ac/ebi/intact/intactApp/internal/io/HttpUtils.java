package uk.ac.ebi.intact.intactApp.internal.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cytoscape.io.util.StreamUtil;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtils {
    public static JsonNode getJSON(String url, Map<String, String> queryMap,
                                   IntactManager manager) {

        // Set up our connection
        URL trueURL;
        try {
            if (queryMap.size() > 0) {
                String args = HttpUtils.getStringArguments(queryMap);
                manager.info("URL: " + url + "?" + args);
                trueURL = new URL(url + "?" + args);
            } else {
                manager.info("URL: " + url);
                trueURL = new URL(url);
            }
        } catch (MalformedURLException e) {
            manager.info("URL malformed");
            return NullNode.getInstance();
        }

        JsonNode jsonObject = NullNode.getInstance();

        try {
            URLConnection connection = manager.getService(StreamUtil.class).getURLConnection(trueURL);

            InputStream entityStream = connection.getInputStream();
            jsonObject = new ObjectMapper().readTree(entityStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JsonNode postJSON(String url, Map<String, String> queryMap,
                                    IntactManager manager) {

        // Set up our connection
        ObjectNode jsonObject = new ObjectNode(JsonNodeFactory.instance);


        URLConnection connection;
        try {
            connection = executeWithRedirect(manager, url, queryMap);
            InputStream entityStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entityStream));

            reader.mark(2097152); // Set a mark so that if we get a parse failure, we can recover the error

            try {
                jsonObject.set(IntactManager.RESULT, new ObjectMapper().readTree(reader));
            } catch (Exception parseFailure) {
                // Get back to the start of the error
                reader.reset();
                StringBuilder errorString = new StringBuilder();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        errorString.append(line);
                    }
                } catch (Exception ioe) {
                    // ignore
                }
                manager.error("Exception reading JSON from STRING: " + parseFailure.getMessage());
                System.out.println("Exception reading JSON from STRING: " + parseFailure.getMessage() + "\n Text: " + errorString);
                return null;
            }

        } catch (Exception e) {
            // e.printStackTrace();
            manager.error("Unexpected error when parsing JSON from server: " + e.getMessage());
            return null;
        }

        return jsonObject;
    }


    public static String getStringArguments(Map<String, String> args) {
        StringBuilder s = null;
        try {
            for (String key : args.keySet()) {
                if (s == null)
                    s = new StringBuilder(key + "=" + URLEncoder.encode(args.get(key), StandardCharsets.UTF_8.displayName()));
                else
                    s.append("&").append(key).append("=").append(URLEncoder.encode(args.get(key), StandardCharsets.UTF_8.displayName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s != null ? s.toString() : "";
    }


    public static String truncate(String str) {
        if (str.length() > 1000)
            return str.substring(0, 1000) + "...";
        return str;
    }


    private static URLConnection executeWithRedirect(IntactManager manager, String url, Map<String, String> queryMap) throws Exception {
        // Get the connection from Cytoscape
        HttpURLConnection connection = (HttpURLConnection) manager.getService(StreamUtil.class).getURLConnection(new URL(url));

        // We want to write on the stream
        connection.setDoOutput(true);
        // We want to deal with redirection ourself
        connection.setInstanceFollowRedirects(false);

        // We write the POST arguments
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(getStringArguments(queryMap));
        out.close();

        // Check for redirections
        int statusCode = connection.getResponseCode();
        switch (statusCode) {
            case HttpURLConnection.HTTP_MOVED_PERM: // code 301
            case HttpURLConnection.HTTP_MOVED_TEMP: // code 302
            case HttpURLConnection.HTTP_SEE_OTHER: // code 303
                // Got a redirect.
                // Get the new location
                return executeWithRedirect(manager, connection.getHeaderField("Location"), queryMap);
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
            case HttpURLConnection.HTTP_BAD_REQUEST:
                manager.error(readStream(connection.getErrorStream()));
                return connection;
        }

        return connection;
    }

    private static String readStream(InputStream stream) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line); // + "\r\n"(no need, json has no line breaks!)
            }
        }
        System.out.println("JSON error response: " + builder.toString());
        return builder.toString();
    }

    public static String getJsonTextForUrl(String jsonQuery) {
        String jsonText = "";
        try {
            URL url = new URL(jsonQuery);
            URLConnection olsConnection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(olsConnection.getInputStream()));
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            jsonText = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonText;
    }

    public static JsonNode getJsonForUrl(String jsonQuery) {
        String jsonText = getJsonTextForUrl(jsonQuery);
        if (jsonText.length() > 0) {
            try {
                return new ObjectMapper().readTree(jsonText);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
