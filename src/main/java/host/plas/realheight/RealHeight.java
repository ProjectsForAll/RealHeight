package host.plas.realheight;

import host.plas.bou.BetterPlugin;
import host.plas.realheight.commands.HeightCMD;
import host.plas.realheight.config.DatabaseConfig;
import host.plas.realheight.config.MainConfig;
import host.plas.realheight.data.PlayerManager;
import host.plas.realheight.database.MainOperator;
import host.plas.realheight.events.MainListener;
import host.plas.realheight.timers.HeightTimer;
import lombok.Getter;
import lombok.Setter;

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
        setInstance(this); // Set the instance of the plugin. // For use in other classes.

        setMainConfig(new MainConfig()); // Instantiate the main config and set it.
        setDatabaseConfig(new DatabaseConfig()); // Instantiate the database config and set it.

        setDatabase(new MainOperator()); // Instantiate the database operator and set it. // Uses the database config.

        setMainListener(new MainListener()); // Instantiate the main listener and set it.

        setHeightCMD(new HeightCMD());

        setHeightTimer(new HeightTimer());
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic
        if (getHeightTimer() != null) {
            getHeightTimer().cancel();
        }

        PlayerManager.getLoadedPlayers().forEach(playerData -> {
            // Save and unload all loaded player data.
            // Saves it in sync (hence the false) so it doesn't lose data.
            playerData.saveAndUnload(false);
        });
    }
}
