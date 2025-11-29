package host.plas.realheight.database;

import gg.drak.thebase.async.AsyncUtils;
import host.plas.bou.sql.DBOperator;
import host.plas.bou.sql.DatabaseType;
import host.plas.realheight.RealHeight;
import host.plas.realheight.data.PlayerData;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class MainOperator extends DBOperator {
    public MainOperator() {
        super(RealHeight.getDatabaseConfig().getConnectorSet(), RealHeight.getInstance());
    }

    @Override
    public void ensureTables() {
        String s1 = Statements.getStatement(Statements.StatementType.CREATE_TABLES, getConnectorSet());

        execute(s1, stmt -> {});
    }

    @Override
    public void ensureDatabase() {
        String s1 = Statements.getStatement(Statements.StatementType.CREATE_DATABASE, getConnectorSet());

        execute(s1, stmt -> {});
    }

    public void putPlayer(PlayerData playerData) {
        putPlayer(playerData, true);
    }

    public void putPlayer(PlayerData playerData, boolean async) {
        if (async) {
            putPlayerThreaded(playerData);
        } else {
            putPlayerThreaded(playerData).join();
        }
    }

    public CompletableFuture<Void> putPlayerThreaded(PlayerData playerData) {
        return AsyncUtils.executeAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PUSH_PLAYER_MAIN, getConnectorSet());

            execute(s1, stmt -> {
                try {
                    stmt.setString(1, playerData.getIdentifier());
                    stmt.setDouble(2, playerData.getScale());

                    if (getType() == DatabaseType.MYSQL) {
                        stmt.setDouble(3, playerData.getScale());
                    }
                } catch (Throwable e) {
                    RealHeight.getInstance().logWarning("Failed to set values for statement: " + s1, e);
                }
            });
        });
    }

    public CompletableFuture<Optional<PlayerData>> pullPlayerThreaded(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PULL_PLAYER_MAIN, getConnectorSet());

            AtomicReference<Optional<PlayerData>> ref = new AtomicReference<>(Optional.empty());

            executeQuery(s1, stmt -> {
                try {
                    stmt.setString(1, uuid);
                } catch (Throwable e) {
                    RealHeight.getInstance().logWarning("Failed to set values for statement: " + s1, e);
                }
            }, rs -> {
                try {
                    if (rs.next()) {
                        double scale = rs.getDouble("Scale");

                        PlayerData playerData = new PlayerData(uuid, scale);
                        ref.set(Optional.of(playerData));
                    }
                } catch (Throwable e) {
                    RealHeight.getInstance().logWarning("Failed to get values from result set for statement: " + s1, e);
                }
            });

            return ref.get();
        });
    }
}
