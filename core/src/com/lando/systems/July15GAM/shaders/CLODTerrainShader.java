package com.lando.systems.July15GAM.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.lando.systems.July15GAM.scene.terrain.CLODTerrain;

/**
 * Brian Ploeckelman created on 7/22/2015.
 */
public class CLODTerrainShader implements Shader {

    Renderable    renderable;
    ShaderProgram program;
    Camera        camera;
    RenderContext context;
    int           u_projTransPos;

    public CLODTerrainShader(Renderable renderable) {
        this.renderable = renderable;
    }

    @Override
    public void init() {
        String vert = Gdx.files.internal("shaders/default.vert").readString();
        String frag = Gdx.files.internal("shaders/default.frag").readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTransPos = program.getUniformLocation("u_projTrans");
    }

    @Override
    public void dispose() {
        program.dispose();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        program.setUniformMatrix(u_projTransPos, camera.combined);
        context.setDepthMask(true);
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        // TODO: set u_worldTrans here from renderable.worldTransform
        renderable.mesh.render(program,
                               renderable.primitiveType,
                               renderable.meshPartOffset,
                               renderable.meshPartSize);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return false;
    }

}
