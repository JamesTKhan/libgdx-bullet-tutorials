package com.jpcodes.physics.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

/**
 * @author JamesTKhan
 * @version September 21, 2022
 */
public class BoundingBoxCollision extends BaseScreen {

    private final GameObject boxOne;
    private final GameObject boxTwo;
    private final BoundingBox boundingBox = new BoundingBox();
    private final BoundingBox boundingBox2 = new BoundingBox();

    private final Array<GameObject> gameObjects = new Array<>();

    static class GameObject extends ModelInstance {
        public final BoundingBox boundingBox;
        public boolean hasCollided = false;

        public GameObject(Model model) {
            super(model);
            boundingBox = new BoundingBox();
            calculateBoundingBox(boundingBox);

        }
    }

    public BoundingBoxCollision(Game game) {
        super(game);

        boxOne = createBox();
        gameObjects.add(boxOne);

        boxTwo = createBox();
        gameObjects.add(boxTwo);

        boxTwo.transform.trn(20, 0, 0);
    }

    @Override
    public void render(float delta) {
        // Update the boxes
        boundingBox.set(boxOne.boundingBox);
        boundingBox.mul(boxOne.transform);

        boundingBox2.set(boxTwo.boundingBox);
        boundingBox2.mul(boxTwo.transform);

        // Check for intersection
        if (boundingBox.intersects(boundingBox2)) {
            boxTwo.hasCollided = true;
        }

        if (!boxTwo.hasCollided)
            boxTwo.transform.trn(-2 * delta, 0, 0);

        super.render(delta);
    }

    private GameObject createBox() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        Material material = new Material();
        material.set(ColorAttribute.createDiffuse(getRandomColor()));
        MeshPartBuilder builder = modelBuilder.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, material);

        BoxShapeBuilder.build(builder, 0, 0, 0, 5f, 10f, 5f);

        GameObject box = new GameObject(modelBuilder.end());

        renderInstances.add(box);
        return box;
    }
}
