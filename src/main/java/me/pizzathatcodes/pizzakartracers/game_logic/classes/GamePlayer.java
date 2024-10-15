package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GamePlayer {

    UUID uuid;
    Kart kart;
    double driftDelay;

    public GamePlayer(UUID uuid, Kart kart) {
        this.uuid = uuid;
        this.kart = kart;
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return the kart
     */
    public Kart getKart() {
        return kart;
    }

    /**
     * Set the kart
     * @param kart the kart to set
     */
    public void setKart(Kart kart) {
        this.kart = kart;
    }

    /**
     * Set the uuid
     * @param uuid the uuid to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public void createKart() {
        Player player = Bukkit.getPlayer(getUuid());
        Location loc = player.getLocation();
        ArmorStand armorStand = loc.getWorld().spawn(loc.clone().subtract(0, 0, 0), ArmorStand.class);

        // Make the armor stand invisible and not affected by gravity
        armorStand.setVisible(false);
        armorStand.setGravity(true);

        // Create a Jukebox ItemStack
        ItemStack jukebox = new ItemStack(Material.JUKEBOX);

        // Set the jukebox as the helmet (on the armor stand's head)
        armorStand.getEquipment().setHelmet(jukebox);

        getKart().kartEntity = armorStand;

        if(player.getVehicle() == null) {
            getKart().getKartEntity().setPassenger(player);
        }

    }
}
