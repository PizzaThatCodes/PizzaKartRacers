package me.pizzathatcodes.pizzakartracers.queue_logic.classes;

import fr.mrmicky.fastboard.FastBoard;

import java.util.UUID;

public class QueuePlayer {

    UUID uuid;
    FastBoard board;

    /**
     * Constructor for QueuePlayer
     * @param uuid the UUID of the player
     * @param board the FastBoard (Scoreboard) of the player
     */
    public QueuePlayer(UUID uuid, FastBoard board) {
        this.uuid = uuid;
        this.board = board;
    }

    /**
     * @return the UUID of the player
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Set the UUID of the player
     * @param uuid the UUID to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the FastBoard (Scoreboard) of the player
     */
    public FastBoard getBoard() {
        return board;
    }

    /**
     * Set the FastBoard (Scoreboard) of the player
     * @param board the FastBoard to set
     */
    public void setBoard(FastBoard board) {
        this.board = board;
    }

}
