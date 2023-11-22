package io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.net.httpserver.HttpServer;
import io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79.fetch.NonStaticFetch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.NodeFilter;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79.fetch.Fetch.*;

public class Main {

    private static URL url;
    private static String prf;
    private static Args args;

    public static class Args {
        @Parameter(names = "--username", required = true)
        public String username;

        @Parameter(names = "--password", required = true)
        public String password;

        @Parameter(names = "--sentralPrefix", description = "The prefix used when fetching info", required = true)
        public String sentralId;
    }
    private static String token;
    public static void main(String... argv) throws IOException {
        Main.args = new Args();
        Args args = Main.args;
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        prf = "https://" + args.sentralId + ".sentral.com.au/";
        CookieManager manager = new CookieManager();
        CookieManager.setDefault(manager);
        url = new URL("https://" + args.sentralId + ".sentral.com.au/portal2/");

        String homeworkUrl = "https://" + args.sentralId + ".sentral.com.au/portal/dashboard/homework";
        String timetableUrl = "https://" + args.sentralId + ".sentral.com.au/portal/timetable/mytimetable";
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 5383), 0);
        server.createContext("/timetable/cyclical", exchange -> {
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
        server.createContext("/homework", exchange -> {
            List<Map<String, String>
                    > a = new ArrayList<>();
            {
                Response join = null;
                try {
                    join = getResponse(homeworkUrl);
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
                    join = getResponse(homeworkUrl + "?type=completed");
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
        });
        server.start();
        System.out.println("Started on " + server.getAddress().toString());
    }

    private static Response getResponse(String timetableUrl) throws IOException, URISyntaxException {
        var str = Map.<String, List<String>>of("Cookie", List.of());
        CookieManager.getDefault().put(URI.create(timetableUrl), str);
        CompletableFuture<Response> timetable = fetch(timetableUrl, str);
        Response response = timetable.join();
        assert url != null;
        assert prf != null;
        if (url.toURI().equals(response.getResponseUri())) {
            CompletableFuture<Response> fetch = fetch(prf + "portal2/user", "POST", Map.of(
                    "Content-Type", "application/json;charset=UTF-8",
                    "Referer", url.toString()
            ), String.format( """
                    {
                        "action": "login",
                        "username": "%s",
                        "remember_username": false,
                        "password": "%s"
                    }
                    """, args.username, args.password));
            return getResponse(timetableUrl);
        }
        return response;
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

    private static final NonStaticFetch nsf = new NonStaticFetch(new CookieManager());
    private static CompletableFuture<Response> fetch(String url, String method, Map<String, ?> headers, String body) {
        return nsf.fetch(url, method, headers, body);
    }

    private static CompletableFuture<Response> fetch(String url, Map<String, ?> headers) {
        return fetch(url, "GET", headers, null);
    }
}
