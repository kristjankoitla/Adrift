package ee.taltech.iti0200.physics;

import org.junit.jupiter.api.Test;

import javax.vecmath.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class AABBTest {

    @Test
    void testIntersectSamePosition() {
        BoundingBox box1 = new BoundingBox(
            new Vector2d(5.0, 5.0),
            new Vector2d(1.0, 1.0),
            true
        );
        BoundingBox box2 = new BoundingBox(
            new Vector2d(5.0, 5.0),
            new Vector2d(2.0, 2.0),
            true
        );
        assertTrue(box1.intersects(box2));
        assertTrue(box2.intersects(box1));
    }

    @Test
    void testIntersectCenterOutsideOtherBoxButIntersecting() {
        BoundingBox box1 = new BoundingBox(
            new Vector2d(5.0, 5.0),
            new Vector2d(1.0, 1.0),
            true
        );
        BoundingBox box2 = new BoundingBox(
            new Vector2d(5.75, 5.75),
            new Vector2d(2.0, 2.0),
            true
        );
        assertTrue(box1.intersects(box2));
        assertTrue(box2.intersects(box1));
    }

    @Test
    void testIntersectPlayerFallingThroughFloorClosest() {
        BoundingBox player = new BoundingBox(
            new Vector2d(20.0, 1.05),
            new Vector2d(1.5, 1.5),
            true
        );
        BoundingBox terrain = new BoundingBox(
            new Vector2d(19.0, 1.0),
            new Vector2d(2.0, 2.0),
            true
        );
        assertTrue(player.intersects(terrain));
        assertTrue(terrain.intersects(player));
    }

}