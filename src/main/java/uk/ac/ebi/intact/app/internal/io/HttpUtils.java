package uk.ac.ebi.intact.app.internal.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.Field;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static uk.ac.ebi.intact.app.internal.model.managers.Manager.INTACT_GRAPH_WS;

public class HttpUtils {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private static final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");

    private static final ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .build();

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    public static JsonNode getJsonNetworkWithRequestBody(String query, int page) throws IOException, InterruptedException {
        String baseUrl = INTACT_GRAPH_WS + "network/fromPagedInteractions";

        Map<String, Object> params = Map.of(
                "query", query,
                "advancedSearch", isAdvancedSearch(query),
                "page", page
        );

        HttpRequest request = requestBuilder
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(params)))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        return mapper.readTree(response.body());
    }

    public static boolean isAdvancedSearch(String query) {
        for (Field field : Field.values()) {
            if (query.contains(field.getMiqlQuery())) {
                return true;
            }
        }
        return false;
    }

    public static JsonNode getJSON(String url, Map<String, String> queryMap, Manager manager) {
        URL trueURL;
        try {
            if (queryMap.size() > 0) {
                String args = getStringArguments(queryMap);
                manager.utils.info("URL: " + url + "?" + args);
                trueURL = new URL(url + "?" + args);
            } else {
                manager.utils.info("URL: " + url);
                trueURL = new URL(url);
            }
        } catch (MalformedURLException e) {
            manager.utils.info("URL malformed");
            return NullNode.getInstance();
        }

        JsonNode jsonObject = NullNode.getInstance();

        try {
            InputStream entityStream = trueURL.openStream();
            jsonObject = new ObjectMapper().readTree(entityStream);
            entityStream.close();

        } catch (Exception e) {
            logError(e.getMessage());
        }
        return jsonObject;
    }

    public static JsonNode postJSON(String url, Map<Object, Object> data, Manager manager) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(buildFormDataFromMap(data))
                    .uri(URI.create(url))
                    .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response == null) {
                manager.utils.error("No response from " + url + " with post data = " + data);
                return null;
            }
            if (response.statusCode() != 200) {
                manager.utils.error("Error " + response.statusCode() + " from " + url + " with post data = " + data);
            }
            return new ObjectMapper().readTree(response.body());

        } catch (Exception e) {
            // e.printStackTrace();
            manager.utils.error("Unexpected error when parsing JSON from server: " + e.getMessage());
            return null;
        }
    }

    public static JsonNode postJSON(String url, Map<Object, Object> data, Manager manager, Supplier<Boolean> isCancelled) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(buildFormDataFromMap(data))
                    .uri(URI.create(url))
                    .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("accept", "application/json")
                    .build();
            Instant begin = Instant.now();
            CompletableFuture<String> body = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            manager.utils.error("Error " + response.statusCode() + " from " + url + " with post data = " + data);
                        }
                        return response;
                    }).thenApply(HttpResponse::body);

            while (!body.isDone()) {
                if (isCancelled.get()) {
                    body.cancel(true);
                    return null;
                }
            }
            System.out.println("Response received in " + Duration.between(begin, Instant.now()).toSeconds() + "s from " + url);
            return new ObjectMapper().readTree(body.get());

        } catch (Exception e) {
            // e.printStackTrace();
            manager.utils.error("Unexpected error while parsing JSON from server: " + e.getMessage());
            return null;
        }
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
            logError(e.getMessage());
        }
        return s != null ? s.toString() : "";
    }

    public static String truncate(String str) {
        if (str.length() > 1000)
            return str.substring(0, 1000) + "...";
        return str;
    }

    private static URLConnection executeWithRedirect(Manager manager, String url, Map<String, String> queryMap) throws Exception {
        // Get the connection from Cytoscape
        HttpURLConnection connection = (HttpURLConnection) manager.utils.getService(StreamUtil.class).getURLConnection(new URL(url));

        // We want to write on the stream
        connection.setDoOutput(true);
        // We want to deal with redirection ourselves
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
                manager.utils.error(readStream(connection.getErrorStream()));
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
        System.out.println("JSON error response: " + builder);
        return builder.toString();
    }

    public static String getRequestResultForUrl(String requestURL) {
        String jsonText = "";
        try {
            URL url = new URL(requestURL);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            jsonText = builder.toString();
        } catch (IOException e) {
            logError(e.getMessage());
        }

        return jsonText;
    }

    public static JsonNode getJsonForUrl(String jsonQuery) {
        String jsonText = getRequestResultForUrl(jsonQuery);
        if (jsonText.length() > 0) {
            try {
                return new ObjectMapper().readTree(jsonText);
            } catch (JsonProcessingException e) {
                logError(e.getMessage());
            }
        }
        return null;
    }

    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            if (entry.getValue() instanceof Iterable) {
                for (Object element : (Iterable<?>) entry.getValue()) {
                    builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
                    builder.append("=");
                    builder.append(URLEncoder.encode(element.toString(), StandardCharsets.UTF_8));
                    builder.append("&");
                }
                builder.deleteCharAt(builder.length() - 1);
            } else {
                builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
                builder.append("=");
                builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            }
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private static void logError(String message) {
        LOGGER.error(message);
    }
}
