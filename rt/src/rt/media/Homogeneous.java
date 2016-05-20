package rt.media;

import rt.Ray;
import rt.Spectrum;

/**
 * Created by adrian on 20.05.16.
 */
public class Homogeneous
{
    Spectrum sigma_t;
    Spectrum emission;
    int n;

    public Homogeneous(Spectrum sigma_t, Spectrum emission, int nSamples)
    {
        this.sigma_t = new Spectrum(sigma_t);
        this.emission = new Spectrum(emission);
        this.n = nSamples;
    }

    public Spectrum evaluateTransmission(Ray r, float s_in, float s_out)
    {
        Spectrum transmission = new Spectrum(1, 1, 1);

        float ds = (s_out - s_in) / n;
        for(float s_i = s_in; s_i <= s_out; s_i += ds)
        {
            Spectrum c = new Spectrum(sigma_t);
            c.mult(ds);
            c.mult(-1);
            c.add(1);
            transmission.mult(c);
        }

        return transmission;
    }
}
