package rt.media;

import rt.HitRecord;
import rt.Material;
import rt.Medium;
import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 30.05.16.
 */
public class Milk extends Homogeneous
{

    @Override
    public PhaseFunction getPhaseFunction()
    {
        return new HenyeyGreensteinPhaseFunction(new Spectrum(0.932f, 0.902f, 0.859f));
    }

    @Override
    public Spectrum getAbsorptionCoefficient()
    {
        return new Spectrum(0.000002f, 0.000004f, 0.000008f);
    }

    @Override
    public Spectrum getScatteringCoefficient()
    {
        return new Spectrum(0.009124f, 0.010744f, 0.012492f);
    }
}
