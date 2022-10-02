package com.jpcodes.physics.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.jpcodes.physics.MotionState;

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

    private void createObjects() {
        // Create some random shapes
        for (int i = -6; i < 6; i+=2) {
            for (int j = -6; j < 6; j+=2) {
                ModelBuilder modelBuilder = new ModelBuilder();
                modelBuilder.begin();
                Material material = new Material();
                material.set(ColorAttribute.createDiffuse(getRandomColor()));
                MeshPartBuilder builder = modelBuilder.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, material);

                btCollisionShape shape;

                int random = MathUtils.random(1, 4);
                switch (random) {
                    case 1:
                        BoxShapeBuilder.build(builder, 0, 0, 0, 1f, 1f, 1f);
                        shape = new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f));
                        break;
                    case 2:
                        ConeShapeBuilder.build(builder, 1, 1, 1, 8);
                        shape = new btConeShape(0.5f, 1f);
                        break;
                    case 3:
                        SphereShapeBuilder.build(builder, 1, 1, 1, 8, 8);
                        shape = new btSphereShape(0.5f);
                        break;
                    case 4:
                    default:
                        CylinderShapeBuilder.build(builder, 1, 1, 1, 8);
                        shape = new btCylinderShape(new Vector3(0.5f, 0.5f, 0.5f));
                        break;
                }

                ModelInstance box = new ModelInstance(modelBuilder.end());
                box.transform.setToTranslation(i, MathUtils.random(10, 20), j);
                box.transform.rotate(new Quaternion(Vector3.Z, MathUtils.random(0f, 270f)));

                float mass = 1f;

                Vector3 localInertia = new Vector3();
                shape.calculateLocalInertia(mass, localInertia);

                btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
                btRigidBody body = new btRigidBody(info);

                MotionState motionState = new MotionState(box.transform);
                body.setMotionState(motionState);

                renderInstances.add(box);
                bulletPhysicsSystem.addBody(body);
            }
        }
    }
}
