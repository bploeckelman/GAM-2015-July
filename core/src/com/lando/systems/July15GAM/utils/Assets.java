package com.lando.systems.July15GAM.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Brian Ploeckelman created on 5/21/2015.
 */
public class Assets {

    public static SpriteBatch batch;
    public static ModelBatch  modelBatch;
    public static BitmapFont  font;

    public static Pixmap terrainHeightmap;

    public static Texture testTexture;
    public static Texture grassTexture;

    public static ShaderProgram defaultShader;
    public static ShaderProgram atmosphereShader;

    private static AssetManager assetManager;
    public static  Model        skydomeModel;

    public static void load() {
        batch = new SpriteBatch();
        modelBatch = new ModelBatch();

        font = new BitmapFont();
        font.getData().markupEnabled = true;

        defaultShader = compileShaderProgram(Gdx.files.internal("shaders/default.vert"),
                                             Gdx.files.internal("shaders/default.frag"));
        atmosphereShader = compileShaderProgram(Gdx.files.internal("shaders/passthru.vert"),
                                                Gdx.files.internal("shaders/atmosphere.frag"));

        terrainHeightmap = new Pixmap(Gdx.files.internal("heightmap.png"));

        testTexture = new Texture("badlogic.jpg");
        grassTexture = new Texture("grass.png");

        assetManager = new AssetManager();
        assetManager.load("models/skydome.g3db", Model.class);
        assetManager.finishLoading();
        skydomeModel = assetManager.get("models/skydome.g3db");
    }

    public static void dispose() {
        batch.dispose();
        modelBatch.dispose();
        font.dispose();
        testTexture.dispose();
        grassTexture.dispose();
        defaultShader.dispose();
        atmosphereShader.dispose();
        terrainHeightmap.dispose();
        assetManager.dispose();
    }

    private static ShaderProgram compileShaderProgram(FileHandle vertSource, FileHandle fragSource) {
        ShaderProgram.pedantic = false;
        final ShaderProgram shader = new ShaderProgram(vertSource, fragSource);
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Failed to compile shader program:\n" + shader.getLog());
        }
        else if (shader.getLog().length() > 0) {
            Gdx.app.debug("SHADER", "ShaderProgram compilation log:\n" + shader.getLog());
        }
        return shader;
    }

}
