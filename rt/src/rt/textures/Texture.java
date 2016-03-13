package rt.textures;

import rt.Spectrum;

import javax.imageio.ImageIO;
import javax.media.jai.InterpolationBilinear;
import javax.vecmath.Tuple2f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Adrian on 12.03.2016.
 */
public class Texture {

    public enum InterpolationMethod {
        nearest, bilinear
    }

    BufferedImage texture;
    InterpolationMethod method;

    /**
     * Creates a texture from an image file.
     * @param filename  The path to the image file
     */
    public Texture(String filename) {
        this(filename, InterpolationMethod.bilinear);
    }

    /**
     * Creates a texture from an image file.
     * @param filename  The path to the image file
     * @param method    The interpolation method to use. The default method is bilinear interpolation.
     */
    public Texture (String filename,  InterpolationMethod method) {
        readImage(filename);
        this.method = method;
    }

    /**
     * Texture look-up.
     *
     * @param coordinate    The tuple of u, v coordinates to query, expected to be in the range [0, 1].
     * @return              The interpolated texture value as a spectrum (red, green, blue).
     */
    public Spectrum lookUp(Tuple2f coordinate) {
        return lookUp(coordinate.x, coordinate.y);
    }

    /**
     * Texture look-up.
     *
     * @param u     The u coordinate (horizontal), expected to be in the range [0, 1].
     * @param v     The v coordinate (vertical), expected to be in the range [0, 1].
     * @return      The interpolated texture value as a spectrum (red, green, blue).
     */
    public Spectrum lookUp(float u, float v) {
        switch (method) {
            case nearest:
                return nearestLookUp(u, v);
            case bilinear:
                return bilinearLookUp(u, v);
        }
        return null;
    }

    /**
     * Texture look-up.
     *
     * @param x     The x coordinate (horizontal), expected to be in the range [0, width].
     * @param y     The y coordinate (vertical), expected to be in the range [0, height].
     * @return      The texture value as a spectrum (red, green, blue).
     */
    public Spectrum lookUp(int x, int y) {
        x = Math.min(Math.max(x, 0), texture.getWidth() - 1);
        y = Math.min(Math.max(y, 0), texture.getWidth() - 1);

        int rgb = texture.getRGB(x, y);
        Color color = new Color(rgb);

        float[] rgb_components = new float[3];
        color.getRGBColorComponents(rgb_components);

        return new Spectrum(rgb_components[0], rgb_components[1], rgb_components[2]);
    }

    private Spectrum nearestLookUp(float u, float v) {

        int x = Math.round(u * (texture.getWidth() - 1));
        int y = Math.round(v * (texture.getHeight() - 1));

        return lookUp(x, y);
    }

    private Spectrum bilinearLookUp(float u, float v) {

        float indexU = (u * (texture.getWidth() - 1));
        float indexV = (v * (texture.getHeight() - 1));

        int s00u = (int) Math.floor(indexU);
        int s00v = (int) Math.ceil(indexV);

        int s01u = (int) Math.ceil(indexU);
        int s01v = (int) Math.ceil(indexV);

        int s10u = (int) Math.floor(indexU);
        int s10v = (int) Math.floor(indexV);

        int s11u = (int) Math.ceil(indexU);
        int s11v = (int) Math.floor(indexV);

        Spectrum s00 = lookUp(s00u, s00v);
        Spectrum s01 = lookUp(s01u, s01v);
        Spectrum s10 = lookUp(s10u, s10v);
        Spectrum s11 = lookUp(s11u, s11v);

        InterpolationBilinear interp = new InterpolationBilinear();
        float r = interp.interpolate(s00.r, s01.r, s10.r, s11.r, u, v);
        float g = interp.interpolate(s00.g, s01.g, s10.g, s11.g, u, v);
        float b = interp.interpolate(s00.b, s01.b, s10.b, s11.b, u, v);

        return new Spectrum(r, g, b);
    }

    private void readImage(String filename) {

        try {
            texture = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.err.println("Texture not found: " + filename);
            return;
        }
    }


    public int getWidth() {
        return texture.getWidth();
    }

    public int getHeight() {
        return texture.getHeight();
    }

 }
