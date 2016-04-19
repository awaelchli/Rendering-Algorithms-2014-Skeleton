package rt.importanceSampling;

/**
 * Created by Adrian on 19.04.2016.
 */
public class BalanceHeuristic implements Heuristic
{
    @Override
    public float evaluate(float p1, float p2)
    {
        return p1 / (p1 + p2);
    }
}
