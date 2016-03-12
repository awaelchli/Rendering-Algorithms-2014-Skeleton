package rt;

import javax.vecmath.*;

/**
 * A ray represented by an origin and a direction.
 */
public class Ray {

	public Point3f origin;
	public Vector3f direction;
	
	public Ray(Point3f origin, Vector3f direction)
	{
		this.origin = new Point3f(origin); 
		this.direction = new Vector3f(direction);
	}
	
	public Point3f pointAt(float t) {
		Point3f p = new Point3f(direction);
		p.scaleAdd(t, origin);
		return p;
	}

	/**
	 * Translates the origin of the ray by the given vector.
     */
	public void translate(Vector3f translation) {
		origin.add(translation);
	}

	public static Ray reflect(HitRecord hitRecord) {

		Vector3f incident = new Vector3f(hitRecord.w);
		incident.negate();

		float s = -2 * incident.dot(hitRecord.normal);

		Vector3f r = new Vector3f();
		r.scaleAdd(s, hitRecord.normal, incident);

		return new Ray(hitRecord.position, r);
	}
}
