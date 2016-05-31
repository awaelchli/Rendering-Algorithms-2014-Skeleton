package rt.media;

import rt.Medium;
import rt.Ray;
import rt.Spectrum;

/**
 * Created by Adrian on 22.05.2016.
 */
public class Opaque implements Medium
{
    @Override
    public Spectrum evaluateTransmission(Ray r, float s_in, float s_out)
    {
        return new Spectrum();
    }

    @Override
    public Spectrum evaluateTransmission(float ds)
    {
        return new Spectrum();
    }

    @Override
    public PhaseFunction getPhaseFunction()
    {
        return null;
    }

    @Override
    public Spectrum getAbsorptionCoefficient()
    {
        return new Spectrum();
    }

    @Override
    public Spectrum getScatteringCoefficient()
    {
        return new Spectrum();
    }
}
