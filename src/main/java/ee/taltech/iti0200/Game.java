package ee.taltech.iti0200;

import ee.taltech.iti0200.domain.Player;
import ee.taltech.iti0200.domain.Terrain;
import ee.taltech.iti0200.domain.World;
import ee.taltech.iti0200.graphics.Graphics;
import ee.taltech.iti0200.input.Input;
import ee.taltech.iti0200.network.Network;
import ee.taltech.iti0200.physics.Physics;

import javax.vecmath.Vector2d;

public class Game {

    private World world;
    private Graphics graphics;
    private Physics physics;
    private Network network;
    private Input input;
    private Timer timer;
    private long tick = 0;

    private void run() {
        world = new World(0.0, 40.0, 0.0, 40.0, 10);
        graphics = new Graphics(world);
        physics = new Physics(world);
        network = new Network();
        input = new Input();
        timer = new Timer(20F);

        initialize();
        loop();
        terminate();
    }

    private void initialize() {
        timer.initialize();
        graphics.initialize();
        initializeBasicWorld();
    }

    private void initializeBasicWorld() {
        world.addBody(new Player(new Vector2d(20.0, 4.0)), true);
        for (int i = 0; i < 20; i++) {
            world.addBody(new Terrain(new Vector2d(i * 2.0 + 1.0, 1.0)), false);
        }
        world.addBody(new Terrain(new Vector2d(1.0, 3.0)), false);
        world.addBody(new Terrain(new Vector2d(39.0, 3.0)), false);
    }

    private void loop() {
        while (isGameRunning()) {
            network.update(tick);
            physics.step(tick);
            graphics.render(tick);
            input.update(tick);
            network.propagate(tick);

            tick = timer.sleep();
        }
    }

    private boolean isGameRunning() {
        return graphics.isWindowOpen();
    }

    private void terminate() {
        graphics.terminate();
    }

    public static void main(String[] args) {
        new Game().run();
    }

}
