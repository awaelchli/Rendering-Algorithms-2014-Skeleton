package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

/**
 * Makes a {@link PointLightIntegrator}.
 */
public class WhittedIntegratorFactory implements IntegratorFactory {

    int recursionDepth;

    public WhittedIntegratorFactory()
    {
        this.recursionDepth = 1;
    }

    public Integrator make(Scene scene) {
        WhittedIntegrator integrator = new WhittedIntegrator(scene);
        integrator.recursionDepth = this.recursionDepth;
        return integrator;
    }

    public void setRecursionDepth(int depth) {
        this.recursionDepth = depth;
    }

    public void prepareScene(Scene scene) {
        // TODO Auto-generated method stub
    }

}
