package com.lando.systems.July15GAM.scene.terrain;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.lando.systems.July15GAM.shaders.CLODTerrainShader;

/**
 * Brian Ploeckelman created on 7/20/2015.
 *
 * Borrows heavily from Xoppa's HeightField class, <3 Xoppa:
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/g3d/HeightField.java
 */
public class CLODTerrain extends Renderable implements Disposable {

    private static final float HEIGHT_SCALE = 20f;

    private Texture heightmap;
    private int numVerticesWide;
    private int numVerticesLong;
    private int numVertices;
    private int numIndices;
    private int posPos;
    private int norPos;
    private int uvPos;
    private int colPos;
    private int stride; // floats per vertex, based on which vertex attributes are used
//    private Mesh mesh; // NOTE: add if not extending Renderable

    // Store locally so we don't have to pull from GPU to query, read only after ctor
    private float[] heights;
    private float[] vertices;
    private short[] indices;

    public CLODTerrain(Pixmap heightmapPixmap, Environment environment) {
        final boolean isStatic = true;
        final VertexAttributes attributes = MeshBuilder.createAttributes(
                Usage.Position | Usage.Normal | Usage.TextureCoordinates | Usage.ColorUnpacked);

        this.heightmap       = new Texture(heightmapPixmap);
        this.numVerticesWide = heightmap.getWidth();
        this.numVerticesLong = heightmap.getHeight();
        this.numVertices     = numVerticesWide * numVerticesLong;
        this.numIndices      = (numVerticesWide - 1) * (numVerticesLong - 1) * 6;
        this.posPos          = attributes.getOffset(Usage.Position, -1);
        this.norPos          = attributes.getOffset(Usage.Normal, -1);
        this.uvPos           = attributes.getOffset(Usage.TextureCoordinates, -1);
        this.colPos          = attributes.getOffset(Usage.ColorUnpacked, -1);
        this.stride          = attributes.vertexSize / 4;
        this.mesh = new Mesh(isStatic, numVertices, numIndices, attributes);
        generateHeights(heightmapPixmap);
        generateVertices();
        generateIndices();
        this.mesh.setVertices(vertices);
        this.mesh.setIndices(indices);

        this.meshPartOffset = 0;
        this.meshPartSize = mesh.getNumIndices();
        this.material = new Material();
        this.material.set(
                ColorAttribute.createAmbient(0.2f, 0.2f, 0.2f, 1f),
                TextureAttribute.createDiffuse(heightmap)
//                TextureAttribute.createDiffuse(Assets.grassTexture)
        );
        this.primitiveType = GL20.GL_TRIANGLES;
        this.environment = environment;
        this.shader = new CLODTerrainShader(this);
        this.shader.init();
    }

    @Override
    public void dispose() {
        mesh.dispose();
        shader.dispose();
        heightmap.dispose();
    }

    // ------------------------------------------------------------------------
    // Private Implementation
    // ------------------------------------------------------------------------

    /**
     * Calculate height values for the regular grid based on luminance values
     * @param pixmap source of color data to compute luminance from
     * @return the calculated height values
     */
    private float[] generateHeights(Pixmap pixmap) {
        heights = new float[numVertices];

        for (int y = 0; y < numVerticesLong; ++y) {
            for (int x = 0; x < numVerticesWide; ++x) {
                final int pixel = pixmap.getPixel(x, y);
                final Color color = new Color(pixel);
                final float height = 0.2126f * color.r + 0.7152f * color.g + 0.0722f * color.b * HEIGHT_SCALE;

                final int index = y * numVerticesWide + x;
                heights[index] = height;
            }
        }

        return heights;
    }

    /**
     * Generate basic vertex data for regular grid based on attributes and height values
     * @return the generated vertex data in a flat float array
     */
    private float[] generateVertices() {
        vertices = new float[numVertices * stride];
        final VertexInfo vertex = new VertexInfo();
        for (int y = 0; y < numVerticesLong; ++y) {
            for (int x = 0; x < numVerticesWide; ++x) {
                final int index = y * numVerticesWide + x;
                setVertex(index, getVertexAt(vertex, x, y));
            }
        }
        return vertices;
    }

