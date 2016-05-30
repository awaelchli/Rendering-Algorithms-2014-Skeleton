package rt;

import rt.Ray;
import rt.Spectrum;
import rt.media.PhaseFunction;

/**
 * Created by Adrian on 22.05.2016.
 */
public interface Medium
{
    public Spectrum evaluateTransmission(Ray r, float s_in, float s_out);

    public Spectrum evaluateTransmission(float ds);

    public PhaseFunction getPhaseFunction();
}
