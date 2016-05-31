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
        Ray r = super.makeWorldSpaceRay(i, j, sample);

        // Sample a point on the aperture disk
        float[][] apSample = (new RandomSampler()).makeSamples(1, 2);
        float phi = (float) (2 * Math.PI * apSample[0][0]);
        float tmp = (float) Math.sqrt(apSample[0][1] * apertureRadius);
        float apertureX = (float) (Math.cos(phi) * tmp);
        float apertureY = (float) (Math.sin(phi) * tmp);
        Point3f aperturePoint = new Point3f(apertureX, apertureY, 0);
        cameraToWorld.transform(aperturePoint);

        // Find intersection with plane in look-At point (at focal distance)
        Vector3f normal = getImagePlaneNormal();
        normal.negate();
        float focalDistance = -lookAt.dot(normal);
        float t = -(normal.dot(new Vector3f(r.origin)) + focalDistance) / normal.dot(r.direction);
        Point3f imagePoint = r.pointAt(t);

        return new Ray(aperturePoint, StaticVecmath.sub(imagePoint, aperturePoint));
    }
}
