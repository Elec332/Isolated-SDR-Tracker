package elec332.isdrt.util;

import elec332.isdrt.Config;

import java.io.File;
import java.io.IOException;

/**
 * Created by Elec332 on 6-10-2019
 */
public interface ISetup {

    public void setup(File workFolder, Config config) throws IOException;

}
