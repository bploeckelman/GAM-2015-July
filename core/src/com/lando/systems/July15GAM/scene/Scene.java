package com.lando.systems.July15GAM.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.lando.systems.July15GAM.utils.Assets;

/**
 * Brian Ploeckelman created on 7/8/2015.
 */
public class Scene {

    Environment          environment;
    Array<ModelInstance> modelInstances;
    ModelInstance        skydomeTopInstance;
    ModelInstance        skydomeBottomInstance;

    public Scene() {
        modelInstances = new Array<ModelInstance>();
        skydomeTopInstance = new ModelInstance(Assets.skydomeModel);
        skydomeBottomInstance = new ModelInstance(Assets.skydomeModel);
        skydomeBottomInstance.transform.rotate(1f, 0f, 1f, 180f);
    }

    public void update(float delta, Camera camera) {
        skydomeTopInstance.transform.setTranslation(camera.position);
        skydomeBottomInstance.transform.setTranslation(camera.position);
    }

    /**
     * Assumes frameBuffer begin()/end() happens outside of this call
     * @param camera
     * @param batch
     * @param modelBatch
     */
    public void render(Camera camera, SpriteBatch batch, ModelBatch modelBatch) {
        modelBatch.begin(camera);
        modelBatch.render(modelInstances, environment);
        modelBatch.render(skydomeTopInstance);
        modelBatch.render(skydomeBottomInstance);
        modelBatch.end();
    }

}
