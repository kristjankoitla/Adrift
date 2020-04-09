package ee.taltech.iti0200.domain.entity;

import ee.taltech.iti0200.physics.BoundingBox;

public class Damageable extends Entity {

    private static final long serialVersionUID = 1L;

    protected int health;
    protected int maxHealth;

    public Damageable(double mass, BoundingBox boundingBox, int health) {
        super(mass, boundingBox);
        this.health = health;
        this.maxHealth = health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

}
