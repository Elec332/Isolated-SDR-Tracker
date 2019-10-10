package elec332.isdrt.setup;

import elec332.isdrt.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 6-10-2019
 */
public class SDRSetup extends AbstractSetup {

    @Override
    public void setup(File workFolder, Config config) throws IOException {
        File sdrFolder = new File(workFolder, "sdrsharp");
        File mainConfig = new File(sdrFolder, "SDRSharp.exe.Config");
        setupMainSDRConfig(mainConfig, config.dataFolder, config);
        File ddeConfig = new File(sdrFolder, "DDESchedule.xml");
        writeSchedule(ddeConfig, config.commands, config);
    }

    private void setupMainSDRConfig(File file, File dataFolder, Config config) throws IOException {
        mapFile(file, map -> {
            map.put("spyserver.uri",            "    <add key=\"spyserver.uri\" value=\"" + config.spyServer_uri + "\" />");
            map.put("outputDevice",             "    <add key=\"outputDevice\" value=\"" + config.outputDevice + "\" />");
            map.put("AudioRecorderFileName",    "    <add key=\"AudioRecorderFileName\" value=\"date&quot;_&quot;time&quot;_" + config.tracking_sat.replace(" ", "_") + "&quot;\" />");
            map.put("WriteFolder",              "    <add key=\"WriteFolder\" value=\"" + dataFolder.getAbsolutePath() + "\" />");
            map.put("QPSKRecorderPath",         "    <add key=\"QPSKRecorderPath\" value=\"" + dataFolder.getAbsolutePath() + "\" />");
            map.put("WriteOneFile",             "    <add key=\"WriteOneFile\" value=\"True\" />");
            map.put("QPSKFileEnabled",          "    <add key=\"QPSKFileEnabled\" value=\"True\" />");
            map.put("QPSKTrackingEnable",       "    <add key=\"QPSKTrackingEnable\" value=\"True\" />");
            map.put("DDESchedulerEnable",       "    <add key=\"DDESchedulerEnable\" value=\"True\" />");
        });
    }

    private void writeSchedule(File ddeConfig, File commandsFile, Config config) throws IOException {
        int centerFreq = 137500000;
        final int dlF = config.getDownLinkFrequency();
        if (dlF >= centerFreq + 300000){
            centerFreq = dlF - 300000;
        } else if (dlF <= centerFreq - 300000){
            centerFreq = dlF + 300000;
        }
        final int cf = centerFreq;
        BufferedReader reader = new BufferedReader(new FileReader(commandsFile));
        List<String> l = reader.lines()
                .map(s -> s.replace("<", "&lt;").replace(">", "&gt;").trim())
                .collect(Collectors.toList());
        reader.close();
        Iterator<String> commands = l.iterator();
        writeToFile(ddeConfig, writer -> {
            writer.accept("<?xml version=\"1.0\"?>");
            writer.accept("<ArrayOfSchedulerEntry xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
            writer.accept("  <SchedulerEntry>");
            writer.accept("    <SatName>" + config.locking_sat.replace(" ", "_") + "</SatName>");
            writer.accept("    <OnCommands>");
            writer.accept("      <string>radio_Start</string>");
            writer.accept("    </OnCommands>");
            writer.accept("    <OffComands />");
            writer.accept("  </SchedulerEntry>");

            writer.accept("  <SchedulerEntry>");
            writer.accept("    <SatName>" + config.tracking_sat.replace(" ", "_") + "</SatName>");
            writer.accept("    <OnCommands>");
            writer.accept("      <string>radio_center_frequency_Hz&lt;" + cf + "&gt;</string>");
            writer.accept("      <string>radio_frequency_Hz&lt;" + dlF + "&gt;</string>");
            writer.accept("      <string>radio_modulation_type&lt;wfm&gt;</string>");
            writer.accept("      <string>radio_bandwidth_Hz&lt;" + Integer.parseInt(config.bandwidth) + "&gt;</string>");
            writer.accept("      <string>radio_Start</string>");

            String cmd;
            cmd = commands.hasNext() ? commands.next() : "";
            while (!cmd.trim().equals("")){
                writer.accept("      <string>" + cmd + "</string>");
                if (!commands.hasNext()){
                    break;
                }
                cmd = commands.next();
            }

            writer.accept("    </OnCommands>");
            writer.accept("    <OffComands>");

            cmd = commands.hasNext() ? commands.next() : "";
            while (!cmd.trim().equals("")){
                writer.accept("      <string>" + cmd + "</string>");
                if (!commands.hasNext()){
                    break;
                }
                cmd = commands.next();
            }

            writer.accept("      <string>radio_Stop</string>");
            writer.accept("    </OffComands>");
            writer.accept("  </SchedulerEntry>");
            writer.accept("</ArrayOfSchedulerEntry>");
        });
    }

}
