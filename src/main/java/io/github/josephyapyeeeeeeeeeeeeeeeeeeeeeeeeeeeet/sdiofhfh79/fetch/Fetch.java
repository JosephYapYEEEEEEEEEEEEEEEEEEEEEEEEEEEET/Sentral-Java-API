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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Fetch {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static NonStaticFetch create() {
        return new NonStaticFetch(new CookieManager());
    }

    public static class Response {
        private final int status;
        private final byte[] bytes;
        private final Map<String, List<String>> responseHeaders;
        private final URI responseUri;

        public Response(int status, byte[] bytes, Map<String, List<String>> responseHeaders, URI responseUri) {
            this.status = status;
            this.bytes = bytes;
            this.responseHeaders = responseHeaders;
            this.responseUri = responseUri;
        }

        public URI getResponseUri() {
            return responseUri;
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

    @Deprecated
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
