package rt.integrators;

import java.util.Iterator;

/**
 * Created by adrian on 06.05.16.
 */
public class Path implements Iterable<PathVertex>
{
    PathVertex root;

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
            return current.next != null;
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
