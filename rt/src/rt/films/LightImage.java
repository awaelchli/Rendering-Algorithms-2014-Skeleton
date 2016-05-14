package rt.films;

import rt.Film;
import rt.Spectrum;

/**
 * A light-image used for bidirectional path tracing.
 */
public class LightImage implements Film
{
    private int width, height;
    private Spectrum[][] image;

    public LightImage(int width, int height)
    {
        this.width = width;
        this.height = height;
        init();
    }

    private void init()
    {
        image = new Spectrum[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                image[i][j] = new Spectrum();
            }
        }
    }

    /**
     * Scale all values of this film by {@param scale}.
     */
    public void scale(float scale)
    {
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                image[i][j].mult(scale);
            }
        }
    }

    @Override
    public void addSample(double x, double y, Spectrum s)
    {
        int _x = (int) x;
        int _y = (int) y;
        if(_x >= 0 && _x < width && _y >= 0 && _y < height)
        {
            image[_x][_y].add(s);
        }
    }

    @Override
    public Spectrum[][] getImage()
    {
        return image;
    }

    @Override
    public int getWidth()
    {
        return this.width;
    }

    @Override
    public int getHeight()
    {
        return this.height;
    }
}
