package me.pizzathatcodes.pizzakartracers.queue_logic;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.queue_logic.classes.QueuePlayer;
import me.pizzathatcodes.pizzakartracers.queue_logic.events.PlayerJoinEvent;
import me.pizzathatcodes.pizzakartracers.queue_logic.events.PlayerLeaveEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;

public class Queue {

    public ArrayList<Player> playerList = new ArrayList<>();
    public int timeWaitLeft;
    public String id;

    public String generateCode() {

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 5;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        return generatedString;

    }

    public Queue() {
        this.timeWaitLeft = 240;
        this.id = generateCode();
    }

    public String getID() {
        return id;
    }

    public int getTimeWaitLeft() {
        return timeWaitLeft;
    }

    public void setTimeWaitLeft(int timeWaitLeft) {
        this.timeWaitLeft = timeWaitLeft;
    }

    public void removeTimeLeft(int removeTime) {
        timeWaitLeft -= removeTime;
    }

    public void addPlayer(Player player) {
        playerList.add(player);
        QueuePlayer playerSystem = null;
        for(QueuePlayer p : Main.getQueuePlayerBoards()) {
            if(p.getUuid().equals(player.getUniqueId())) {
                playerSystem = p;
                break;
            }
        }

    }

//    public void removePlayer(Player player) {
//        playerList.remove(player);
//        QueuePlayer playerSystem = null;
//        for(QueuePlayer p : Main.getQueuePlayerBoards()) {
//            if(p.getUuid().equals(player.getUniqueId())) {
//                playerSystem = p;
//                break;
//            }
//        }
//
//        Main.getQueuePlayerBoards().remove(playerSystem);
//
//    }


    public void registerQueueEvents() {
        Main.getInstance().getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), Main.getInstance());
        Main.getInstance().getServer().getPluginManager().registerEvents(new PlayerJoinEvent(), Main.getInstance());
    }

    public ArrayList<Player> getPlayers() {
        return playerList;
    }
}
