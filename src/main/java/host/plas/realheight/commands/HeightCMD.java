package host.plas.realheight.commands;


import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.MathUtils;
import host.plas.realheight.RealHeight;
import host.plas.realheight.data.PlayerData;
import host.plas.realheight.data.PlayerManager;
import host.plas.realheight.utils.HeightMaths;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class HeightCMD extends SimplifiedCommand {
    public HeightCMD() {
        super("height", RealHeight.getInstance());
    }

    @Override
    public boolean command(CommandContext ctx) {
        if (ctx.isConsole()) {
            ctx.sendMessage("&cThis command can only be executed by a player.");
            return false;
        }

        Player player = ctx.getPlayer().orElse(null);
        if (player == null) {
            ctx.sendMessage("&cAn error occurred while trying to execute this command.");
            return false;
        }

        if (! ctx.isArgUsable(0)) {
            ctx.sendMessage("&cUsage: /height <set|get|reset> <height>");
            return false;
        }

        String action = ctx.getStringArg(0).toLowerCase();
        switch (action) {
            case "set":
                return set(ctx, player, ctx.getPlayerArg(2));
            case "get":
                return get(ctx, player, ctx.getPlayerArg(1));
            case "reset":
                return reset(ctx, player, ctx.getPlayerArg(1));
            default:
                ctx.sendMessage("&cUnknown action&8.");
                ctx.sendMessage("&eAvailable actions&8: &aset&7, &aget&7, &areset&7.");
                return false;
        }
    }

    public boolean set(CommandContext ctx, Player player, Optional<Player> other) {
        boolean samePlayer = other.isPresent() && other.get().getUniqueId().equals(player.getUniqueId());

        if (! player.hasPermission(getBasePermission() + ".set")) {
            ctx.sendMessage("&cYou do not have permission to set heights.");
            return false;
        }
        if (! samePlayer) {
            if (! player.hasPermission(getBasePermission() + ".set.others")) {
                ctx.sendMessage("&cYou do not have permission to set other players' heights.");
                return false;
            }
        }

        boolean isCm = false; // true = centimeters, false = feet
        double height = 0;

        String heightArg = ctx.getStringArg(1);
        if (heightArg.equals("?")) {
            ctx.sendMessage("&ePlease provide the height followed by a unit:");
            ctx.sendMessage("&c- &eFor &fcentimeters&7, &euse &a<height>c &for &a<height>m&f (e.g., &a170c &for &a1.7m&f).");
            ctx.sendMessage("&c- &eFor &ffeet&7, &euse &a<height>f &for &a<height>i&f (e.g., &a5.6f &for &a67.2i&f).");
            return true;
        }

        try {
            String heightStr = heightArg;
            if (heightArg.endsWith("c")) {
                isCm = true;
                heightStr = heightArg.substring(0, heightArg.length() - 1);
                height = Double.parseDouble(heightStr);
            } else if (heightArg.endsWith("m")) {
                isCm = true;
                heightStr = heightArg.substring(0, heightArg.length() - 1);
                height = Double.parseDouble(heightStr) * 100.0;
            } else if (heightArg.endsWith("f")) {
                heightStr = heightArg.substring(0, heightArg.length() - 1);
                height = Double.parseDouble(heightStr);
            } else if (heightArg.endsWith("i")) {
                heightStr = heightArg.substring(0, heightArg.length() - 1);
                height = Double.parseDouble(heightStr) / 12.0;
            } else { // Default to feet.
                height = Double.parseDouble(heightStr);
            }
        } catch (NumberFormatException e) {
            ctx.sendMessage("&cInvalid height value. Please provide a valid number.");
            return false;
        }

        double scale = 0.0;
        if (isCm) { // Centimeters
            scale = HeightMaths.getScaleOfCm(height);
        } else { // Feet
            scale = HeightMaths.getScaleOfFt(height);
        }

        if (! samePlayer) {
            Player otherPlayer = other.get();

            PlayerData data = PlayerManager.getOrCreatePlayer(otherPlayer);
            data.setScale(scale);
            data.save();

            data.setAsScale();

            ctx.sendMessage(otherPlayer.getDisplayName() + "&e'&7s height has been set to &a" + (isCm ? String.format("%.2f &fcm", height) : String.format("%.2f &fft", height)) + " &7(&bScale&7: &a" + String.format("%.4f", scale) + "&7)");
        } else {
            PlayerData data = PlayerManager.getOrCreatePlayer(player);
            data.setScale(scale);
            data.save();

            data.setAsScale();

            ctx.sendMessage("&eYour height has been set to &a" + (isCm ? String.format("%.2f &fcm", height) : String.format("%.2f &fft", height)) + " &7(&bScale&7: &a" + String.format("%.4f", scale) + "&7)");
        }

        return true;
    }

    public boolean get(CommandContext ctx, Player player, Optional<Player> other) {
        boolean samePlayer = other.isPresent() && other.get().getUniqueId().equals(player.getUniqueId());

        if (! player.hasPermission(getBasePermission() + ".get")) {
            ctx.sendMessage("&cYou do not have permission to get heights.");
            return false;
        }
        if (! samePlayer) {
            if (! player.hasPermission(getBasePermission() + ".get.others")) {
                ctx.sendMessage("&cYou do not have permission to get other players' heights.");
                return false;
            }
        }

        if (! samePlayer) {
            Player otherPlayer = other.get();

            PlayerData data = PlayerManager.getOrCreatePlayer(otherPlayer);
            double scale = data.getScale();
            double heightCm = HeightMaths.getCmOfScale(scale);
            double heightFt = HeightMaths.getFtOfScale(scale);

            ctx.sendMessage(otherPlayer.getDisplayName() + "&7'&es current height is &a" + String.format("%.2f &fcm", heightCm) + " &7(&a" + String.format("%.2f &fft", heightFt) + "&7)&8. (&bScale&7: &a" + String.format("%.4f", scale) + "&7)");
        } else {
            PlayerData data = PlayerManager.getOrCreatePlayer(player);
            double scale = data.getScale();
            double heightCm = HeightMaths.getCmOfScale(scale);
            double heightFt = HeightMaths.getFtOfScale(scale);

            ctx.sendMessage("&eYour current height is &a" + String.format("%.2f &fcm", heightCm) + " &7(&a" + String.format("%.2f &fft", heightFt) + "&7)&8. (&bScale&7: &a" + String.format("%.4f", scale) + "&7)");
        }

        return true;
    }

    public boolean reset(CommandContext ctx, Player player, Optional<Player> other) {
        boolean samePlayer = other.isPresent() && other.get().getUniqueId().equals(player.getUniqueId());

        if (! player.hasPermission(getBasePermission() + ".reset")) {
            ctx.sendMessage("&cYou do not have permission to reset heights.");
            return false;
        }
        if (! samePlayer) {
            if (! player.hasPermission(getBasePermission() + ".reset.others")) {
                ctx.sendMessage("&cYou do not have permission to reset other players' heights.");
                return false;
            }
        }

        if (! samePlayer) {
            Player otherPlayer = other.get();

            PlayerData data = PlayerManager.getOrCreatePlayer(otherPlayer);
            data.setScale(HeightMaths.DEFAULT_SCALE);
            data.save();

            data.setAsScale();

            ctx.sendMessage(otherPlayer.getDisplayName() + "&e'&7s height has been reset to default&8. &7(Scale&7: &a" + String.format("%.4f", HeightMaths.DEFAULT_SCALE) + "&7)");
        } else {
            PlayerData data = PlayerManager.getOrCreatePlayer(player);
            data.setScale(HeightMaths.DEFAULT_SCALE);
            data.save();

            data.setAsScale();

            ctx.sendMessage("&eYour height has been reset to default&8. &7(Scale&7: &a" + String.format("%.4f", HeightMaths.DEFAULT_SCALE) + "&7)");
        }

        return true;
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        return doTabCompletion(ctx);
    }

    public static ConcurrentSkipListSet<String> doTabCompletion(CommandContext ctx) {
        ConcurrentSkipListSet<String> results = new ConcurrentSkipListSet<>();

        if (ctx.getArgCount() <= 1) {
            results.add("set");
            results.add("get");
            results.add("reset");
        }

        if (ctx.getArgCount() == 2) {
            String action = ctx.getStringArg(0).toLowerCase();
            if (action.equals("set")) {
                String partial = ctx.getStringArg(1).toLowerCase();
                results.add("?");

                if (isNumber(partial)) {
                    if (! partial.endsWith("c")) results.add(partial + "c");
                    if (! partial.endsWith("m")) results.add(partial + "m");
                    if (! partial.endsWith("f")) results.add(partial + "f");
                    if (! partial.endsWith("i")) results.add(partial + "i");
                }
            } else if (action.equals("get") || action.equals("reset")) {
                results.addAll(EntityUtils.getOnlinePlayerNames());
            }
        }

        if (ctx.getArgCount() == 3) {
            results.addAll(EntityUtils.getOnlinePlayerNames());
        }

        return results;
    }

    public static boolean isNumber(String str) {
        if (str == null) return false;

        try {
            Double.parseDouble(str);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
