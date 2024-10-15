package me.pizzathatcodes.pizzakartracers.events;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUniqueId());
        if(gamePlayer == null) return;
        gamePlayer.getKart().getKartEntity().remove();
        gamePlayer.getKart().stopAllTasks();
        Main.getGame().removePlayer(gamePlayer);
    }

}
