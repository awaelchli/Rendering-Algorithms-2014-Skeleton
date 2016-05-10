package rt.integrators;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

/**
 * Created by adrian on 06.05.16.
 */
public class PathVertex
{
    HitRecord hitRecord;
    Material.ShadingSample shadingSample;
    public int index;
    public Spectrum alpha;

    public boolean isRoot()
    {
        return index == 0;
    }
}
