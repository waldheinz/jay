/*
 * KdTree.java
 *
 * Created on 23. Juli 2007, 19:35
 */

package jay.scene.primitives.accelerators;

import java.util.ArrayList;
import java.util.List;
import jay.maths.AABB;
import jay.maths.Ray;
import jay.scene.primitives.Group;
import jay.scene.primitives.Intersection;
import jay.scene.primitives.NearestIntersection;
import jay.scene.primitives.Primitive;

public class KdTree extends Accelerator {
    private int[] tree;
    private int[] primitives;
    private List<Primitive> primitiveList;
    private AABB bounds;

    private int maxPrims;

    private static final float INTERSECT_COST = 2f;
    private static final float TRAVERSAL_COST = 1;
    private static final float EMPTY_BONUS = 0.3f;
    private static final int MAX_DEPTH = 64;

    private ThreadLocal stackStorage = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            StackNode[] stack = new StackNode[MAX_DEPTH + 2];
            for (int i = 0; i < stack.length; i++)
                stack[i] = new StackNode();
            
            return stack;
        }
    };
    
    public KdTree(Group group) {
        this(group, 0);
    }

    public KdTree(Group group, int maxPrims) {
        super(group);
        this.maxPrims = maxPrims;
    }

    private static class BuildStats {
        private int numNodes;
        private int numLeaves;
        private int sumObjects;
        private int minObjects;
        private int maxObjects;
        private int sumDepth;
        private int minDepth;
        private int maxDepth;
        private int numLeaves0;
        private int numLeaves1;
        private int numLeaves2;
        private int numLeaves3;
        private int numLeaves4;
        private int numLeaves4p;

        BuildStats() {
            numNodes = numLeaves = 0;
            sumObjects = 0;
            minObjects = Integer.MAX_VALUE;
            maxObjects = Integer.MIN_VALUE;
            sumDepth = 0;
            minDepth = Integer.MAX_VALUE;
            maxDepth = Integer.MIN_VALUE;
            numLeaves0 = 0;
            numLeaves1 = 0;
            numLeaves2 = 0;
            numLeaves3 = 0;
            numLeaves4 = 0;
            numLeaves4p = 0;
        }

        void updateInner() {
            numNodes++;
        }

        void updateLeaf(int depth, int n) {
            numLeaves++;
            minDepth = Math.min(depth, minDepth);
            maxDepth = Math.max(depth, maxDepth);
            sumDepth += depth;
            minObjects = Math.min(n, minObjects);
            maxObjects = Math.max(n, maxObjects);
            sumObjects += n;
            switch (n) {
                case 0:
                    numLeaves0++;
                    break;
                case 1:
                    numLeaves1++;
                    break;
                case 2:
                    numLeaves2++;
                    break;
                case 3:
                    numLeaves3++;
                    break;
                case 4:
                    numLeaves4++;
                    break;
                default:
                    numLeaves4p++;
                    break;
            }
        }

        void printStats() {
//            UI.printDetailed(Module.ACCEL, );
            System.out.printf("KDTree stats:\n");
            System.out.printf("  * Nodes:          %d\n", numNodes);
            System.out.printf("  * Leaves:         %d\n", numLeaves);
            System.out.printf("  * Objects: min    %d\n", minObjects);
            System.out.printf("             avg    %.2f\n", (float) sumObjects / numLeaves);
            System.out.printf("           avg(n>0) %.2f\n", (float) sumObjects / (numLeaves - numLeaves0));
            System.out.printf("             max    %d\n", maxObjects);
            System.out.printf("  * Depth:   min    %d\n", minDepth);
            System.out.printf("             avg    %.2f\n", (float) sumDepth / numLeaves);
            System.out.printf("             max    %d\n", maxDepth);
            System.out.printf("  * Leaves w/: N=0  %3d%%\n", 100 * numLeaves0 / numLeaves);
            System.out.printf("               N=1  %3d%%\n", 100 * numLeaves1 / numLeaves);
            System.out.printf("               N=2  %3d%%\n", 100 * numLeaves2 / numLeaves);
            System.out.printf("               N=3  %3d%%\n", 100 * numLeaves3 / numLeaves);
            System.out.printf("               N=4  %3d%%\n", 100 * numLeaves4 / numLeaves);
            System.out.printf("               N>4  %3d%%\n", 100 * numLeaves4p / numLeaves);
        }
    }

    public void build(List<Primitive> primitives) {
//        UI.printDetailed(Module.ACCEL, "KDTree settings");
//        UI.printDetailed(Module.ACCEL, "  * Max Leaf Size:  %d", maxPrims);
//        UI.printDetailed(Module.ACCEL, "  * Max Depth:      %d", MAX_DEPTH);
//        UI.printDetailed(Module.ACCEL, "  * Traversal cost: %.2f", TRAVERSAL_COST);
//        UI.printDetailed(Module.ACCEL, "  * Intersect cost: %.2f", INTERSECT_COST);
//        UI.printDetailed(Module.ACCEL, "  * Empty bonus:    %.2f", EMPTY_BONUS);
//        UI.printDetailed(Module.ACCEL, "  * Dump leaves:    %s", dump ? "enabled" : "disabled");

        this.primitiveList = primitives;
        // get the object space bounds
        bounds = AABB.EMPTY;
        for (Primitive p : primitives)
            bounds = bounds.extend(p.worldBounds());
        
        int nPrim = primitiveList.size(), nSplits = 0;
        BuildTask task = new BuildTask(nPrim);
   
        for (int i = 0; i < nPrim; i++) {
            for (int axis = 0; axis < 3; axis++) {
                float ls = primitiveList.get(i).worldBounds().min.get(axis);
                float rs = primitiveList.get(i).worldBounds().max.get(axis);
                if (ls == rs) {
                    // flat in this dimension
                    task.splits[nSplits] = pack(ls, PLANAR, axis, i);
                    nSplits++;
                } else {
                    task.splits[nSplits + 0] = pack(ls, OPENED, axis, i);
                    task.splits[nSplits + 1] = pack(rs, CLOSED, axis, i);
                    nSplits += 2;
                }
            }
        }
        task.n = nSplits;
        IntArray tempTree = new IntArray();
        IntArray tempList = new IntArray();
        tempTree.add(0);
        tempTree.add(1);
       
        // sort it
        radix12(task.splits, task.n);
        // build the actual tree
        BuildStats stats = new BuildStats();
        buildTree(bounds.min.x, bounds.max.x, 
              bounds.min.y, bounds.max.y, bounds.min.z, bounds.max.z, 
              task, 1, tempTree, 0, tempList, stats);
        
        // write out final arrays
        // free some memory
        task = null;
        tree = tempTree.trim();
        tempTree = null;
        this.primitives = tempList.trim();
        tempList = null;
        // display some extra info
        stats.printStats();
//        UI.printDetailed(Module.ACCEL, "  * Node memory:    %s", Memory.sizeof(tree));
//        UI.printDetailed(Module.ACCEL, "  * Object memory:  %s", Memory.sizeof(this.primitives));
//        UI.printDetailed(Module.ACCEL, "  * Prepare time:   %s", prepare);
//        UI.printDetailed(Module.ACCEL, "  * Sorting time:   %s", sorting);
//        UI.printDetailed(Module.ACCEL, "  * Tree creation:  %s", t);
//        UI.printDetailed(Module.ACCEL, "  * Build time:     %s", total);
        
    }

    // type is encoded as 2 shifted bits
    private static final long CLOSED = 0L << 30;
    private static final long PLANAR = 1L << 30;
    private static final long OPENED = 2L << 30;
    private static final long TYPE_MASK = 3L << 30;

    // pack split values into a 64bit integer
    private static long pack(float split, long type, int axis, int object) {
        // pack float in sortable form
        int f = Float.floatToRawIntBits(split);
        int top = f ^ ((f >> 31) | 0x80000000);
        long p = ((long) top & 0xFFFFFFFFL) << 32;
        p |= type; // encode type as 2 bits
        p |= ((long) axis) << 28; // encode axis as 2 bits
        p |= (object & 0xFFFFFFFL); // pack object number
        return p;
    }

    private static int unpackObject(long p) {
        return (int) (p & 0xFFFFFFFL);
    }

    private static int unpackAxis(long p) {
        return (int) (p >>> 28) & 3;
    }

    private static long unpackSplitType(long p) {
        return p & TYPE_MASK;
    }

    private static float unpackSplit(long p) {
        int f = (int) ((p >>> 32) & 0xFFFFFFFFL);
        int m = ((f >>> 31) - 1) | 0x80000000;
        return Float.intBitsToFloat(f ^ m);
    }

    // radix sort on top 36 bits - returns sorted result
    private static void radix12(long[] splits, int n) {
        // allocate working memory
        final int[] hist = new int[2048];
        final long[] sorted = new long[n];
        // parallel histogramming pass
        for (int i = 0; i < n; i++) {
            long pi = splits[i];
            hist[0x000 + ((int) (pi >>> 28) & 0x1FF)]++;
            hist[0x200 + ((int) (pi >>> 37) & 0x1FF)]++;
            hist[0x400 + ((int) (pi >>> 46) & 0x1FF)]++;
            hist[0x600 + ((int) (pi >>> 55))]++;
        }

        // sum the histograms - each histogram entry records the number of
        // values preceding itself.
        {
            int sum0 = 0, sum1 = 0, sum2 = 0, sum3 = 0;
            int tsum;
            for (int i = 0; i < 512; i++) {
                tsum = hist[0x000 + i] + sum0;
                hist[0x000 + i] = sum0 - 1;
                sum0 = tsum;
                tsum = hist[0x200 + i] + sum1;
                hist[0x200 + i] = sum1 - 1;
                sum1 = tsum;
                tsum = hist[0x400 + i] + sum2;
                hist[0x400 + i] = sum2 - 1;
                sum2 = tsum;
                tsum = hist[0x600 + i] + sum3;
                hist[0x600 + i] = sum3 - 1;
                sum3 = tsum;
            }
        }

        // read/write histogram passes
        for (int i = 0; i < n; i++) {
            long pi = splits[i];
            int pos = (int) (pi >>> 28) & 0x1FF;
            sorted[++hist[0x000 + pos]] = pi;
        }
        for (int i = 0; i < n; i++) {
            long pi = sorted[i];
            int pos = (int) (pi >>> 37) & 0x1FF;
            splits[++hist[0x200 + pos]] = pi;
        }
        for (int i = 0; i < n; i++) {
            long pi = splits[i];
            int pos = (int) (pi >>> 46) & 0x1FF;
            sorted[++hist[0x400 + pos]] = pi;
        }
        for (int i = 0; i < n; i++) {
            long pi = sorted[i];
            int pos = (int) (pi >>> 55);
            splits[++hist[0x600 + pos]] = pi;
        }
    }

    private static class BuildTask {
        long[] splits;
        int numObjects;
        int n;
        byte[] leftRightTable;

        BuildTask(int numObjects) {
            splits = new long[6 * numObjects];
            this.numObjects = numObjects;
            n = 0;
            // 2 bits per object
            leftRightTable = new byte[(numObjects + 3) / 4];
        }

        BuildTask(int numObjects, BuildTask parent) {
            splits = new long[6 * numObjects];
            this.numObjects = numObjects;
            n = 0;
            leftRightTable = parent.leftRightTable;
        }
    }

    private void buildTree(float minx, float maxx, float miny, float maxy, float minz, float maxz, BuildTask task, int depth, IntArray tempTree, int offset, IntArray tempList, BuildStats stats) {
        // get node bounding box extents
        if (task.numObjects > maxPrims && depth < MAX_DEPTH) {
            float dx = maxx - minx;
            float dy = maxy - miny;
            float dz = maxz - minz;
            // search for best possible split
            float bestCost = INTERSECT_COST * task.numObjects;
            int bestAxis = -1;
            int bestOffsetStart = -1;
            int bestOffsetEnd = -1;
            float bestSplit = 0;
            boolean bestPlanarLeft = false;
            int bnl = 0, bnr = 0;
            // inverse area of the bounding box (factor of 2 ommitted)
            float area = (dx * dy + dy * dz + dz * dx);
            float ISECT_COST = INTERSECT_COST / area;
            // setup counts for each axis
            int[] nl = { 0, 0, 0 };
            int[] nr = { task.numObjects, task.numObjects, task.numObjects };
            // setup bounds for each axis
            float[] dp = { dy * dz, dz * dx, dx * dy };
            float[] ds = { dy + dz, dz + dx, dx + dy };
            float[] nodeMin = { minx, miny, minz };
            float[] nodeMax = { maxx, maxy, maxz };
            // search for best cost
            int nSplits = task.n;
            long[] splits = task.splits;
            byte[] lrtable = task.leftRightTable;
            for (int i = 0; i < nSplits;) {
                // extract current split
                long ptr = splits[i];
                float split = unpackSplit(ptr);
                int axis = unpackAxis(ptr);
                // mark current position
                int currentOffset = i;
                // count number of primitives start/stopping/lying on the
                // current plane
                int pClosed = 0, pPlanar = 0, pOpened = 0;
                long ptrMasked = ptr & (~TYPE_MASK & 0xFFFFFFFFF0000000L);
                long ptrClosed = ptrMasked | CLOSED;
                long ptrPlanar = ptrMasked | PLANAR;
                long ptrOpened = ptrMasked | OPENED;
                while (i < nSplits && (splits[i] & 0xFFFFFFFFF0000000L) == ptrClosed) {
                    int obj = unpackObject(splits[i]);
                    lrtable[obj >>> 2] = 0;
                    pClosed++;
                    i++;
                }
                while (i < nSplits && (splits[i] & 0xFFFFFFFFF0000000L) == ptrPlanar) {
                    int obj = unpackObject(splits[i]);
                    lrtable[obj >>> 2] = 0;
                    pPlanar++;
                    i++;
                }
                while (i < nSplits && (splits[i] & 0xFFFFFFFFF0000000L) == ptrOpened) {
                    int obj = unpackObject(splits[i]);
                    lrtable[obj >>> 2] = 0;
                    pOpened++;
                    i++;
                }
                // now we have summed all contributions from this plane
                nr[axis] -= pPlanar + pClosed;
                // compute cost
                if (split >= nodeMin[axis] && split <= nodeMax[axis]) {
                    // left and right surface area (factor of 2 ommitted)
                    float dl = split - nodeMin[axis];
                    float dr = nodeMax[axis] - split;
                    float lp = dp[axis] + dl * ds[axis];
                    float rp = dp[axis] + dr * ds[axis];
                    // planar prims go to smallest cell always
                    boolean planarLeft = dl < dr;
                    int numLeft = nl[axis] + (planarLeft ? pPlanar : 0);
                    int numRight = nr[axis] + (planarLeft ? 0 : pPlanar);
                    float eb = ((numLeft == 0 && dl > 0) || (numRight == 0 && dr > 0)) ? EMPTY_BONUS : 0;
                    float cost = TRAVERSAL_COST + ISECT_COST * (1 - eb) * (lp * numLeft + rp * numRight);
                    if (cost < bestCost) {
                        bestCost = cost;
                        bestAxis = axis;
                        bestSplit = split;
                        bestOffsetStart = currentOffset;
                        bestOffsetEnd = i;
                        bnl = numLeft;
                        bnr = numRight;
                        bestPlanarLeft = planarLeft;
                    }
                }
                // move objects left
                nl[axis] += pOpened + pPlanar;
            }
            // debug check for correctness of the scan
            for (int axis = 0; axis < 3; axis++) {
                int numLeft = nl[axis];
                int numRight = nr[axis];
                if (numLeft != task.numObjects || numRight != 0)
                    System.err.println("did not scan full range");
//                    UI.printError(Module.ACCEL, "Didn't scan full range of objects @depth=%d. Left overs for axis %d: [L: %d] [R: %d]", depth, axis, numLeft, numRight);
            }
            // found best split?
            if (bestAxis != -1) {
                // allocate space for child nodes
                BuildTask taskL = new BuildTask(bnl, task);
                BuildTask taskR = new BuildTask(bnr, task);
                int lk = 0, rk = 0;
                for (int i = 0; i < bestOffsetStart; i++) {
                    long ptr = splits[i];
                    if (unpackAxis(ptr) == bestAxis) {
                        if (unpackSplitType(ptr) != CLOSED) {
                            int obj = unpackObject(ptr);
                            lrtable[obj >>> 2] |= 1 << ((obj & 3) << 1);
                            lk++;
                        }
                    }
                }
                for (int i = bestOffsetStart; i < bestOffsetEnd; i++) {
                    long ptr = splits[i];
                    assert unpackAxis(ptr) == bestAxis;
                    if (unpackSplitType(ptr) == PLANAR) {
                        if (bestPlanarLeft) {
                            int obj = unpackObject(ptr);
                            lrtable[obj >>> 2] |= 1 << ((obj & 3) << 1);
                            lk++;
                        } else {
                            int obj = unpackObject(ptr);
                            lrtable[obj >>> 2] |= 2 << ((obj & 3) << 1);
                            rk++;
                        }
                    }
                }
                for (int i = bestOffsetEnd; i < nSplits; i++) {
                    long ptr = splits[i];
                    if (unpackAxis(ptr) == bestAxis) {
                        if (unpackSplitType(ptr) != OPENED) {
                            int obj = unpackObject(ptr);
                            lrtable[obj >>> 2] |= 2 << ((obj & 3) << 1);
                            rk++;
                        }
                    }
                }
                // output new splits while maintaining order
                long[] splitsL = taskL.splits;
                long[] splitsR = taskR.splits;
                int nsl = 0, nsr = 0;
                for (int i = 0; i < nSplits; i++) {
                    long ptr = splits[i];
                    int obj = unpackObject(ptr);
                    int idx = obj >>> 2;
                    int mask = 1 << ((obj & 3) << 1);
                    if ((lrtable[idx] & mask) != 0) {
                        splitsL[nsl] = ptr;
                        nsl++;
                    }
                    if ((lrtable[idx] & (mask << 1)) != 0) {
                        splitsR[nsr] = ptr;
                        nsr++;
                    }
                }
                taskL.n = nsl;
                taskR.n = nsr;
                // free more memory
                task.splits = splits = splitsL = splitsR = null;
                task = null;
                // allocate child nodes
                int nextOffset = tempTree.getSize();
                tempTree.add(0);
                tempTree.add(0);
                tempTree.add(0);
                tempTree.add(0);
                // create current node
                tempTree.set(offset + 0, (bestAxis << 30) | nextOffset);
                tempTree.set(offset + 1, Float.floatToRawIntBits(bestSplit));
                // recurse for child nodes - free object arrays after each step
                stats.updateInner();
                switch (bestAxis) {
                    case 0:
                        buildTree(minx, bestSplit, miny, maxy, minz, maxz, taskL, depth + 1, tempTree, nextOffset, tempList, stats);
                        taskL = null;
                        buildTree(bestSplit, maxx, miny, maxy, minz, maxz, taskR, depth + 1, tempTree, nextOffset + 2, tempList, stats);
                        taskR = null;
                        return;
                    case 1:
                        buildTree(minx, maxx, miny, bestSplit, minz, maxz, taskL, depth + 1, tempTree, nextOffset, tempList, stats);
                        taskL = null;
                        buildTree(minx, maxx, bestSplit, maxy, minz, maxz, taskR, depth + 1, tempTree, nextOffset + 2, tempList, stats);
                        taskR = null;
                        return;
                    case 2:
                        buildTree(minx, maxx, miny, maxy, minz, bestSplit, taskL, depth + 1, tempTree, nextOffset, tempList, stats);
                        taskL = null;
                        buildTree(minx, maxx, miny, maxy, bestSplit, maxz, taskR, depth + 1, tempTree, nextOffset + 2, tempList, stats);
                        taskR = null;
                        return;
                    default:
                        assert false;
                }
            }
        }
        // create leaf node
        int listOffset = tempList.getSize();
        int n = 0;
        for (int i = 0; i < task.n; i++) {
            long ptr = task.splits[i];
            if (unpackAxis(ptr) == 0 && unpackSplitType(ptr) != CLOSED) {
                tempList.add(unpackObject(ptr));
                n++;
            }
        }
        stats.updateLeaf(depth, n);
        if (n != task.numObjects)
            System.err.println("error creating leaf");
//            UI.printError(Module.ACCEL, "Error creating leaf node - expecting %d found %d", task.numObjects, n);
        tempTree.set(offset + 0, (3 << 30) | listOffset);
        tempTree.set(offset + 1, task.numObjects);
        // free some memory
        task.splits = null;
    }

    public Intersection nearestIntersection(Ray r) {
        float intervalMin = r.tmin;
        float intervalMax = r.tmax;
        float orgX = r.o.x;
        float dirX = r.d.x, invDirX = 1 / dirX;
        float t1, t2;
        t1 = (bounds.min.x - orgX) * invDirX;
        t2 = (bounds.max.x - orgX) * invDirX;
        if (invDirX > 0) {
            if (t1 > intervalMin)
                intervalMin = t1;
            if (t2 < intervalMax)
                intervalMax = t2;
        } else {
            if (t2 > intervalMin)
                intervalMin = t2;
            if (t1 < intervalMax)
                intervalMax = t1;
        }
        if (intervalMin > intervalMax)
            return null;
        
        float orgY = r.o.y;
        float dirY = r.d.y, invDirY = 1 / dirY;
        t1 = (bounds.min.y - orgY) * invDirY;
        t2 = (bounds.max.y - orgY) * invDirY;
        if (invDirY > 0) {
            if (t1 > intervalMin)
                intervalMin = t1;
            if (t2 < intervalMax)
                intervalMax = t2;
        } else {
            if (t2 > intervalMin)
                intervalMin = t2;
            if (t1 < intervalMax)
                intervalMax = t1;
        }
        if (intervalMin > intervalMax)
            return null;
        float orgZ = r.o.z;
        float dirZ = r.d.z, invDirZ = 1 / dirZ;
        t1 = (bounds.min.z - orgZ) * invDirZ;
        t2 = (bounds.max.z - orgZ) * invDirZ;
        if (invDirZ > 0) {
            if (t1 > intervalMin)
                intervalMin = t1;
            if (t2 < intervalMax)
                intervalMax = t2;
        } else {
            if (t2 > intervalMin)
                intervalMin = t2;
            if (t1 < intervalMax)
                intervalMax = t1;
        }
        if (intervalMin > intervalMax)
            return null;

        // compute custom offsets from direction sign bit
        int offsetXFront = (Float.floatToRawIntBits(dirX) & (1 << 31)) >>> 30;
        int offsetYFront = (Float.floatToRawIntBits(dirY) & (1 << 31)) >>> 30;
        int offsetZFront = (Float.floatToRawIntBits(dirZ) & (1 << 31)) >>> 30;

        int offsetXBack = offsetXFront ^ 2;
        int offsetYBack = offsetYFront ^ 2;
        int offsetZBack = offsetZFront ^ 2;

        StackNode[] stack = (StackNode[])stackStorage.get();
        NearestIntersection ni = new NearestIntersection();
        int stackTop = 0;
        int stackPos = stackTop;
        int node = 0;

        while (true) {
            int tn = tree[node];
            int axis = tn & (3 << 30);
            int offset = tn & ~(3 << 30);
            r.cellsVisited++;
            
            switch (axis) {
                case 0: {
                    float d = (Float.intBitsToFloat(tree[node + 1]) - orgX) * invDirX;
                    int back = offset + offsetXBack;
                    node = back;
                    if (d < intervalMin)
                        continue;
                    node = offset + offsetXFront; // front
                    if (d > intervalMax)
                        continue;
                    // push back node
                    stack[stackPos].node = back;
                    stack[stackPos].near = (d >= intervalMin) ? d : intervalMin;
                    stack[stackPos].far = intervalMax;
                    stackPos++;
                    // update ray interval for front node
                    intervalMax = (d <= intervalMax) ? d : intervalMax;
                    continue;
                }
                case 1 << 30: {
                    // y axis
                    float d = (Float.intBitsToFloat(tree[node + 1]) - orgY) * invDirY;
                    int back = offset + offsetYBack;
                    node = back;
                    if (d < intervalMin)
                        continue;
                    node = offset + offsetYFront; // front
                    if (d > intervalMax)
                        continue;
                    // push back node
                    stack[stackPos].node = back;
                    stack[stackPos].near = (d >= intervalMin) ? d : intervalMin;
                    stack[stackPos].far = intervalMax;
                    stackPos++;
                    // update ray interval for front node
                    intervalMax = (d <= intervalMax) ? d : intervalMax;
                    continue;
                }
                case 2 << 30: {
                    // z axis
                    float d = (Float.intBitsToFloat(tree[node + 1]) - orgZ) * invDirZ;
                    int back = offset + offsetZBack;
                    node = back;
                    if (d < intervalMin)
                        continue;
                    node = offset + offsetZFront; // front
                    if (d > intervalMax)
                        continue;
                    // push back node
                    stack[stackPos].node = back;
                    stack[stackPos].near = (d >= intervalMin) ? d : intervalMin;
                    stack[stackPos].far = intervalMax;
                    stackPos++;
                    // update ray interval for front node
                    intervalMax = (d <= intervalMax) ? d : intervalMax;
                    continue;
                }
                default: {
                    // leaf - test some objects
                    int n = tree[node + 1];
                    while (n > 0) {
                        
                        ni.set(primitiveList.get(primitives[offset]).nearestIntersection(r));
                        r.intersections++;
                        n--;
                        offset++;
                    }
                    if (r.tmax < intervalMax)
                        return ni.get();
                    
                    do {
                        // stack is empty?
                        if (stackPos == stackTop)
                            return ni.get();
                        // move back up the stack
                        stackPos--;
                        intervalMin = stack[stackPos].near;
                        if (r.tmax < intervalMin)
                            continue;
                        node = stack[stackPos].node;
                        intervalMax = stack[stackPos].far;
                        break;
                    } while (true);
                }
            } // switch
        } // traversal loop
    }

    public final class IntArray {
        private int[] array;
        private int size;

        public IntArray() {
            array = new int[10];
            size = 0;
        }

        public IntArray(int capacity) {
            array = new int[capacity];
            size = 0;
        }

        /**
         * Append an integer to the end of the array.
         * 
         * @param i
         */
        public final void add(int i) {
            if (size == array.length) {
                int[] oldArray = array;
                array = new int[(size * 3) / 2 + 1];
                System.arraycopy(oldArray, 0, array, 0, size);
            }
            array[size] = i;
            size++;
        }

        /**
         * Write a value to the specified index. Assumes the array is already big
         * enough.
         * 
         * @param index
         * @param value
         */
        public final void set(int index, int value) {
            array[index] = value;
        }

        /**
         * Read value from the array.
         * 
         * @param index index into the array
         * @return value at the specified index
         */
        public final int get(int index) {
            return array[index];
        }

        /**
         * Returns the number of elements added to the array.
         * 
         * @return current size of the array
         */
        public final int getSize() {
            return size;
        }

        /**
         * Return a copy of the array, trimmed to fit the size of its contents
         * exactly.
         * 
         * @return a new array of exactly the right length
         */
        public final int[] trim() {
            if (size < array.length) {
                int[] oldArray = array;
                array = new int[size];
                System.arraycopy(oldArray, 0, array, 0, size);
            }
            return array;
        }
    }
    
    public static final class StackNode {
        public int node;
        public float near;
        public float far;
    }
    
    public void rebuild() {
        /* make a flat array of primitives */
        List<Primitive> prims = new ArrayList<Primitive>();
        for (Primitive p : group.getPrimitives())
            Primitive.recursiveRefine(prims, p);
        
        this.build(prims);
    }

    public boolean intersects(final Ray r) {
        float intervalMin = r.tmin;
        float intervalMax = r.tmax;
        float orgX = r.o.x;
        float dirX = r.d.x, invDirX = 1 / dirX;
        float t1, t2;
        t1 = (bounds.min.x - orgX) * invDirX;
        t2 = (bounds.max.x - orgX) * invDirX;
        if (invDirX > 0) {
            if (t1 > intervalMin)
                intervalMin = t1;
            if (t2 < intervalMax)
                intervalMax = t2;
        } else {
            if (t2 > intervalMin)
                intervalMin = t2;
            if (t1 < intervalMax)
                intervalMax = t1;
        }
        if (intervalMin > intervalMax)
            return false;
        
        float orgY = r.o.y;
        float dirY = r.d.y, invDirY = 1 / dirY;
        t1 = (bounds.min.y - orgY) * invDirY;
        t2 = (bounds.max.y - orgY) * invDirY;
        if (invDirY > 0) {
            if (t1 > intervalMin)
                intervalMin = t1;
            if (t2 < intervalMax)
                intervalMax = t2;
        } else {
            if (t2 > intervalMin)
                intervalMin = t2;
            if (t1 < intervalMax)
                intervalMax = t1;
        }
        if (intervalMin > intervalMax)
            return false;
        float orgZ = r.o.z;
        float dirZ = r.d.z, invDirZ = 1 / dirZ;
        t1 = (bounds.min.z - orgZ) * invDirZ;
        t2 = (bounds.max.z - orgZ) * invDirZ;
        if (invDirZ > 0) {
            if (t1 > intervalMin)
                intervalMin = t1;
            if (t2 < intervalMax)
                intervalMax = t2;
        } else {
            if (t2 > intervalMin)
                intervalMin = t2;
            if (t1 < intervalMax)
                intervalMax = t1;
        }
        if (intervalMin > intervalMax)
            return false;

        // compute custom offsets from direction sign bit
        int offsetXFront = (Float.floatToRawIntBits(dirX) & (1 << 31)) >>> 30;
        int offsetYFront = (Float.floatToRawIntBits(dirY) & (1 << 31)) >>> 30;
        int offsetZFront = (Float.floatToRawIntBits(dirZ) & (1 << 31)) >>> 30;

        int offsetXBack = offsetXFront ^ 2;
        int offsetYBack = offsetYFront ^ 2;
        int offsetZBack = offsetZFront ^ 2;

        StackNode[] stack = (StackNode[])stackStorage.get();
        int stackTop = 0;
        int stackPos = stackTop;
        int node = 0;

        while (true) {
            int tn = tree[node];
            int axis = tn & (3 << 30);
            int offset = tn & ~(3 << 30);
            r.cellsVisited++;
            
            switch (axis) {
                case 0: {
                    float d = (Float.intBitsToFloat(tree[node + 1]) - orgX) * invDirX;
                    int back = offset + offsetXBack;
                    node = back;
                    if (d < intervalMin)
                        continue;
                    node = offset + offsetXFront; // front
                    if (d > intervalMax)
                        continue;
                    // push back node
                    stack[stackPos].node = back;
                    stack[stackPos].near = (d >= intervalMin) ? d : intervalMin;
                    stack[stackPos].far = intervalMax;
                    stackPos++;
                    // update ray interval for front node
                    intervalMax = (d <= intervalMax) ? d : intervalMax;
                    continue;
                }
                case 1 << 30: {
                    // y axis
                    float d = (Float.intBitsToFloat(tree[node + 1]) - orgY) * invDirY;
                    int back = offset + offsetYBack;
                    node = back;
                    if (d < intervalMin)
                        continue;
                    node = offset + offsetYFront; // front
                    if (d > intervalMax)
                        continue;
                    // push back node
                    stack[stackPos].node = back;
                    stack[stackPos].near = (d >= intervalMin) ? d : intervalMin;
                    stack[stackPos].far = intervalMax;
                    stackPos++;
                    // update ray interval for front node
                    intervalMax = (d <= intervalMax) ? d : intervalMax;
                    continue;
                }
                case 2 << 30: {
                    // z axis
                    float d = (Float.intBitsToFloat(tree[node + 1]) - orgZ) * invDirZ;
                    int back = offset + offsetZBack;
                    node = back;
                    if (d < intervalMin)
                        continue;
                    node = offset + offsetZFront; // front
                    if (d > intervalMax)
                        continue;
                    // push back node
                    stack[stackPos].node = back;
                    stack[stackPos].near = (d >= intervalMin) ? d : intervalMin;
                    stack[stackPos].far = intervalMax;
                    stackPos++;
                    // update ray interval for front node
                    intervalMax = (d <= intervalMax) ? d : intervalMax;
                    continue;
                }
                default: {
                    // leaf - test some objects
                    int n = tree[node + 1];
                    while (n > 0) {
                        
                        if (primitiveList.get(primitives[offset]).intersects(r))
                            return true;
                        
                        n--;
                        offset++;
                    }
                    if (r.tmax < intervalMax)
                        return false;
                    do {
                        // stack is empty?
                        if (stackPos == stackTop)
                            return false;
                        // move back up the stack
                        stackPos--;
                        intervalMin = stack[stackPos].near;
                        if (r.tmax < intervalMin)
                            continue;
                        node = stack[stackPos].node;
                        intervalMax = stack[stackPos].far;
                        break;
                    } while (true);
                }
            } // switch
        } // traversal loop
    }

    @Override
    public AABB worldBounds() {
        return bounds;
    }

}
