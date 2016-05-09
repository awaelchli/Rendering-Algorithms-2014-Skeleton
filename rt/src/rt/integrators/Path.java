package rt.integrators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by adrian on 06.05.16.
 */
public class Path implements Iterable<PathVertex>
{
    private List<PathVertex> vertices;

    public Path()
    {
       vertices = new ArrayList<PathVertex>();
    }

    public PathVertex getLast()
    {
        return vertices.get(vertices.size() - 1);
    }

    public PathVertex getFirst()
    {
        return vertices.get(0);
    }

    public void add(PathVertex vertex)
    {
        vertices.add(vertex);
    }

    public int indexOf(PathVertex vertex)
    {
        return vertices.indexOf(vertex);
    }

    /**
     * Returns the number of segments in this path.
     */
    public int length()
    {
        return vertices.size() - 1;
    }

    /**
     * Returns the number of vertices in this path.
     */
    public int numberOfVertices()
    {
        return vertices.size();
    }

    @Override
    public Iterator<PathVertex> iterator()
    {
        return vertices.iterator();
    }

    @Override
    public String toString()
    {
        String s = "";
        String arrow = " --> ";
        for(PathVertex v : this)
        {
            s += arrow + v.hitRecord.position.toString();
        }
        return s;
    }
}
