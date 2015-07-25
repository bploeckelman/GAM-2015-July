package com.lando.systems.July15GAM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.lando.systems.July15GAM.July15GAM;
import com.lando.systems.July15GAM.scene.Scene;
import com.lando.systems.July15GAM.utils.Assets;

/**
 * Brian Ploeckelman created on 7/2/2015.
 */
public class AtmosphereScreen extends ScreenAdapter {

    private static final float CAM_SPEED   = 5f;

    private OrthographicCamera    camera;
    private PerspectiveCamera     sceneCamera;
    private Scene                 scene;
    private SpriteBatch           batch;
    private ModelBatch            modelBatch;
    private FirstPersonCameraController camController;

    private GlyphLayout glyphLayout = new GlyphLayout();


    public AtmosphereScreen(July15GAM game) {
        camera = new OrthographicCamera(July15GAM.win_width, July15GAM.win_height);
        camera.translate(July15GAM.win_width / 2f, -July15GAM.win_height / 2f);
        camera.update();

        sceneCamera = new PerspectiveCamera(67f, July15GAM.win_width, July15GAM.win_height);
        sceneCamera.position.set(0f, 5f, 0f);
        sceneCamera.near = 1f;
        sceneCamera.far = 300f;
        sceneCamera.update();
        camController = new FirstPersonCameraController(sceneCamera);
        camController.setVelocity(CAM_SPEED);

        scene = new Scene(game);

        batch = Assets.batch;
        modelBatch = Assets.modelBatch;

        enableInput();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            scene.getTerrain().toggleWireframe();
        }

        camController.update(delta);
        sceneCamera.update();
        scene.update(delta, sceneCamera);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        scene.render(sceneCamera, batch, modelBatch);

        // NOTE: just debug things
//        final float posX = sceneCamera.position.x;
//        final float posY = sceneCamera.position.z;
//        final String text = "height(" + String.format("%.2f", posX) + ", " + String.format("%.2f", posY) + ") = "
//                          + String.format("%.2f", scene.getTerrain().getHeightAt(posX, posY));
//        glyphLayout.setText(Assets.font, text);
//        batch.begin();
//        batch.setProjectionMatrix(camera.combined);
//        Assets.font.draw(batch, text, 10f, -glyphLayout.height);
//        batch.end();
    }

    @Override
    public void dispose() {
        scene.dispose();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {
        disableInput();
    }

    @Override
    public void resume() {
        enableInput();
    }

    private void enableInput() {
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(camController);
        Gdx.input.setInputProcessor(inputMux);
    }

    private void disableInput() {
        Gdx.input.setInputProcessor(null);
    }

}
