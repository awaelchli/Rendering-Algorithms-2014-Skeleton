package rt;

import javax.vecmath.*;

/**
 * Stores information about a ray-surface intersection. This information 
 * is typically used for shading.
 */
public class HitRecord  {

	/**
	 * Hit position.
	 */
	public Point3f position;
	
	/**
	 * Normal at hit point.
	 */
	public Vector3f normal;
	
	/**
	 * Tangent vectors at hit point.
	 */
	public Vector3f t1, t2;
	
	/**
	 * Texture coordinates at hit point.
	 */
	public float u, v;			
	
	/**
	 * Direction towards origin of ray that hit surface. By convention it points away from 
	 * the surface, that is, in the direction opposite to the incident ray.
	 */
	public Vector3f w;
	
	/**
	 * t parameter of the ray at the hit point.
	 */
	public float t;				
	
	/**
	 * The {@link Intersectable} that was hit.
	 */
	public Intersectable intersectable;	
	
	/** 
	 * The material at the hit point.
	 */
	public Material material;
	
	/**
	 * Area probability density. This is typically used when a hit record is generated by 
	 * sampling the geometry, like in implementations of {@link LightGeometry}. 
	 */
	public float p;
	
	
	public HitRecord ()
	{
		// do nothing
	}
	
	public HitRecord(float t, Point3f position, Vector3f normal, Vector3f w, Intersectable intersectable, Material material, float u, float v)
	{
		this.t = t;
		this.position = position;
		this.normal = normal;
		this.w = w;
		this.intersectable = intersectable;
		this.material = material;
		this.u = u;
		this.v = v;
		
		// Make tangent frame: t1, t2, normal is a right handed frame
		t1 = new Vector3f(1,0,0);
		t1.cross(t1, normal);
		if(t1.length()==0)
		{
			t1 = new Vector3f(0,1,0);
			t1.cross(t1, normal);
		}
		t1.normalize();
		t2 = new Vector3f();
		t2.cross(normal, t1);
	}

	/**
	 * Transforms the points and vectors of this {@link HitRecord}, i.e. position, normal, etc.
	 *
	 * @param t The transformation to be applied.
	 * @param inv Pre-computed inverse for the transformation of the normal.
     */
	public void transform(Matrix4f t, Matrix4f inv) {
		if (position != null) {
			t.transform(position);
		}

		if (normal != null) {
			Matrix4f normalMatrix = new Matrix4f(inv);
			normalMatrix.transpose();
			normalMatrix.transform(normal);
		}

		if (w != null) {
			t.transform(w);
		}

		if (t1 != null && t2 != null) {
			Matrix4f normalMatrix = new Matrix4f(inv);
			normalMatrix.transpose();
			normalMatrix.transform(t1);
			normalMatrix.transform(t2);
		}
	}

	public void transform(Matrix4f t) {
		Matrix4f inv = new Matrix4f(t);
		inv.invert();
		transform(t, inv);
	}
	
}
