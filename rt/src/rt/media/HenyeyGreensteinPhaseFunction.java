package rt.media;

import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 30.05.16.
 */
public class HenyeyGreensteinPhaseFunction implements PhaseFunction
{
    Spectrum g;

    public HenyeyGreensteinPhaseFunction(Spectrum g)
    {
        this.g = g;
    }

    @Override
    public Spectrum probability(Vector3f wIn, Vector3f wOut)
    {
        float cos = wIn.dot(wOut);
        Spectrum g_squared = new Spectrum(g);
        g_squared.mult(g);
        double fourPI = 4 * Math.PI;

        double r = (1 - g_squared.r) / (fourPI * Math.pow(1 + g_squared.r - 2 * this.g.r * cos, 1.5));
        double g = (1 - g_squared.g) / (fourPI * Math.pow(1 + g_squared.g - 2 * this.g.g * cos, 1.5));
        double b = (1 - g_squared.b) / (fourPI * Math.pow(1 + g_squared.b - 2 * this.g.b * cos, 1.5));

        return new Spectrum((float) r, (float) g, (float) b);
    }
}
