package uk.cloudmc.swrc.util;

import java.awt.*;

public class ColorUtil {
    public static int lerpColor(int colorA, int colorB, float t) {
        int aA = (colorA >>> 24) & 0xFF;
        int rA = (colorA >>> 16) & 0xFF;
        int gA = (colorA >>> 8) & 0xFF;
        int bA = colorA & 0xFF;

        int aB = (colorB >>> 24) & 0xFF;
        int rB = (colorB >>> 16) & 0xFF;
        int gB = (colorB >>> 8) & 0xFF;
        int bB = colorB & 0xFF;

        int a = Math.round(aA + (aB - aA) * t);
        int r = Math.round(rA + (rB - rA) * t);
        int g = Math.round(gA + (gB - gA) * t);
        int b = Math.round(bA + (bB - bA) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int lerpHue(int colorA, int colorB, float t) {
        int aA = (colorA >>> 24) & 0xFF;
        int rA = (colorA >>> 16) & 0xFF;
        int gA = (colorA >>> 8) & 0xFF;
        int bA = colorA & 0xFF;

        int aB = (colorB >>> 24) & 0xFF;
        int rB = (colorB >>> 16) & 0xFF;
        int gB = (colorB >>> 8) & 0xFF;
        int bB = colorB & 0xFF;

        // Convert RGB to HSB
        float[] hsbA = Color.RGBtoHSB(rA, gA, bA, null);
        float[] hsbB = Color.RGBtoHSB(rB, gB, bB, null);

        // Interpolate hue, taking shortest path around the circle
        float hueA = hsbA[0];
        float hueB = hsbB[0];

        float deltaHue = hueB - hueA;

        if (deltaHue > 0.5f) {
            deltaHue -= 1.0f;
        } else if (deltaHue < -0.5f) {
            deltaHue += 1.0f;
        }

        float hue = (hueA + deltaHue * t) % 1.0f;
        if (hue < 0) hue += 1.0f;

        float saturation = hsbA[1] + (hsbB[1] - hsbA[1]) * t;
        float brightness = hsbA[2] + (hsbB[2] - hsbA[2]) * t;

        int alpha = Math.round(aA + (aB - aA) * t);

        int rgb = Color.HSBtoRGB(hue, saturation, brightness);

        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}
