package rt.bsp;

import rt.intersectables.Aggregate;

/**
 * The leaf node in the BSP tree.
 * It has no above and below node and no split axis/plane.
 * It stores a reference to the objects that are inside its bounding box.
 */
public class BSPLeaf extends BSPNode {

    /**
     * Creates a new leaf node.
     *
     * @param boundingBox   The bounding box of this node.
     * @param objects       The objects that intersect with the {@param boundingBox} of this node.
     */
    public BSPLeaf(AABoundingBox boundingBox, Aggregate objects) {
        this.bb = boundingBox;
        this.objects = objects;
    }

    @Override
    public boolean isLeaf()
    {
        return true;
    }
}
