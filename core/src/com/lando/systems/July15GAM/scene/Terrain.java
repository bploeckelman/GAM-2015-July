package com.lando.systems.July15GAM.scene;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.*;
import com.lando.systems.July15GAM.utils.Assets;
import com.lando.systems.July15GAM.utils.SimplexNoise;

/**
 * Created by dsgraham on 7/8/15.
 */
public class Terrain extends Renderable {



    public Terrain (Environment environment){
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
        int width = 10;
        int tiles = 10;
        for (int i = 0; i < tiles; i++) {
            for (int j = 0; j < tiles; j++) {
                MeshPart part = meshBuilder.part("" + i+"" + j, GL20.GL_TRIANGLES);
                VertexInfo[] vertices = new VertexInfo[width * width];

                for (int y = 0; y < width; y++) {
                    for (int x = 0; x < width; x++) {
                        int xx = x + i *(width-1);
                        int yy = y + j * (width -1);
                        double z = SimplexNoise.noise(xx, yy) * .1f +
                                   SimplexNoise.noise(xx * .1, yy * .1) * .4f +
                                   SimplexNoise.noise(xx * .01, yy * .01) * .5f;
                        vertices[x + y * width] = new VertexInfo().setPos(xx, (float) z, yy).setNor(0, 1, 0).setUV(x/(float)(width), y /(float)(width));
                    }
                }

                for (int y = 0; y < width - 1; y++) {
                    for (int x = 0; x < width - 1; x++) {
                        meshBuilder.rect(vertices[x + y * width], vertices[x + (y + 1) * width], vertices[x + 1 + (y + 1) * width], vertices[x + 1 + y * width]);
                    }
                }
            }
        }
        mesh = meshBuilder.end();
        meshPartSize = mesh.getNumIndices();
        material = new Material();
        worldTransform.idt();
        worldTransform.translate(-50, 0, -50);
        primitiveType = GL20.GL_TRIANGLES;

        shader = new DefaultShader(this);
        shader.init();

        this.environment = environment;
    }


    public void update(float dt){

    }


}
