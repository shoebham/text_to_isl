// qskin.frag
// Essentially unchanged w.r.t. skinning.frag.

#ifdef GL_ES
	precision highp float;
#endif

uniform sampler2D Texture;

varying vec3 Normal;
varying vec2 TexCoord0;


void main(void)
{
    // Ambient term.
    vec3 lighting = vec3(0.5, 0.5, 0.5) * 0.7;

    // Simple lighting, with 3 directional lights:
    // - forwards and slightly upwards, to illuminate underside of chin;
    // - forwards and from left to right;
    // - forwards and from right to left.
    // The lights are in view space, following the camera.

    vec3 dirscale = vec3(0.8, 0.8, 0.6);
    vec3 dirA = normalize(vec3( 0.0, -0.2, 0.8));
    vec3 dirB = normalize(vec3(-0.8,  0.4, 0.8));
    vec3 dirC = normalize(vec3( 0.8,  0.4, 0.8));

    lighting += dot(Normal, dirA) * dirscale * 0.6;
    lighting += dot(Normal, dirB) * dirscale * 0.4;
    lighting += dot(Normal, dirC) * dirscale * 0.4;

    gl_FragColor = vec4(texture2D(Texture, TexCoord0).xyz * lighting, 1.0);
    //gl_FragColor = vec4(Normal * 0.5 + vec3(0.5), 1.0);
}
