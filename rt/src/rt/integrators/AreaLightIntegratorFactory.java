package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

/**
 * Makes a {@link PointLightIntegrator}.
 */
public class AreaLightIntegratorFactory extends WhittedIntegratorFactory {

    float samplesPerUnitArea = 1;

    public Integrator make(Scene scene)
    {
        AreaLightIntegrator integrator = new AreaLightIntegrator(scene, recursionDepth);
        integrator.areaLightSamplesPerUnitArea = samplesPerUnitArea;
        return integrator;
    }

    public void setLightSamplingDensity(float samplesPerUnitArea)
    {
        this.samplesPerUnitArea = samplesPerUnitArea;
    }

}
