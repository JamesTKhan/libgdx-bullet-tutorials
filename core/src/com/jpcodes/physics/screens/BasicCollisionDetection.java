package com.jpcodes.physics.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
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
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author JamesTKhan
 * @version September 15, 2022
 */
public class BasicCollisionDetection extends BaseScreen {

    /**
     * Allows to configure Bullet collision detection
     * stack allocator size, default collision algorithms and persistent manifold pool size
     */
    btCollisionConfiguration collisionConfig;

    /**
     * A collision dispatcher iterates over each pair, searches for a matching collision algorithm based on the
     * types of objects involved and executes the collision algorithm computing contact points.
     */
    btDispatcher dispatcher;

    Array<GameObject> gameObjects;
    GameObject floorObject;

    static class GameObject extends ModelInstance implements Disposable {
        public final btCollisionObject body;
        public boolean hasCollided = false;

        public GameObject(Model model, btCollisionShape shape) {
            super(model);
            body = new btCollisionObject();
            body.setCollisionShape(shape);
        }

        @Override
        public void dispose() {
            body.dispose();
        }
    }

    public BasicCollisionDetection(Game game) {
        super(game);
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        gameObjects = new Array<>();

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

                GameObject box = new GameObject(modelBuilder.end(), shape);
                box.transform.setToTranslation(i, MathUtils.random(10, 20), j);
                box.transform.rotate(new Quaternion(Vector3.Z, MathUtils.random(0f, 270f)));
                box.body.setWorldTransform(box.transform);

                renderInstances.add(box);
                gameObjects.add(box);
            }
        }

        createFloor();
    }

    @Override
    public void render(float delta) {
        for (GameObject gameObject : gameObjects) {

            if (checkCollision(gameObject.body, floorObject.body)) {
                if (gameObject.hasCollided) continue;

                gameObject.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
                gameObject.hasCollided = true;

                continue;
            }

            gameObject.transform.trn(0, -2f * delta, 0f);
            gameObject.body.setWorldTransform(gameObject.transform);
        }

        super.render(delta);
    }

    /**
     * Manually checks for collisions between two collision objects.
     */
    public boolean checkCollision (btCollisionObject b1, btCollisionObject b2) {
        CollisionObjectWrapper co0 = new CollisionObjectWrapper(b1);
        CollisionObjectWrapper co1 = new CollisionObjectWrapper(b2);

        // For each pair of shape types, Bullet will dispatch a certain collision algorithm, by using the dispatcher.
        // So we use the dispatcher here to find the algorithm needed for the two shape types being checked, ex. btSphereBoxCollisionAlgorithm
        btCollisionAlgorithm algorithm = dispatcher.findAlgorithm(co0.wrapper, co1.wrapper, null, ebtDispatcherQueryType.BT_CONTACT_POINT_ALGORITHMS);

        btDispatcherInfo info = new btDispatcherInfo();
        btManifoldResult result = new btManifoldResult(co0.wrapper, co1.wrapper);

        // Execute the algorithm using processCollision, this stores the result (the contact points) in the btManifoldResult
        algorithm.processCollision(co0.wrapper, co1.wrapper, info, result);

        // Free the algorithm back to a pool for reuse later
        dispatcher.freeCollisionAlgorithm(algorithm.getCPointer());

        boolean r = false;

        // btPersistentManifold is a contact point cache to store contact points for a given pair of objects.
        btPersistentManifold man = result.getPersistentManifold();
        if (man != null) {
            // If the number of contact points is more than zero, then there is a collision.
            r = man.getNumContacts() > 0;
        }

        result.dispose();
        info.dispose();
        co1.dispose();
        co0.dispose();

        return r;
    }

    private void createFloor() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder = modelBuilder.part("floor", GL20.GL_TRIANGLES, VertexAttribute.Position().usage |VertexAttribute.Normal().usage | VertexAttribute.TexCoords(0).usage, new Material());

        BoxShapeBuilder.build(meshBuilder, 20, 1, 20);
        Model floor = modelBuilder.end();

        btBoxShape btBoxShape = new btBoxShape(new Vector3(10f, 0.5f, 10f));
        floorObject = new GameObject(floor, btBoxShape);
        floorObject.transform.trn(0, -0.5f, 0f);
        floorObject.body.setWorldTransform(floorObject.transform);

        renderInstances.add(floorObject);
    }
}
