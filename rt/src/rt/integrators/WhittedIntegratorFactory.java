package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

/**
 * Makes a {@link PointLightIntegrator}.
 */
public class WhittedIntegratorFactory implements IntegratorFactory {

    int recursionDepth = 1;

    public Integrator make(Scene scene) {
        return new WhittedIntegrator(scene, recursionDepth);
    }

    public void setRecursionDepth(int depth) {
        this.recursionDepth = depth;
    }

    public void prepareScene(Scene scene) {
        // TODO Auto-generated method stub
    }

}
