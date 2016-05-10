package rt.integrators;

import rt.*;
import rt.films.BoxFilterFilm;
import rt.tonemappers.ClampTonemapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrian on 04.05.16.
 */
public class BDPathTracingIntegratorFactory implements IntegratorFactory
{
    int minEyeVertices = BDPathTracingIntegrator.DEFAULT_MIN_VERTICES;
    int maxEyeVertices = BDPathTracingIntegrator.DEFAULT_MAX_VERTICES;
    int minLightVertices = BDPathTracingIntegrator.DEFAULT_MIN_VERTICES;
    int maxLightVertices = BDPathTracingIntegrator.DEFAULT_MAX_VERTICES;
    float eyePathTerminationProbability = BDPathTracingIntegrator.DEFAULT_TERMINATION_PROBABILITY;
    float lightPathTerminationProbability = BDPathTracingIntegrator.DEFAULT_TERMINATION_PROBABILITY;

    private List<BDPathTracingIntegrator> integrators;

    public BDPathTracingIntegratorFactory()
    {
        integrators = new ArrayList<>();
    }

    @Override
    public Integrator make(Scene scene)
    {
        BDPathTracingIntegrator integrator = new BDPathTracingIntegrator(scene);
        integrator.minEyeVertices = minEyeVertices;
        integrator.maxEyeVertices = maxEyeVertices;
        integrator.minLightVertices = minLightVertices;
        integrator.maxLightVerices = maxLightVertices;
        integrator.eyeTerminationProbability = eyePathTerminationProbability;
        integrator.lightTerminationProbability = lightPathTerminationProbability;
        integrators.add(integrator);
        return integrator;
    }

    public void setMinEyeVertices(int minEyeVertices)
    {
        this.minEyeVertices = minEyeVertices;
    }

    public void setMaxEyeVertices(int maxEyeVertices)
    {
        this.maxEyeVertices = maxEyeVertices;
    }

    public void setMinLightVertices(int minLightVertices)
    {
        this.minLightVertices = minLightVertices;
    }

    public void setMaxLightVertices(int maxLightVertices)
    {
        this.maxLightVertices = maxLightVertices;
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

    public void writeLightImage(String s)
    {
        BoxFilterFilm film = null;
        for(BDPathTracingIntegrator integrator : integrators)
        {
            if(film == null)
            {
                film = integrator.getLightImage();
                continue;
            }

            Spectrum[][] im = integrator.getLightImage().getImage();
            for(int i = 0; i < im.length; i++) {
                for(int j = 0; j < im[i].length; j++) {
                    film.addSample(i, j, im[i][j]);
                }
            }
        }

        BufferedImage img = new ClampTonemapper().process(film);
        try
        {
            ImageIO.write(img, "png", new File(s + ".png"));
        } catch (IOException e) {
            System.out.println("Could not write light image.");
        }
    }

    public void addLightImage(Film film)
    {
        for(BDPathTracingIntegrator integrator : integrators)
        {
            integrator.addLightImage(film);
        }
    }
}
