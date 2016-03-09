package rt.intersectables;

import javax.vecmath.*;

import rt.*;
import rt.materials.Diffuse;
import sun.font.GlyphLayout;

import java.util.ArrayList;

/**
 * A cube implemented using planes and CSG. The cube occupies the volume [-1,1] x [-1,1] x [-1,1]. 
 */
public class CSGCube extends CSGSolid {

	public enum Side {
		FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM
	};

	CSGNode root;

	private CSGPlane front, back, left, right, top, bottom;

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

	public void setMaterial(Material material, Side side){
		switch (side) {
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
}
