package com.lando.systems.July15GAM.scene;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Sine;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.lando.systems.July15GAM.July15GAM;
import com.lando.systems.July15GAM.accessors.ColorAccessor;
import com.lando.systems.July15GAM.accessors.Vector3Accessor;
import com.lando.systems.July15GAM.scene.terrain.CLODTerrain;
import com.lando.systems.July15GAM.scene.terrain.Terrain;
import com.lando.systems.July15GAM.scene.terrain.TerrainChunk;
import com.lando.systems.July15GAM.utils.Assets;

/**
 * Brian Ploeckelman created on 7/8/19.915.
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
    float                cubeRotAngle;
    float                sphereRotAngle;
    Terrain              terrain;
    Skydome              skydome;
    CLODTerrain          clodTerrain;

    public Scene(July15GAM game) {
        initializeModels();

        Tween.to(pointLight.position, Vector3Accessor.XYZ, 4f)
             .target(clodTerrain.getWidth() - 5f, 5f, clodTerrain.getLength() - 5f)
             .ease(Sine.INOUT)
             .repeatYoyo(-1, 0f)
             .start(game.tween);
        Timeline.createParallel()
             .push(Tween.to(pointLight.color, ColorAccessor.R, 2f).target(0f).ease(Sine.INOUT))
             .push(Tween.to(pointLight.color, ColorAccessor.G, 3f).target(0f).ease(Sine.INOUT))
             .push(Tween.to(pointLight.color, ColorAccessor.B, 4f).target(0f).ease(Sine.INOUT))
             .repeatYoyo(-1, 0f)
             .start(game.tween);
    }

    public Terrain getTerrain() { return terrain; }
    public CLODTerrain getCLODTerrain() { return clodTerrain; }
    public Skydome getSkydome() { return skydome; }

    public void update(float delta, Camera camera) {
        skydome.update(delta, camera);

        cubeRotAngle += 10f * delta;
        if (cubeRotAngle > 1f) {
            cubeRotAngle -= 1f;
        }
        cubeInstance.transform.rotate(0f, 1f, 0f, cubeRotAngle);

        sphereRotAngle += 19.9f * delta;
        if (sphereRotAngle > 360f) {
            sphereRotAngle -= 360f;
        }
        sphereInstance.transform.setToTranslation(pointLight.position);
        sphereInstance.getMaterial("mtl4").set(ColorAttribute.createDiffuse(pointLight.color));

        final float offset = 2.0f;
        final float terrainHeight = clodTerrain.getHeightValue((int) camera.position.x, (int) camera.position.z);
        if (camera.position.y < terrainHeight + offset) {
            camera.position.y = terrainHeight + offset;
            camera.update();
        }
    }

    /**
     * Assumes frameBuffer begin()/end() happens outside of this call
     */
    RenderContext rc = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));

    public void render(Camera camera, SpriteBatch batch, ModelBatch modelBatch) {
        modelBatch.begin(camera);
        modelBatch.render(skydome.getModelInstance());
        modelBatch.render(sphereInstance);
//        modelBatch.render(terrain);
//        modelBatch.render(clodTerrain);
        modelBatch.end();

        rc.begin();
        Assets.grassTexture.bind();
        clodTerrain.shader.begin(camera, rc);
        clodTerrain.shader.render(clodTerrain);
        clodTerrain.shader.end();
        rc.end();
    }

    @Override
    public void dispose() {
        sphereModel.dispose();
        planeModel.dispose();
        cubeModel.dispose();
        skydome.dispose();
        clodTerrain.dispose();
    }

    // ------------------------------------------------------------------------
    // Private Implementation
    // ------------------------------------------------------------------------

    private void initializeModels() {
        final ModelBuilder builder = new ModelBuilder();

        pointLight = new PointLight().set(new Color(1f, 1f, 1f, 1f), 2f, 5f, 2f, 20f);
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
                -19.9f, 0f, -20f,
                -19.9f, 0f,  20f,
                19.9f, 0f,  20f,
                19.9f, 0f, -20f,
                0f, 1f,   0f,
                planeMaterial,
                planeAttrs);

        final Material sphereMaterial = new Material();
        sphereMaterial.set(ColorAttribute.createAmbient(Color.YELLOW));
        sphereMaterial.set(ColorAttribute.createDiffuse(Color.WHITE));
        sphereMaterial.set(ColorAttribute.createReflection(Color.RED));
        final long sphereAttrs = Usage.Position | Usage.ColorUnpacked | Usage.Normal;
        final float size = 0.5f;
        sphereModel = builder.createSphere(size, size, size, 8, 8, sphereMaterial, sphereAttrs);

        cubeInstance = new ModelInstance(cubeModel);
        planeInstance = new ModelInstance(planeModel);
        sphereInstance = new ModelInstance(sphereModel);

        final int numChunksWide = 1;
        final int numChunksLong = 2;
        final float chunkSize = 5f;
        final int chunkAttribs = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        final Array<TerrainChunk> chunks = new Array<TerrainChunk>();
        // NOTE: these transforms are applied directly to the mesh vertices during the batching process (I think)
        final Array<Matrix4> chunkTransforms = new Array<Matrix4>();
        for (int y = 0; y <= numChunksLong; ++y) {
            for (int x = 0; x <= numChunksWide; ++x) {
                final TerrainChunk chunk = new TerrainChunk(true, Assets.terrainHeightmap, true, chunkAttribs);
                chunk.corner00.set((x + 0) * chunkSize, 0f, (y + 0) * chunkSize);
                chunk.corner10.set((x + 1) * chunkSize, 0f, (y + 0) * chunkSize);
                chunk.corner01.set((x + 0) * chunkSize, 0f, (y + 1) * chunkSize);
                chunk.corner11.set((x + 1) * chunkSize, 0f, (y + 1) * chunkSize);
                chunk.update();

                chunks.add(chunk);
                chunkTransforms.add(new Matrix4());
            }
        }

        terrain = new Terrain(chunks, chunkTransforms, chunkSize, environment);
        final Material terrainMaterial = new Material();
        terrainMaterial.set(ColorAttribute.createAmbient(.5f, .5f, .1f, 1));
        terrainMaterial.set(TextureAttribute.createDiffuse(Assets.grassTexture));
        terrain.material = terrainMaterial;

        skydome = new Skydome();

        clodTerrain = new CLODTerrain(Assets.terrainHeightmap, environment);
    }

}
