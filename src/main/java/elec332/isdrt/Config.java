package elec332.isdrt;

import elec332.isdrt.util.ClassProperties;

import java.io.File;

/**
 * Created by Elec332 on 5-10-2019
 */
public class Config {

    @ClassProperties.PropertyData(canBeImported = true)
    public String TLEs = "weather";

    //SDR
    @ClassProperties.PropertyData(canBeImported = true)
    public String spyServer_uri = "spyserver_uri";

    @ClassProperties.PropertyData(canBeImported = true)
    public String outputDevice = "output_device";

    //Orbitron
    @ClassProperties.PropertyData(canBeImported = true)
    public String locking_sat = "40732"; //40732

    public String tracking_sat = "25338"; //25338;28654;33591;44387;40732

    public String downlink = "137.621";

    public String bandwidth = "120000";

    public transient int lock_sat_id = -1;
    public transient int track_sat_id = -1;
    public transient int downlinkI = -1;

    public transient File dataFolder;
    public transient File commands;

    public void initialize(){
        if (downlinkI < 0) {
            String mhz;
            String low;
            if (!downlink.contains(".")) {
                if (downlink.length() > 6){
                    low = downlink.substring(downlink.length() - 6);
                    mhz = downlink.substring(0, downlink.length() - 6);
                } else {
                    StringBuilder p1Builder = new StringBuilder(downlink);
                    while (p1Builder.length() < 6) {
                        p1Builder.insert(0, "0");
                    }
                    low = p1Builder.toString();
                    mhz = "0";
                }
            } else {
                String[] split = downlink.split("\\.");
                mhz = split[0];
                while (split[1].length() < 6) {
                    split[1] += "0";
                }
                low = split[1];
            }
            downlink = mhz + "." + low;
            downlinkI = Integer.parseInt(mhz + low);
        }
    }

    public int getDownLinkFrequency(){
        if (downlinkI < 0){
            throw new RuntimeException();
        }
        return downlinkI;
    }

    /*
    [Radio]
44387_dn=137.900000
44387_dm=FM-W
40069_dn=137.100000
40069_dm=FM-W
33591_dn=137.100000
33591_dm=FM-W
25338_dn=137.621000
25338_dm=FM-W
28654_dn=137.912500
43770_dn=137.100000
43770_dm=FM-W
39260_dn=137.100000
39260_dm=FM-W
     */
}
