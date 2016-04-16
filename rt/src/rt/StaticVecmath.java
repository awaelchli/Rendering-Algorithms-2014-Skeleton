package rt;

import javax.vecmath.*;
import java.util.Vector;

/**
 * Static utility functions to make it easier to work with the vecmath package.
 */
public class StaticVecmath {

	public static float dist2(Tuple3f v1, Tuple3f v2)
	{
		Vector3f tmp = new Vector3f(v1);
		tmp.sub(v2);
		return tmp.lengthSquared();
	}
	
	public static Vector3f sub(Tuple3f v1, Tuple3f v2)
	{
		Vector3f r = new Vector3f(v1);
		r.sub(v2);
		return r;
	}
	
	public static Vector3f negate(Vector3f v)
	{
		Vector3f r = new Vector3f(v);
		r.negate();
		return r;
	}
	
	public static Matrix4f invert(Matrix4f m)
	{
		Matrix4f r = new Matrix4f(m);
		r.invert();
		return r;
	}

	public static void set(Tuple3f tuple, int index, float value) {
		assert index >= 0 && index <= 2;

		switch (index)
		{
			case 0:
				tuple.x =  value;
				break;
			case 1:
				tuple.y = value;
				break;
			case 2:
				tuple.z = value;
				break;
		}
	}

	public static float get(Tuple3f tuple, int index)
	{
		assert index >= 0 && index <= 2;

		switch (index)
		{
			case 0:
				return tuple.x;
			case 1:
				return tuple.y;
			case 2:
				return tuple.z;
		}
		return Float.NaN;
	}

	public static Vector3f cross(Vector3f v1, Vector3f v2)
	{
		Vector3f cross = new Vector3f();
		cross.cross(v1, v2);
		return cross;
	}

	public static Vector3f reflect(Vector3f direction, Vector3f halfVector)
	{
		Vector3f incident = new Vector3f(direction);
		incident.normalize();
		incident.negate();

		float s = -2 * incident.dot(halfVector);

		Vector3f r = new Vector3f();
		r.scaleAdd(s, halfVector, incident);

		return r;
	}
}
