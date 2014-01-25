/*
 * KdTree.java
 *
 * Created on 1. März 2006, 19:31
 */

package jay.utils.kdtree;

import java.util.*;
import java.util.List;
import jay.maths.AABB;
import jay.maths.Point;

/**
 *
 * @author Matthias Treydte
 */
public class KdTree<NodeData extends AbstractNodeData> {
    
    int nNodes, nextFreeNode;
    KdNode[] nodes;
    AbstractNodeData[] nodeData;
    
    /** Creates a new instance of KdTree */
    public KdTree(List<NodeData> data) {
        nNodes = data.size();
        nextFreeNode = 1;
        nodes = new KdNode[nNodes];
        nodeData = new AbstractNodeData[nNodes];

        AbstractNodeData[] buildNodes = new AbstractNodeData[data.size()];
        for (int i=0; i < data.size(); i++) {
            buildNodes[i] = data.get(i);
        }
        
        recursiveBuild(0, 0, nNodes, buildNodes);
    }
    
    void recursiveBuild(int nodeNum, int start, int end,
            AbstractNodeData[] buildNodes) {
        
        /* Blatt erstellen */
        if (start + 1 == end) {
            nodes[nodeNum] = new KdNode();
            nodeData[nodeNum] = buildNodes[start];
            return;
        }
        // Choose split direction and partition data
        // Compute bounds of data from _start_ to _end_
        
        AABB bound = AABB.EMPTY;
        for (int i=start; i<end; ++i)
            bound.extend(buildNodes[i].p);
        
        int splitAxis = bound.maximumExtent();
        int splitPos = (start+end)/2;
        
        /* TODO: eigentlich muss nur das Element bei splitPos
         * an die richtige Position, nicht alle...
         */
        Arrays.sort(buildNodes, start, end, new CompareNode(splitAxis));
        
        
        // Allocate kd-tree node and continue recursively
        nodes[nodeNum] =
                new KdNode(buildNodes[splitPos].p.get(splitAxis), splitAxis);
        
        nodeData[nodeNum] = buildNodes[splitPos];
        if (start < splitPos) {
            nodes[nodeNum].setHasLeftChild(true);
            int childNum = nextFreeNode++;
            recursiveBuild(childNum, start, splitPos, buildNodes);
        }
        
        if (splitPos+1 < end) {
            nodes[nodeNum].setRightChild(nextFreeNode++);
            recursiveBuild(nodes[nodeNum].getRightChild(), splitPos+1,
                    end, buildNodes);
        }
    }
    
    public void lookup(Point p, Visitor<NodeData> visitor, float maxDistSquared) {
        privateLookup(0, p, visitor, maxDistSquared);
    }
    
    @SuppressWarnings("unchecked")
    void privateLookup(int nodeNum,
            Point p, Visitor<NodeData> visitor,
            float maxDistSquared)  {
        KdNode node = nodes[nodeNum];
        
        // Process kd-tree node's children
        int axis = node.getSplitAxis();

        if (axis != 3) {
            /* innerer Knoten */
            float dist2 = (p.get(axis) - node.getSplitPos()) *
                    (p.get(axis) - node.getSplitPos());
            
            if (p.get(axis) <= node.getSplitPos()) {
                if (node.getHasLeftChild())
                    privateLookup(nodeNum+1, p,
                            visitor, maxDistSquared);
                if (dist2 < maxDistSquared &&
                        node.getRightChild() > 0)
                    privateLookup(node.getRightChild(),
                            p,
                            visitor,
                            maxDistSquared);
            } else {
                if (node.getRightChild() > 0)
                    privateLookup(node.getRightChild(),
                            p,
                            visitor,
                            maxDistSquared);
                if (dist2 < maxDistSquared && node.getHasLeftChild())
                    privateLookup(nodeNum+1,
                            p,
                            visitor,
                            maxDistSquared);
            }
        }
        // Hand kd-tree node to processing function
        //float dist2 = DistanceSquared(nodeData[nodeNum].p, p);
        float dist2 = nodeData[nodeNum].p.sub(p).lengthSquared();
        if (dist2 < maxDistSquared)
            maxDistSquared = 
                    visitor.visit((NodeData)nodeData[nodeNum], dist2);
    }
    
    static class KdNode {
        public KdNode(float p, int a) {
            splitPos = p;
            data = a << 1;
        }
        
        /**
         * Erstellt ein Blatt
         */
        public KdNode() {
            data = ~0;
        }
        
        public void setHasLeftChild(boolean has) {
            data &= ~1;
            data |= has?1:0;
        }
        
        public boolean getHasLeftChild() {
            return ((data & 1) == 1)?true:false;
        }
        
        public void setRightChild(int num) {
            data &= 7;
            data |= num << 3;
        }
        
        public int getRightChild() {
            return data >> 3;
        }
        
        public int getSplitAxis() {
            return ((data >> 1) & 3);
        }
        
        public float getSplitPos() {
            return splitPos;
        }
        
        float splitPos;
        
        /**
         * bit 0 : hat linkes Kind?
         * bit 1-2 : spilt - Achse
         * bit 3..31 : # rechtes Kind
         */
        int data;
    }
    
    class CompareNode implements Comparator<AbstractNodeData> {
        final int axis;
        
        public CompareNode(int axis) {
            this.axis = axis;
        }
        
        public int compare(AbstractNodeData d1, AbstractNodeData d2) {
            return d1.p.get(axis) == d2.p.get(axis) ?
                (d1.compareTo(d2)) :
                (int)Math.signum(d1.p.get(axis) - d2.p.get(axis));
        }
        
    }
    
    
}
