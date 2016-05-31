package rt;

/**
 * Stores a spectrum of color values. In this implementation, we work with RGB colors.
 */
public class Spectrum {

	public float r, g, b;
	
	public Spectrum()
	{
		r = 0.f;
		g = 0.f;
		b = 0.f;
	}
	
	public Spectrum(float r, float g, float b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public Spectrum(Spectrum s)
	{
		this.r = s.r;
		this.g = s.g;
		this.b = s.b;
	}
	
	public void mult(float t)
	{
		r = r*t;
		g = g*t;
		b = b*t;
	}
	
	public void mult(Spectrum s)
	{
		r = r*s.r;
		g = g*s.g;
		b = b*s.b;
	}

	public void div(float d)
	{
		div(new Spectrum(d, d, d));
	}

	public void div(Spectrum d)
	{
		r /= d.r;
		g /= d.g;
		b /= d.b;
	}
	
	public void add(Spectrum s)
	{
		r = r+s.r;
		g = g+s.g;
		b = b+s.b;
	}

	public void sub(Spectrum s)
	{
		r -= s.r;
		g -= s.g;
		b -= s.b;
	}

	public void add(float t)
	{
		add(new Spectrum(t, t, t));
	}
	
	public void clamp(float min, float max)
	{
		r = Math.min(max,  r);
		r = Math.max(min, r);
		g = Math.min(max,  g);
		g = Math.max(min, g);
		b = Math.min(max,  b);
		b = Math.max(min, b);
	}

	@Override
	public String toString() {
		return "Spectrum{" + "r=" + r + ", g=" + g + ", b=" + b + '}';
	}
}
