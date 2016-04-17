package rt.integrators;

import rt.Integrator;
import rt.Sampler;
import rt.Scene;

/**
 * Makes a {@link PointLightIntegrator}.
 */
public class AreaLightIntegratorFactory extends WhittedIntegratorFactory {

    int numberOfSamples = 1;
    Sampler sampler;

    public Integrator make(Scene scene)
    {
        AreaLightIntegrator integrator = new AreaLightIntegrator(scene, recursionDepth);
        integrator.numberOfSamples = this.numberOfSamples;
        integrator.sampler = this.sampler;
        return integrator;
    }

    public void setSamplingDensity(int numberOfSamples)
    {
        this.numberOfSamples = numberOfSamples;
    }

    public void setSampler(Sampler sampler)
    {
        this.sampler = sampler;
    }

}
