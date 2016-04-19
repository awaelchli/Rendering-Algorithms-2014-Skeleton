package rt.importanceSampling;

/**
 * A simple enum to characterize the sampling technique used for multiple importance sampling.
 */
public enum SamplingTechnique
{
    Light,  // Samples the light directly on the light sources
    BRDF,   // Samples the BRDF directly with the directional form
    MIS     // Multiple importance sampling, both techniques are applied and combined via a weighted sum
}
