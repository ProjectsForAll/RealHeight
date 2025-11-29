package host.plas.realheight.events.own;

import gg.drak.thebase.events.components.BaseEvent;
import host.plas.bou.BukkitOfUtils;
import host.plas.realheight.RealHeight;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OwnEvent extends BaseEvent {
    public OwnEvent() {
        super();
    }

    public RealHeight getPlugin() {
        return RealHeight.getInstance();
    }

    public BukkitOfUtils getBou() {
        return BukkitOfUtils.getInstance();
    }
}
