package com.lando.systems.July15GAM;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.lando.systems.July15GAM.utils.Assets;

/**
 * Brian Ploeckelman created on 7/24/2015.
 */
public class Player {

    private static final float ACCEL = 10f;

    public final Vector3       position;
    public final Vector3       velocity;
    public final ModelInstance model;

    public Player() {
        position = new Vector3(32f, 3f, 5f);
        velocity = new Vector3();
        model = new ModelInstance(Assets.shipModel);
    }

    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x = ACCEL;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x = -ACCEL;
        }
        velocity.scl(delta);
        position.add(velocity);
        model.transform.setTranslation(position);
    }

    public void render(ModelBatch batch, Environment environment) {
        batch.render(model, environment);
    }

}
