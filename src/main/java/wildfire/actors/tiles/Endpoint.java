package wildfire.actors.tiles;

import wildfire.Assets;

/**
 * A start or finish tile on either side of the chasm.
 */
public class Endpoint extends Buildable {
    /**
     * Whether the player can build from this tile.
     */
    public boolean locked;

    public Endpoint(boolean locked) {
        setImage(Assets.ENDPOINT);
        this.locked = locked;
    }

    @Override
    public void act() {
        // Prevent building on a locked tile until it has been reached
        if (locked && hasNeighbour())  locked = false;
    }
}