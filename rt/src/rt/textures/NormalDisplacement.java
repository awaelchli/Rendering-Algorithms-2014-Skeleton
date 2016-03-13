package rt.textures;

import rt.HitRecord;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 13.03.2016.
 */
public abstract class NormalDisplacement {

    public void bumpNormal(HitRecord hitRecord) {
        bumpNormal(hitRecord.normal, hitRecord.t1, hitRecord.t2, hitRecord.u, hitRecord.v);
    }

    public void bumpNormal(Vector3f normal, Vector3f tangentU, Vector3f tangentV, float u, float v) {

        // Transformation matrix from tangent space to world space
        Matrix3f toWorld = new Matrix3f();
        toWorld.setColumn(0, tangentU);
        toWorld.setColumn(1, tangentV);
        toWorld.setColumn(2, normal);

        // The new normal in local coordinates
        Vector3f localNormal = getTangentSpaceNormal(u, v);

        // Transform the new normal to world coordinates
        toWorld.transform(localNormal);
        normal.set(localNormal);
    }

    public abstract Vector3f getTangentSpaceNormal(float u, float v);

}
