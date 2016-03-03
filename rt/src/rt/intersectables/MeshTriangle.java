package rt.intersectables;

import javax.vecmath.*;
import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

/**
 * Defines a triangle by referring back to a {@link Mesh}
 * and its vertex and index arrays. 
 */
public class MeshTriangle implements Intersectable {

	private Mesh mesh;
	private int index;
	
	/**
	 * Make a triangle.
	 * 
	 * @param mesh the mesh storing the vertex and index arrays
	 * @param index the index of the triangle in the mesh
	 */
	public MeshTriangle(Mesh mesh, int index)
	{
		this.mesh = mesh;
		this.index = index;		
	}
	
	public HitRecord intersect(Ray r)
	{
		float vertices[] = mesh.vertices;
		
		// Access the triangle vertices as follows (same for the normals):		
		// 1. Get three vertex indices for triangle
		int v0 = mesh.indices[index*3];
		int v1 = mesh.indices[index*3+1];
		int v2 = mesh.indices[index*3+2];
		
		// 2. Access x,y,z coordinates for each vertex
		float x0 = vertices[v0*3];
		float x1 = vertices[v1*3];
		float x2 = vertices[v2*3];
		float y0 = vertices[v0*3+1];
		float y1 = vertices[v1*3+1];
		float y2 = vertices[v2*3+1];
		float z0 = vertices[v0*3+2];
		float z1 = vertices[v1*3+2];
		float z2 = vertices[v2*3+2];

		// The three vertex positions of the triangle
		Point3f a = new Point3f(x0, y0, z0);
		Point3f b = new Point3f(x1, y1, z1);
		Point3f c = new Point3f(x2, y2, z2);

		// Edge vectors a->b and a->c
		Vector3f ab = new Vector3f();
		ab.sub(b, a);
		Vector3f ac = new Vector3f();
		ac.sub(c, a);

		Vector3f normal = new Vector3f();
		normal.cross(ab, ac);
		normal.normalize();

		float nDotRay = normal.dot(r.direction);

		if (nDotRay == 0) // Ray direction is parallel to triangle plane
			return null;

		// Distance of triangle plane to the origin
		float d = normal.dot(new Vector3f(a));

		// Compute the t-Parameter
		float nDotRayOrigin = normal.dot(new Vector3f(r.origin));
		float t = (d - nDotRayOrigin) / nDotRay;

		// Intersection point q
		Point3f q = r.pointAt(t);

		/*
		 *	Compute barycentric coordinates of q
		 */
		Matrix3f mat = new Matrix3f();
		mat.setRow(0, new Vector3f(a));
		mat.setRow(1, new Vector3f(b));
		mat.setRow(2, new Vector3f(c));

		mat.invert();

		Vector3f alphaCoeff = new Vector3f();
		Vector3f betaCoeff = new Vector3f();
		Vector3f gammaCoeff = new Vector3f();

		mat.getRow(0, alphaCoeff);
		mat.getRow(1, betaCoeff);
		mat.getRow(2, gammaCoeff);

		float alpha_q = alphaCoeff.dot(new Vector3f(q));
		float beta_q = betaCoeff.dot(new Vector3f(q));
		float gamma_q = gammaCoeff.dot(new Vector3f(q));

		if (alpha_q <= 0 || beta_q <= 0 || gamma_q <= 0) {
			// Intersection is not within the triangle
			return null;
		}

		/*
		 * Interpolate the normal and texture coordinates
		 */
		Vector3f interpNormal = normal;
		float u = 0;
		float v = 0;

		// Return the hit record
		Vector3f w = new Vector3f(r.direction);
		w.negate();

		HitRecord hit = new HitRecord(t, q, interpNormal, w, mesh, mesh.material, u, v);
		return hit;
	}
	
}
