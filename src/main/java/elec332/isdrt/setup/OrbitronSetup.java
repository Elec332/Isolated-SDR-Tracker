package elec332.isdrt.setup;

import elec332.isdrt.Config;

import java.io.File;
import java.io.IOException;

/**
 * Created by Elec332 on 6-10-2019
 */
public class OrbitronSetup extends AbstractSetup {

    @Override
    public void setup(File workFolder, Config config) throws IOException {
        File orbitronBase = new File(workFolder, "/orbitron");
//        File tleFolder = new File(orbitronBase, "Tle");
//        setupTLEs(tleFolder);
        if (config.lock_sat_id < 0 || config.track_sat_id < 0){
            throw new IllegalArgumentException("Invalid Sat Names!");
        }
        File orbitronConfigBase = new File(orbitronBase, "Config");
        File mainConfig = new File(orbitronConfigBase, "Setup.cfg");
        setupOrbitronConfig(mainConfig, config);
        File freqConfig = new File(orbitronConfigBase, "Radio.his");
        createOrbitronFreqFile(freqConfig, config);
    }

    private void setupOrbitronConfig(File file, Config config) throws IOException {
        mapFile(file, map -> {
            StringBuilder tle = new StringBuilder();
            for (String s : config.TLEs.split(";")){
                tle.append(";Tle\\");
                tle.append(s);
                tle.append(".txt");
            }
            map.put("TLEFiles=", "TLEFiles=" + tle.substring(1));
            map.put("Tracked_1=", "Tracked_1=" + config.track_sat_id + ";" + config.lock_sat_id);
            map.put("Active=", "Active=" + config.lock_sat_id);

            //Sanity checks
            map.put("All=1", "All=0"); //NOT all sats on
            map.put("TLEUpdateConfirmation=1", "TLEUpdateConfirmation=0");
            map.put("ExitConfirmation=1", "ExitConfirmation=0");
        });
    }

    private void setupTLEConfig(File file) throws IOException {
        mapFile(file, map -> {

        });
    }

    private void createOrbitronFreqFile(File file, Config config) throws IOException {
        writeToFile(file, writer -> {
            writer.accept("[Radio]");
            writer.accept(config.track_sat_id + "_dn=" + config.downlink);
            writer.accept(config.track_sat_id + "_dm=FM-W");
        });
    }

}
