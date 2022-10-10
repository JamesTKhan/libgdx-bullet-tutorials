package com.jpcodes.physics.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.jpcodes.physics.BulletPhysicsSystem;
import com.jpcodes.physics.MotionState;
import com.jpcodes.physics.SelectScreen;
import com.jpcodes.physics.controllers.camera.CameraController;
import com.jpcodes.physics.controllers.camera.FirstPersonCameraController;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * @author JamesTKhan
 * @version September 15, 2022
 */
public class BaseScreen extends ScreenAdapter {
    private static boolean drawDebug = false;

    protected PerspectiveCamera camera;
    protected CameraController cameraController;
    protected ModelBatch modelBatch;
    protected ModelBatch shadowBatch;
    protected Array<ModelInstance> renderInstances;
    protected Environment environment;
    protected DirectionalShadowLight shadowLight;
    protected BulletPhysicsSystem bulletPhysicsSystem;
    protected Game game;

    private final Array<Color> colors;

    private final Stage stage;
    private final VisLabel fpsLabel;

    final float GRID_MIN = -100f;
    final float GRID_MAX = 100f;
    final float GRID_STEP = 10f;

    public BaseScreen(Game game) {

        this.game = game;
        bulletPhysicsSystem = new BulletPhysicsSystem();

        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 500;
        camera.position.set(0,10, 50f);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add((shadowLight = new DirectionalShadowLight(2048, 2048, 30f, 30f, 1f, 100f)).set(0.8f, 0.8f, 0.8f, -.4f, -.4f, -.4f));
        environment.shadowMap = shadowLight;

        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        fpsLabel = new VisLabel();
        fpsLabel.setPosition(10, 10);
        stage.addActor(fpsLabel);

        modelBatch = new ModelBatch();
        shadowBatch = new ModelBatch(new DepthShaderProvider());
        renderInstances = new Array<>();

        cameraController = new FirstPersonCameraController(camera);
        ((FirstPersonCameraController) cameraController).setVelocity(50f);
        ((FirstPersonCameraController) cameraController).setDegreesPerPixel(0.2f);
        Gdx.input.setInputProcessor(cameraController);

        colors = new Array<>();
        colors.add(Color.PURPLE);
        colors.add(Color.BLUE);
        colors.add(Color.TEAL);
        colors.add(Color.BROWN);
        colors.add(Color.FIREBRICK);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new SelectScreen(game));
            dispose();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            drawDebug = !drawDebug;
        }

        bulletPhysicsSystem.update(delta);
        cameraController.update(delta);

        ScreenUtils.clear(Color.BLACK, true);

        shadowLight.begin(Vector3.Zero, camera.direction);
        shadowBatch.begin(shadowLight.getCamera());
        shadowBatch.render(renderInstances);
        shadowBatch.end();
        shadowLight.end();

        modelBatch.begin(camera);
        modelBatch.render(renderInstances, environment);
        modelBatch.end();

        if (drawDebug) {
            bulletPhysicsSystem.render(camera);
        }

        stage.act();
        stage.draw();

        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    public void setCameraController(CameraController cameraController) {
        this.cameraController = cameraController;
        Gdx.input.setInputProcessor(cameraController);
    }

    protected void createFloor(float width, float height, float depth) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder = modelBuilder.part("floor", GL20.GL_TRIANGLES, VertexAttribute.Position().usage |VertexAttribute.Normal().usage | VertexAttribute.TexCoords(0).usage, new Material());

        BoxShapeBuilder.build(meshBuilder, width, height, depth);
        btBoxShape btBoxShape = new btBoxShape(new Vector3(width/2f, height/2f, depth/2f));
        Model floor = modelBuilder.end();

        ModelInstance floorInstance = new ModelInstance(floor);
        floorInstance.transform.trn(0, -0.5f, 0f);

        btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(0, null, btBoxShape, Vector3.Zero);
        btRigidBody body = new btRigidBody(info);

        body.setWorldTransform(floorInstance.transform);

        renderInstances.add(floorInstance);
        bulletPhysicsSystem.addBody(body);
    }

    void createAxes() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.LIGHT_GRAY);
        for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
            builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
            builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
        }
        builder = modelBuilder.part("axes", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.RED);
        builder.line(0, .1f, 0, 100, 0, 0);
        builder.setColor(Color.GREEN);
        builder.line(0, .1f, 0, 0, 100, 0);
        builder.setColor(Color.BLUE);
        builder.line(0, .1f, 0, 0, 0, 100);
        Model axesModel = modelBuilder.end();
        ModelInstance axesInstance = new ModelInstance(axesModel);

        renderInstances.add(axesInstance);
    }

    protected void createObjects() {
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

    protected Color getRandomColor(){
        return colors.get(MathUtils.random(0, colors.size-1));
    }
}
