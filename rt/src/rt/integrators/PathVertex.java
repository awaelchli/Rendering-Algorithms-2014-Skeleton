package rt.integrators;

import rt.HitRecord;
import rt.Material;

/**
 * Created by adrian on 06.05.16.
 */
public class PathVertex
{
    PathVertex next;
    HitRecord hitRecord;
    Material.ShadingSample shadingSample;
    int index;

    public boolean isRoot()
    {
        return index == 0;
    }
}
