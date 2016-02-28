package rt.intersectables;

import rt.Ray;

import java.util.ArrayList;

/**
 * Created by Adrian on 28.02.2016.
 */
public class CSGSphere extends CSGSolid {

    public CSGSphere(){

    }

    @Override
    ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
        return null;
    }
}
