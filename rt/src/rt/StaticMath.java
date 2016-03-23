package rt;

/**
 * Contains a collection of static math functions.
 */
public class StaticMath
{
    /**
     * Returns true if the intervals [a, b] and [c, d] intersect, and false otherwise.
     */
    public static boolean doesIntervalIntersect(float a, float b, float c, float d)
    {
        // Correct ordering of interval boundaries
        if (a > b)
        {
            return doesIntervalIntersect(b, a, c, d);
        }
        if (c > d)
        {
            return doesIntervalIntersect(a, b, d, c);
        }

        return !(c > b || a > d);

    }

    public static boolean doesIntervalContain(float a, float b, float v)
    {
        if (a > b)
        {
            return doesIntervalContain(b, a, v);
        }
        return v >= a && v <= b;
    }
}
