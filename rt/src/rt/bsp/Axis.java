package rt.bsp;

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
        switch (this) {
            case X:
                return new Vector3f(1, 0, 0);
            case Y:
                return new Vector3f(0, 1, 0);
            case Z:
                return new Vector3f(0, 0, 1);
        }
        return null;
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
        switch (this) {
            case X:
                return tuple.x;
            case Y:
                return tuple.y;
            case Z:
                return tuple.z;
        }

        return Float.NaN;
    }
}
