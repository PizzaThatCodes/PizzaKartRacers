package me.pizzathatcodes.pizzakartracers.game_logic;

import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;

import java.util.ArrayList;
import java.util.UUID;

public class Game {

    private ArrayList<GamePlayer> players;

    public Game() {
        players = new ArrayList<>();
    }



    public ArrayList<GamePlayer> getPlayers() {
        return players;
    }

    public void addPlayer(GamePlayer player) {
        players.add(player);
    }

    public void removePlayer(GamePlayer player) {
        players.remove(player);
    }

    public GamePlayer getGamePlayer(UUID uuid) {
        return players.stream().filter(gamePlayer -> gamePlayer.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public GamePlayer findGamePlayerFromKart(Kart kart) {
        return players.stream().filter(gamePlayer -> gamePlayer.getKart().equals(kart)).findFirst().orElse(null);
    }

}
