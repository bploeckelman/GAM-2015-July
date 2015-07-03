package com.lando.systems.July15GAM.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.lando.systems.July15GAM.July15GAM;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = July15GAM.win_width;
		config.height = July15GAM.win_height;
		config.resizable = July15GAM.win_resizeable;
		config.title = July15GAM .win_title;
		config.backgroundFPS = July15GAM.win_bgfps;
		new LwjglApplication(new July15GAM(), config);
	}
}
