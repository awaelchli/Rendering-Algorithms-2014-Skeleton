package rt.bsp;

import javax.vecmath.Tuple3f;

/**
 * Simple enum to identify each axis.
 */
enum Axis {

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

    static float getValueFromAxis(Tuple3f tuple, Axis axis) {
        switch (axis) {
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
