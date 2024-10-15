package me.pizzathatcodes.pizzakartracers.utils;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class util {

    private static configManager messageFile;

    /**
     * Translate a string with color codes to have colored text (hex supported)
     * @param message returns a colored string using & (hex supported)
     * @return
     */
    public static String translate(String message) {
        // TODO: check if server version is 1.16 or above
        try {
            Method method = Class.forName("net.md_5.bungee.api.ChatColor").getMethod("of", String.class);
            message = message.replaceAll("&#",  "#");
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String color = message.substring(matcher.start(), matcher.end());
                message = message.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
                matcher = pattern.matcher(message);
            }
        } catch (Exception e) {
            // Server version is below 1.16
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }


    /**
     * Get the message file
     * @return messageFile
     */
    public static configManager getMessageFile() {
        return messageFile;
    }

    /**
     * Set the message file
     * @param messageFile
     */
    public static void setMessageFile(configManager messageFile) {
        util.messageFile = messageFile;
    }

    public static void handleSidewayMovement(Player player, float sideways) {
        GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUniqueId());
        if (gamePlayer == null) return;

        ArmorStand armorStand = gamePlayer.getKart().getKartEntity();
        EulerAngle currentHeadPose = armorStand.getHeadPose();
        double currentTilt = currentHeadPose.getZ();  // Get the current tilt in radians

        // Determine if player is turning
        if (sideways != 0) {


            if(gamePlayer.getKart().acceleration > 0) {
                if(gamePlayer.getKart().tiltDelay == 0) {
                    gamePlayer.getKart().tiltDelay = 2;
                    gamePlayer.getKart().acceleration -= 1;
                } else if (gamePlayer.getKart().tiltDelay > 0) {
                    gamePlayer.getKart().tiltDelay--;
                }
            }
            else if(gamePlayer.getKart().acceleration < 0) {
                if(gamePlayer.getKart().tiltDelay == 0) {
                    gamePlayer.getKart().tiltDelay = 2;
                    gamePlayer.getKart().acceleration += 1;
                } else if (gamePlayer.getKart().tiltDelay > 0) {
                    gamePlayer.getKart().tiltDelay--;
                }
            }

            float newSideways = sideways < 0 ? -1f : 1f;

            // Calculate the target tilt based on the direction
            double targetTilt = newSideways < 0 ? Math.toRadians(-15) : Math.toRadians(15);  // Left tilt or right tilt

            // Gradually tilt towards the target
            if (gamePlayer.getKart().tiltTask != null) {
                gamePlayer.getKart().tiltTask.cancel();  // Cancel any existing tilt task
            }

            gamePlayer.getKart().tiltTask = new BukkitRunnable() {
                final double increment = Math.toRadians(2);  // Control how fast the tilt changes
                double currentTilt = armorStand.getHeadPose().getZ();  // Start with the current tilt

                @Override
                public void run() {
                    if (Math.abs(currentTilt - targetTilt) < Math.toRadians(1)) {
                        currentTilt = targetTilt;  // Snap to target tilt when close enough
                        armorStand.setHeadPose(new EulerAngle(0, 0, currentTilt));  // Update head pose
                        cancel();  // Stop the task
                    } else {
                        // Move towards the target tilt gradually
                        if (currentTilt < targetTilt) {
                            currentTilt = Math.min(currentTilt + increment, targetTilt);  // Increase tilt
                        } else {
                            currentTilt = Math.max(currentTilt - increment, targetTilt);  // Decrease tilt
                        }
                        armorStand.setHeadPose(new EulerAngle(0, 0, currentTilt));  // Update head pose
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 2L);  // Run every 2 ticks (adjust as needed)

        } else {
            // If sideways == 0 (no turning), start resetting the tilt back to 0
            if (gamePlayer.getKart().tiltTask != null) {
                gamePlayer.getKart().tiltTask.cancel();  // Cancel any existing tilt task
            }

            gamePlayer.getKart().tiltTask = new BukkitRunnable() {
                final double decrement = Math.toRadians(2);  // Control how fast the tilt returns to zero
                double currentTilt = armorStand.getHeadPose().getZ();  // Start with the current tilt

                @Override
                public void run() {
                    if (Math.abs(currentTilt) < Math.toRadians(1)) {
                        armorStand.setHeadPose(new EulerAngle(0, 0, 0));  // Reset to neutral position
                        cancel();  // Stop the task once it reaches zero
                    } else {
                        // Move back towards zero tilt
                        if (currentTilt > 0) {
                            currentTilt = Math.max(currentTilt - decrement, 0);  // Gradually decrease positive tilt
                        } else {
                            currentTilt = Math.min(currentTilt + decrement, 0);  // Gradually increase negative tilt
                        }
                        armorStand.setHeadPose(new EulerAngle(0, 0, currentTilt));  // Update head pose
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 2L);  // Run every 2 ticks (adjust as needed)
        }
    }




}
