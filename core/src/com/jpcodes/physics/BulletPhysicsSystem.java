package com.jpcodes.physics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author JamesTKhan
 * @version September 29, 2022
 */
public class BulletPhysicsSystem implements Disposable {

    /**
     * Stores all btCollisionObjects and provides an interface to
     * perform queries.
     */
    private final btDynamicsWorld dynamicsWorld;

    /**
     * Allows to configure Bullet collision detection
     * stack allocator size, default collision algorithms and persistent manifold pool size
     */
    private final btCollisionConfiguration collisionConfig;

    /**
     * A collision dispatcher iterates over each pair, searches for a matching collision algorithm based on the
     * types of objects involved and executes the collision algorithm computing contact points.
     */
    private final btDispatcher dispatcher;

    /**
     * Broadphase collision detection provides acceleration structure to quickly reject pairs of objects
     * based on axis aligned bounding box (AABB) overlap.
     */
    private final btBroadphaseInterface broadphase;

    private final btConstraintSolver constraintSolver;

    private final DebugDrawer debugDrawer;

    private final float fixedTimeStep = 1/60f;

    public BulletPhysicsSystem() {
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);

        // General purpose, well optimized broadphase, adapts dynamically to the dimensions of the world.
        broadphase = new btDbvtBroadphase();
        constraintSolver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);

        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_DrawWireframe);

        dynamicsWorld.setDebugDrawer(debugDrawer);
    }

    public void update(float delta) {
        // performs collision detection and physics simulation
        dynamicsWorld.stepSimulation(delta, 1, fixedTimeStep);
    }

    public void render(Camera camera) {
        debugDrawer.begin(camera);
        dynamicsWorld.debugDrawWorld();
        debugDrawer.end();
    }

    public void addBody(btRigidBody body) {
        dynamicsWorld.addRigidBody(body);
    }

    @Override
    public void dispose() {
        collisionConfig.dispose();
        dispatcher.dispose();
        broadphase.dispose();
        constraintSolver.dispose();
        dynamicsWorld.dispose();
    }
}
