package rt.bsp;

import rt.StaticVecmath;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * Simple enum to identify each axis.
 */
public enum Axis {

    X, Y, Z;

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

    Vector3f getUnitVector() {
        Vector3f v = new Vector3f();
        StaticVecmath.set(v, getIndex(), 1);
        return v;
    }

    int getIndex() {
        switch (this) {
            case X: return 0;
            case Y: return 1;
            case Z: return 2;
        }
        return -1;
    }

    float getValue(Tuple3f tuple) {
        return StaticVecmath.get(tuple, getIndex());
    }
}
