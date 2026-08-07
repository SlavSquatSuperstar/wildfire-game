package wildfire.world;

import greenfoot.Actor;
import greenfoot.Greenfoot;
import greenfoot.MouseInfo;
import greenfoot.World;
import wildfire.Assets;
import wildfire.Constants;
import wildfire.Counter;
import wildfire.Util;
import wildfire.actors.Flame;
import wildfire.actors.Powerup;
import wildfire.actors.UIIcon;
import wildfire.actors.tiles.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The scene where the game occurs.
 */
public class WildfireWorld extends World {

    // Counters
    private Counter fireCounter, buildCounter;
    private boolean firstMeteor;

    // Tiles
    private Endpoint start, finish;
    private Buildable lastNearest;

    // Icons
    private UIIcon buildIcon, adrenalineIcon;
    private Powerup rainIcon;

    public WildfireWorld() {
        super(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, Constants.TILE_SIZE);
        Util.log("===================================");
        Util.log("Game starting. Place any tile!");
        init();
    }

    /**
     * Set up the stage.
     */
    private void init() {
        buildCounter = new Counter(0, false);
        fireCounter = new Counter(Constants.METEOR_DELAY, false);

        // Create world generator
        WorldGenerator gen = new WorldGenerator(getWidth(), getHeight());

        // Create UI elements
        addObject(buildIcon = new UIIcon(Assets.BUILD_READY), getWidth() - 2, getHeight() - 1);
        addObject(adrenalineIcon = new UIIcon(Assets.ADRENALINE_IDLE), buildIcon.getX() - 2, buildIcon.getY());
        addObject(rainIcon = new Powerup(), adrenalineIcon.getX() - 2, buildIcon.getY());

        // Add start and finish tiles
        addObject(start = new Endpoint(false), gen.getStart().x, gen.getStart().y);
        addObject(finish = new Endpoint(true), gen.getFinish().x, gen.getFinish().y);

        // Add random obstacles
        int numObstacles = 0;
        outer:
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (Greenfoot.getRandomNumber(100) < gen.getObstacleLocations()[x][y]) {
                    addObject(new Obstacle(), x, y);
                    if (++numObstacles >= Constants.OBSTACLE_COUNT) break outer;
                }
            }
        }
    }

    @Override
    public void act() {
        buildCounter.count();
        fireCounter.count();

        // Show the player valid build spots
        if (Greenfoot.mouseMoved(null)) showBuildPreview(Greenfoot.getMouseInfo());

        // Check for player input
        if (buildCounter.value() <= 0) { // Only allow buidling/breaking tiles once cooldown reached
            buildIcon.setImage(Assets.BUILD_READY);
            if (Greenfoot.mouseClicked(null))
                buildTile(Greenfoot.getMouseInfo());
        } else if (buildCounter.value() <= Constants.BUILD_COOLDOWN / 2) {
            buildIcon.setImage(Assets.BUILD_WAIT2);
        } else {
            buildIcon.setImage(Assets.BUILD_WAIT1);
        }

        if (!buildCounter.started) return;

        // Win the game if the start and end are connected
        if (checkForWin()) endGame(true);

        // Lose if all the tiles have been burned
        // Flaw: can still lose the game if no tiles placed or if break all tiles
        // TODO: confirmation message: do you want to break your last tile?
        if (fireCounter.started && getObjects(BridgeTile.class).isEmpty()) endGame(false);

        // Spread the fire
        if (fireCounter.value() <= 0) {
            createMeteorEvent();
            fireCounter.reset();
        }
    }

    /**
     * Show the valid build spaces for the tile nearest to the mouse.
     */
    public void showBuildPreview(MouseInfo mouse) {
        // Sometimes when pressing "Act", Greenfoot returns a null MouseInfo object
        if (mouse == null) return;
        int x = mouse.getX();
        int y = mouse.getY();

        Buildable nearest = null;
        double min = Integer.MAX_VALUE;
        for (Buildable tile : getObjects(Buildable.class)) {
            if (tile instanceof Endpoint && ((Endpoint) tile).locked) continue;
            // Don't count if tile completely blocked
            double distance = tile.getDistance(new Point(x, y));
            if (distance < min) {
                min = distance;
                nearest = tile;
            }
        }

        if (nearest != null && nearest.equals(lastNearest)) return;

        lastNearest = nearest;
        removeObjects(getObjects(PreviewTile.class));
        if (lastNearest != null) lastNearest.showBuildableSpots();
    }

    /**
     * Attempts to place, remove, or extinguish a tile at the provided location.
     */
    public void buildTile(MouseInfo mouse) {
        // Sometimes when pressing "Act", Greenfoot returns a null MouseInfo object
        if (mouse == null) return;
        int x = mouse.getX();
        int y = mouse.getY();
        boolean worldChanged = false; // Only reset build cooldown if a block was modified

        // When mouse clicked on a tile
        if (mouse.getButton() == 1) {
            // If the powerup is active, extinguish the tile at this cell if it exists
            if (rainIcon.isActive() && getObject(x, y, BridgeTile.class) != null) {
                for (int x0 = x - 1; x0 <= x + 1; x0++)
                    for (int y0 = y - 1; y0 <= y + 1; y0++)
                        if (getObject(x0, y0, BridgeTile.class) != null)
                            getObject(x0, y0, BridgeTile.class).setState(BridgeTile.State.WET);

                rainIcon.reset();
                worldChanged = true;
            }
            // Place a tile if clicking an empty cell and a buildable is nearby
            if (checkAdjacent(x, y) && getObject(x, y, Tile.class) == null) {
                addObject(new BridgeTile(), x, y);
                worldChanged = true;
            }
        } else if (mouse.getButton() == 3) {
            BridgeTile tileAtPos = getObject(x, y, BridgeTile.class);
            // If a tile exists at this cell, then remove it unless it is burning
            if (tileAtPos != null && !tileAtPos.isBurning()) {
                removeObject(tileAtPos);
                worldChanged = true;
            }
        }

        if (!worldChanged) return;

        // Execute after placing first block
        if (!buildCounter.started) {
            buildCounter.setMax(Constants.BUILD_COOLDOWN);
            buildCounter.started = true;
            fireCounter.started = true;
            Util.log("The race is on! Cross the gap!");
        }

        // Apply cooldown buff (adrenaline) based on amount of bridge burning
        int burningCount = 0;
        List<BridgeTile> tiles = getObjects(BridgeTile.class);
        for (BridgeTile tile : tiles) {
            if (tile.isBurning()) burningCount++;
        }

        // Reset build timer
        if (burningCount / (double) tiles.size() >= Constants.ADRENALINE_THRESHOLD) {
            buildCounter.setMax((int) (Constants.BUILD_COOLDOWN * Constants.ADRENALINE_REDUCTION));
            adrenalineIcon.setImage(Assets.ADRENALINE_ACTIVE);
        } else {
            buildCounter.setMax(Constants.BUILD_COOLDOWN);
            adrenalineIcon.setImage(Assets.ADRENALINE_IDLE);
        }
        buildCounter.reset();

        // Show build preview
        showBuildPreview(mouse);
    }

    // Game Methods

    /**
     * Creates a two opposite moving fireballs at a random location.
     */
    public void createMeteorEvent() {
        // Create initial round of fire
        if (!firstMeteor) {
            firstMeteor = true;
            Util.log("The wildfire is spreading! Can you escape in time?");

            // Set all tiles in the leftmost column on fire
            boolean found = false;
            for (int x = 0; x < getWidth(); x++) {
                for (int y = 0; y < getHeight(); y++) {
                    if (getObject(x, y, BridgeTile.class) != null) {
                        found = true;
                    }
                }

                if (found) {
                    addObject(new Flame("up"), x, start.getY());
                    addObject(new Flame("down"), x, start.getY());
                    break;
                }
            }
        } else {
            // Generate random waves of fire
            ArrayList<BridgeTile> tiles = new ArrayList<>();
            tiles.addAll(getObjects(BridgeTile.class));
            int r = Greenfoot.getRandomNumber(tiles.size());
            int x = tiles.get(r).getX() + Util.random(-Constants.WORLDGEN_BUFFER, Constants.WORLDGEN_BUFFER);
            int y = tiles.get(r).getY() + Util.random(-Constants.WORLDGEN_BUFFER, Constants.WORLDGEN_BUFFER);

            if (Util.random(0, 100) < 80) {
                addObject(new Flame("up"), x, y);
                addObject(new Flame("down"), x, y);
            } else {
                addObject(new Flame("left"), x, y);
                addObject(new Flame("right"), x, y);
            }
        }
    }

    /**
     * Attempts to find a path between both endpoints. See the A* Pathfinding Algorithm:
     * https://www.youtube.com/watch?v=-L-WgKMFuhE
     *
     * @return Whether the pathfinding was successful.
     */
    public boolean checkForWin() {
        // Skip if too few tiles
        if (getObjects(Buildable.class).size() < (int) start.getDistance(finish)) return false;
        // Skip if both sides have no neighbours
        if (!(start.hasNeighbour() && finish.hasNeighbour())) return false;

        List<Buildable> open = new ArrayList<>();
        List<Buildable> closed = new ArrayList<>();
        open.add(start);

        Buildable current = start;
        while (!open.isEmpty()) {
            // Set current to the open tile with the lowest f-cost
            int min = Integer.MAX_VALUE;
            for (Buildable tile : open) {
                int f_cost = tile.getFCost(start, finish);
                if (f_cost < min) {
                    min = f_cost;
                    current = tile;
                }
            }

            // Stop searching if we found the end
            if (current.equals(finish)) return true;

            open.remove(current);
            closed.add(current);

            // Add all the non-closed neighbours to open
            List<Buildable> nbrs = current.getNeighbours();
            nbrs.removeAll(closed);
            open.addAll(nbrs);
        }

        return false;
    }

    /**
     * Finishes the game and displays a message.
     *
     * @param win Whether the game was won.
     */
    public void endGame(boolean win) {
        UIIcon banner;

        if (win) {
            banner = new UIIcon(Assets.WIN_MSG);
            //showText("You Escaped the Wildfire!", getWidth() / 2, getHeight() / 2);
            Util.log("Game won! Can you beat your time?");
        } else {
            banner = new UIIcon(Assets.LOSE_MSG);
            //showText("The Wildfire Consumed You!", getWidth() / 2, getHeight() / 2);
            Util.log("Game lost! Better luck next time!");
        }

        addObject(banner, getWidth() / 2, getHeight() / 2);
        Util.log("Time: %.1fsec", Util.currentTime());
        Greenfoot.stop();
    }

    // Tile Methods

    /**
     * Check if there are any unlocked buildable tiles adjacent to the given cell.
     */
    public boolean checkAdjacent(int x, int y) {
        int[][] coords = {{x, y - 1}, {x, y + 1}, {x - 1, y}, {x + 1, y}};
        for (int[] coord : coords) {
            Buildable tile = getObject(coord[0], coord[1], Buildable.class);
            if (tile != null) { // Check if has neighbouring tiles
                if (tile instanceof Endpoint && ((Endpoint) tile).locked) continue;
                return true;
            }
        }
        return false;
    }

    private <T extends Actor> T getObject(int x, int y, Class<T> cls) {
        List<T> objectsAtPos = getObjectsAt(x, y, cls);
        // Should only exist one, so just return the first
        if (!objectsAtPos.isEmpty()) return objectsAtPos.get(0);
        return null;
    }

}
