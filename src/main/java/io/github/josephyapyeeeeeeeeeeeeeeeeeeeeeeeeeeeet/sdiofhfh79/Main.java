package io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79.fetch.Fetch.*;

public class Main {
    public static class Args {
        @Parameter(names = "--token", description = "The 'PortalSID' cookie", required = true)
        public String token;

        @Parameter(names = "--sentralPrefix", description = "The prefix used when fetching info", required = true)
        public String sentralId;
    }
    public static void main(String[] argv) throws IOException {
        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        String gg = "PortalSID=" + args.token + ";PortalLoggedIn=1;";
        String homeworkUrl = "https://" + args.sentralId + ".sentral.com.au/portal/dashboard/homework";

        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 5383), 0);
        server.createContext("/timetable", exchange -> {

        });
        server.createContext("/homework", exchange -> {
            List<Map<String, String>> a = new ArrayList<>();
            {
                CompletableFuture<Response> request = fetch(homeworkUrl, Map.of("Cookie", gg));
                Response join = request.join();
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
                CompletableFuture<Response> request = fetch(homeworkUrl + "?type=completed", Map.of("Cookie", gg));
                Response join = request.join();
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
}
