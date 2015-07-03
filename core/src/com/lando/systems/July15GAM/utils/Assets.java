package com.lando.systems.July15GAM.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Brian Ploeckelman created on 5/21/2015.
 */
public class Assets {

    public static BitmapFont font;

    public static Texture testTexture;

    public static ShaderProgram defaultShader;

    public static void load() {
        font = new BitmapFont();
        font.getData().markupEnabled = true;

        defaultShader = compileShaderProgram(Gdx.files.internal("shaders/default.vert"),
                                             Gdx.files.internal("shaders/default.frag"));

        testTexture = new Texture("badlogic.jpg");

    }

    public static void dispose() {
        font.dispose();
        testTexture.dispose();
    }

    private static ShaderProgram compileShaderProgram(FileHandle vertSource, FileHandle fragSource) {
        ShaderProgram.pedantic = false;
        final ShaderProgram shader = new ShaderProgram(vertSource, fragSource);
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Failed to compile shader program:\n" + shader.getLog());
        }
        else if (shader.getLog().length() > 0) {
            Gdx.app.error("SHADER", "ShaderProgram compilation log:\n" + shader.getLog());
        }
        return shader;
    }

}
