package com.lando.systems.July15GAM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.lando.systems.July15GAM.July15GAM;
import com.lando.systems.July15GAM.utils.Assets;

/**
 * Brian Ploeckelman created on 7/2/2015.
 */
public class AtmosphereScreen extends ScreenAdapter {

    private static final float HALF_PI     = MathUtils.PI / 2.f;
    public static final  float INFINITY    = 3.3e+38f;
    public static final  float RAD_PER_SEC = 0.000072921150f;

    private OrthographicCamera camera;
    private Mesh               quad;
    private ShaderProgram      atmosphereShader;

    /**
     * Distribution coefficients for the luminance(Y) distribution function
     */
    private float distributionLuminance[][] = { // Perez distributions
                                                {  0.17872f, -1.46303f }, // a = darkening or brightening of the horizon
                                                { -0.35540f,  0.42749f }, // b = luminance gradient near the horizon,
                                                { -0.02266f,  5.32505f }, // c = relative intensity of the
                                                // circumsolar region
                                                {  0.12064f, -2.57705f }, // d = width of the circumsolar region
                                                { -0.06696f,  0.37027f } }; // e = relative backscattered light
    /**
     * Distribution coefficients for the x distribution function
     */
    private float distributionXcomp[][]     = { { -0.01925f, -0.25922f },
                                                { -0.06651f,  0.00081f }, { -0.00041f, 0.21247f },
                                                { -0.06409f, -0.89887f }, { -0.00325f, 0.04517f } };
    /**
     * Distribution coefficients for the y distribution function
     */
    private float distributionYcomp[][]     = { { -0.01669f, -0.26078f },
                                                { -0.09495f,  0.00921f }, { -0.00792f, 0.21023f },
                                                { -0.04405f, -1.65369f }, { -0.01092f, 0.05291f } };
    /**
     * Zenith x value
     */
    private float zenithXmatrix[][]         = {
            {  0.00165f, -0.00375f,  0.00209f, 0.00000f },
            { -0.02903f,  0.06377f, -0.03202f, 0.00394f },
            {  0.11693f, -0.21196f,  0.06052f, 0.25886f } };
    /**
     * Zenith y value
     */
    private float zenithYmatrix[][]         = {
            {  0.00275f, -0.00610f,  0.00317f, 0.00000f },
            { -0.04214f,  0.08970f, -0.04153f, 0.00516f },
            {  0.15346f, -0.26756f,  0.06670f, 0.26688f } };

    public AtmosphereScreen(July15GAM game) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera(1, h / w);

        // We wont need indices if we use GL_TRIANGLE_FAN to draw our quad
        // TRIANGLE_FAN will draw the verts in this order: 0, 1, 2; 0, 2, 3
        quad = new Mesh(true, 4, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"));

        // Set our verts up in a CCW (Counter Clock Wise) order
        quad.setVertices(new float[] { -5f, -5f, // bottom left
                                        5f, -5f, // bottom right
                                        5f,  5f, // top right
                                       -5f,  5f }); // top left

        quad.setAutoBind(true);

        atmosphereShader = Assets.atmosphereShader;
    }

    @Override
    public void dispose() {
        quad.dispose();
    }

    private float thetaSun = MathUtils.degreesToRadians * 10.f;
    private float   phiSun;
    private float   turbidity = 2.f;
    private float   overcast;
//    private boolean isLinearExpControl;
    private float   exposure;
    private float   gammaCorrection;
    private float   zenithLuminance;
    private float   zenithX;
    private float   zenithY;
    private float[] perezLuminance;
    private float[] perezX;
    private float[] perezY;
    private Vector3 sunDirection = new Vector3();

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        thetaSun += (RAD_PER_SEC * 1000) / Gdx.graphics.getFramesPerSecond();
        if (thetaSun > HALF_PI + (MathUtils.degreesToRadians * 30.f)) {
            thetaSun = 0;
        }
        phiSun = MathUtils.degreesToRadians * 90.f;

        turbidity = 2.f;
        turbidity = MathUtils.clamp(turbidity, 1.0f, 512.0f);

        overcast = 0.f;
        overcast = MathUtils.clamp(overcast, 0.0f, 1.0f);

        //		isLinearExpControl = true;


        //		// Numbers to squash exp curve
        //		// 1 3 -0.3 11
        //		// 0.5 3.2 -0.3 14
        //		// 0.5 3.5 -0.3 13.5
        //		// 0.3 3.3 -0.3 17.2
        //		// 0.1 3.3 -0.7 17.2
        //		double a = 0.1; // Vertical Stretching or Compression
        //		double b = 3.3; // Horizontal Stretching or Compression
        //		double c = -0.7; // Translate Graph Horizontally
        //		double d = 17.2; // Translate Graph Vertically, Minimum exposure of 15
        //		exposure = (float) (a
        //				* Math.exp(b * ((thetaSun / HALF_PI) - c)) + d);

        exposure = 18.f;
        exposure = 1.0f / MathUtils.clamp(exposure, 1.0f, INFINITY);

