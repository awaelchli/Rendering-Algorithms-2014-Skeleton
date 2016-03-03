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
		Point3f A = new Point3f(x0, y0, z0);
		Point3f B = new Point3f(x1, y1, z1);
		Point3f C = new Point3f(x2, y2, z2);

		// Edge vectors b->a and c->a
		Vector3f ba = new Vector3f();
		ba.sub(A, B);
		Vector3f ca = new Vector3f();
		ca.sub(A, C);

		Matrix3f mat = new Matrix3f();
		mat.setColumn(0, ba);
		mat.setColumn(1, ca);
		mat.setColumn(2, r.direction);

		Vector3f rightHandSide = new Vector3f();
		rightHandSide.sub(A, r.origin);

		/*
		 * Computation of beta, gamma and t according to Shirley, p. 79
		 */
		float a = mat.m00;
		float b = mat.m10;
		float c = mat.m20;
		float d = mat.m01;
		float e = mat.m11;
		float f = mat.m21;
		float g = mat.m02;
		float h = mat.m12;
		float i = mat.m22;

		float j = rightHandSide.x;
		float k = rightHandSide.y;
		float l = rightHandSide.z;

		float ei_minus_hf = e * i - h * f;
		float gf_minus_di = g * f - d * i;
		float dh_minus_eg = d * h - e * g;
		float ak_minus_jb = a * k - j * b;
		float jc_minus_al = j * c - a * l;
		float bl_minus_kc = b * l - k * c;

		float m = a * ei_minus_hf + b * gf_minus_di + c * dh_minus_eg;

		float t = - (f * ak_minus_jb + e * jc_minus_al + d * bl_minus_kc) / m;

		if (t <= 0) // Intersection is behind the eye
			return null;

		float gamma = (i * ak_minus_jb + h * jc_minus_al + g * bl_minus_kc) / m;

		if (gamma < 0 || gamma > 1) // Intersection is outside the triangle
			return null;

		float beta = (j * ei_minus_hf + k * gf_minus_di + l * dh_minus_eg) / m;

		if (beta < 0 || beta > 1 - gamma) // Intersection is outside the triangle
			return null;

		// Valid intersection point on triangle
		Point3f q = r.pointAt(t);

		Vector3f w = new Vector3f(r.direction);
		w.negate();

		Vector3f normal = new Vector3f();
		normal.cross(ca, ba);
		normal.normalize();
		// TODO: interpolate vertex normals
		Vector3f interpNormal = normal;

		// TODO: interpolate texture coordinates
		float u = 0;
		float v = 0;

		HitRecord hit = new HitRecord(t, q, interpNormal, w, mesh, mesh.material, u, v);
		return hit;
	}
	
}
