package me.pizzathatcodes.pizzakartracers;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.objects.ServerGroupObject;
import cloud.timo.TimoCloud.api.objects.ServerObject;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.pizzathatcodes.pizzakartracers.commands.getKartCommand;
import me.pizzathatcodes.pizzakartracers.events.DriftHandler;
import me.pizzathatcodes.pizzakartracers.events.PlayerLeaveEvent;
import me.pizzathatcodes.pizzakartracers.game_logic.Game;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.queue_logic.Queue;
import me.pizzathatcodes.pizzakartracers.queue_logic.classes.QueuePlayer;
import me.pizzathatcodes.pizzakartracers.startup_logic.mapSystem;
import me.pizzathatcodes.pizzakartracers.utils.configManager;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.slimeworksapi.SlimeWorksAPI;
import net.slimeworksapi.database.model.Games_Running;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static Game game;

    private static boolean disabling = false;

    private static ArrayList<QueuePlayer> queuePlayerBoards = new ArrayList<>();

    private static SlimeWorksAPI slimeworksAPI;

    /**
     * @return The Slimeworks API instance
     */
    public static SlimeWorksAPI getSlimeworksAPI() {
        return slimeworksAPI;
    }

    public static ArrayList<QueuePlayer> getQueuePlayerBoards() {
        return queuePlayerBoards;
    }

    public static Main getInstance() {
        return instance;
    }

    public static Game getGame() {
        return game;
    }

    public static Queue queue;

    public static Queue getQueue() {
        return queue;
    }

    public static mapSystem map;

    @Override
    public void onEnable() {
        instance = this;

        util.setConfigFile(new configManager("config.yml"));
        if(!util.getConfigFile().getConfigFile().exists())
            Main.getInstance().saveResource("config.yml", false);

        util.getConfigFile().updateConfig(Arrays.asList());
        util.getConfigFile().saveConfig();
        util.getConfigFile().reloadConfig();

        util.setMessageFile(new configManager("messages.yml"));
        if(!util.getMessageFile().getConfigFile().exists())
            Main.getInstance().saveResource("messages.yml", false);

        util.getMessageFile().updateConfig(Arrays.asList());
        util.getMessageFile().saveConfig();
        util.getMessageFile().reloadConfig();

        util.setMapFile(new configManager("maps.yml"));
        if(!util.getMapFile().getConfigFile().exists())
            Main.getInstance().saveResource("maps.yml", false);

        util.getMapFile().updateConfig(Arrays.asList());
        util.getMapFile().saveConfig();
        util.getMapFile().reloadConfig();

        slimeworksAPI = new SlimeWorksAPI(this);

        getCommand("getkart").setExecutor(new getKartCommand());

        getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);
        getServer().getPluginManager().registerEvents(new DriftHandler(), this);

        queue = new Queue();
        getQueue().registerQueueEvents();

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
            int delay = 0;
            int highDelay = 0;
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player.getVehicle() instanceof ArmorStand) {
                    float forward = event.getPacket().getFloat().read(1);
                    float sideways = event.getPacket().getFloat().read(0);
                    GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUniqueId());
                    if(gamePlayer == null) return;
                    if(sideways != 0) {
                        float newSideways = sideways < 0 ? -1f : 1f;
                        float newYaw = gamePlayer.getKart().getKartEntity().getLocation().getYaw() + (newSideways * -7.5f);  // Adjust yaw based on sideways input
                        gamePlayer.getKart().yaw = newYaw;
//                        player.sendMessage("Yaw: " + newYaw);
                        if(gamePlayer.getKart().rotationTask == null) {
                            gamePlayer.getKart().rotationTask = new BukkitRunnable() {
                                @Override
                                public void run() {
//                                    player.sendMessage("Yaw: " + gamePlayer.getKart().yaw);
                                    gamePlayer.getKart().getKartEntity().setRotation(gamePlayer.getKart().yaw, 0);  // Rotate the armor stand
                                }
                            }.runTaskTimer(Main.getInstance(), 0, 1);
                        }
                    }
                    util.handleSidewayMovement(player, sideways);

                    // Handle forward movement (acceleration)
                    if (forward > 0) { // Going forward
                        if(delay == 0 && gamePlayer.getKart().getAcceleration() > 45) {
                            delay = 2;
                        } else if (delay > 0 && gamePlayer.getKart().getAcceleration() > 45) {
                            delay--;
                            return;
                        }
                        if (gamePlayer.getKart().getAcceleration() < 63 && gamePlayer.getKart().getAcceleration() < 60) {
                            gamePlayer.getKart().setAcceleration(gamePlayer.getKart().getAcceleration() + 2);
                        } else if(gamePlayer.getKart().getAcceleration() < 63 && gamePlayer.getKart().getAcceleration() >= 60) {
                            if(highDelay == 0 && gamePlayer.getKart().getAcceleration() > 60) {
                                highDelay = 2;
                            } else if (highDelay > 0 && gamePlayer.getKart().getAcceleration() > 60) {
                                highDelay--;
                                return;
                            }
                            gamePlayer.getKart().setAcceleration(gamePlayer.getKart().getAcceleration() + 1);
                        }
                        // If player is moving, cancel any existing deceleration task
                        if (gamePlayer.getKart().accelerationTask != null) {
                            gamePlayer.getKart().accelerationTask.cancel();
                            gamePlayer.getKart().accelerationTask = null;
                        }
                    } else if (forward < 0) { // Going in reverse
                        if(delay == 0 && gamePlayer.getKart().getAcceleration() < -50) {
                            delay = 2;
                        } else if (delay > 0 && gamePlayer.getKart().getAcceleration() < -50) {
                            delay--;
                            return;
                        }
                        if (gamePlayer.getKart().getAcceleration() > -60 && gamePlayer.getKart().getAcceleration() > -60) {
                            gamePlayer.getKart().setAcceleration(gamePlayer.getKart().getAcceleration() - 2); // Decrease acceleration
                        } else if(gamePlayer.getKart().getAcceleration() > -60 && gamePlayer.getKart().getAcceleration() <= -60) {
                            if(highDelay == 0 && gamePlayer.getKart().getAcceleration() < -60) {
                                highDelay = 2;
                            } else if (highDelay > 0 && gamePlayer.getKart().getAcceleration() < -60) {
                                highDelay--;
                                return;
                            }
                            gamePlayer.getKart().setAcceleration(gamePlayer.getKart().getAcceleration() - 1);
                        }
                        // Cancel deceleration task if player is still moving
                        if (gamePlayer.getKart().accelerationTask != null) {
                            gamePlayer.getKart().accelerationTask.cancel();
                            gamePlayer.getKart().accelerationTask = null;
                        }
                    } else { // Player has stopped
                        if (gamePlayer.getKart().accelerationTask == null) { // Only start deceleration if no task is running
                            gamePlayer.getKart().accelerationTask = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (gamePlayer.getKart().getAcceleration() > 0) {
                                        gamePlayer.getKart().setAcceleration(gamePlayer.getKart().getAcceleration() - 3);
                                    } else if (gamePlayer.getKart().getAcceleration() < 0) {
                                        gamePlayer.getKart().setAcceleration(gamePlayer.getKart().getAcceleration() + 3); // Smooth deceleration
                                    }

                                    if (Math.abs(gamePlayer.getKart().getAcceleration()) <= 2) {
                                        gamePlayer.getKart().setAcceleration(0);
                                        cancel(); // Stop the task once the acceleration reaches 0
                                        gamePlayer.getKart().accelerationTask = null;
                                    }
                                }
                            }.runTaskTimer(Main.getInstance(), 0, 1);
                        }
                    }

                }
            }
        });


        game = new Game();

        for(Player player : getServer().getOnlinePlayers()) {
            if(game.getGamePlayer(player.getUniqueId()) != null) continue;
            GamePlayer gamePlayer = new GamePlayer(
                    player.getUniqueId(),
                    new Kart(0,
                            0,
                            0)
            );

            Main.getGame().addPlayer(gamePlayer);

            gamePlayer.createKart();
        }

        if(map == null || !map.isMapLoading()) {
            map = new mapSystem();
            map.loadMap();
        }


        getLogger().info("FormulaKartRacers has been enabled!");


        ServerObject server = TimoCloudAPI.getBukkitAPI().getThisServer();

        Games_Running games_running = new Games_Running(
                server.getName(),
                "pizzakartracers",
                new ArrayList<>(),
                "waiting"
        );

        getSlimeworksAPI().getGameRunningDatabase().createInformation(games_running);
        getLogger().info("Created game data for server " + server.getName());

    }

    public static boolean isDisabling() {
        return disabling;
    }

    @Override
    public void onDisable() {
        disabling = true;
        while (Main.getGame().getPlayers().size() > 0) {
            GamePlayer gamePlayer = Main.getGame().getPlayers().get(0);
            Bukkit.getPlayer(gamePlayer.getUuid()).eject();
            Bukkit.getPlayer(gamePlayer.getUuid()).leaveVehicle();
            gamePlayer.getKart().getKartEntity().remove();
            gamePlayer.getKart().stopAllTasks();
            Main.getGame().removePlayer(gamePlayer);
            getLogger().info("Removed player " + gamePlayer.getUuid());
        }

        for(Player player : Bukkit.getOnlinePlayers()) {
            for(ServerObject goober_lobby : TimoCloudAPI.getUniversalAPI().getServerGroup("Lobby").getServers()) {
                if(goober_lobby.getOnlinePlayerCount() < goober_lobby.getMaxPlayerCount()) {
                    TimoCloudAPI.getUniversalAPI().getPlayer(player.getUniqueId()).sendToServer(goober_lobby);
                    break;
                }
            }
        }

        ServerObject server = TimoCloudAPI.getBukkitAPI().getThisServer();
        Games_Running games_running = getSlimeworksAPI().getGameRunningDatabase().findGameDataByID(server.getName());
        if(games_running != null) {
            getSlimeworksAPI().getGameRunningDatabase().deleteInformation(games_running);
        }
        ServerGroupObject pizzakartracers = TimoCloudAPI.getUniversalAPI().getServerGroup("pizzakartracers");
        if(pizzakartracers.getOnlineAmount() > 0)
            pizzakartracers.setOnlineAmount(pizzakartracers.getOnlineAmount() - 1);


        getLogger().info("FormulaKartRacers has been disabled!");
        game = null;



    }
}
