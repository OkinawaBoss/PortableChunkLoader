package me.okinawaboss.chunkloader;

import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChunkLoaderMoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Chunk oldChunk;
    private final Chunk newChunk;
    private final Location oldLocation;
    private final Location newLocation;
    private final UUID ownerUUID;
    private final long timestamp;
    private final String reason;

    public ChunkLoaderMoveEvent(Chunk oldChunk, Chunk newChunk, Location oldLocation, Location newLocation, UUID ownerUUID, long timestamp, String reason) {
        this.oldChunk = oldChunk;
        this.newChunk = newChunk;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
        this.ownerUUID = ownerUUID;
        this.timestamp = timestamp;
        this.reason = reason;
    }

    public Chunk getOldChunk() {
        return oldChunk;
    }

    public Chunk getNewChunk() {
        return newChunk;
    }

    public Location getOldLocation() {
        return oldLocation;
    }

    public Location getNewLocation() {
        return newLocation;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
