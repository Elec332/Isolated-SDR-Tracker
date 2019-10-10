package elec332.isdrt.setup;

import elec332.isdrt.Config;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 6-10-2019
 */
public class TLESetup extends AbstractSetup {

    @Override
    public void setup(File workFolder, Config config) throws IOException {
        setupTLEs(new File(workFolder, "orbitron/Tle"), config);
    }

    private void setupTLEs(File tleFolder, Config config) throws IOException {
        String urlBase = "http://www.celestrak.com/NORAD/elements/";
        for (String s : config.TLEs.split(";")) {
            if (s.equals("noaa")) { //Nope, not today...
                continue;
            }
            String txt = s + ".txt";
            downloadTLE(urlBase + txt, new File(tleFolder, txt), config);
        }
    }

    private void downloadTLE(String url, File dest, Config config) throws IOException {
        String charset = "UTF-8";
        URLConnection connection = new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream response = connection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(response, charset));
        List<String> tree = br.lines().collect(Collectors.toList());
        tree = findSats(tree, config);
        writeToFile(dest, tree::forEach);
    }

    private List<String> findSats(List<String> tree, Config config) {
        List<String> ret = new ArrayList<>();
        if (tree.size() < 3) {
            return tree;
        }
        String track = config.track_sat_id < 0 ? config.tracking_sat : null;
        String lock = config.lock_sat_id < 0 ? config.locking_sat : null;

        boolean ti = false, li = false;
        if (track != null) {
            try {
                track = Integer.parseInt(track) + "U";
                ti = true;
            } catch (Exception e) {
                track = track + "   ";
            }
        }
        if (lock != null) {
            try {
                lock = Integer.parseInt(lock) + "U";
                li = true;
            } catch (Exception e) {
                lock = lock + "   ";
            }
        }
        Iterator<String> it = tree.iterator();
        String last = it.next();
        while (it.hasNext()) {
            String n = it.next();

            if (track != null && n.contains(track)) {
                if (ti) {
                    config.track_sat_id = Integer.parseInt(config.tracking_sat);
                    config.tracking_sat = last.trim();
                    ret.add(last);
                    ret.add(n);
                    ret.add(it.next());
                } else {
                    ret.add(n);
                    ret.add(it.next());
                    String dat2 = it.next();
                    ret.add(dat2);
                    config.track_sat_id = Integer.parseInt(dat2.split(" ")[1]);
                }
            }

            if (lock != null && n.contains(lock)) {
                if (li) {
                    config.lock_sat_id = Integer.parseInt(config.locking_sat);
                    config.locking_sat = last.trim();
                    ret.add(last);
                    ret.add(n);
                    ret.add(it.next());
                } else {
                    ret.add(n);
                    ret.add(it.next());
                    String dat2 = it.next();
                    ret.add(dat2);
                    config.lock_sat_id = Integer.parseInt(dat2.split(" ")[1]);
                }
            }

            last = n;

        }
        return ret;
    }

}
