package rt.textures;

import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 13.03.2016.
 */
public class NormalMap extends NormalDisplacement {

    Texture map;

    public NormalMap(Texture texture) {
        this.map = texture;
    }

    @Override
    public Vector3f getTangentSpaceNormal(float u, float v) {

        Spectrum rgb = map.lookUp(u, v);
        Vector3f normal = new Vector3f(rgb.r, rgb.g, rgb.b);
        normal.normalize();

        return normal;
    }
}
