package rt.integrators;

import rt.HitRecord;
import rt.Material;

/**
 * Created by adrian on 06.05.16.
 */
public class PathVertex
{
    HitRecord hitRecord;
    Material.ShadingSample shadingSample;
    public int index;

    public boolean isRoot()
    {
        return index == 0;
    }
}
