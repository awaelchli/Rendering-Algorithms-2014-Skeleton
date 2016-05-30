package rt.media;

import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 30.05.16.
 */
public interface PhaseFunction
{
    public Spectrum probability(Vector3f wIn, Vector3f wOut);
}
