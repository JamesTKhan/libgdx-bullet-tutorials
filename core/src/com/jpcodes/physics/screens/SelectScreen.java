package com.jpcodes.physics.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * @author JamesTKhan
 * @version September 16, 2022
 */
public class SelectScreen extends ScreenAdapter {
    private final Stage stage;
    private final VisSelectBox<ScreenEnum> screenSelect;
    private final Game game;

    enum ScreenEnum {
        NotSelected,
        BoundingBox,
        BasicCollisionDetection
    }

    public SelectScreen(final Game game) {
        if (!VisUI.isLoaded())
            VisUI.load();

        this.game = game;
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        VisTable selectTable = new VisTable();
        screenSelect = new VisSelectBox<>();
        screenSelect.setItems(ScreenEnum.values());
        screenSelect.setSelected(ScreenEnum.NotSelected);
        screenSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                switch (screenSelect.getSelected()) {
                    case BoundingBox:
                        game.setScreen(new BoundingBoxCollision(game));
                        break;
                    case BasicCollisionDetection:
                        game.setScreen(new BasicCollisionDetection(game));
                        break;
                }
            }
        });

        selectTable.add(screenSelect);
        selectTable.setPosition(stage.getWidth() / 2, stage.getHeight() / 2);
        stage.addActor(selectTable);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }
}
