package rt.bsp;

import rt.intersectables.IntersectableList;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPLeaf extends BSPNode {

    IntersectableList objects;

    public BSPLeaf(float planePos, Axis axis) {
        super(planePos, axis);
    }
}
