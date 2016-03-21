package rt.bsp;

import rt.BoundingBox;
import rt.intersectables.IntersectableList;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPLeaf extends BSPNode {

    IntersectableList objects;

    public BSPLeaf(float planePos, Axis axis, BoundingBox boundingBox) {
        super(planePos, axis, boundingBox);
    }
}
