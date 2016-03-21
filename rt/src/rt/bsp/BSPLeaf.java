package rt.bsp;

import rt.intersectables.Aggregate;
import rt.intersectables.IntersectableList;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPLeaf extends BSPNode {

    public BSPLeaf(BoundingBox boundingBox) {
        this.bb = boundingBox;
    }

    @Override
    public boolean isLeaf()
    {
        return true;
    }
}
