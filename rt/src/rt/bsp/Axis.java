package rt.bsp;

import rt.StaticVecmath;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * Simple enum to identify each axis.
 */
public enum Axis {

    X, Y, Z;

    /**
     * Returns the next axis in the cycle.
     * X -> Y, Y -> Z, Z -> X
     */
    static Axis nextAxis(Axis current) {
        switch (current) {
            case X:
                return Y;
            case Y:
                return Z;
            case Z:
                return X;
        }
        return null;
    }

    /**
     * Returns the unit vector of this axis.
     */
    Vector3f getUnitVector() {
        Vector3f v = new Vector3f();
        StaticVecmath.set(v, getIndex(), 1);
        return v;
    }

    /**
     * Returns the index for this axis, i.e. 0 for X, 1 for Y and 2 for Z.
     */
    int getIndex() {
        switch (this) {
            case X: return 0;
            case Y: return 1;
            case Z: return 2;
        }
        return -1;
    }

    /**
     * Returns the value in the {@param tuple} associated with this axis.
     */
    float getValue(Tuple3f tuple) {
        return StaticVecmath.get(tuple, getIndex());
    }
}
