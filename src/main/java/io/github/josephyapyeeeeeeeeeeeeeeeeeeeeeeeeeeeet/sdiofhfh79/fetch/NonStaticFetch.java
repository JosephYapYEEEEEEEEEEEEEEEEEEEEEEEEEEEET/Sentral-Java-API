package io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79.fetch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraftforge.unsafe.UnsafeHacks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NonStaticFetch {

    private final CookieManager manager;

    public NonStaticFetch(CookieManager manager) {
        this.manager = manager;
    }
    public CompletableFuture<Fetch.Response> fetch(String url) {
        return fetch(url, "GET", new HashMap<>(), null);
    }

    public CompletableFuture<Fetch.Response> fetch(String url, String method, Map<String, ?> headers) {
        return fetch(url, method, headers, null);
    }

    public CompletableFuture<Fetch.Response> fetch(String url, Map<String, ?> headers) {
        return fetch(url, "GET", headers, null);
    }

    public CompletableFuture<Fetch.Response> fetch(String url, String method, Map<String, ?> headers, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String decodedURL = URLDecoder.decode(url, StandardCharsets.UTF_8);
                URL urld = new URL(decodedURL);
                URI uri = new URI(urld.getProtocol(), urld.getUserInfo(), urld.getHost(), urld.getPort(), urld.getPath(), urld.getQuery(), urld.getRef());
                String decodedURLAsString = uri.toASCIIString();
                URL apiUrl = new URL(decodedURLAsString);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                setCookieHandler(connection);
                connection.setRequestMethod(method);
                System.out.println(connection.getInstanceFollowRedirects());
                connection.setInstanceFollowRedirects(true);

                if (headers != null) {
                    for (Map.Entry<String, ?> entry : headers.entrySet()) {
                        if (entry.getValue() instanceof List<?> list) {
                            for (Object o : list) {
                                connection.addRequestProperty(entry.getKey(), (String) o);
                            }
                        } else if (entry.getValue() instanceof String str) {
                            connection.setRequestProperty(entry.getKey(), str);
                        }

                    }
                }

                if (body != null && !body.isEmpty()) {
                    connection.setDoOutput(true);
                    byte[] requestBodyBytes = body.getBytes(StandardCharsets.UTF_8);
                    connection.getOutputStream().write(requestBodyBytes);
                }

                int responseCode = connection.getResponseCode();

                byte[] bytes;
                if (responseCode >= 200 && responseCode < 300) {
                    bytes = readBytesFromInputStream(connection.getInputStream());
                } else {
                    bytes = readBytesFromInputStream(connection.getErrorStream());
                }

                Fetch.Response fetchResponse = new Fetch.Response(responseCode, bytes, connection.getHeaderFields(), connection.getURL().toURI());
                System.out.println(connection.getURL());
                connection.disconnect();

                return fetchResponse;
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setCookieHandler(HttpURLConnection connection) {
        try {
            String sunHttpClass;
            if (Class.forName("sun.net.www.protocol.https.HttpsURLConnectionImpl").isAssignableFrom(connection.getClass())) {
                sunHttpClass = "sun.net.www.protocol.https.HttpsURLConnectionImpl";
                Field delegate = Class.forName(sunHttpClass).getDeclaredField("delegate");
                UnsafeHacks.setAccessible(delegate);

                sunHttpClass = "sun.net.www.protocol.http.HttpURLConnection";
                Field cookieHandler = Class.forName(sunHttpClass).getDeclaredField("cookieHandler");
                UnsafeHacks.setAccessible(cookieHandler);
                cookieHandler.set(delegate.get(connection), this.manager);
            } else if (Class.forName("sun.net.www.protocol.http.HttpURLConnection").isAssignableFrom(connection.getClass())) {
                sunHttpClass = "sun.net.www.protocol.http.HttpURLConnection";
                Field cookieHandler = Class.forName(sunHttpClass).getDeclaredField("cookieHandler");
                UnsafeHacks.setAccessible(cookieHandler);
                cookieHandler.set(connection, this.manager);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) return new byte[0];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // or any desired buffer size

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        return outputStream.toByteArray();
    }
}
