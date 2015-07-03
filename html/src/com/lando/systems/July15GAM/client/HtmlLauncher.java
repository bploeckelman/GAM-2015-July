package com.lando.systems.July15GAM.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.lando.systems.July15GAM.July15GAM;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(July15GAM.win_width, July15GAM.win_height);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new July15GAM();
        }
}