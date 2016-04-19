package rt.integrators;

import rt.Integrator;
import rt.Sampler;
import rt.Scene;
import rt.importanceSampling.Heuristic;
import rt.importanceSampling.SamplingTechnique;
import rt.samplers.RandomSampler;

/**
 * Makes a {@link PointLightIntegrator}.
 */
public class AreaLightIntegratorFactory extends WhittedIntegratorFactory {

    int numberOfSamples;
    Sampler sampler;
    SamplingTechnique samplingTechnique;
    Heuristic heuristic;

    public AreaLightIntegratorFactory()
    {
        super();
        this.numberOfSamples = 1;
        this.sampler = new RandomSampler();
        this.samplingTechnique = SamplingTechnique.MIS;
    }

    public Integrator make(Scene scene)
    {
        AreaLightIntegrator integrator = new AreaLightIntegrator(scene);
        integrator.recursionDepth = this.recursionDepth;
        integrator.numberOfSamples = this.numberOfSamples;
        integrator.sampler = this.sampler;
        integrator.samplingTechnique = this.samplingTechnique;
        integrator.heuristic = heuristic;
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

    public void setSamplingTechnique(SamplingTechnique samplingTechnique)
    {
        this.samplingTechnique = samplingTechnique;
    }

    public void setHeuristic(Heuristic h)
    {
        this.heuristic = h;
    }
}
