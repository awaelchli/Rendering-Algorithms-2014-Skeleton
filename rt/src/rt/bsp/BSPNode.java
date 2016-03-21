package rt.bsp;

import rt.BoundingBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPNode {

    float planePos;
    BoundingBox bb;
    Axis axis;
    final List<BSPNode> children;

    public BSPNode(float planePos, Axis axis, BoundingBox boundingBox) {
        this.planePos = planePos;
        this.axis = axis;
        this.bb = boundingBox;
        children = new ArrayList<>();
    }

}
