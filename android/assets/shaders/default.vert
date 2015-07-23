#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec3 a_normal;

uniform mat4 u_projTrans;

varying LOWP vec4 v_color;
varying vec2 v_texCoord0;

void main() {
	v_color = a_color * a_position.y;
	v_texCoord0 = a_texCoord0;
	gl_Position = u_projTrans * a_position;
}
