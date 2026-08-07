package wildfire.actors.tiles;

import wildfire.Constants;

import java.awt.*;
import java.util.List;

/**
 * An object in the world that can be built from.
 */
public abstract class Buildable extends Tile {

    public List<Buildable> getNeighbours() {
        return getNeighbours(1, false, Buildable.class);
    }

    public boolean hasNeighbour() {
        return !getNeighbours(1, false, Buildable.class).isEmpty();
    }

    public void showBuildableSpots() {
        int x = getX();
        int y = getY();
        Point[] adjacent = {new Point(x, y - 1), new Point(x, y + 1), new Point(x - 1, y), new Point(x + 1, y)};
        Rectangle worldBounds = new Rectangle(0, 0, Constants.WORLD_WIDTH - 1, Constants.WORLD_HEIGHT - 1);

        for (Point p : adjacent) {
            if (worldBounds.contains(p)) // Make sure the location is inside the world
                if (getWorld().getObjectsAt(p.x, p.y, Tile.class).isEmpty()) // Make sure there are no objects there
                    getWorld().addObject(new PreviewTile(), p.x, p.y);
        }
    }

}
