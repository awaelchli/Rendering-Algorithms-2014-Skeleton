package rt.cameras;

import rt.Ray;
import rt.StaticVecmath;
import rt.samplers.RandomSampler;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Created by adrian on 31.05.16.
 */
public class ApertureCamera extends PinholeCamera
{
    float apertureRadius;
    Matrix4f cameraToWorld;

    public ApertureCamera(Vector3f position, Vector3f lookAt, Vector3f up, float verticalFOV, float aspect, int width, int height, float aperture)
    {
        super(position, lookAt, up, verticalFOV, aspect, width, height);
        this.apertureRadius = aperture;
        cameraToWorld = getCameraToWorldMatrix();
    }

    @Override
    public Ray makeWorldSpaceRay(int i, int j, float[] sample)
    {
        float focalDistance = StaticVecmath.sub(position, lookAt).length();
        // Make point on image plane in viewport coordinates, that is range [0,width-1] x [0,height-1]
        // The assumption is that pixel [i,j] is the square [i,i+1] x [j,j+1] in viewport coordinates
        Vector4f d = new Vector4f((float) i + sample[0], (float) j + sample[1], -1.f, 1.f);

        // Sample a point on the aperture disk
        float[][] apSample = (new RandomSampler()).makeSamples(1, 2);
        float phi = (float) (2 * Math.PI * apSample[0][0]);
        float tmp = (float) Math.sqrt(apSample[0][1] * apertureRadius);
        float apertureX = (float) (Math.cos(phi) * tmp);
        float apertureY = (float) (Math.sin(phi) * tmp);
        Vector4f aperturePoint = new Vector4f(apertureX, apertureY, 0, 1);

        m.transform(d);
        cameraToWorld.transform(aperturePoint);

        // Make ray consisting of origin and direction in world coordinates
        Vector3f dir = new Vector3f();
        dir.sub(new Vector3f(d.x, d.y, d.z), position);
        Point3f pinhole = new Point3f(position);

        Ray r = new Ray(pinhole, dir);

        // Find intersection with plane in look-At point
        Vector3f normal = getImagePlaneNormal();
        float planeDistance = lookAt.dot(normal);
        normal.negate();
        float t = -(normal.dot(new Vector3f(r.origin)) + planeDistance) / normal.dot(r.direction);
        Point3f imagePoint = r.pointAt(t);
        Point3f apPoint = new Point3f(aperturePoint.x, aperturePoint.y, aperturePoint.z);
        return new Ray(apPoint, StaticVecmath.sub(imagePoint, apPoint));
    }
}
