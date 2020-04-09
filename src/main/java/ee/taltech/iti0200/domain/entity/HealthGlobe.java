package ee.taltech.iti0200.domain.entity;

import ee.taltech.iti0200.domain.World;
import ee.taltech.iti0200.physics.BoundingBox;
import ee.taltech.iti0200.physics.Vector;

import java.util.Random;

public class HealthGlobe extends Consumable implements HealingSource {

    private static final Vector SIZE = new Vector(1.5, 1.5);
    private int healAmount = 10 + new Random().nextInt(20);

    public HealthGlobe(Vector position, World world) {
        super(new BoundingBox(position, SIZE), world);
    }

    @Override
    public int getHealing() {
        return healAmount;
    }
}
