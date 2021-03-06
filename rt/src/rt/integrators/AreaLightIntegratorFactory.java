package rt.integrators;

import rt.Integrator;
import rt.Sampler;
import rt.Scene;
import rt.importanceSampling.Heuristic;
import rt.importanceSampling.PowerHeuristic;
import rt.importanceSampling.SamplingTechnique;
import rt.samplers.RandomSampler;

/**
 * Makes a {@link PointLightIntegrator}.
 */
public class AreaLightIntegratorFactory extends WhittedIntegratorFactory {

    SamplingTechnique samplingTechnique;
    Heuristic heuristic;

    public AreaLightIntegratorFactory()
    {
        super();
        this.samplingTechnique = SamplingTechnique.MIS;
        this.heuristic = new PowerHeuristic();
    }

    public Integrator make(Scene scene)
    {
        AreaLightIntegrator integrator = new AreaLightIntegrator(scene);
        integrator.recursionDepth = this.recursionDepth;
        integrator.samplingTechnique = this.samplingTechnique;
        integrator.heuristic = heuristic;
        return integrator;
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
