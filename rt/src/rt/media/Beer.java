package rt.media;

import rt.Spectrum;

/**
 * Created by adrian on 31.05.16.
 */
public class Beer extends Homogeneous
{

    private final Spectrum extinction = new Spectrum(0.001486f, 0.003210f, 0.007360f);

    @Override
    public PhaseFunction getPhaseFunction()
    {
        return new HenyeyGreensteinPhaseFunction(new Spectrum(0.917f, 0.956f, 0.982f));
    }

    @Override
    public Spectrum getAbsorptionCoefficient()
    {
        Spectrum absorption = new Spectrum(extinction);
        absorption.sub(getScatteringCoefficient());
        return  absorption;
    }

    @Override
    public Spectrum getScatteringCoefficient()
    {
        return new Spectrum(0.000037f, 0.000069f, 0.000074f);
    }
}
