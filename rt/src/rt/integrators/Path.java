package rt.integrators;

import java.util.Iterator;

/**
 * Created by adrian on 06.05.16.
 */
public class Path implements Iterable<PathVertex>
{
    private PathVertex root;

    public Path(PathVertex root)
    {
        this.root = root;
        int k = 0;
        for(PathVertex v : this)
        {
            v.index = k;
            k++;
        }
    }

    public PathVertex getLast()
    {
        PathVertex last = null;
        for(PathVertex v : this) { last = v; }
        return last;
    }

    public void add(PathVertex vertex)
    {
        getLast().next = vertex;
    }

    @Override
    public Iterator<PathVertex> iterator()
    {
        return new PathIterator<PathVertex>(root);
    }

    class PathIterator<P> implements Iterator
    {
        PathVertex current;

        PathIterator(PathVertex start)
        {
            this.current = start;
        }

        @Override
        public boolean hasNext()
        {
            return current != null && current.next != null;
        }

        @Override
        public PathVertex next()
        {
            this.current = this.current.next;
            return current;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
