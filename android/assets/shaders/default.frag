#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord0;

uniform sampler2D u_texture;

void main() {
	vec4 texColor = texture2D(u_texture, v_texCoord0);
	gl_FragColor = v_color * texColor;
}