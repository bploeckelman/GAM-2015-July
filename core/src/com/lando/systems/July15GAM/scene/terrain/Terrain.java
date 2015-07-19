package com.lando.systems.July15GAM.scene.terrain;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Terrain extends Renderable {

    private Array<TerrainChunk> terrainChunks;

    public Terrain(Array<TerrainChunk> chunks, Array<Matrix4> transformations, Environment environment) {
        final Array<Mesh> meshes = new Array<Mesh>(chunks.size);
        for (TerrainChunk chunk : chunks) {
            chunk.update();
            meshes.add(chunk.mesh);
        }

        this.terrainChunks = chunks;
        this.mesh = mergeMeshes(meshes, transformations);
        this.meshPartOffset = 0;
        this.meshPartSize = mesh.getNumIndices();
        this.material = new Material(); // TODO: materials on a per chunk basis?
        this.primitiveType = GL20.GL_TRIANGLES;
        this.environment = environment;
        this.shader = new DefaultShader(this);
        this.shader.init();
    }

    public void toggleWireframe() {
        if      (primitiveType == GL20.GL_TRIANGLES) primitiveType = GL20.GL_LINE_LOOP;
        else if (primitiveType == GL20.GL_LINE_LOOP)     primitiveType = GL20.GL_TRIANGLES;
    }

    // TODO: work in progress
    private final Vector3 vertexPos = new Vector3();
    public float getHeightAt(float worldX, float worldY) {
        final float chunkScale = 10f;
        final int chunkX = (int) (worldX / chunkScale);
        final int chunkY = (int) (worldY / chunkScale);
        final int numChunksWide = 3;
        final int chunkIndex = chunkY * numChunksWide + chunkX;
        if (chunkIndex < 0 || chunkIndex >= terrainChunks.size) {
            return 0f;
        }

        final TerrainChunk chunk = terrainChunks.get(chunkIndex);
        final int vertIndex = (int) (worldX + worldY * chunk.width);
        if (vertIndex < 0 || vertIndex >= chunk.width * chunk.height) {
            return 0f;
        }
        final float vertexHeight = chunk.getPositionAt(vertexPos, (int) worldX, (int) worldY).y;
        return vertexHeight;
    }

    // ------------------------------------------------------------------------
    // Source: http://www.badlogicgames.com/forum/viewtopic.php?t=10842&p=54709#p54709
    // ------------------------------------------------------------------------

    /**
     * Merge the specified collection of Mesh objects into a single batched Mesh
     *
     * @param meshes collection of Mesh objects to merge
     * @param transformations transformation matrices for each Mesh to merge
     * @return the merged batch Mesh
     */
    private Mesh mergeMeshes(Array<Mesh> meshes, Array<Matrix4> transformations) {
        if (meshes.size == 0) {
            throw new GdxRuntimeException("Attempted to merge meshes without providing anything to merge.");
        }

        // Stash vertex usage attributes
        final VertexAttributes vertexAttribs = meshes.get(0).getVertexAttributes();
        final int vertexUsages[] = new int[vertexAttribs.size()];
        for (int i = 0; i < vertexAttribs.size(); ++i) {
            vertexUsages[i] = vertexAttribs.get(i).usage;
        }

        // Calculate array sizes and stuff
        int vertexArrayTotalSize = 0;
        int indexArrayTotalSize = 0;
        for (int i = 0; i < meshes.size; ++i) {
            final Mesh mesh = meshes.get(i);
            if (mesh.getVertexAttributes().size() != vertexAttribs.size()) {
                meshes.set(i, copyMesh(mesh, true, false, vertexUsages));
            }

            vertexArrayTotalSize += mesh.getNumVertices() * mesh.getVertexSize() / 4;
            indexArrayTotalSize += mesh.getNumIndices();
        }

        final float vertices[] = new float[vertexArrayTotalSize];
        final short indices[] = new short[indexArrayTotalSize];

        int indexOffset = 0;
        int vertexOffset = 0;
        int vertexSizeOffset = 0;
        int vertexSize = 0;

        // For each mesh to merge...
        for (int i = 0; i < meshes.size; ++i) {
            final Mesh mesh = meshes.get(i);

            vertexSize = mesh.getVertexSize() / 4;
            final VertexAttribute posAttr = mesh.getVertexAttribute(Usage.Position);

            int numIndices = mesh.getNumIndices();
            int numVertices = mesh.getNumVertices();
            int baseSize = numVertices * vertexSize;
            int offset = posAttr.offset / 4;
            int numComponents = posAttr.numComponents;

            // Complement the array indices
            mesh.getIndices(indices, indexOffset);
            for(int c = indexOffset; c < (indexOffset + numIndices); ++c) {
                indices[c] += vertexOffset;
            }
            indexOffset += numIndices;

            mesh.getVertices(0, baseSize, vertices, vertexSizeOffset);
            Mesh.transform(transformations.get(i), vertices, vertexSize, offset, numComponents, vertexOffset, numVertices);
            vertexOffset += numVertices;
            vertexSizeOffset += baseSize;
        }

        // Generate the final merged mesh using the merged vertices, indices, and attributes
        final Mesh result = new Mesh(true, vertexOffset, indices.length, meshes.get(0).getVertexAttributes());
        result.setVertices(vertices);
        result.setIndices(indices);
        return result;
    }

    /**
     * Copies the specified Mesh object based on the specified parameters
     *
     * @param meshToCopy input Mesh
     * @param isStatic will the Mesh ever change
     * @param removeDuplicates should we remove duplicate TODO: what, meshes? verts? indices?
     * @param attribUsageFlags array of vertex attribute usage flags
     * @return the copied Mesh object
     */
    private Mesh copyMesh(Mesh meshToCopy, boolean isStatic, boolean removeDuplicates, final int[] attribUsageFlags) {
        final int vertexSize = meshToCopy.getVertexSize() / 4;
        int numVertices = meshToCopy.getNumVertices();
        float[] vertices = new float[numVertices * vertexSize];
        short[] checks = null;
        int newVertexSize = 0;
        VertexAttribute[] attrs = null;

        // Copy mesh vertices into buffer
        meshToCopy.getVertices(0, vertices.length, vertices);

        // If we have a collection of attribute usages...
        if (attribUsageFlags != null) {
            int size = 0;
            int as = 0;

            // Determine how many vertex attribute components we have
            for (int attribUsageFlag : attribUsageFlags) {
                if (meshToCopy.getVertexAttribute(attribUsageFlag) != null) {
                    size += meshToCopy.getVertexAttribute(attribUsageFlag).numComponents;
                    as++;
                }
            }

            // If we have some vertex attribute components...
            if (size > 0) {
                attrs = new VertexAttribute[as];
                checks = new short[size];

                int idx = -1;
                int ai = -1;

                for (int attribUsageFlag : attribUsageFlags) {
                    final VertexAttribute a = meshToCopy.getVertexAttribute(attribUsageFlag);
                    if (a == null) continue;

                    for (int j = 0; j < a.numComponents; ++j) {
                        checks[++idx] = (short) (a.offset / 4 + j);
                    }

                    attrs[++ai] = new VertexAttribute(a.usage, a.numComponents, a.alias);
                    newVertexSize += a.numComponents;
                }
            }
        }

        if (checks == null) {
            checks = new short[vertexSize];
            for (short i = 0; i < vertexSize; ++i) {
                checks[i] = i;
            }
            newVertexSize = vertexSize;
        }

        int numIndices = meshToCopy.getNumIndices();
        short[] indices = null;
        if (numIndices > 0) {
            indices = new short[numIndices];
            meshToCopy.getIndices(indices);

            if (removeDuplicates || newVertexSize != vertexSize) {
                float[] tmp = new float[vertices.length];
                int size = 0;
                for (int i = 0; i < numIndices; ++i) {
                    final int idx1 = indices[i] * vertexSize;
                    short newIndex = -1;

                    if (removeDuplicates) {
                        for (short j = 0; j < size && newIndex < 0; j++) {
                            final int idx2 = j * newVertexSize;
                            boolean found = true;

                            for (int k = 0; k < checks.length && found; ++k) {
                                if (tmp[idx2+k] != vertices[idx1 + checks[k]]) {
                                    found = false;
                                }
                            }

                            if (found) {
                                newIndex = j;
                            }
                        }
                    }

                    if (newIndex > 0) {
                        indices[i] = newIndex;
                    } else {
                        final int idx = size * newVertexSize;
                        for (int j = 0; j < checks.length; ++j) {
                            tmp[idx + j] = vertices[idx1 + checks[j]];
                        }

                        indices[i] = (short) size;
                        size++;
                    }
                }

                vertices = tmp;
                numVertices = size;
            }
        }

        final int maxNumIndices = (indices == null) ? 0 : indices.length;

        // Generate the resultant Mesh from the calculated vertices and indices
        final Mesh result;
        if (attrs == null) result = new Mesh(isStatic, numVertices, maxNumIndices, meshToCopy.getVertexAttributes());
        else               result = new Mesh(isStatic, numVertices, maxNumIndices, attrs);
        result.setVertices(vertices, 0, numVertices * newVertexSize);
        result.setIndices(indices);
        return result;
    }

}
