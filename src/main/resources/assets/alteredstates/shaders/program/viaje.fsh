#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec3 RedMatrix;
uniform vec3 GreenMatrix;
uniform vec3 BlueMatrix;
uniform vec3 ColorOffset;
uniform vec3 ColorScale;

out vec4 fragColor;

void main() {
    vec4 InTexel = texture(DiffuseSampler, texCoord);

    float RedValue   = dot(InTexel.rgb, RedMatrix);
    float GreenValue = dot(InTexel.rgb, GreenMatrix);
    float BlueValue  = dot(InTexel.rgb, BlueMatrix);

    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);
    OutColor = (OutColor * ColorScale) + ColorOffset;

    fragColor = vec4(OutColor, InTexel.a);
}