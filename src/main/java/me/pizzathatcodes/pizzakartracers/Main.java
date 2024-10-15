package me.pizzathatcodes.pizzakartracers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.pizzathatcodes.pizzakartracers.commands.getKartCommand;
import me.pizzathatcodes.pizzakartracers.events.DriftHandler;
import me.pizzathatcodes.pizzakartracers.events.PlayerKartMove;
import me.pizzathatcodes.pizzakartracers.events.PlayerLeaveEvent;
import me.pizzathatcodes.pizzakartracers.game_logic.Game;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.utils.configManager;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static Game game;

    public static Main getInstance() {
        return instance;
    }

    public static Game getGame() {
        return game;
    }

    @Override
    public void onEnable() {
        instance = this;

        util.setMessageFile(new configManager("messages.yml"));
        if(!util.getMessageFile().getConfigFile().exists())
            Main.getInstance().saveResource("messages.yml", false);

        util.getMessageFile().updateConfig(Arrays.asList());
        util.getMessageFile().saveConfig();
        util.getMessageFile().reloadConfig();

        getCommand("getkart").setExecutor(new getKartCommand());

        getServer().getPluginManager().registerEvents(new PlayerKartMove(), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);
        getServer().getPluginManager().registerEvents(new DriftHandler(), this);

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

        getLogger().info("FormulaKartRacers has been enabled!");
    }

    @Override
    public void onDisable() {
        while (Main.getGame().getPlayers().size() > 0) {
            GamePlayer gamePlayer = Main.getGame().getPlayers().get(0);
            Bukkit.getPlayer(gamePlayer.getUuid()).eject();
            Bukkit.getPlayer(gamePlayer.getUuid()).leaveVehicle();
            gamePlayer.getKart().getKartEntity().remove();
            gamePlayer.getKart().stopAllTasks();
            Main.getGame().removePlayer(gamePlayer);
            getLogger().info("Removed player " + gamePlayer.getUuid());
        }
        getLogger().info("FormulaKartRacers has been disabled!");
        game = null;



    }
}
