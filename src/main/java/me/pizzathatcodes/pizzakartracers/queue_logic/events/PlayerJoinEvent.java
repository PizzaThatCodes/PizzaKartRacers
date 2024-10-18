package me.pizzathatcodes.pizzakartracers.queue_logic.events;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.objects.ServerObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import fr.mrmicky.fastboard.FastBoard;
import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.queue_logic.Queue;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.slimeworksapi.database.model.Games_Running;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinEvent implements Listener {

    @EventHandler
    public void onPrePlayerJoin(AsyncPlayerPreLoginEvent event) {
        ServerObject server = TimoCloudAPI.getBukkitAPI().getThisServer();
        Games_Running games_running = Main.getSlimeworksAPI().getGameRunningDatabase().findGameDataByID(server.getName());

        if(games_running.getStatus().equalsIgnoreCase("running") && !games_running.getPlayers().contains(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "You do not have permission to join!");
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {

        Player player = e.getPlayer();

        ServerObject server = TimoCloudAPI.getBukkitAPI().getThisServer();
        Games_Running games_running = Main.getSlimeworksAPI().getGameRunningDatabase().findGameDataByID(server.getName());

        if(games_running.getStatus().equalsIgnoreCase("waiting")) {
            games_running.addPlayer(player.getUniqueId());
            Main.getSlimeworksAPI().getGameRunningDatabase().updateInformation(games_running);
            Main.map.teleportPlayerToWaitingRoom(player);
            Main.getQueue().addPlayer(player);


            new BukkitRunnable() {
                @Override
                public void run() {
                    FastBoard board = new FastBoard(player) {
                        @Override
                        public boolean hasLinesMaxLength() {
                            return Via.getAPI().getPlayerVersion(getPlayer()) < ProtocolVersion.v1_13.getVersion();
                        }
                    };
                    board.updateTitle(util.translate("&e&lPizza Kart Racers"));


                    GamePlayer gamePlayer = new GamePlayer(
                            player.getUniqueId(),
                            new Kart(0,
                                    0,
                                    0),
                            board
                    );

                    Main.getGame().addPlayer(gamePlayer);

                    gamePlayer.createKart();



                }
            }.runTaskLater(Main.getInstance(), 2L);


            e.setJoinMessage(util.translate(player.getName() + "&e has joined (&b" + Main.getQueue().getPlayers().size() + "&e/&b12&e)"));

        }


    }

}
