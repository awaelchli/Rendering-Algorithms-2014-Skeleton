package rt.bsp;

import rt.Intersectable;
import rt.intersectables.Aggregate;
import rt.intersectables.IntersectableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPNode {

    float planePos;
    Axis axis;
    BoundingBox bb;
    List<BSPNode> children;

    /**
     * Only for leaf nodes
     */
    Aggregate objects;

    BSPNode() {}

    public BSPNode(float planePos, Axis axis, BoundingBox boundingBox) {
        this.planePos = planePos;
        this.axis = axis;
        this.bb = boundingBox;
        children = new ArrayList<>();
    }

    public BoundingBox getBoundingBox()
    {
        return bb;
    }

    public boolean isLeaf() {
        return false;
    }

    public Aggregate getObjects()
    {
        return objects;
    }
}
