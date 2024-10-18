package me.pizzathatcodes.pizzakartracers.commands;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.queue_logic.Queue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class setTimer implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if(player.getName().equalsIgnoreCase("IPirateMovies")) {
            if(args.length == 0) {
                player.sendMessage("Usage: /setTimer <time>");
                return true;
            }
            if(Main.getQueue() != null) {
                Main.getQueue().timeWaitLeft = Integer.parseInt(args[0]);
            }
            return true;
        } else {
            player.sendMessage("You do not have permission to use this command!");
        }

        return true;
    }
}
