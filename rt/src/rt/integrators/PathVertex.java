package rt.integrators;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.StaticVecmath;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 06.05.16.
 */
public class PathVertex
{
    HitRecord hitRecord;
    Material.ShadingSample shadingSample;
    int index;
    Spectrum alpha;
    float pE, pL;

    public boolean isRoot()
    {
        return index == 0;
    }

    public Vector3f vector(PathVertex to)
    {
        return StaticVecmath.sub(to.hitRecord.position, this.hitRecord.position);
    }

    public float dist2(PathVertex other)
    {
        return vector(other).lengthSquared();
    }
}
