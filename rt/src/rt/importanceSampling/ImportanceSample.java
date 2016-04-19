package rt.importanceSampling;

import rt.Spectrum;

public class ImportanceSample
{
    public float directionalProbability;
    public float areaProbability;
    public Spectrum spectrum;

    public ImportanceSample()
    {
        directionalProbability = 0;
        areaProbability = 0;
        spectrum = new Spectrum(0, 0, 0);
    }
}
