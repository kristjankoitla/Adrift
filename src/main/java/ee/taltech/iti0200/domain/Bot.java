package ee.taltech.iti0200.domain;

import ee.taltech.iti0200.physics.Body;
import ee.taltech.iti0200.physics.Vector;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Bot extends Living {

    private static final Vector size = new Vector(1.5, 1.5);
    private static final double mass = 70.0;
    private static final Random random = new Random();
    private static final double elasticity = 0.25;
    private static final double frictionCoefficient = 0.99;
    private World world;

    private Vector acceleration;
    private Gun gun;

    public Bot(Vector position, World world) {
        super(new Body(mass, size, position, true, true), false);
        setElasticity(elasticity);
        setFrictionCoefficient(frictionCoefficient);
        acceleration = new Vector(0.0, 0.0);
        this.world = world;
        this.gun = new Gun();
    }

    public void update(long tick) {
        if (tick % 10 == 0) {
            gun.passTime(10 * world.getTimeStep());
            move();
            if (gun.canShoot()) {
                look().ifPresent(this::shoot);
            }
        }
    }

    private void shoot(Map.Entry<Vector, Double> target) {
        if (target.getValue() < 0.2) {

            Vector speed = new Vector(target.getKey());
            speed.normalize();
            speed.scale(gun.getProjectileSpeed());

            Vector position = new Vector(getBoundingBox().getCentre());

            world.addBody(new Projectile(position, speed), true);
        }
    }

    private Optional<AbstractMap.SimpleEntry<Vector, Double>> look() {
        return world.getLivingEntities().stream()
            .map(living -> {
                Vector vector = new Vector(living.getBoundingBox().getCentre());
                vector.sub(getBoundingBox().getCentre());
                return new AbstractMap.SimpleEntry<>(vector, vector.angle(this.speed));
            })
            .filter(entry -> entry.getValue() < 0.2)
            .min(Comparator.comparing(entry -> entry.getKey().lengthSquared()));
    }

    private void move() {
        acceleration.add(new Vector(random.nextDouble() - 0.5, 0));
        acceleration.scale(0.9);
        speed.add(acceleration);
    }

}
