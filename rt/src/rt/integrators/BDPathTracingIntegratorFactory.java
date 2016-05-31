package rt.integrators;

import rt.*;
import rt.films.BoxFilterFilm;
import rt.films.LightImage;
import rt.tonemappers.ClampTonemapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    private LightImage lightImage;

    @Override
    public Integrator make(Scene scene)
    {
        BDPathTracingIntegrator integrator = new BDPathTracingIntegrator(scene);
        integrator.minEyeVertices = minEyeVertices;
        integrator.maxEyeVertices = maxEyeVertices;
        integrator.minLightVertices = minLightVertices;
        integrator.maxLightVertices = maxLightVertices;
        integrator.eyeTerminationProbability = eyePathTerminationProbability;
        integrator.lightTerminationProbability = lightPathTerminationProbability;
        integrator.lightImage = this.lightImage;
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
        lightImage = new LightImage(scene.getFilm().getWidth(), scene.getFilm().getHeight());
    }

    public void writeLightImage(String s)
    {
        BufferedImage img = new ClampTonemapper().process(lightImage);

        try
        {
            ImageIO.write(img, "png", new File(s + ".png"));
        } catch (IOException e) {
            System.out.println("Could not write light image.");
        }
    }

    public void addLightImage(Film film)
    {
        film.add(lightImage);
    }
}
