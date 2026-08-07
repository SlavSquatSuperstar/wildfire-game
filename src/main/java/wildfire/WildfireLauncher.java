package wildfire;

import wildfire.world.WildfireWorld;

/**
 * The launcher for the Wildfire game.
 */
public class WildfireLauncher extends GreenfootRunner {

    public WildfireLauncher(Configuration configuration) {
        super(configuration);
    }

    public static void main(String[] args) {
        Configuration configuration = Configuration
                .forWorld(WildfireWorld.class) // Greenfoot world
                .projectName("Escape the Wildfire!") // Window title
                .lockScenario(false) // User set simulation speed
                .hideControls(false); // Start and reset buttons
        new WildfireLauncher(configuration);
    }

}
