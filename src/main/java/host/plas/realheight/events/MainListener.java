package host.plas.realheight.events;

import host.plas.realheight.data.PlayerData;
import host.plas.realheight.data.PlayerManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MainListener extends AbstractConglomerate {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerData data = PlayerManager.getOrCreatePlayer(player);
        data.whenLoaded(PlayerData::setAsScale);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PlayerData data = PlayerManager.getOrCreatePlayer(player);
        data.saveAndUnload();
    }
}
