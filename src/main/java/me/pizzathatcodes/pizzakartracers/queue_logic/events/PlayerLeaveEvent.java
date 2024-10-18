package me.pizzathatcodes.pizzakartracers.queue_logic.events;

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

        GamePlayer queuePlayer = Main.getGame().getGamePlayer(player.getUniqueId());

        if(queuePlayer != null) {
            queuePlayer.getBoard().delete();
        }

        Main.getGame().removePlayer(queuePlayer);
        Main.getQueue().getPlayers().remove(player);

    }

}
