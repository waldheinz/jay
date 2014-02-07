/*
 * KdTreeAccelerator.java
 *
 * Created on 25. Februar 2006, 15:41
 */

package jay.scene.primitives.accelerators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import jay.maths.*;
import jay.scene.primitives.*;
import jay.utils.IntArray;
import jay.utils.statistics.StatsCounter;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class KdTreeAccelerator extends Accelerator {
    
    public static enum EdgeType { START, PLANAR, END };
    
    int isectCost, traversalCost, maxPrims;
    
    /** for counting the total number of prims in all leaf nodes */
    int totalPrims;
    
    /** the number of leaf nodes in the tree */
    int totalLeafs;
    
    /** for calculating the average tree depth */
    int totalDepth;
    
    /** The cost bonus for an empty node [0..1]. */
    float emptyBonus;
    
    /** The maximum recursion depth allowed. */
    int maxDepth;
    
    /** Temporärer Speicherplatz für die Knoten des Baums */
    ArrayList<KdTreeNode> nodesW =
            new ArrayList<KdTreeNode>();
    
    /** Der Baum als Array */
    KdTreeNode[] nodes;
    
    Primitive[] primitives;
    AABB bounds;
    
    private ThreadLocal traversalToDoStorage = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            KdToDo todo[] = new KdToDo[maxDepth + 2];
            
            for (int i=0; i < maxDepth+2; i++)
                todo[i] = new KdToDo();
            
            return todo;
        }
    };
    
    StatsCounter intersections = new StatsCounter("Kd-Tree intersection tests");
    StatsCounter neari = new StatsCounter("Kd-Tree nearest intersections");
    final static Logger log = Logger.getLogger(KdTreeAccelerator.class.getName());
    
    public KdTreeAccelerator(Group group) {
        this(group, 80, 1, 0.5f, 1);
    }
    
    /** Creates a new instance of KdTreeAccelerator */
    public KdTreeAccelerator(Group group, int iCost, int tCost,
            float eBonus, int maxP) {
        
        super(group);
        
        isectCost = iCost;
        traversalCost = tCost;
        maxPrims = maxP;
        emptyBonus = eBonus;
    }
        
    public boolean intersects(final Ray ray) {
        intersections.increment();
        float[] tminmax = bounds.intersections(ray);
        if (tminmax == null) return false;
        float tmin = tminmax[0], tmax = tminmax[1];
        
        final Vector invDir = new Vector(1.0f/ray.d.x, 1.0f/ray.d.y, 1.0f/ray.d.z);
        
        int todoPos = 0;        
        int nodeNum = 0;
        
        KdToDo[] traversalToDo = (KdToDo[]) traversalToDoStorage.get();
        
        while (nodes[nodeNum] != null) {
            if (nodes[nodeNum].isLeaf()) {
                /* check leaf node */
                
                for (Primitive p : nodes[nodeNum].prims) {
                    if (p.intersects(ray)) return true;
                }
                
                /* Grab next node to process from todo list */
                if (todoPos > 0) {
                    --todoPos;
                    nodeNum = traversalToDo[todoPos].nodeNum;
                    tmin = traversalToDo[todoPos].tmin;
                    tmax = traversalToDo[todoPos].tmax;
                } else
                    break;
            } else {
                /* process interior node */
                
                /* parametric distance along ray to split plane */
                int axis = nodes[nodeNum].splitAxis();
                float tplane = (nodes[nodeNum].splitPos() - ray.o.get(axis)) *
                        invDir.get(axis);
                
                // Get node children pointers for ray
                int firstChild, secondChild;
                boolean belowFirst =
                        ray.o.get(axis) <= nodes[nodeNum].splitPos();
                
                if (belowFirst) {
                    firstChild = nodeNum + 1;
                    secondChild = nodes[nodeNum].getAboveChild();
                } else {
                    firstChild = nodes[nodeNum].getAboveChild();
                    secondChild = nodeNum + 1;
                }
                
                /* advance to next child node, possibly enqueue other child */
                if (tplane > tmax || tplane < 0)
                    nodeNum = firstChild;
                else if (tplane < tmin)
                    nodeNum = secondChild;
                else {
                    /* enqueue _secondChild_ in todo list */
                    traversalToDo[todoPos].nodeNum = secondChild;
                    traversalToDo[todoPos].tmin = tplane;
                    traversalToDo[todoPos].tmax = tmax;
                    ++todoPos;
                    nodeNum = firstChild;
                    tmax = tplane;
                }
            }
        }
        
        return false;
    }
    
    public Intersection nearestIntersection(final Ray ray) {
        neari.increment();
        float[] tminmax = bounds.intersections(ray);
        if (tminmax == null) return null;
        float tmin = tminmax[0], tmax = tminmax[1];
        
        /* prepare traversal */
        final Vector invDir = new Vector(1.0f/ray.d.x, 1.0f/ray.d.y, 1.0f/ray.d.z);
        int todoPos = 0;
        int nodeNum = 0;
        NearestIntersection ni = new NearestIntersection();
        KdToDo[] traversalToDo = (KdToDo[]) traversalToDoStorage.get();
        
        while (nodes[nodeNum] != null) {
            // Bail out if we found a hit closer than the current node
            if (ray.tmax < tmin) break;
            
            ray.cellsVisited++;
            
            if (!nodes[nodeNum].isLeaf()) {
                /* compute parametric distance along ray to split plane */
                int axis = nodes[nodeNum].splitAxis();
                float tplane = (nodes[nodeNum].splitPos() - ray.o.get(axis)) *
                        invDir.get(axis);
                
                /* get node children pointers for ray */
                int firstChild, secondChild;
                float pos = nodes[nodeNum].splitPos();
                boolean belowFirst = ray.o.get(axis) <= nodes[nodeNum].splitPos();
                
                if (belowFirst) {
                    firstChild = nodeNum + 1;
                    secondChild = nodes[nodeNum].getAboveChild();
                } else {
                    firstChild = nodes[nodeNum].getAboveChild();
                    secondChild = nodeNum + 1;
                }
                
                /* advance to next node */
                if (tplane > tmax || tplane < 0){
                    nodeNum = firstChild;
                } else if (tplane < tmin){
                    nodeNum = secondChild;
                } else {
                    /* need to visit both children. enqueue other child. */
                    traversalToDo[todoPos].nodeNum = secondChild;
                    traversalToDo[todoPos].tmin = tplane;
                    traversalToDo[todoPos].tmax = tmax;
                    ++todoPos;
                    nodeNum = firstChild;
                    tmax = tplane;
                }
            } else {
                
                for (Primitive p : nodes[nodeNum].prims) {
                    ni.set(p.nearestIntersection(ray));
                    ray.intersections++;
                }
                
                /* get next node to process from todo list */
                if (todoPos > 0) {
                    --todoPos;
                    nodeNum = traversalToDo[todoPos].nodeNum;
                    tmin = traversalToDo[todoPos].tmin;
                    tmax = traversalToDo[todoPos].tmax;
                } else
                    break;
            }
        }
        
        return ni.get();
    }
    
    public void rebuild() {
        log.finer("rebuilding KdTree");
        
        /* make a flat array of primitives */
        List<Primitive> prims = new ArrayList<Primitive>();
        for (Primitive p : group.getPrimitives())
            Primitive.recursiveRefine(prims, p);
        
        log.finest("found " + prims.size() + " primitives");
        
        nodesW = new ArrayList<KdTreeNode>();
        
        maxDepth = Math.max(5, Math.round(8 + 1.4f * (float)Math.log(prims.size())));
        
        primitives = new Primitive[prims.size()];
        prims.toArray(primitives);
        
        /* AABBs bestimmen */
        ArrayList<AABB> primBounds =
                new ArrayList<AABB>(prims.size());
        
        bounds = AABB.EMPTY;
        
        for (final Primitive p : prims) {
            final AABB b = p.worldBounds();
            bounds = bounds.extend(b);
            primBounds.add(b);
        }

        /* primNums initialisieren */
        IntArray primNums = new IntArray(prims.size());
        for (int i=0; i < prims.size(); ++i)
            primNums.set(i, i);
       
        /*
        BoundEdge[][] edges = new BoundEdge[3][];
        
        for (int axis=0; axis < 3; axis++) {
            // Kanten für Achse initialisieren 
            
            ArrayList<BoundEdge> e = new ArrayList<BoundEdge>();
            
            for (int i=0; i < prims.size(); i++) {
                int pn = primNums.get(i);
                AABB bbox = primBounds.get(pn);
                
                if (bbox.diagonal().get(axis) == 0.0f) {
                    e.add(new BoundEdge(bbox.min.get(axis), pn, EdgeType.PLANAR));
                } else {
                    e.add(new BoundEdge(bbox.min.get(axis), pn, EdgeType.START));
                    e.add(new BoundEdge(bbox.max.get(axis), pn, EdgeType.END));
                }
            }
            
            edges[axis] = new BoundEdge[e.size()];
            e.toArray(edges[axis]);
        }
        */
        
//        for (int axis=0; axis < 3; axis++) {
//            for (int p=0; p < prims.size()*2; p++) {
//                edges[axis][p] = new BoundEdge();
//            }
//        }
        
        //IntArray prims0 = new IntArray(prims.size());
        //IntArray prims1 = new IntArray((maxDepth+1) * prims.size());
        
        totalLeafs = totalDepth = totalPrims = 0;
        
        buildTree(0, bounds, primBounds, primNums,
                prims.size(), maxDepth, 0);
        
        /* Nodes in ein Array umkopieren */
        nodes = new KdTreeNode[nodesW.size()];
        nodes = nodesW.toArray(nodes);
        nodesW = null;
        
        System.out.println("KdTree built: " + 
                "nodes: " + nodes.length + ", " +
                "prims: " + totalPrims + ", " +
                "prims/leaf: " + (float)totalPrims / totalLeafs + ", " +
                "avg. depth: " + (float)totalDepth / totalLeafs);
        
    }
    
    private ArrayList<BoundEdge> mkEdges(
            ArrayList<AABB> bounds, IntArray primNums, int axis) {
        
        final ArrayList<BoundEdge> result = new ArrayList<BoundEdge>();
        
        for (int i=0; i < primNums.size(); i++) {
            int pn = primNums.get(i);
            AABB bbox = bounds.get(pn);
            
            if (bbox.diagonal().get(axis) == 0.0f) {
                result.add(new BoundEdge(bbox.min.get(axis), pn, EdgeType.PLANAR));
            } else {
                result.add(new BoundEdge(bbox.min.get(axis), pn, EdgeType.START));
                result.add(new BoundEdge(bbox.max.get(axis), pn, EdgeType.END));
            }
        }
        
        return result;
    }
    
    private void buildTree(int nodeNum, AABB nodeBounds,
            ArrayList<AABB> allPrimBounds,
            IntArray primNums, int nPrims, int depth,
            int badRefines) {
        
        /* evtl. Rekursion beenden */
        if (nPrims <= maxPrims || depth == 0) {
            if ((nPrims > 16) && (depth == 0)) {
                System.out.println("reached max. KdTree depth with " + nPrims + 
                        " primitives");
            }
            
            nodesW.add(makeLeaf(primNums, nPrims, primitives));
            totalPrims += nPrims;
            totalDepth += (maxDepth - depth);
            totalLeafs += 1;
            return;
        }
        
        /* Split - Position bestimmen */
        
        int bestAxis = -1, bestOffset = -1;
        float bestCost = Float.POSITIVE_INFINITY;
        float oldCost = isectCost * (float)nPrims;
        Vector d = nodeBounds.max.sub(nodeBounds.min);
        float totalSA = (2.f * (d.x*d.y + d.x*d.z + d.y*d.z));
        float invTotalSA = 1.f / totalSA;
        
        /* Achse wählen */
        int axis = d.dominantAxis();
        int retries = 0;
        boolean retry = false;
        ArrayList<BoundEdge> edges = null;
        
//        final int splitCount = edges[axis].length;
        
        do {
            edges = mkEdges(allPrimBounds, primNums, axis);
            Collections.sort(edges);
            final int splitCount = edges.size();
            
//            java.util.Arrays.sort(edges[axis], 0, splitCount);
            
            /* beste Split - Position für diese Achse finden */
            int nBelow = 0, nAbove = nPrims;
            
            for (int i = 0; i < splitCount; ++i) {
                if (edges.get(i).type == EdgeType.END) --nAbove;
                float edget = edges.get(i).t;
                
                if (edget > nodeBounds.min.get(axis) &&
                        edget < nodeBounds.max.get(axis)) {
                    // Compute cost for split at _i_th edge
                    int otherAxis[][] = { {1,2}, {0,2}, {0,1} };
                    int otherAxis0 = otherAxis[axis][0];
                    int otherAxis1 = otherAxis[axis][1];
                    
                    float belowSA = 2 * (d.get(otherAxis0) * d.get(otherAxis1) +
                            (edget - nodeBounds.min.get(axis)) *
                            (d.get(otherAxis0) + d.get(otherAxis1)));
                    
                    float aboveSA = 2 * (d.get(otherAxis0) * d.get(otherAxis1) +
                            (nodeBounds.max.get(axis) - edget) *
                            (d.get(otherAxis0) + d.get(otherAxis1)));
                    
                    float pBelow = belowSA * invTotalSA;
                    float pAbove = aboveSA * invTotalSA;
                    
                    float eb = (nAbove == 0 || nBelow == 0) ? emptyBonus : 0.f;
                    float cost = traversalCost + isectCost * (1.f - eb) *
                            (pBelow * nBelow + pAbove * nAbove);
                  
                    if (cost < bestCost)  {
                        /* neuer bester gefunden */
                        bestCost = cost;
                        bestAxis = axis;
                        bestOffset = i;
                    }
                }
                
                if (edges.get(i).type == EdgeType.START) ++nBelow;
            }
            
            if (!(nBelow == nPrims && nAbove == 0))
                throw new IllegalStateException("hmm");
            
            retry = ((bestAxis == -1) && (retries < 2));
            
            if (retry) {
                ++retries;
                axis = (axis+1) % 3;
            }
            
        } while (retry);
        
        if (bestCost > oldCost) ++badRefines;
        if ((bestCost > 4.f * oldCost && nPrims < 16) ||
                bestAxis == -1 || badRefines == 3) {
            nodesW.add(makeLeaf(primNums, nPrims, primitives));
            if (nPrims > 16)
                System.out.println("aborting KdTree build recursion (bc=" +
                    bestCost + ", oc=" + oldCost + ", #prims=" + 
                    nPrims + ", br=" + badRefines + ")");
            totalPrims += nPrims;
            totalDepth += (maxDepth - depth);
            totalLeafs += 1;
            return;
        }
        
        /* Primitive nach oben / unten sortieren */
        int n0 = 0, n1 = 0;
        
        final ArrayList<Integer> prims0Tmp = new ArrayList<Integer>();
        final ArrayList<Integer> prims1Tmp = new ArrayList<Integer>();
        
        for (int i = 0; i < bestOffset; ++i) {
            if (edges.get(i).type == EdgeType.START) {
                prims0Tmp.add(edges.get(n0++).primNum);
//                prims0.set(n0++, edges[bestAxis][i].primNum);
            }
        }
        
        for (int i = bestOffset + 1; i < edges.size(); ++i) {
            if (edges.get(i).type == EdgeType.END) {
                prims1Tmp.add(edges.get(n1++).primNum);
//                prims1.set(n1++, edges[bestAxis][i].primNum);
            }
        }
        
        /* rekursiver Abstieg */
        float tsplit = edges.get(bestOffset).t;
        
        nodesW.add(makeInterior(bestAxis, tsplit));
        
        AABB bounds[] = nodeBounds.split(tsplit, bestAxis);
        
        final IntArray prims0 = new IntArray(prims0Tmp.size());
        for (int i=0; i < prims0Tmp.size(); i++) {
            prims0.set(i, prims0Tmp.get(i));
        }
        
        final IntArray prims1 = new IntArray(prims1Tmp.size());
        for (int i=0; i < prims1Tmp.size(); i++) {
            prims1.set(i, prims1Tmp.get(i));
        }
        
        buildTree(nodeNum+1, bounds[0], allPrimBounds,
                prims0, n0, depth-1, badRefines);
        
        nodesW.get(nodeNum).setAboveChild(nodesW.size());
        
        buildTree(nodesW.size(), bounds[1], allPrimBounds,
                prims1, n1, depth-1, badRefines);
    }
    
    public void writeTree(File file) throws IOException {
        java.io.Writer w = new java.io.BufferedWriter(new java.io.FileWriter(file));
        
    }
    
    private static KdTreeNode makeLeaf(final IntArray primNums, int nPrims,
            final Primitive[] prims) {
        KdTreeNode node = new KdTreeNode();
        
        node.flags = 3;
        
        node.prims = new Primitive[nPrims];
        
        for (int i=0; i < nPrims; i++) {
            node.prims[i] = prims[primNums.get(i)];
        }
        
        return node;
    }
    
    private static KdTreeNode makeInterior(int axis, float split) {
        KdTreeNode node = new KdTreeNode();
        
        node.flags = Float.floatToRawIntBits(split);
        node.flags &= ~3;
        node.flags |= axis;
        
        return node;
    }
    
    public AABB worldBounds() {
        return bounds;
    }
    
    static class BoundEdge implements Comparable {
        
        public int primNum;
        public float t;
        public EdgeType type;
        
        public BoundEdge(float tt, int pn, EdgeType type) {
            this.type = type;
            this.t = tt;
            this.primNum = pn;
        }
        
        public int compareTo(Object o) {
            BoundEdge other = (BoundEdge)o;
            if (t == other.t)
                return type.ordinal() - other.type.ordinal();
            else
                return (int)Math.signum(t - other.t);
        }
    }
    
    protected static class KdToDo {
        int nodeNum;
        float tmin, tmax;
    }
    
    protected static class KdTreeNode {
        int flags;
        Primitive[] prims;
        private int aboveChild;
        
        public int getAboveChild() { return aboveChild; }
        public void setAboveChild(int ac) {
          aboveChild = ac;
        }
        public boolean isLeaf() { return ((flags & 3) == 3); }
        public int splitAxis() { return (flags & 3); }
        public float splitPos() { return Float.intBitsToFloat(flags); }
    }
    
}
