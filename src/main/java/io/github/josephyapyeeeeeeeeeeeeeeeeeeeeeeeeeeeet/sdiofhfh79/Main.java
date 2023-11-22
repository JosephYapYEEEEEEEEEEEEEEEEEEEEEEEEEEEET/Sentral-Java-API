package io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79.fetch.Fetch.*;

public class Main {
    public static void main(String... argv) throws IOException {
        SentralCred.startGc();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8000), 0);
     /* server.createContext("/timetable/cyclical", exchange -> {
            Response response = null;
            try {
                response = getResponse(timetableUrl);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            Document document = Jsoup.parse(response.text().join());
            Element timetableTable = document.getElementsByClass("timetable table").get(0);
            Element tbody = timetableTable.getElementsByTag("tbody").get(0);
            Element tr = tbody.getElementsByTag("tr").get(0);
            List<String> days = new ArrayList<>();
            for (Element timetableDay : tr.getElementsByClass("timetable-day")) {
                days.add(timetableDay.text());
            }
            for (Element element : tbody.getElementsByTag("tr").filter((a, b) -> a == tr ? NodeFilter.FilterResult.REMOVE : NodeFilter.FilterResult.CONTINUE)) {

            }
            exchange.sendResponseHeaders(200, days.toString().getBytes().length);
            exchange.getResponseBody().write(days.toString().getBytes());
            exchange.close();
        });
     */ server.createContext("/homework", exchange -> {
            try {
                homework(exchange);
            } catch (Throwable t) {
                String message = t.getMessage();
                exchange.sendResponseHeaders(t instanceof StatusException s ? s.getCode() : 500, message.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
                exchange.close();
                t.printStackTrace();
            }
        }); // to be removed
        server.start();
        System.out.println("Started on " + server.getAddress().toString());
    }

    // https://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
    private static  Map<String, List<String>> splitQuery(URI url) {
        if (url.getQuery() == null || "".equals(url.getQuery())) {
            return Collections.emptyMap();
        }
        return Arrays.stream(url.getQuery().split("&"))
                .map(Main::splitQueryParameter)
                .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    // https://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
    private static  AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        assert value != null;
        return new AbstractMap.SimpleImmutableEntry<>(
                URLDecoder.decode(key, StandardCharsets.UTF_8),
                URLDecoder.decode(value, StandardCharsets.UTF_8)
        );
    }

    private static void homework(HttpExchange exchange) throws IOException {
        System.out.println(exchange);
        Map<String, List<String>> stringListMap = splitQuery(exchange.getRequestURI());
        String username = stringListMap.get("username").get(0);
        String password = stringListMap.get("password").get(0);
        String prefix = stringListMap.get("sentralPrefix").get(0);
        SentralCred sentralCred = SentralCred.of(username, password, prefix);
        String homeworkUrl = sentralCred.getHomeworkUrl();
        List<Map<String, String>
                > a = new ArrayList<>();
        {
            Response join = null;
            try {
                join = getResponse(sentralCred, homeworkUrl);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            String join1 = join.text().join();
            Document parse = Jsoup.parse(join1);
            Element elem = parse.getElementsByClass("table table-stripped table-hover table-condensed").get(0);
            Element thead = elem.getElementsByTag("thead").get(0);
            List<String> store = new ArrayList<>();
            for (Element element : thead.getElementsByTag("tr").get(0).getElementsByTag("th")) {
                store.add(element.text());
            }
            Element tbody = elem.getElementsByTag("tbody").get(0);
            for (Element element : tbody.getElementsByTag("tr")) {
                Map<String, String> map = new HashMap<>();
                map.put("Completed", "No");
                int i = 0;
                for (Element element1 : element.getElementsByTag("td")) {
                    if (i == 0) {
                        String a1 = element1.getElementsByTag("a").get(0).attr("data-content");
                        map.put("Comment", Jsoup.parse(a1).text());
                    }
                    map.put(store.get(i), element1.text());
                    i++;
                }
                map.put("UUID", "" + randomUUID((map.get("Title") + map.get("Due Date")).hashCode()));
                a.add(map);
            }
        }
        {
            Response join = null;
            try {
                join = getResponse(sentralCred, homeworkUrl + "?type=completed");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            String join1 = join.text().join();
            Document parse = Jsoup.parse(join1);
            Element elem = parse.getElementsByClass("table table-stripped table-hover table-condensed").get(0);
            Element thead = elem.getElementsByTag("thead").get(0);
            List<String> store = new ArrayList<>();
            for (Element element : thead.getElementsByTag("tr").get(0).getElementsByTag("th")) {
                store.add(element.text());
            }
            Element tbody = elem.getElementsByTag("tbody").get(0);
            for (Element element : tbody.getElementsByTag("tr")) {
                Map<String, String> map = new HashMap<>();
                map.put("Completed", "Yes");
                int i = 0;
                for (Element element1 : element.getElementsByTag("td")) {
                    if (i == 0) {
                        String a1 = element1.getElementsByTag("a").get(0).attr("data-content");
                        map.put("Comment", Jsoup.parse(a1).text());
                    }
                    map.put(store.get(i), element1.text());
                    i++;
                }
                map.put("UUID", "" + randomUUID((map.get("Title") + map.get("Due Date")).hashCode()));
                a.add(map);
            }
        }
        exchange.sendResponseHeaders(200, GSON.toJson(a).getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(GSON.toJson(a).getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    private static Response getResponse(SentralCred cred, String url, int attempts) throws IOException, URISyntaxException {
        if (attempts >= 3) {
            throw new StatusException("Failed to login", 403);
        }
        CompletableFuture<Response> timetable = cred.getFetch().fetch(url);
        Response response = timetable.join();
        if (cred.getLoginUrl().toURI().equals(response.getResponseUri())) {
            CompletableFuture<Response> fetch = cred.getFetch().fetch(cred.getLoginUrlHook().toString(), "POST", Map.of(
                    "Content-Type", "application/json;charset=UTF-8",
                    "Referer", url.toString()
            ), String.format( """
                    {
                        "action": "login",
                        "username": "%s",
                        "remember_username": false,
                        "password": "%s"
                    }
                    """, cred.getUsername(), cred.getPassword()));
            return getResponse(cred, url, attempts+1);
        } else if (!URI.create(url).equals(response.getResponseUri())) {
            return getResponse(cred, url, attempts+1);
        }
        return response;
    }

    private static Response getResponse(SentralCred cred, String url) throws IOException, URISyntaxException {
        return getResponse(cred, url, 0);
    }

    public static UUID randomUUID(long co) {
        Random ng = new Random(co);

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6]  &= 0x0f;  /* clear version        */
        randomBytes[6]  |= 0x40;  /* set to version 4     */
        randomBytes[8]  &= 0x3f;  /* clear variant        */
        randomBytes[8]  |= 0x80;  /* set to IETF variant  */
        long msb = 0;
        long lsb = 0;
        byte[] data = randomBytes;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }
}
