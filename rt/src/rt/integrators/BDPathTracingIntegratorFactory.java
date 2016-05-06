package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

/**
 * Created by adrian on 04.05.16.
 */
public class BDPathTracingIntegratorFactory implements IntegratorFactory
{
    int minEyePathLength = BDPathTracingIntegrator.DEFAULT_MIN_DEPTH;
    int maxEyePathLength = BDPathTracingIntegrator.DEFAULT_MAX_DEPTH;
    int minLightPathLength = BDPathTracingIntegrator.DEFAULT_MIN_DEPTH;
    int maxLightPathLength = BDPathTracingIntegrator.DEFAULT_MAX_DEPTH;
    float eyePathTerminationProbability = BDPathTracingIntegrator.DEFAULT_TERMINATION_PROBABILITY;
    float lightPathTerminationProbability = BDPathTracingIntegrator.DEFAULT_TERMINATION_PROBABILITY;

    @Override
    public Integrator make(Scene scene)
    {
        BDPathTracingIntegrator integrator = new BDPathTracingIntegrator(scene);
        integrator.minEyeDepth = minEyePathLength;
        integrator.maxEyeDepth = maxEyePathLength;
        integrator.minLightDepth = minLightPathLength;
        integrator.maxLightDepth = maxLightPathLength;
        integrator.eyeTerminationProbability = eyePathTerminationProbability;
        integrator.lightTerminationProbability = lightPathTerminationProbability;
        return integrator;
    }

    public void setMinEyePathLength(int minEyePathLength)
    {
        this.minEyePathLength = minEyePathLength;
    }

    public void setMaxEyePathLength(int maxEyePathLength)
    {
        this.maxEyePathLength = maxEyePathLength;
    }

    public void setMinLightPathLength(int minLightPathLength)
    {
        this.minLightPathLength = minLightPathLength;
    }

    public void setMaxLightPathLength(int maxLightPathLength)
    {
        this.maxLightPathLength = maxLightPathLength;
    }

    public void setEyePathTerminationProbability(float p)
    {
        this.eyePathTerminationProbability = p;
    }

    public void setLightPathTerminationProbability(float p)
    {
        this.lightPathTerminationProbability = p;
    }

    @Override
    public void prepareScene(Scene scene)
    {

    }
}
