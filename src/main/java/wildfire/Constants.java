package wildfire;

/**
 * A storage class for game parameters and statistics.
 */
public final class Constants {

    // TODO read from config file
    private Constants() {}

    // World Parameters
    public static final int WORLD_WIDTH = 21;
    public static final int WORLD_HEIGHT = 15;
    public static final int TILE_SIZE = 32;
    public static final int TILE_HEALTH = 12;
    public static final int OBSTACLE_COUNT = 3;
    public static final int WORLDGEN_BUFFER = 2;

    // Build Parameters
    public static final int BUILD_COOLDOWN = 65; // How long to wait until placing/breaking blocks
    public static final double ADRENALINE_THRESHOLD = 0.35; // When to apply "adrenaline" (% bridge burning)
    public static final double ADRENALINE_REDUCTION = 0.4; // How much to reduce build time when under adrenaline

    // Fire Spread Parameters
    public static final int FIRE_DELAY = 60; // How long to wait spread fire
    public static final double SPREAD_THRESHOLD = 0.60; // When to spread fire (% health remaining)
    public static final int METEOR_DELAY = FIRE_DELAY * 5; // How long to wait create a meteor event
    public static final int METEOR_MOVE_DELAY = 10; // How long to wait to move the flame particles
    public static final int METEOR_STRENGTH = 2; // How many tiles the flame particles can ignite

    // Extinguish Parameters
    public static final int RAIN_COOLDOWN = 600; // How long to wait to reuse extinguish ability
    public static final int WET_DURATION = 400; // How long to wait till water dries

}
