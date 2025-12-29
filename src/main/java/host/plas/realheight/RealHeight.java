package host.plas.realheight;

import host.plas.bou.BetterPlugin;
import host.plas.realheight.commands.HeightCMD;
import host.plas.realheight.config.DatabaseConfig;
import host.plas.realheight.config.MainConfig;
import host.plas.realheight.data.PlayerData;
import host.plas.realheight.data.PlayerManager;
import host.plas.realheight.database.MainOperator;
import host.plas.realheight.events.MainListener;
import host.plas.realheight.timers.HeightTimer;
import host.plas.realheight.utils.HeightMaths;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

@Getter @Setter
public final class RealHeight extends BetterPlugin {
    @Getter @Setter
    private static RealHeight instance;
    @Getter @Setter
    private static MainConfig mainConfig;
    @Getter @Setter
    private static DatabaseConfig databaseConfig;

    @Getter @Setter
    private static MainOperator database;

    @Getter @Setter
    private static MainListener mainListener;

    @Getter @Setter
    private static HeightCMD heightCMD;

    @Getter @Setter
    private static HeightTimer heightTimer;

    public RealHeight() {
        super();
    }

    @Override
    public void onBaseEnabled() {
        // Plugin startup logic
        setInstance(this);

        setMainConfig(new MainConfig());

        if (! getMainConfig().isUseApi()) {
            setDatabaseConfig(new DatabaseConfig());

            setDatabase(new MainOperator());
        }

        setMainListener(new MainListener());

        setHeightCMD(new HeightCMD());

        setHeightTimer(new HeightTimer());
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic
        if (getHeightTimer() != null) {
            getHeightTimer().cancel();
        }

        PlayerManager.getLoadedPlayers().forEach(PlayerData::saveAndUnload);

        Bukkit.getOnlinePlayers().forEach(player -> {
            AttributeInstance ai = player.getAttribute(Attribute.SCALE);
            ai.setBaseValue(HeightMaths.DEFAULT_SCALE);
        });
    }
}
