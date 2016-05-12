package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

/**
 * Created by Adrian on 23.04.2016.
 */
public class PathTracingIntegratorFactory implements IntegratorFactory
{
    int maxDepth = PathTracingIntegrator.DEFAULT_MAX_DEPTH;
    int minDepth = PathTracingIntegrator.DEFAULT_MIN_DEPTH;
    float terminationProbability = PathTracingIntegrator.DEFAULT_TERMINATION_PROBABILITY;
    float shadowRayContributionThreshold = PathTracingIntegrator.DEFAULT_SHADOWRAY_CONTRIBUTION_THRESHOLD;

    @Override
    public Integrator make(Scene scene)
    {
        PathTracingIntegrator integrator = new PathTracingIntegrator(scene);
        integrator.maxDepth = this.maxDepth;
        integrator.minDepth = this.minDepth;
        integrator.terminationProbability = this.terminationProbability;
        integrator.shadowRayContributionThreshold = this.shadowRayContributionThreshold;
        return integrator;
    }

    @Override
    public void prepareScene(Scene scene)
    {

    }

    public void setMaxDepth(int maxDepth)
    {
        this.maxDepth = maxDepth;
    }

    public void setMinDepth(int minDepth)
    {
        this.minDepth = minDepth;
    }

    public void setTerminationProbability(float terminationProbability)
    {
        this.terminationProbability = terminationProbability;
    }

    public void setShadowRayContributionThreshold(float shThresh)
    {
        this.shadowRayContributionThreshold = shThresh;
    }
}
