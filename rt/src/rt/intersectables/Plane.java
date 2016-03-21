package rt.intersectables;

import javax.vecmath.*;

import rt.*;
import rt.materials.Diffuse;

/**
 * A plane that can be intersected by a ray.
 */
public class Plane implements Intersectable {

	Vector3f normal;
	float d;
	public Material material;

	/**
	 * Construct a plane given its normal @param n and distance to the origin @param
	 * d. Note that the distance is along the direction that the normal points.
	 * The sign matters!
	 * 
	 * @param normal
	 *            normal of the plane
	 * @param d
	 *            distance to origin measured along normal direction
	 */
	public Plane(Vector3f normal, float d) {
		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));

		this.normal = new Vector3f(normal);
		this.normal.normalize();
		this.d = d;
	}

	public HitRecord intersect(Ray r) {

		float tmp = normal.dot(r.direction);

		if (tmp != 0) {
			float t = -(normal.dot(new Vector3f(r.origin)) + d) / tmp;
			if (t <= 0)
				return null;
			Point3f position = r.pointAt(t);
			Vector3f retNormal = new Vector3f(normal);
			// wIn is incident direction; convention is that it points away from
			// surface
			Vector3f wIn = new Vector3f(r.direction);
			wIn.negate();
			wIn.normalize();
			HitRecord hit = new HitRecord(t, position, retNormal, wIn, this, material,0.f, 0.f);

			// Compute texture coordinates
			Tuple2f texCoords = getTexCoords(position, hit.t1, hit.t2);
			hit.u = texCoords.x;
			hit.v = texCoords.y;

			return hit;
		} else {
			return null;
		}
	}

	private Tuple2f getTexCoords(Point3f position, Vector3f tangentU, Vector3f tangentV) {
		Vector3f p = new Vector3f(position);

		// Center of plane (closest point to origin)
		Vector3f c = new Vector3f(normal);
		c.scale(d);

		Vector3f d = new Vector3f();
		d.sub(p, c);

		float u = d.dot(tangentU);
		float v = d.dot(tangentV);

		return new Point2f(u, v);
	}

	@Override
	/**
	 * @return null. Axis aligned bounding boxes are not supported for infinite planes.
	 */
	public BoundingBox getBoundingBox() {
		return null;
	}

}
