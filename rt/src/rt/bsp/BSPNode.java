package rt.bsp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPNode {

    public enum Axis {
        X, Y, Z;
    }

    float planePos;
    Axis axis;
    final List<BSPNode> children;

    public BSPNode(float planePos, Axis axis) {
        this.planePos = planePos;
        this.axis = axis;
        children = new ArrayList<>();
    }

}
