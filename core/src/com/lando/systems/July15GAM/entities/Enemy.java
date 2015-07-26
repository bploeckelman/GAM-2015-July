package com.lando.systems.July15GAM.entities;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.lando.systems.July15GAM.utils.Assets;

/**
 * Brian Ploeckelman created on 7/25/2015.
 */
public class Enemy {

    public final Vector3       position;
    public final ModelInstance model;

    public Enemy() {
        position = new Vector3(32f, 3f, 60f);
        model = new ModelInstance(Assets.shipModel);
        model.transform.rotate(0f, 1f, 0f, 180f);
        model.transform.translate(position);
    }

    public void update(float delta) {
        model.transform.setTranslation(position);
    }

    public void render(ModelBatch batch, Environment environment) {
        batch.render(model, environment);
    }

}
