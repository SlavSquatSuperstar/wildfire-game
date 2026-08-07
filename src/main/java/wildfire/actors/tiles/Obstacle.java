package wildfire.actors.tiles;

import wildfire.Assets;
import wildfire.Constants;

/**
 * A physical object that cannot be moved or destroyed.
 */
public class Obstacle extends Tile {
    public Obstacle() {
        setImage(Assets.OBSTACLE);
    }
}
