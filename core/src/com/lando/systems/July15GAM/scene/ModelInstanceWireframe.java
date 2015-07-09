package com.lando.systems.July15GAM.scene;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;

/**
 * Brian Ploeckelman created on 7/8/2015.
 *
 * Quick and dirty hack to render Models as wireframe
 * see: http://stackoverflow.com/questions/23462105/libgdx-display-wireframe-of-loaded-model
 */
public class ModelInstanceWireframe extends ModelInstance {

    public ModelInstanceWireframe(final Model model) {
        super(model);
    }

    @Override
    public Renderable getRenderable(final Renderable out, final Node node, final NodePart nodePart) {
        super.getRenderable(out, node, nodePart);
        out.primitiveType = GL20.GL_LINE_STRIP;
//        out.primitiveType = GL20.GL_LINES;
        return out;
    }

}
