package io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.NodeFilter;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
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
        if (url.toURI().equals(response.getConnection().getURL().toURI())) {
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
}
