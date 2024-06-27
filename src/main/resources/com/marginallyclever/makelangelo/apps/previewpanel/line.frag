#version 330 core

in vec4 fragmentColor;
in vec2 lineStart;
in vec2 lineEnd;

out vec4 finalColor;

uniform float thickness;
uniform float feather; // Control the anti-aliasing width
uniform float zoom;

// Function to compute the shortest distance from a point to a line segment
float sdLine(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

void main() {
    // Calculate the distance from the fragment to the nearest line segment
    float d = sdLine(gl_FragCoord.xy, lineStart, lineEnd);
    // Use smoothstep for anti-aliasing
    float alpha = smoothstep((thickness - feather)*zoom, (thickness + feather)*zoom, d);

    finalColor = vec4(fragmentColor.rgb, fragmentColor.a * (1.0-alpha));
}
