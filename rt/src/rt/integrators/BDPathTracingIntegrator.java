package rt.integrators;

import rt.Ray;
import rt.Scene;
import rt.Spectrum;

/**
 * Created by adrian on 04.05.16.
 */
public class BDPathTracingIntegrator extends AbstractIntegrator
{
    public BDPathTracingIntegrator(Scene scene)
    {
        super(scene);
    }

    @Override
    public Spectrum integrate(Ray r)
    {
        return null;
    }
}
