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

    @Override
    public Integrator make(Scene scene)

    {
        PathTracingIntegrator integrator = new PathTracingIntegrator(scene);
        integrator.maxDepth = this.maxDepth;
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
}
