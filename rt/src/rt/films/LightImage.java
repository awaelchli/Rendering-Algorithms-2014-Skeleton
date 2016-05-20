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
    private Spectrum[][] unnormalized;
    private int numSamples;

    public LightImage(int width, int height)
    {
        this.width = width;
        this.height = height;
        init();
    }

    private void init()
    {
        unnormalized = new Spectrum[width][height];
        image = new Spectrum[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                unnormalized[i][j] = new Spectrum();
                image[i][j] = new Spectrum();
            }
        }
        numSamples = 0;
    }

    /**
     *  Scale all values of this film by {@param scale}.
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
        numSamples++;
        if(_x >= 0 && _x < width && _y >= 0 && _y < height)
        {
            unnormalized[_x][_y].add(s);

            image[_x][_y] = new Spectrum(unnormalized[_x][_y]);
            image[_x][_y].mult(width * height);
            image[_x][_y].mult(1f / numSamples);
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

    @Override
    public void add(Film film)
    {
        assert film.getHeight() == this.getHeight() && film.getWidth() == this.getWidth();

        Spectrum[][] im = film.getImage();

        for(int i = 0; i < getWidth(); i++)
        {
            for(int j = 0; j < getHeight(); j++)
            {
                image[i][j].add(im[i][j]);
            }
        }
    }
}
