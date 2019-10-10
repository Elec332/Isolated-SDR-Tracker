package elec332.isdrt;

import elec332.isdrt.setup.OrbitronSetup;
import elec332.isdrt.setup.SDRSetup;
import elec332.isdrt.setup.TLESetup;
import elec332.isdrt.util.ClassProperties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Elec332 on 5-10-2019
 */
public class Main {

    static final String SDR_FOLDER = "SDR";

    public static void main(String... args) throws Exception {

        final Map<String, String> params = new HashMap<>();

        String p = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (p != null){
                    params.put(p, "");
                }
                if (a.length() < 2) {
                    throw new RuntimeException("Invalid cmd");
                }
                p = a.substring(1);
            } else if (p != null) {
                params.put(p, a);
                p = null;
            } else {
                throw new RuntimeException("Invalid cmd chain");
            }
        }

        System.out.println(params);

        File runDir;
        String folderBase;
        if (!params.containsKey("runDir")) {
            try {
                runDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
                if (!runDir.exists()) {
                    throw new RuntimeException();
                }
                folderBase = runDir.getCanonicalPath();
            } catch (Exception e) {
                throw new RuntimeException("Cannot find rundir!");
            }
        } else {
            try {
                folderBase = params.get("runDir");
                runDir = new File(folderBase);
                if (!runDir.exists() && !runDir.mkdirs()) {
                    throw new RuntimeException();
                }
            } catch (Exception e){
                throw new RuntimeException("Cannot find rundir!");
            }
        }



        String configName = params.getOrDefault("cfg", folderBase + "/config.cfg");
        String sdrZip = params.getOrDefault("zip", folderBase + "/SDRTrack.zip");
        configFile = new File(configName);
        config = ClassProperties.readProperties(Config.class, configFile);

        if (params.containsKey("globalCfg")){
            File f = new File(params.get("globalCfg"));
            ClassProperties.importProperties(config, f);
        }

        config.initialize();

        PreStartup preStartup = new PreStartup(runDir, config);
        preStartup.clearWorkingDir();
        preStartup.extractProgram(new File(sdrZip));
        preStartup.setup(new TLESetup());

        String dataAdd = params.containsKey("i") ? "/" + config.tracking_sat : "";

        config.dataFolder = new File(params.getOrDefault("data", folderBase + "/data") + dataAdd);
        config.commands = new File(params.getOrDefault("commands", folderBase + "/commands.txt"));

        if (!config.dataFolder.exists() && !config.dataFolder.mkdirs()){
            throw new RuntimeException("Unable to arrange data folder");
        }

        preStartup.setup(new OrbitronSetup(), new SDRSetup());

        sdrSharp = Runtime.getRuntime().exec(folderBase + "/SDR/sdrsharp/SDRSharp.exe");
        orbitron = Runtime.getRuntime().exec(folderBase + "/SDR/orbitron/Orbitron.exe Force");

        startup = preStartup;
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            exit();
        });
        Runtime.getRuntime().addShutdownHook(new Thread(Main::exit));
        new Thread(new Checker(sdrSharp)).start();
        new Thread(new Checker(orbitron)).start();
    }

    private static Process sdrSharp, orbitron;
    private static File configFile;
    private static Config config;
    private static boolean exited = false;
    private static PreStartup startup;

    private static void exit(){
        if (exited) {
            return;
        }
        exited = true;
        try {
            if (!configFile.exists()) {
                ClassProperties.writeProperties(config, configFile, "Isolated SDR Tracker Config File");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sdrSharp.isAlive()) {
            sdrSharp.destroy();
        }
        if (orbitron.isAlive()) {
            orbitron.destroy();
        }
        try {
            startup.clearWorkingDir();
        } catch (IOException e) {
            //
        }
        System.exit(0);
    }

    private static class Checker implements Runnable {

        private Checker(Process monitor){
            this.monitor = monitor;
        }

        private final Process monitor;

        @Override
        public void run() {
            try {
                monitor.waitFor();
            } catch (Exception e){
                //
            }
            exit();
        }

    }

}
