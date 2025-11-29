package host.plas.realheight.timers;

import host.plas.bou.scheduling.BaseRunnable;
import host.plas.realheight.data.PlayerData;
import host.plas.realheight.data.PlayerManager;
import org.bukkit.Bukkit;

public class HeightTimer extends BaseRunnable {
    public HeightTimer() {
        super(20L, 5L);
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerData data = PlayerManager.getOrCreatePlayer(player);
            data.whenLoaded(PlayerData::setAsScale);
        });
    }
}
