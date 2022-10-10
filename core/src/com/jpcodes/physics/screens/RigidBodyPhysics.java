package com.jpcodes.physics.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

/**
 * @author JamesTKhan
 * @version September 29, 2022
 */
public class RigidBodyPhysics extends BaseScreen {
    private static final Vector3 rayFromWorld = new Vector3();
    private static final Vector3 rayToWorld   = new Vector3();
    private final ClosestRayResultCallback callback = new ClosestRayResultCallback(new Vector3(), new Vector3());

    public RigidBodyPhysics(Game game) {
        super(game);

        createFloor(20, 1, 20);
        createObjects();
    }

    @Override
    public void render(float delta) {
        // On left click perform raycast
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // Reset the callback
            callback.setClosestHitFraction(1.0f);
            callback.setCollisionObject(null);

            // Get a pick ray of the current mouse coordinates
            Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

            rayFromWorld.set(ray.origin);
            rayToWorld.set(ray.direction).scl(100f).add(ray.origin);

            bulletPhysicsSystem.raycast(rayFromWorld, rayToWorld, callback);

            if (callback.hasHit()) {
                btCollisionObject collisionObject = callback.getCollisionObject();
                if (collisionObject instanceof btRigidBody) {
                    // Activate and push the object in the direction of the ray
                    collisionObject.activate();
                    ((btRigidBody) collisionObject).applyCentralImpulse(ray.direction.scl(50f));
                }
            }
        }


        super.render(delta);
    }
}
