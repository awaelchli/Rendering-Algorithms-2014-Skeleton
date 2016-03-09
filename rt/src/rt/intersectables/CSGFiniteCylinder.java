package rt.intersectables;

import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.materials.Diffuse;

import javax.vecmath.Vector3f;
import java.util.ArrayList;

/**
 * Created by Adrian on 09.03.2016.
 */
public class CSGFiniteCylinder extends CSGSolid {

    public enum Face {
        BODY, TOP, BOTTOM
    }

    CSGNode root;

    private CSGInfiniteCylinder body;
    private CSGPlane top, bottom;

    public CSGFiniteCylinder(float radius, float length) {

        body = new CSGInfiniteCylinder(radius);
        top = new CSGPlane(new Vector3f(0, 0, -1), -length / 2);
        bottom = new CSGPlane(new Vector3f(0, 0, 1), -length / 2);

        CSGNode node1 = new CSGNode(body, top, CSGNode.OperationType.INTERSECT);
        CSGNode node2 = new CSGNode(node1, bottom, CSGNode.OperationType.INTERSECT);
        root = node2;
    }

    public void setMaterial(Material material, Face face) {
        switch (face) {
            case BODY:
                body.material = material;
                break;
            case TOP:
                top.material = material;
                break;
            case BOTTOM:
                bottom.material = material;
                break;
        }
    }

    public void setMaterial(Material material) {
        body.material = material;
        top.material = material;
        bottom.material = material;
    }

    @Override
    ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
        return root.getIntervalBoundaries(r);
    }
}
