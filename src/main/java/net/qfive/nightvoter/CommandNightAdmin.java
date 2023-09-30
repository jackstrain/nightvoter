package net.qfive.nightvoter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
//import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;




public class CommandNightAdmin implements CommandExecutor {

    private NightVoter plugin;


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        /*
        TODO:
        - Reload command
        - See who has voted
        - Force vote
        - End night early

         */

        // Don't think I need this for the admin one
        if (sender instanceof Player) {
            Player player = (Player) sender;

        }

        String prefix = ChatColor.translateAlternateColorCodes('&', NightVoter.plugin.getConfig().getString("message_prefix") + " ");

        if (args.length == 0) {
            // Display help info
            sender.sendMessage(prefix + ChatColor.WHITE + "Admin Commands");
            sender.sendMessage(prefix + ChatColor.WHITE + "/nightadmin reload - Reload config");

        } else if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            // Reload config
            /*if (NightVoter.reloadConf()) {
                sender.sendMessage(prefix + ChatColor.WHITE + "Reload successful");
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "An error occurred trying to reload. Check the server log for details.");
            }*/
            //NightVoter.reloadConf();
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("votelist")) {
            // Vote list
        } else if (args.length == 1 && args[0].equalsIgnoreCase("forcevote")) {
            // Force vote, no world specified
        } else if (args.length == 2 && args[0].equalsIgnoreCase("forcevote")) {
            // Force vote, world specified
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("endnight")) {
            // End night early, no world specified
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("endnight")) {
            // End night early, world specified
        } else {
            // Unknown command, see help for details
        }



        // If the player (or console) uses our command correct, we can return true
        return true;
    }

}
