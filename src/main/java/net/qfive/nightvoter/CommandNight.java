package net.qfive.nightvoter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandNight implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {



        String prefix = ChatColor.translateAlternateColorCodes('&', NightVoter.plugin.getConfig().getString("message_prefix") + " ");
        boolean displayWorldNameEnabled = NightVoter.plugin.getConfig().getBoolean("display_world_name");

        if (sender instanceof Player && args.length == 0) {

            Player player = (Player) sender; // Get player object
            //String world = player.getWorld().getName(); // Get current world object

            int result = NightVoter.addVote(player, prefix, displayWorldNameEnabled);

            if (result == 1) {
                return true;
            } else if (result == 2) {
                return true;
            } else if (result == 3) {
                return true;
            } else if (result == 4) {
                return true;
            } else {
                player.sendMessage(prefix + ChatColor.RED + "An unexpected error has occurred. Please report this to a server administrator.");
                return false;
            }



        } else if (sender instanceof Player && args.length >= 1 && args[0].equalsIgnoreCase("test")) {

            sender.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "test");
            return true;

        } else if (sender instanceof Player && args.length >= 1 ) {

            sender.sendMessage(prefix + ChatColor.WHITE + "Unknown command");
            return false;

        } else {

            // Say this command can only be used in game
            sender.sendMessage(prefix + ChatColor.WHITE + "This command can only be used in game.");
            return false;

        }


    }

}

