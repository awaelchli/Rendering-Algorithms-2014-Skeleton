package rt.bsp;

/**
 * A simple stack item used by {@link BSPAccelerator} to calculate intersections.
 */
public class BSPStackItem
{
    BSPNode node;
    float tmin, tmax, tsplit;
}
