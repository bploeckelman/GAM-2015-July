package com.lando.systems.July15GAM.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.lando.systems.July15GAM.utils.Assets;

/**
 * Brian Ploeckelman created on 7/8/2015.
 */
public class Scene implements Disposable {

    PointLight           pointLight;
    Environment          environment;
    Model                cubeModel;
    Model                planeModel;
    Model                sphereModel;
    ModelInstance        cubeInstance;
    ModelInstance        planeInstance;
    ModelInstance        sphereInstance;
    ModelInstance        skydomeTopInstance;
    ModelInstance        skydomeBottomInstance;
    float                cubeRotAngle;
    float                sphereRotAngle;
    Terrain              terrain;

    public Scene() {
        initializeModels();
    }

    public ModelInstance getSkydomeTop() { return skydomeTopInstance; }
    public ModelInstance getSkydomeBottom() { return skydomeBottomInstance; }

    public void update(float delta, Camera camera) {
        skydomeTopInstance.transform.setTranslation(camera.position.x, camera.position.y - 30f, camera.position.z);
        skydomeBottomInstance.transform.setTranslation(camera.position.x, camera.position.y - 30f, camera.position.z);

        cubeRotAngle += 10f * delta;
        if (cubeRotAngle > 1f) {
            cubeRotAngle -= 1f;
        }
        cubeInstance.transform.rotate(0f, 1f, 0f, cubeRotAngle);

        sphereRotAngle += 20f * delta;
        if (sphereRotAngle > 360f) {
            sphereRotAngle -= 360f;
        }
        final float dist = 5f;

        pointLight.position.set(dist * MathUtils.cosDeg(sphereRotAngle), 9.5f, dist * MathUtils.sinDeg(sphereRotAngle));
        sphereInstance.transform.setToTranslation(pointLight.position);
    }

    /**
     * Assumes frameBuffer begin()/end() happens outside of this call
     */
    public void render(Camera camera, SpriteBatch batch, ModelBatch modelBatch) {
        modelBatch.begin(camera);
        modelBatch.render(cubeInstance, environment);
        modelBatch.render(sphereInstance, environment);
        modelBatch.render(skydomeTopInstance);
//        modelBatch.render(skydomeBottomInstance);
        modelBatch.render(terrain);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        sphereModel.dispose();
        planeModel.dispose();
        cubeModel.dispose();
    }

    // ------------------------------------------------------------------------
    // Private Implementation
    // ------------------------------------------------------------------------

    private void initializeModels() {
        final ModelBuilder builder = new ModelBuilder();

        pointLight = new PointLight().set(new Color(1f, 1f, 1f, 1f), 0f, 5f, 0f, 20f);
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
        environment.add(pointLight);

        final Material cubeMaterial = new Material();
        cubeMaterial.set(ColorAttribute.createAmbient(new Color(0.2f, 0.2f, 0.2f, 1f)));
        cubeMaterial.set(ColorAttribute.createSpecular(Color.WHITE));
        final long cubeAttrs = Usage.Position | Usage.Normal;
        cubeModel = builder.createBox(5f, 5f, 5f, cubeMaterial, cubeAttrs);

        final Material planeMaterial = new Material();
        planeMaterial.set(ColorAttribute.createAmbient(Color.WHITE));
        planeMaterial.set(ColorAttribute.createSpecular(Color.WHITE));
        planeMaterial.set(TextureAttribute.createDiffuse(Assets.testTexture));
        final long planeAttrs = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
        planeModel = builder.createRect(
                -20f, 0f, -20f,
                -20f, 0f,  20f,
                20f, 0f,  20f,
                20f, 0f, -20f,
                0f, 1f,   0f,
                planeMaterial,
                planeAttrs);

        final Material sphereMaterial = new Material();
        sphereMaterial.set(ColorAttribute.createAmbient(Color.YELLOW));
        sphereMaterial.set(ColorAttribute.createDiffuse(Color.WHITE));
        sphereMaterial.set(ColorAttribute.createReflection(Color.RED));
        final long sphereAttrs = Usage.Position | Usage.Normal;
        final float size = 0.5f;
        sphereModel = builder.createSphere(size, size, size, 8, 8, sphereMaterial, sphereAttrs);

        cubeInstance = new ModelInstance(cubeModel);
        planeInstance = new ModelInstance(planeModel);
        sphereInstance = new ModelInstance(sphereModel);

        cubeInstance.transform.setTranslation(0f, 7.5f, 0f);

        skydomeTopInstance = new ModelInstance(Assets.skydomeModel);
        skydomeBottomInstance = new ModelInstance(Assets.skydomeModel);
        skydomeBottomInstance.transform.rotate(1f, 0f, 1f, 180f);

        terrain = new Terrain(environment, Assets.terrainHeightmap);
        final Material terrainMaterial = new Material();
        //terrainMaterial.set(ColorAttribute.createAmbient(new Color(0.2f, 0.2f, 0.2f, 1f)));
        terrainMaterial.set(TextureAttribute.createDiffuse(Assets.grassTexture));
        terrainMaterial.set(ColorAttribute.createAmbient(.5f,.5f,.1f,1));
        terrain.material = terrainMaterial;
    }

}
