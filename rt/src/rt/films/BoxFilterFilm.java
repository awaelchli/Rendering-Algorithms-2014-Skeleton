package rt.films;

import rt.Film;
import rt.Spectrum;

/**
 * Uses a box filter when accumulating samples on a film. A box filter means
 * that samples contribute only to the pixel that they lie in. Sample values
 * are simply averaged.
 */
public class BoxFilterFilm implements Film {
	
	private Spectrum[][] image;
	public int width, height;
	private Spectrum[][] unnormalized;
	private float nSamples[][];
	
	public BoxFilterFilm(int width, int height)
	{
		this.width = width;
		this.height = height;
		image = new Spectrum[width][height];
		unnormalized = new Spectrum[width][height];
		nSamples = new float[width][height];
		
		for(int i=0; i<width; i++)
		{
			for(int j=0; j<height; j++)
			{
				image[i][j] = new Spectrum();
				unnormalized[i][j] = new Spectrum();
			}
		}
	}
	
	public void addSample(double x, double y, Spectrum s)
	{
		if((int)x>=0 && (int)x<width && (int)y>=0 && (int)y<height)
		{
			int idx_x = (int)x;
			int idx_y = (int)y;
			unnormalized[idx_x][idx_y].add(s);
			nSamples[idx_x][idx_y]++;

			image[idx_x][idx_y] = new Spectrum(unnormalized[idx_x][idx_y]);
			image[idx_x][idx_y].mult(1f / nSamples[idx_x][idx_y]);
		}
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}

	public Spectrum[][] getImage()
	{
		return image;
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
