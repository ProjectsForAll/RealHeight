package host.plas.realheight.events;

import gg.drak.thebase.events.BaseEventHandler;
import host.plas.bou.events.ListenerConglomerate;
import host.plas.realheight.RealHeight;
import org.bukkit.Bukkit;

public class AbstractConglomerate implements ListenerConglomerate {
    public AbstractConglomerate() {
        register();
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, RealHeight.getInstance());
        BaseEventHandler.bake(this, RealHeight.getInstance());
        RealHeight.getInstance().logInfo("Registered listeners for: &c" + this.getClass().getSimpleName());
    }
}