    /**
     * Generate triangle index data for regular grid, used to lookup into vertex array
     * @return the generated index data in a flat short array
     */
    private short[] generateIndices() {
        indices = new short[numIndices];

        int i = -1;
        int w = numVerticesWide - 1;
        int h = numVerticesLong - 1;
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                final int c00 = y * numVerticesWide + x;
                final int c10 = c00 + 1;
                final int c01 = c00 + numVerticesWide;
                final int c11 = c10 + numVerticesWide;
                indices[++i] = (short) c11;
                indices[++i] = (short) c10;
                indices[++i] = (short) c00;
                indices[++i] = (short) c00;
                indices[++i] = (short) c01;
                indices[++i] = (short) c11;
            }
        }

        return indices;
    }

    /**
     * Populates the provided VertexInfo object with vertex data for the
     * specified grid location.
     *
     * NOTE: does not calculate proper vertex normals at this time
     * @param out the VertexInfo object to be populated
     * @param x the x coordinate of the regular grid for this vertex
     * @param y the y coordinate of the regular grid for this vertex
     * @return the provided VertexInfo object, after being populated
     */
    protected VertexInfo getVertexAt(final VertexInfo out, int x, int y) {
        final float dx = (float)x / (float)(numVerticesWide - 1);
        final float dy = (float)y / (float)(numVerticesLong - 1);
        final float height = getHeightValue(x, y);
        out.position.set(x, height, y);
        out.normal.set(getWeightedNormalAt(out.normal, x, y));
        out.color.set(Color.WHITE);
        out.uv.set(dx, dy); // .scl(uvScale).add(uvOffset)
        return out;
    }

    /**
     * Populates the vertex array with the specified VertexInfo.
     * Only sets values for vertex attributes that are in use.
     * @param index the base index of the vertex to write to
     * @param info the vertex data to write to the buffer
     */
    private void setVertex(int index, MeshPartBuilder.VertexInfo info) {
        index *= stride;
        if (posPos >= 0) {
            vertices[index + posPos + 0] = info.position.x;
            vertices[index + posPos + 1] = info.position.y;
            vertices[index + posPos + 2] = info.position.z;
        }
        if (norPos >= 0) {
            vertices[index + norPos + 0] = info.normal.x;
            vertices[index + norPos + 1] = info.normal.y;
            vertices[index + norPos + 2] = info.normal.z;
        }
        if (uvPos >= 0) {
            vertices[index + uvPos + 0] = info.uv.x;
            vertices[index + uvPos + 1] = info.uv.y;
        }
        if (colPos >= 0) {
            vertices[index + colPos + 0] = info.color.r;
            vertices[index + colPos + 1] = info.color.g;
            vertices[index + colPos + 2] = info.color.b;
            vertices[index + colPos + 3] = info.color.a;
        }
    }

    /**
     * Find a height value for the specified grid coordinate, if such
     * a value exists.
     * @param x the x coordinate of the regular grid for desired height value
     * @param y the y coordinate of the regular grid for desired height value
     * @return the specified height value if it exists, zero otherwise
     */
    public float getHeightValue(int x, int y) {
        final int index = y * numVerticesWide + x;
        return ((index < 0 || index >= heights.length) ? 0f : heights[index]);
    }

    private Vector3 getPositionAt (Vector3 out, int x, int y) {
        out.set(x, getHeightValue(x, y), y);
        return out;
    }

    private Vector3 tmpV1 = new Vector3();
    private Vector3 tmpV2 = new Vector3();
    private Vector3 tmpV3 = new Vector3();
    private Vector3 tmpV4 = new Vector3();
    private Vector3 tmpV5 = new Vector3();
    private Vector3 tmpV6 = new Vector3();
    private Vector3 tmpV7 = new Vector3();

    private Vector3 getWeightedNormalAt(Vector3 out, int x, int y) {
        // This commented code is based on http://www.flipcode.com/archives/Calculating_Vertex_Normals_for_Height_Maps.shtml
        // Note that this approach only works for a heightfield on the XZ plane with a magnitude on the y axis
        // float sx = data[(x < numVerticesWide - 1 ? x + 1 : x) + y * numVerticesWide] + data[(x > 0 ? x-1 : x) + y * numVerticesWide];
        // if (x == 0 || x == (numVerticesWide - 1))
        // sx *= 2f;
        // float sy = data[(y < height - 1 ? y + 1 : y) * numVerticesWide + x] + data[(y > 0 ? y-1 : y) * numVerticesWide + x];
        // if (y == 0 || y == (height - 1))
        // sy *= 2f;
        // float xScale = (corner11.x - corner00.x) / (numVerticesWide - 1f);
        // float zScale = (corner11.z - corner00.z) / (height - 1f);
        // float yScale = magnitude.len();
        // out.set(-sx * yScale, 2f * xScale, sy*yScale*xScale / zScale).nor();
        // return out;

        // The following approach weights the normal of the four triangles (half quad) surrounding the position.
        // A more accurate approach would be to weight the normal of the actual triangles.
        int faces = 0;
        out.set(0, 0, 0);

        Vector3 center = getPositionAt(tmpV1, x, y);
        Vector3 left = x > 0 ? getPositionAt(tmpV2, x - 1, y) : null;
        Vector3 right = x < (numVerticesWide - 1) ? getPositionAt(tmpV3, x + 1, y) : null;
        Vector3 bottom = y > 0 ? getPositionAt(tmpV4, x, y - 1) : null;
        Vector3 top = y < (numVerticesLong - 1) ? getPositionAt(tmpV5, x, y + 1) : null;
        if (top != null && left != null) {
            out.add(tmpV6.set(top).sub(center).nor().crs(tmpV7.set(center).sub(left).nor()).nor());
            faces++;
        }
        if (left != null && bottom != null) {
            out.add(tmpV6.set(left).sub(center).nor().crs(tmpV7.set(center).sub(bottom).nor()).nor());
            faces++;
        }
        if (bottom != null && right != null) {
            out.add(tmpV6.set(bottom).sub(center).nor().crs(tmpV7.set(center).sub(right).nor()).nor());
            faces++;
        }
        if (right != null && top != null) {
            out.add(tmpV6.set(right).sub(center).nor().crs(tmpV7.set(center).sub(top).nor()).nor());
            faces++;
        }
        if (faces != 0)
            out.scl(1f / (float) faces);
        else
            out.set(0, 1, 0);
        return out;
    }

    public float getWidth() {
        return numVerticesWide;
    }

    public float getLength() {
        return numVerticesLong;
    }

}
