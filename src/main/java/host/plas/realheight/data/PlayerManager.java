package host.plas.realheight.data;

import gg.drak.thebase.async.AsyncUtils;
import gg.drak.thebase.objects.SingleSet;
import host.plas.bou.utils.UuidUtils;
import host.plas.realheight.RealHeight;
import host.plas.bou.drakapi.DrakAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class PlayerManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<PlayerData> loadedPlayers = new ConcurrentSkipListSet<>();

    public static void loadPlayer(PlayerData player) {
        unloadPlayer(player);

        loadedPlayers.add(player);
    }

    public static void unloadPlayer(String uuid) {
        getLoadedPlayers().removeIf(p -> p.getIdentifier().equalsIgnoreCase(uuid));
    }

    public static void unloadPlayer(PlayerData player) {
        unloadPlayer(player.getIdentifier());
    }

    public static Optional<PlayerData> getPlayer(String uuid) {
        return getLoadedPlayers().stream().filter(p -> p.getIdentifier().equalsIgnoreCase(uuid)).findFirst();
    }

    public static boolean hasPlayer(String uuid) {
        return getPlayer(uuid).isPresent();
    }

    public static void savePlayer(PlayerData player) {
        if (RealHeight.getMainConfig().isUseApi()) {
            AsyncUtils.executeAsync(() -> savePlayerAPIThreaded(player));
        } else {
            RealHeight.getDatabase().putPlayer(player);
        }
    }

    public static void savePlayer(PlayerData player, boolean async) {
        if (RealHeight.getMainConfig().isUseApi()) {
            if (async) {
                AsyncUtils.executeAsync(() -> savePlayerAPIThreaded(player));
            } else {
                savePlayerAPIThreaded(player);
            }
        } else {
            RealHeight.getDatabase().putPlayer(player, async);
        }
    }

    public static void savePlayerAPIThreaded(PlayerData player) {
        SingleSet<Boolean, Double> set = DrakAPI.setScale(player.getIdentifier(), player.getScale());

        boolean success = set.getKey();
        if (! success) {
            RealHeight.getInstance().logWarning("Failed to save scale for player " + player.getIdentifier() + " to DrakAPI.");
        }
    }

    public static PlayerData createPlayer(OfflinePlayer player) {
        return new PlayerData(player);
    }

    public static PlayerData createTemporaryPlayer(String uuid) {
        return new PlayerData(uuid);
    }

    public static PlayerData getOrCreatePlayer(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();

        Optional<PlayerData> data = getPlayer(uuid);
        if (data.isPresent()) return data.get();

        PlayerData d = createPlayer(player);
        d.load();

        if (RealHeight.getMainConfig().isUseApi()) {
            d.augment(doApiCall(uuid), false);
        } else {
            d.augment(RealHeight.getDatabase().pullPlayerThreaded(uuid), false);
        }

        return d;
    }

    public static CompletableFuture<Optional<PlayerData>> doApiCall(String uuid) {
        if (uuid == null) return CompletableFuture.completedFuture(Optional.empty());

        PlayerData data = createTemporaryPlayer(uuid);
        SingleSet<Boolean, Double> set = DrakAPI.getScale(uuid);
        boolean found = set.getKey();
        if (! found) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        double value = set.getValue();

        data.setScale(value);

        return CompletableFuture.completedFuture(Optional.of(data));
    }

    public static Optional<PlayerData> getOrGetPlayer(String uuid) {
        Optional<PlayerData> data = getPlayer(uuid);
        if (data.isPresent()) return data;

        if (! UuidUtils.isValidPlayerUUID(uuid)) return Optional.empty();
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        if (! player.hasPlayedBefore()) return Optional.empty();

        PlayerData d = createTemporaryPlayer(uuid);
        d.load();

        if (RealHeight.getMainConfig().isUseApi()) {
            d.augment(doApiCall(uuid), true);
        } else {
            d.augment(RealHeight.getDatabase().pullPlayerThreaded(uuid), true);
        }

        return Optional.of(d);
    }
}
