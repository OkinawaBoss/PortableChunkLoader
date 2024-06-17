package me.okinawaboss.chunkloader;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.EntityType;

public class LightLevelManager {
    private static final Map<EntityType, LightLevelRange> requiredLightLevels = new HashMap<>();

    static {
        // Passive mobs that require high light levels
        requiredLightLevels.put(EntityType.ALLAY, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.BAT, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.BEE, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.CAT, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.CHICKEN, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.COW, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.DONKEY, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.FOX, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.HORSE, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.MOOSHROOM, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.OCELOT, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.PARROT, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.PIG, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.RABBIT, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.SHEEP, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.SKELETON_HORSE, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.SNOW_GOLEM, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.STRIDER, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.TRADER_LLAMA, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.TURTLE, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.VILLAGER, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.WANDERING_TRADER, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.WOLF, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.ZOMBIE_HORSE, new LightLevelRange(9, 15));

        // Neutral mobs
        requiredLightLevels.put(EntityType.BEE, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.CAT, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.DOLPHIN, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.FOX, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.IRON_GOLEM, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.LLAMA, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.PANDA, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.POLAR_BEAR, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.TURTLE, new LightLevelRange(9, 15));
        requiredLightLevels.put(EntityType.WOLF, new LightLevelRange(9, 15));

        // Hostile mobs that require low light levels to spawn
        requiredLightLevels.put(EntityType.BLAZE, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.CAVE_SPIDER, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.CREEPER, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.DROWNED, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.ELDER_GUARDIAN, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.ENDERMAN, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.ENDERMITE, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.EVOKER, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.GHAST, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.GUARDIAN, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.HOGLIN, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.HUSK, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.MAGMA_CUBE, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.PHANTOM, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.PIGLIN, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.PIGLIN_BRUTE, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.PILLAGER, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.RAVAGER, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.SHULKER, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.SILVERFISH, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.SKELETON, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.SLIME, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.SPIDER, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.STRAY, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.VEX, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.VINDICATOR, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.WARDEN, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.WITCH, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.WITHER_SKELETON, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.ZOGLIN, new LightLevelRange(0, 15));
        requiredLightLevels.put(EntityType.ZOMBIE, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.ZOMBIE_VILLAGER, new LightLevelRange(0, 7));
        requiredLightLevels.put(EntityType.ZOMBIFIED_PIGLIN, new LightLevelRange(0, 15));
    }

    public static boolean canSpawn(EntityType entityType, int currentLightLevel) {
        LightLevelRange range = requiredLightLevels.getOrDefault(entityType, new LightLevelRange(0, 15));
        return currentLightLevel >= range.min && currentLightLevel <= range.max;
    }

    private static class LightLevelRange {
        int min;
        int max;

        LightLevelRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }
}
