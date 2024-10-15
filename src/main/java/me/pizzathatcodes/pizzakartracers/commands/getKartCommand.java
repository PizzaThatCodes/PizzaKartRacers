package me.pizzathatcodes.pizzakartracers.commands;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class getKartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {


        if(!(sender instanceof Player)) {
            sender.sendMessage(util.translate(util.getMessageFile().getConfig().getString("general.no-console")
                    .replace("%prefix%", util.getMessageFile().getConfig().getString("general.prefix"))));
            return true;
        }

        Player player = (Player) sender;

        if(Main.getGame().getPlayers().stream().anyMatch(gamePlayer -> gamePlayer.getUuid().equals(player.getUniqueId()))) {
            player.sendMessage(util.translate(util.getMessageFile().getConfig().getString("game.already-in-game")
                    .replace("%prefix%", util.getMessageFile().getConfig().getString("general.prefix"))));
            return true;
        }

        GamePlayer gamePlayer = new GamePlayer(
                player.getUniqueId(),
                new Kart(0,
                        0,
                        0)
        );

        Main.getGame().addPlayer(gamePlayer);

        gamePlayer.createKart();


        return true;
    }
}
