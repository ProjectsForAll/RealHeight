package host.plas.realheight.data;

import gg.drak.thebase.async.AsyncUtils;
import gg.drak.thebase.objects.Identifiable;
import host.plas.realheight.RealHeight;
import host.plas.realheight.events.own.PlayerCreationEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Getter @Setter
public class PlayerData implements Identifiable {
    private String identifier;

    private double scale;

    private AtomicBoolean fullyLoaded;

    public PlayerData(String identifier, double scale) {
        this.identifier = identifier;

        this.scale = scale;

        this.fullyLoaded = new AtomicBoolean(false);
    }

    public PlayerData(String uuid) {
        this(uuid, 1.0d);
    }

    public PlayerData(Player player) {
        this(player.getUniqueId().toString());
    }

    public Optional<Player> asPlayer() {
        try {
            return Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(identifier)));
        } catch (Throwable e) {
            RealHeight.getInstance().logWarning("Failed to get player from identifier: " + identifier, e);

            return Optional.empty();
        }
    }

    public Optional<OfflinePlayer> asOfflinePlayer() {
        try {
            return Optional.of(Bukkit.getOfflinePlayer(UUID.fromString(identifier)));
        } catch (Throwable e) {
            RealHeight.getInstance().logWarning("Failed to get offline player from identifier: " + identifier, e);

            return Optional.empty();
        }
    }

    public boolean isOnline() {
        return asPlayer().isPresent();
    }

    public void load() {
        PlayerManager.loadPlayer(this);
    }

    public void unload() {
        PlayerManager.unloadPlayer(this);
    }

    public void save() {
        PlayerManager.savePlayer(this);
    }

    public void save(boolean async) {
        PlayerManager.savePlayer(this, async);
    }

    public void augment(CompletableFuture<Optional<PlayerData>> future, boolean isGet) {
        fullyLoaded.set(false);

        future.whenComplete((data, error) -> {
            if (error != null) {
                RealHeight.getInstance().logWarning("Failed to augment player data", error);

                this.fullyLoaded.set(true);
                return;
            }

            if (data.isPresent()) {
                PlayerData newData = data.get();

                this.scale = newData.getScale();
            } else {
                if (! isGet) {
                    new PlayerCreationEvent(this).fire();
                    this.save();
                }
            }

            this.fullyLoaded.set(true);
        });
    }

    public boolean isFullyLoaded() {
        return fullyLoaded.get();
    }

    public void saveAndUnload(boolean async) {
        save(async);
        unload();
    }

    public void saveAndUnload() {
        saveAndUnload(true);
    }

    public PlayerData waitUntilFullyLoaded() {
        while (! isFullyLoaded()) {
            Thread.onSpinWait();
        }
        return this;
    }

    public void whenLoaded(Consumer<PlayerData> consumer) {
        AsyncUtils.executeAsync(() -> {
            waitUntilFullyLoaded();
            consumer.accept(this);
        });
    }

    public void setAsScale() {
        asPlayer().ifPresent(player -> {
            AttributeInstance instance = player.getAttribute(Attribute.SCALE);
            if (instance != null) {
                if (instance.getBaseValue() == this.getScale()) return;

                instance.setBaseValue(this.getScale());
            }
        });
    }
}
