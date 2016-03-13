package rt.textures;

import rt.Spectrum;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 13.03.2016.
 */
public class BumpMap extends NormalDisplacement {

    Texture bump;

    public BumpMap(Texture texture) {
        this.bump = texture;
    }

    @Override
    public Vector3f getTangentSpaceNormal(float u, float v) {
        // The new normal in local coordinates
        float du = derivativeU(u, v);
        float dv = derivativeV(u, v);
        Vector3f localNormal = new Vector3f(du, dv, 1);
        localNormal.normalize();
        return localNormal;
    }

    private float derivativeU (float u, float v) {
        float left = toGrayscale(bump.lookUp(u, v));
        float right = toGrayscale(bump.lookUp(u + deltaU(), v));
        return right - left;
    }

    private float derivativeV (float u, float v) {
        float bottom = toGrayscale(bump.lookUp(u, v));
        float top = toGrayscale(bump.lookUp(u, v + deltaV()));
        return top - bottom;
    }

    private float toGrayscale(Spectrum rgb) {
        return (rgb.r + rgb.g + rgb.b) / 3;
    }

    private float deltaU() {
        return 1f / bump.getWidth();
    }

    private float deltaV() {
        return 1f / bump.getHeight();
    }


}
