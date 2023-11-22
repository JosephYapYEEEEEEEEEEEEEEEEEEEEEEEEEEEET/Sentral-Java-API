package io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79;

import io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79.fetch.NonStaticFetch;

import java.io.UncheckedIOException;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SentralCred {
    public static final ConcurrentHashMap<Integer, SentralCred> sentralCredConcurrentHashMap = new ConcurrentHashMap<>();
    private final String username;
    private final String password;
    private final String sentralId;
    private final NonStaticFetch fetch;
    private final URL url;
    private final URL url2;

    private long lastUse;

    private SentralCred(String username, String password, String sentralId) {
        this.username = username;
        this.password = password;
        this.sentralId = sentralId;
        try {
            this.url = new URL("https://" + sentralId + ".sentral.com.au/portal2/");
            this.url2 = new URL("https://" + sentralId + ".sentral.com.au/portal2/user");
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
        fetch = new NonStaticFetch(new CookieManager());
        this.lastUse = System.currentTimeMillis();
        sentralCredConcurrentHashMap.put((username + password + sentralId).hashCode(), this);
    }

    public static SentralCred of(String username, String password, String sentralId) {
        int hashCode = (username + password + sentralId).hashCode();
        if (sentralCredConcurrentHashMap.containsKey(hashCode)) {
            return sentralCredConcurrentHashMap.get(hashCode);
        }
        return new SentralCred(username, password, sentralId);
    }
    public void used() {
        lastUse = System.currentTimeMillis();
    }
    public String getHomeworkUrl() {
        used();
        return "https://" + sentralId + ".sentral.com.au/portal/dashboard/homework";
    }

    public String getTimetableUrl() {
        used();
        return "https://" + sentralId + ".sentral.com.au/portal/timetable/mytimetable";
    }

    public URL getLoginUrl() {
        used();
        return url;
    }

    public URL getLoginUrlHook() {
        used();
        return url2;
    }

    public NonStaticFetch getFetch() {
        used();
        return fetch;
    }

    public String getUsername() {
        used();
        return username;
    }

    public String getPassword() {
        used();
        return password;
    }

    public static void gc() {
        long time = System.currentTimeMillis();
        int length = sentralCredConcurrentHashMap.size();
        sentralCredConcurrentHashMap.values().removeIf(a -> time > (a.lastUse + 1000 * 60));
        System.out.println("Removed " + (length - sentralCredConcurrentHashMap.size()) + " items.");
    }

    public static void jammingGc() {
        long nextGc = System.currentTimeMillis()+5000;
        while (true) {
            if (nextGc < System.currentTimeMillis()) {
                nextGc = System.currentTimeMillis()+5000;
                gc();
            }
        }
    }

    public static void startGc() {
        Thread thread = new Thread(SentralCred::jammingGc);
        thread.start();
    }
}
