package wildfire.actors.tiles;

import greenfoot.GreenfootImage;
import wildfire.Assets;
import wildfire.Constants;
import wildfire.Counter;

/**
 * A wooden tile used to build a bridge.
 */
public class BridgeTile extends Buildable {

    private int health = Constants.TILE_HEALTH;
    private Counter counter = new Counter(0, true);
    private State state = State.NORMAL;

    public BridgeTile() {
        setImage(Assets.BRIDGE_TILE);
    }

    @Override
    public void act() {
        counter.count();

        if (counter.value() <= 0) {
            counter.reset();

            if (state == State.WET) { // Set state to normal once water wears off
                setState(State.NORMAL);
            } else if (state == State.BURNING) {
                this.health--;

                // Remove if burnt up
                if (percentHealth() <= 0) {
                    getWorld().removeObject(this);
                    return;
                }

                // Spread fire to non-wet neighbours if under health threshold
                if (percentHealth() <= Constants.SPREAD_THRESHOLD) {
                    for (BridgeTile tile : getNeighbours(1, false, BridgeTile.class))
                        tile.ignite();

                }

                updateImage();
            }
        }
    }

    /**
     * @return The remaining health percentage of this tile.
     */
    private double percentHealth() {
        return (double) health / Constants.TILE_HEALTH;
    }

    /**
     * @return Whether this tile is on fire.
     */
    public boolean isBurning() {
        return state == State.BURNING;
    }

    /**
     * Sets this tile on fire.
     */
    public void ignite() {
        if (state == State.NORMAL)
            setState(State.BURNING);
    }

    public void setState(State state) {
        this.state = state;
        if (state == State.WET)
            counter.setMax(Constants.WET_DURATION);
        else
            counter.setMax(Constants.FIRE_DELAY);
        counter.reset();
        updateImage();
    }

    private void updateImage() {
        // Set a fresh image
        setImage(Assets.BRIDGE_TILE);

        // Draw the damage overlay
        // Keep as "if" to layer multiple textures
        if (percentHealth() <= 0.25) drawImage(Assets.OVERLAY3);
        if (percentHealth() <= 0.5) drawImage(Assets.OVERLAY2);
        if (percentHealth() <= 0.75) drawImage(Assets.OVERLAY1);

        // Draw the status overlay
        if (state == State.BURNING) drawImage(Assets.FLAME);
        else if (state == State.WET) drawImage(Assets.WATER_OVERLAY);
    }

    private void drawImage(GreenfootImage img) {
        getImage().drawImage(img, 0, 0);
    }

    /**
     * The condition of this tile.
     */
    public enum State {
        NORMAL, BURNING, WET;
    }

}
