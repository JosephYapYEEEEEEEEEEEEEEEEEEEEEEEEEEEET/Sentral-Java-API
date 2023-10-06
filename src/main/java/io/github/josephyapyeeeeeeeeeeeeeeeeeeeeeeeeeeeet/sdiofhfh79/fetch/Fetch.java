package io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79.fetch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Fetch {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static CompletableFuture<Response> fetch(String url) {
        return fetch(url, "GET", new HashMap<>(), null);
    }

    public static CompletableFuture<Response> fetch(String url, String method, Map<String, String> headers) {
        return fetch(url, method, headers, null);
    }

    public static CompletableFuture<Response> fetch(String url, Map<String, String> headers) {
        return fetch(url, "GET", headers, null);
    }

    public static CompletableFuture<Response> fetch(String url, String method, Map<String, String> headers, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String decodedURL = URLDecoder.decode(url, StandardCharsets.UTF_8);
                URL urld = new URL(decodedURL);
                URI uri = new URI(urld.getProtocol(), urld.getUserInfo(), urld.getHost(), urld.getPort(), urld.getPath(), urld.getQuery(), urld.getRef());
                String decodedURLAsString = uri.toASCIIString();
                URL apiUrl = new URL(decodedURLAsString);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod(method);
                System.out.println(connection.getInstanceFollowRedirects());
                connection.setInstanceFollowRedirects(true);

                if (headers != null) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
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

                Response fetchResponse = new Response(responseCode, bytes, connection.getHeaderFields());
                System.out.println(connection.getURL());
                connection.disconnect();

                return fetchResponse;
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static class Response {
        private final int status;
        private final byte[] bytes;
        private final Map<String, List<String>> responseHeaders;

        public Response(int status, byte[] bytes, Map<String, List<String>> responseHeaders) {
            this.status = status;
            this.bytes = bytes;
            this.responseHeaders = responseHeaders;
        }

        public int getStatus() {
            return status;
        }

        public Map<String, List<String>> responseHeaders() {
            return responseHeaders;
        }

        public CompletableFuture<String> text() {
            return CompletableFuture.completedFuture(new String(bytes));
        }

        public CompletableFuture<byte[]> arrayBuffer() {
            return CompletableFuture.completedFuture(bytes);
        }

        @Deprecated
        public CompletableFuture<Blob> blob() {
            return CompletableFuture.completedFuture(new Blob(bytes));
        }

        public CompletableFuture<JsonElement> json() {
            return CompletableFuture.supplyAsync(() -> {
                String text = new String(bytes);
                return GSON.fromJson(text, JsonElement.class);
            });
        }
    }

    static class Blob {
        private final byte[] bytes;

        public Blob(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }

    public static byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
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