        // Start fading out gammaCorrection after sun passes 70 degrees
        gammaCorrection = 1.f / MathUtils.clamp(2.5f * ((MathUtils.degreesToRadians * 70.f) / thetaSun), 1.f, 2.5f);

        // gammaCorrection = 1.0f / FastMath.clamp(gammaCorrection, 1.f,
        // INFINITY);

        float chi = ((4.0f / 9.0f) - (turbidity / 120.0f))
                    * (MathUtils.PI - (2.0f * thetaSun));
        zenithLuminance = ((4.0453f * turbidity) - 4.9710f) * tan(chi)
                          - (0.2155f * turbidity) + 2.4192f;
        if (zenithLuminance < 0.0f) {
            zenithLuminance = -zenithLuminance;
        }

        sunDirection.x = MathUtils.cos(HALF_PI - thetaSun) * MathUtils.cos(phiSun);
        sunDirection.y = MathUtils.sin(HALF_PI - thetaSun);
        sunDirection.z = MathUtils.cos(HALF_PI - thetaSun) * MathUtils.sin(phiSun);
        sunDirection.nor();

        // get x / y zenith
        zenithX = getZenith(zenithXmatrix, thetaSun, turbidity);
        zenithY = getZenith(zenithYmatrix, thetaSun, turbidity);

        // get perez function parameters
        perezLuminance = getPerez(distributionLuminance, turbidity);
        perezX = getPerez(distributionXcomp, turbidity);
        perezY = getPerez(distributionYcomp, turbidity);

        // make some precalculation
        zenithX = perezFunctionO1(perezX, thetaSun, zenithX);
        zenithY = perezFunctionO1(perezY, thetaSun, zenithY);
        zenithLuminance = perezFunctionO1(perezLuminance, thetaSun, zenithLuminance);

        atmosphereShader.begin();

        // uniform vec3 windowDimensions;
        atmosphereShader.setUniform3fv("windowDimensions", new float[] {
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0.f }, 0, 3);

        // uniform vec3 sunDirection;
        atmosphereShader.setUniform3fv("sunDirection",
                                       new float[] { sunDirection.x, sunDirection.y, sunDirection.z },
                                       0,
                                       3);

        // uniform vec3 zenithData; //zenithX, zenithY, zenithLuminance
        atmosphereShader.setUniform3fv("zenithData", new float[] { zenithX, zenithY, zenithLuminance }, 0, 3);

        // uniform float perezLuminance[5];
        atmosphereShader.setUniform1fv("perezLuminance", perezLuminance, 0, perezLuminance.length);

        // uniform float perezX[5];
        atmosphereShader.setUniform1fv("perezX", perezX, 0, perezX.length);

        // uniform float perezY[5];
        atmosphereShader.setUniform1fv("perezY", perezY, 0, perezY.length);

        //		// uniform float exposure;
        //		atmosphereShader.setUniformf("exposure", exposure);
        //
        //		//uniform float overcast;
        //		atmosphereShader.setUniformf("overcast", overcast);

        //uniform vec3 colourCorrection; //exposure, overcast, gammaCorrection
        atmosphereShader.setUniform3fv("colourCorrection", new float[] { exposure, overcast, gammaCorrection }, 0, 3);

        quad.render(atmosphereShader, GL20.GL_TRIANGLE_FAN);
        atmosphereShader.end();
    }

    public static float exp(float fValue) {
        return (float) Math.exp(fValue);
    }

    public static float tan(float fValue) {
        return (float) Math.tan(fValue);
    }

    public static float sqr(float fValue) {
        return fValue * fValue;
    }

    private float getZenith(float[][] zenithMatrix, float theta, float turbidity) {
        float theta2 = theta * theta;
        float theta3 = theta * theta2;

        return (zenithMatrix[0][0] * theta3 + zenithMatrix[0][1]  * theta2
             +  zenithMatrix[0][2] * theta  + zenithMatrix[0][3]) * turbidity * turbidity
             + (zenithMatrix[1][0] * theta3 + zenithMatrix[1][1]  * theta2
             +  zenithMatrix[1][2] * theta  + zenithMatrix[1][3]) * turbidity
             + (zenithMatrix[2][0] * theta3 + zenithMatrix[2][1]  * theta2
             +  zenithMatrix[2][2] * theta  + zenithMatrix[2][3]);
    }

    private float[] getPerez(float[][] distribution, float turbidity) {
        float[] perez = new float[5];
        perez[0] = distribution[0][0] * turbidity + distribution[0][1];
        perez[1] = distribution[1][0] * turbidity + distribution[1][1];
        perez[2] = distribution[2][0] * turbidity + distribution[2][1];
        perez[3] = distribution[3][0] * turbidity + distribution[3][1];
        perez[4] = distribution[4][0] * turbidity + distribution[4][1];
        return perez;
    }

    private float perezFunctionO1(float[] perezCoeffs, float thetaSun,
                                  float zenithValue) {
        float val = (1.0f + perezCoeffs[0] * exp(perezCoeffs[1]))
                  * (1.0f + perezCoeffs[2] * exp(perezCoeffs[3] * thetaSun)
                  +  perezCoeffs[4] * sqr(MathUtils.cos(thetaSun)));
        return zenithValue / val;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

}
