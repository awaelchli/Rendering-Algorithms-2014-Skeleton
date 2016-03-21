package rt.intersectables;

import javax.vecmath.*;

import rt.*;
import rt.bsp.BoundingBox;
import rt.materials.Diffuse;

import java.util.ArrayList;

/**
 * A cube implemented using planes and CSG. The cube occupies the volume [-1,1] x [-1,1] x [-1,1].
 */
public class CSGCube extends CSGSolid {

	public enum Face {
		FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM
	};

    private CSGPlane front, back, left, right, top, bottom;

	CSGNode root;

    /**
     * Create a solid cube that occupies the volume [-1,1] x [-1,1] x [-1,1] with default diffuse materials for each face.
     */
	public CSGCube()
	{
		right = new CSGPlane(new Vector3f(1.f,0.f,0.f),-1.f);
		left = new CSGPlane(new Vector3f(-1.f,0.f,0.f),-1.f);
		top = new CSGPlane(new Vector3f(0.f,1.f,0.f),-1.f);
		bottom = new CSGPlane(new Vector3f(0.f,-1.f,0.f),-1.f);
		front = new CSGPlane(new Vector3f(0.f,0.f,1.f),-1.f);
		back = new CSGPlane(new Vector3f(0.f,0.f,-1.f),-1.f);

		right.material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
		left.material = new Diffuse(new Spectrum(1.f, 0.f, 0.f));
		top.material = new Diffuse(new Spectrum(0.f, 1.f, 0.f));
		bottom.material = new Diffuse(new Spectrum(0.f, 0.f, 1.f));
		front.material = new Diffuse(new Spectrum(1.f, 1.f, 0.f));
		back.material = new Diffuse(new Spectrum(0.f, 1.f, 1.f));

		CSGNode n1 = new CSGNode(right, left, CSGNode.OperationType.INTERSECT);
		CSGNode n2 = new CSGNode(top, bottom, CSGNode.OperationType.INTERSECT);
		CSGNode n3 = new CSGNode(front, back, CSGNode.OperationType.INTERSECT);
		CSGNode n4 = new CSGNode(n1, n2, CSGNode.OperationType.INTERSECT);
		root = new CSGNode(n3, n4, CSGNode.OperationType.INTERSECT);
	}

    /**
     * Assigns a material to one face of the cube.
     *
     * @param material
     * @param face One of the six faces of the cube.
     */
	public void setMaterial(Material material, Face face){
		switch (face) {
			case FRONT:
				front.material = material;
				break;
			case BACK:
				back.material = material;
				break;
			case LEFT:
				left.material = material;
				break;
			case RIGHT:
				right.material = material;
				break;
			case TOP:
				top.material = material;
				break;
			case BOTTOM:
				bottom.material = material;
				break;
		}
	}

    /**
     * Assigns a material to all of the cubes faces.
     *
     * @param material
     */
	public void setMaterial(Material material) {
		front.material = material;
		back.material = material;
		left.material = material;
		right.material = material;
		top.material = material;
		bottom.material = material;
	}

	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		return root.getIntervalBoundaries(r);
	}

	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(-1, 1, -1, 1, -1, 1);
	}
}
