/*
 * BVHAccelerator.java
 *
 * Created on 26. Dezember 2005, 22:45
 */

package jay.scene.primitives.accelerators;

import java.util.*;
import jay.maths.*;
import jay.scene.primitives.*;

/**
 * Bounding Volume Hierarchy
 *
 * @author Matthias Treydte
 */
public class BVHAccelerator extends Accelerator {
    
    public final static int MAX_PRIMS = 2;
    
    private class Node {
        public AABB bounds;
        public Primitive[] prims;
        public Node leftChild = null;
        public Node rightChild = null;
    }
    
    private Node root;
    
    public BVHAccelerator(Group group) {
        super(group);
        rebuild();
    }
    
    public void rebuild() {
        root = new Node();
        List<Primitive> ps = new ArrayList<Primitive>();
        for (Primitive p : group.getPrimitives())
            Primitive.recursiveRefine(ps, p);
        
        if (ps.size() == 0) {
            root = null;
            return;
        }
        
        root.prims = ps.toArray(new Primitive[0]);
        
        Stack<Node> todo = new Stack<Node>();
        todo.push(root);
        int axis = 0;
        
        /* AABBs vorberechnen */
        final HashMap<Primitive, AABB> boundsMap =
                new HashMap<Primitive, AABB>();
        for (Primitive p : root.prims) boundsMap.put(p, p.worldBounds());
        
        root.bounds = (AABB)boundsMap.values().iterator().next();
        for (AABB b : boundsMap.values()) root.bounds = root.bounds.extend(b);
        
        
        while (!todo.empty()) {
            Node node = todo.pop();
            
            /* terminieren */
            if (node.prims.length <= MAX_PRIMS) continue;
            
            Primitive[] parr = node.prims;
            
            final int thisAxis =
                    node.bounds.max.sub(node.bounds.min).dominantAxis();
            axis = axis % 3;
            
            Arrays.sort(parr, new Comparator<Primitive>() {
                public int compare(Primitive o1, Primitive o2) {
                    AABB b1 = boundsMap.get(o1);
                    AABB b2 = boundsMap.get(o2);
                    if (b1.min.get(thisAxis) < b2.min.get(thisAxis))
                        return -1;
                    
                    if (b1.min.get(thisAxis) > b2.min.get(thisAxis))
                        return 1;
                    
                    return 0;
                }
            });
            
            int splitPos = parr.length / 2;
            
            AABB leftBounds = (AABB)boundsMap.get(parr[0]);
            for (int i = 1; i < splitPos; i++)
                leftBounds = leftBounds.extend(boundsMap.get(parr[i]));
            
            AABB rightBounds = (AABB)boundsMap.get(parr[splitPos]);
            for (int i = splitPos + 1; i < parr.length; i++)
                rightBounds = rightBounds.extend(boundsMap.get(parr[i]));
            
            /* Kinder initialisieren */
            Node leftChild = new Node();
            Node rightChild = new Node();
            leftChild.bounds = leftBounds;
            rightChild.bounds = rightBounds;
            
            leftChild.prims = new Primitive[splitPos];
            rightChild.prims = new Primitive[parr.length - splitPos];
            
            System.arraycopy(parr, 0, leftChild.prims, 0, splitPos);
            System.arraycopy(parr, splitPos, rightChild.prims, 0,
                    parr.length - splitPos);
            node.leftChild = leftChild;
            node.rightChild = rightChild;
            todo.push(leftChild);
            todo.push(rightChild);
            node.prims = null;
        }
        
    }
    
    public boolean intersects(final Ray ray) {
        if ((root == null) || !root.bounds.intersects(ray))
            return false;
        
        Stack<Node> toVisit = new Stack<Node>();
        toVisit.push(root);
        
        while (!toVisit.empty()) {
            Node node = toVisit.pop();
            
            if (node.prims != null) {
                /* Blattknoten */
                for (Primitive p : node.prims)
                    if (p.intersects(ray)) return true;
            } else {
                /* innerer Knoten */
                if (node.leftChild.bounds.intersects(ray))
                    toVisit.push(node.leftChild);
                
                if (node.rightChild.bounds.intersects(ray))
                    toVisit.push(node.rightChild);
            }
        }
        
        return false;
    }
    
    public Intersection nearestIntersection(final Ray ray) {
        if (root == null) return null;
        
        Stack<Node> todo = new Stack<Node>();
        todo.push(root);
        NearestIntersection ni = new NearestIntersection();
        
        while (!todo.empty()) {
            Node current = todo.pop();
            do {
                if (current.bounds.intersects(ray)) {
                    
                    ray.cellsVisited++;
                    
                    if (current.prims != null) {
                        /* Blatt */
                        for (Primitive p : current.prims)
                            ni.set(p.nearestIntersection(ray));
                        
                        current = null;
                    } else {
                        /* innerer */
                        todo.push(current.leftChild);
                        current = current.rightChild;
                    }
                } else {
                    current = null;
                }
                
            } while (current != null);
        }
        
        return ni.get();
    }
    
    public Intersection nearestIntersection1(final Ray ray) {
        if ((root == null) || !root.bounds.intersects(ray))
            return null;
        
        Stack<Node> toVisit = new Stack<Node>();
        toVisit.push(root);
        NearestIntersection ni = new NearestIntersection();
        
        while (!toVisit.empty()) {
            Node node = toVisit.pop();
            
            if (node.prims != null) {
                /* Blattknoten */
                for (Primitive p : node.prims)
                    ni.set(p.nearestIntersection(ray));
            } else {
                /* innerer Knoten */
                
                if (node.leftChild.bounds.intersects(ray))
                    toVisit.push(node.leftChild);
                
                if (node.rightChild.bounds.intersects(ray))
                    toVisit.push(node.rightChild);
            }
        }
        
        return ni.get();
    }
}
