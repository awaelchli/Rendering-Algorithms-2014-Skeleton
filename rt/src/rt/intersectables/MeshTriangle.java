package rt.intersectables;

import javax.vecmath.*;
import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.StaticVecmath;

import java.awt.*;

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
		Point3f[] vertices = getVertexPositions();

		// The three vertex positions of the triangle
		Point3f A = vertices[0];
		Point3f B = vertices[1];
		Point3f C = vertices[2];

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

		Vector3f w = StaticVecmath.negate(r.direction);

		// Compute barycentric coordinates for interpolation
		float[] bary = barycentricCoordinates(q);
		float bary_alpha = bary[0];
		float bary_beta = bary[1];
		float bary_gamma = bary[2];

		Vector3f normal;
		if (mesh.hasNormals()) {
			// Interpolate vertex normals
			Vector3f[] vertexNormals = getVertexNormals();
			normal = interp(vertexNormals[0], vertexNormals[1], vertexNormals[2], bary_alpha, bary_beta, bary_gamma);
		} else {
			normal = new Vector3f();
			normal.cross(ba, ca);
		}
		normal.normalize();

		Point2f texCoord = new Point2f(0, 0);
		if (mesh.hasTextureCoordinates()) {
			// Interpolate texture coordinate
			Point2f[] texCoords = getTextureCoordinates();
			texCoord = interp(texCoords[0], texCoords[1], texCoords[2], bary_alpha, bary_beta, bary_gamma);
		}

		HitRecord hit = new HitRecord(t, q, normal, w, mesh, mesh.material, texCoord.x, texCoord.y);
		return hit;
	}

	/**
	 * Computes the barycentric coordinates alpha, beta and gamma for a point on the triangle.
	 *
	 * @param position
	 * @return a float[] containing the three barycentric coordinates
     */
	public float[] barycentricCoordinates(Point3f position) {
		Point3f[] vertices = getVertexPositions();

		Point3f p1 = vertices[0];
		Point3f p2 = vertices[1];
		Point3f p3 = vertices[2];

		float area1 = signedArea(position, p2, p3);
		float area2 = signedArea(position, p3, p1);
		float area3 = signedArea(position, p1, p2);

		float totalArea = area1 + area2 + area3;
		float alpha = area1 / totalArea;
		float beta = area2 / totalArea;
		float gamma = area3 / totalArea;

		return new float[] {alpha, beta, gamma};
	}

	private Vector3f interp(Vector3f v1, Vector3f v2, Vector3f v3, float alpha, float beta, float gamma) {
		Vector3f interp = new Vector3f();
		interp.scaleAdd(alpha, v1, interp);
		interp.scaleAdd(beta, v2, interp);
		interp.scaleAdd(gamma, v3, interp);
		return interp;
	}

	private Point2f interp(Point2f t1, Point2f t2, Point2f t3, float alpha, float beta, float gamma){
		Point2f interp = new Point2f();
		interp.scaleAdd(alpha, t1, interp);
		interp.scaleAdd(beta, t2, interp);
		interp.scaleAdd(gamma, t3, interp);
		return interp;
	}

	private float signedArea(Point3f p1, Point3f p2, Point3f p3) {
		Vector3f v = StaticVecmath.sub(p2, p1);
		Vector3f w = StaticVecmath.sub(p3, p1);
		Vector3f normal = new Vector3f();
		normal.cross(v, w);
		float area = normal.length() / 2;
		return area;
	}

	/**
	 * Returns the three vertex positions in counter-clockwise order.
	 */
	public Point3f[] getVertexPositions() {
		float vertices[] = mesh.vertices;

		int v0 = mesh.indices[index*3];
		int v1 = mesh.indices[index*3+1];
		int v2 = mesh.indices[index*3+2];

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

		return new Point3f[] {a, b, c};
	}

	/**
	 * Returns the three vertex normals in counter-clockwise order.
	 */
	public Vector3f[] getVertexNormals() {
		float normals[] = mesh.normals;

		int n0 = mesh.indices[index*3];
		int n1 = mesh.indices[index*3+1];
		int n2 = mesh.indices[index*3+2];

		float x0 = normals[n0*3];
		float x1 = normals[n1*3];
		float x2 = normals[n2*3];
		float y0 = normals[n0*3+1];
		float y1 = normals[n1*3+1];
		float y2 = normals[n2*3+1];
		float z0 = normals[n0*3+2];
		float z1 = normals[n1*3+2];
		float z2 = normals[n2*3+2];

		// The three vertex normals of the triangle
		Vector3f normal0 = new Vector3f(x0, y0, z0);
		Vector3f normal1 = new Vector3f(x1, y1, z1);
		Vector3f normal2 = new Vector3f(x2, y2, z2);

		return new Vector3f[] {normal0, normal1, normal2};
	}

	/**
	 * Returns the three texture coordinate pairs in counter-clockwise order.
	 */
	public Point2f[] getTextureCoordinates() {
		float texCoords[] = mesh.texCoords;

		int n0 = mesh.indices[index*3];
		int n1 = mesh.indices[index*3+1];
		int n2 = mesh.indices[index*3+2];

		float u0 = texCoords[n0*2];
		float u1 = texCoords[n1*2];
		float u2 = texCoords[n2*2];
		float v0 = texCoords[n0*2+1];
		float v1 = texCoords[n1*2+1];
		float v2 = texCoords[n2*2+1];

		Point2f tex0 = new Point2f(u0, v0);
		Point2f tex1 = new Point2f(u1, v1);
		Point2f tex2 = new Point2f(u2, v2);

		return new Point2f[] {tex0, tex1, tex2};
	}
	
}
