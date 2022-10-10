package com.jpcodes.physics.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.jpcodes.physics.BulletEntity;
import com.jpcodes.physics.MotionState;
import com.jpcodes.physics.controllers.camera.ThirdPersonCameraController;
import com.jpcodes.physics.controllers.character.DynamicCharacterController;
import com.jpcodes.physics.utils.Utils3D;

/**
 * @author JamesTKhan
 * @version October 02, 2022
 */
public class DynamicCharacterScreen extends BaseScreen {

    private final DynamicCharacterController controller;

    public DynamicCharacterScreen(Game game) {
        super(game);

        createObjects();

        BulletEntity player = createPlayer();
        controller = new DynamicCharacterController(player, bulletPhysicsSystem);

        setCameraController(new ThirdPersonCameraController(camera, player.getModelInstance()));

        camera.position.set(new Vector3(0, 10, -10));
        camera.lookAt(Vector3.Zero);

        // Load a walkable area
        Model sceneModel = Utils3D.loadOBJ(Gdx.files.internal("models/scene.obj"));
        ModelInstance sceneInstance = new ModelInstance(sceneModel);
        sceneInstance.materials.get(0).set(ColorAttribute.createDiffuse(Color.FOREST));
        sceneInstance.materials.get(1).set(ColorAttribute.createDiffuse(Color.TEAL));
        sceneInstance.materials.get(2).set(ColorAttribute.createDiffuse(Color.DARK_GRAY));
        sceneInstance.materials.get(3).set(ColorAttribute.createDiffuse(Color.TAN));

        renderInstances.add(sceneInstance);

        btCollisionShape shape = Bullet.obtainStaticNodeShape(sceneInstance.nodes);
        btRigidBody.btRigidBodyConstructionInfo sceneInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, shape, Vector3.Zero);
        btRigidBody body = new btRigidBody(sceneInfo);
        bulletPhysicsSystem.addBody(body);
    }

    @Override
    public void render(float delta) {
        controller.update(delta);

        super.render(delta);
    }

    private BulletEntity createPlayer() {
        ModelInstance playerModelInstance = new ModelInstance(Utils3D.buildCapsuleCharacter());

        // Move him up above the ground
        playerModelInstance.transform.setToTranslation(0,4,0);

        // Calculate dimension
        BoundingBox boundingBox = new BoundingBox();
        playerModelInstance.calculateBoundingBox(boundingBox);

        Vector3 dimensions = new Vector3();
        boundingBox.getDimensions(dimensions);

        // Scale for half extents
        dimensions.scl(0.5f);

        MotionState motionState = new MotionState(playerModelInstance.transform);
        btCapsuleShape capsuleShape = new btCapsuleShape(dimensions.len() / 2.5f, dimensions.y);

        float mass = 2f;

        Vector3 intertia = new Vector3();
        capsuleShape.calculateLocalInertia(mass, intertia);

        btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, capsuleShape, intertia);
        btRigidBody body = new btRigidBody(info);

        // Prevent body from falling over
        body.setAngularFactor(Vector3.Y);

        // Prevent the body from sleeping
        body.setActivationState(Collision.DISABLE_DEACTIVATION);

        // Add damping so we dont slide forever!
        body.setDamping(0.75f, 0.99f);

        renderInstances.add(playerModelInstance);
        bulletPhysicsSystem.addBody(body);

        return new BulletEntity(body, playerModelInstance);
    }
}
