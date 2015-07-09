package com.lando.systems.July15GAM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.lando.systems.July15GAM.July15GAM;
import com.lando.systems.July15GAM.scene.Scene;
import com.lando.systems.July15GAM.utils.Assets;

/**
 * Brian Ploeckelman created on 5/10/2015.
 */
public class TestScreen extends ScreenAdapter {

    Vector3               mouseScreenPos;
    Vector3               mouseWorldPos;
    SpriteBatch           batch;
    ModelBatch            modelBatch;
    FrameBuffer           sceneFrameBuffer;
    TextureRegion         sceneRegion;
    OrthographicCamera    screenCamera;
    PerspectiveCamera     camera;
    CameraInputController camController;
    UserInterface         userInterface;
    Scene                 scene;

    public TestScreen(July15GAM game) {
        batch = Assets.batch;
        modelBatch = Assets.modelBatch;

        scene = new Scene();

        mouseScreenPos = new Vector3();
        mouseWorldPos = new Vector3();

        sceneFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, July15GAM.win_width, July15GAM.win_height, true);
        sceneRegion = new TextureRegion(sceneFrameBuffer.getColorBufferTexture());
        sceneRegion.flip(false, true);

        camera = new PerspectiveCamera(67f, July15GAM.win_width, July15GAM.win_height);
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();
        camController = new CameraInputController(camera);

        screenCamera = new OrthographicCamera();
        screenCamera.setToOrtho(false, July15GAM.win_width, July15GAM.win_height);
        screenCamera.update();

        userInterface = new UserInterface();

        enableInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
    }

    @Override
    public void render(float delta) {
        update(delta);

        sceneFrameBuffer.begin();
        {
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            scene.render(camera, batch, modelBatch);
        }
        sceneFrameBuffer.end();

        // TODO: add default screen shader
        batch.setShader(null);
        batch.begin();
        {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.setProjectionMatrix(screenCamera.combined);
            batch.draw(sceneRegion, 0, 0);
            userInterface.render(batch);
            batch.end();
        }
    }

    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        userInterface.update(delta);

        camera.update();
        updateMouseVectors(camera);

        scene.update(delta, camera);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth  = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {
        disableInput();
    }

    @Override
    public void resume() {
        enableInput();
    }

    @Override
    public void dispose() {
        batch.dispose();
        modelBatch.dispose();
        scene.dispose();
        sceneFrameBuffer.dispose();
    }

    // ------------------------------------------------------------------------
    // Private Implementation
    // ------------------------------------------------------------------------

    private void updateMouseVectors(Camera camera) {
        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();
        mouseScreenPos.set(mx, my, 0);
        mouseWorldPos.set(mx, my, 0);
        camera.unproject(mouseWorldPos);
    }

    private void enableInput() {
        final InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(userInterface);
        mux.addProcessor(camController);
        Gdx.input.setInputProcessor(mux);
    }

    private void disableInput() {
        Gdx.input.setInputProcessor(null);
    }

}
