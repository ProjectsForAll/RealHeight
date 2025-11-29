package host.plas.realheight.config;

import gg.drak.thebase.storage.resources.flat.simple.SimpleConfiguration;
import host.plas.realheight.RealHeight;

public class MainConfig extends SimpleConfiguration {
    public MainConfig() {
        super("config.yml", RealHeight.getInstance(), false);
    }

    @Override
    public void init() {

    }
}
