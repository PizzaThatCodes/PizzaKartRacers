package me.pizzathatcodes.pizzakartracers.queue_logic.events;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.queue_logic.classes.QueuePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {

        Player player = e.getPlayer();

        QueuePlayer queuePlayer = null;
        for(QueuePlayer p : Main.getQueuePlayerBoards()) {
            if(p.getUuid().equals(player.getUniqueId())) {
                queuePlayer = p;
                break;
            }
        }

        if(queuePlayer != null) {
            queuePlayer.getBoard().delete();
            Main.getQueuePlayerBoards().remove(queuePlayer);
        }

    }

}
