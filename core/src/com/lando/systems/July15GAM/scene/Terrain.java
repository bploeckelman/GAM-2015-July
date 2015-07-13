package com.lando.systems.July15GAM.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.lando.systems.July15GAM.utils.SimplexNoise;

/**
 * Created by dsgraham on 7/8/15.
 */
public class Terrain extends Renderable {

    public Terrain (Environment environment, Pixmap heightPixmap) {
        final long attrs = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
        final int  primitiveType = GL20.GL_TRIANGLES;

        // Build the mesh
        final MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(attrs, primitiveType);
        {
            final int tilesWide = heightPixmap.getWidth();
            final int tilesHigh = heightPixmap.getHeight();
            final VertexInfo[] vertices = new VertexInfo[tilesWide * tilesHigh];

            // Generate vertices from heightmap luminance
            // TODO: so wasteful... should probably require grayscale or use a single channel of a shared image
            for (int tileX = 0; tileX < tilesWide; ++tileX) {
                for (int tileY = 0; tileY < tilesHigh; ++tileY) {
                    final MeshPart part = meshBuilder.part("" + tileX + "" + tileY, primitiveType);
                    final int pixel = heightPixmap.getPixel(tileX, tileY);
                    final int R = ((pixel & 0xff000000) >>> 24);
                    final int G = ((pixel & 0x00ff0000) >>> 16);
                    final int B = ((pixel & 0x0000ff00) >>> 8);
                    final int A = ((pixel & 0x000000ff));
                    final float luminance = (0.21f * R + 0.72f * G + 0.07f * B) / 255f;

                    final int index = tileX + tileY * tilesWide;
                    vertices[index] = new VertexInfo().setPos(tileX, luminance, tileY)
                                                      .setNor(0, 1, 0)
                                                      .setUV(tileX / (float) (tilesWide), tileY / (float) (tilesHigh));
                }
            }

            // Generate mesh rectangles by index
            for (int tileX = 0; tileX < tilesWide - 1; ++tileX) {
                for (int tileY = 0; tileY < tilesHigh - 1; ++tileY) {
                    // neighbor vertex indices
                    final int ll = (tileX + 0) + (tileY + 0) * tilesWide;
                    final int ul = (tileX + 0) + (tileY + 1) * tilesWide;
                    final int ur = (tileX + 1) + (tileY + 1) * tilesWide;
                    final int lr = (tileX + 1) + (tileY + 0) * tilesWide;

                    meshBuilder.rect(vertices[ll], vertices[ul], vertices[ur], vertices[lr]);
                }
            }
        }
        this.mesh = meshBuilder.end();

        // Populate mesh attributes
        this.meshPartSize = mesh.getNumIndices();
        this.material = new Material();
        this.worldTransform.idt();
        this.worldTransform.translate(-50, 0, -50);
        this.worldTransform.scale(10f, 5f, 10f);
//        this.primitiveType = GL20.GL_LINE_LOOP;
        this.primitiveType = primitiveType;
        this.shader = new DefaultShader(this);
        this.shader.init();
        this.environment = environment;
    }


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
