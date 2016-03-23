package rt.bsp;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPLeaf extends BSPNode {

    public BSPLeaf(AABoundingBox boundingBox) {
        this.bb = boundingBox;
    }

    @Override
    public boolean isLeaf()
    {
        return true;
    }
}
