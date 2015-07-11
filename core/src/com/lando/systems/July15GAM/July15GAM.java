package com.lando.systems.July15GAM;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.lando.systems.July15GAM.accessors.*;
import com.lando.systems.July15GAM.screens.AtmosphereScreen;
import com.lando.systems.July15GAM.screens.TestScreen;
import com.lando.systems.July15GAM.utils.Assets;

public class July15GAM extends Game {

	public TweenManager tween;

	@Override
	public void create () {
		Assets.load();

		tween = new TweenManager();
		Tween.registerAccessor(Color.class, new ColorAccessor());
		Tween.registerAccessor(Rectangle.class, new RectangleAccessor());
		Tween.registerAccessor(Vector2.class, new Vector2Accessor());
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());

		setScreen(new AtmosphereScreen(this));
//		setScreen(new TestScreen(this));
	}

	@Override
	public void render () {
		float delta = Gdx.graphics.getDeltaTime();
		tween.update(delta);
		super.render();
	}

	@Override
	public void dispose() {
		Assets.dispose();
	}

	public void exit() {
		Gdx.app.exit();
	}

	// ------------------------------------------------------------------------
	// Game Constants
	// ------------------------------------------------------------------------

	public static final int win_width = 640;
	public static final int win_height = 480;
	public static final float win_aspect = (float) win_width / (float) win_height;
	public static final boolean win_resizeable = false;
	public static final String win_title = "GAM - July 2015";
	public static final int win_bgfps = 1;

}
