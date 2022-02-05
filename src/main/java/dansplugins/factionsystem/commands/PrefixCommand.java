/*
  Copyright (c) 2022 Daniel McCoy Stephenson
  GPL3 License
 */
package dansplugins.factionsystem.commands;

import dansplugins.factionsystem.commands.abs.SubCommand;
import dansplugins.factionsystem.data.PersistentData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Callum Johnson
 */
public class PrefixCommand extends SubCommand {

    public PrefixCommand() {
        super(new String[] {
                "prefix", LOCALE_PREFIX + "CmdPrefix"
        }, true, true, false, true);
    }

    /**
     * Method to execute the command for a player.
     *
     * @param player who sent the command.
     * @param args   of the command.
     * @param key    of the sub-command (e.g. Ally).
     */
    @Override
    public void execute(Player player, String[] args, String key) {
        final String permission = "mf.prefix";
        if (!(checkPermissions(player, permission))) return;
        final String newPrefix = String.join(" ", args);
        if (PersistentData.getInstance().isPrefixTaken(newPrefix)) {
            player.sendMessage(translate("&c" + getText("PrefixTaken")));
            return;
        }
        faction.setPrefix(newPrefix);
        player.sendMessage(translate("&a" + getText("PrefixSet")));
    }

    /**
     * Method to execute the command.
     *
     * @param sender who sent the command.
     * @param args   of the command.
     * @param key    of the command.
     */
    @Override
    public void execute(CommandSender sender, String[] args, String key) {

    }
}