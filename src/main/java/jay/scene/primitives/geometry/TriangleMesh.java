/*
 * TriangleMesh.java
 *
 * Created on 21. Februar 2006, 15:23
 */

package jay.scene.primitives.geometry;

import jay.maths.*;
import jay.utils.GeometryList;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class TriangleMesh extends Geometry {
    
    protected final int nTris;
    
    /** The vertex positions of this mesh */
    final Point[] p;
    
    /** The normals at the vertex positions. */
    final Normal[] n;
    
    /**
     * Array of vertex indices. For the ith triangle,
     * the vertex positions are p[vertexIndex[i]],
     * p[vertexIndex[i+1]] and p[vertexIndex[i+2]]. The
     * same rule applies to the normals and tangents.
     */
    private final int[] vertexIndex;
    
    /**
     * The tangents at vertex positions.
     */
    final Vector[] s;
    
    final float[] uvs;
    
    /**
     * The material indices to be used by the triangles
     * contained in this mesh, or <code>null</code> if the 
     * triangles all use a common material.
     */
    int[] materialIndex;
    
    public TriangleMesh(int[] vptr, final Point[] P) {
        this(Transform.IDENTITY, false, vptr, P, null, null, null, null);
    }

    public TriangleMesh(int[] vptr, Point[] points, Normal[] normals) {
        this(Transform.IDENTITY, false, vptr, points, normals, null, null, null);
    }

    /** Creates a new instance of TriangleMesh */
    public TriangleMesh(final Transform o2w, boolean ro,
            int[] vptr, final Point[] P, final Normal[] N,
            final Vector[] S, float[] uv, int[] matInd) {
        
        this.invertNormals = ro;
        this.g2w = o2w;
        this.w2g = o2w.getInverse();
        nTris = vptr.length / 3;
        p = P;
        n = N;
        s = S;
        materialIndex = matInd;
        vertexIndex = vptr;
        uvs = uv;
        
        if ((p != null) && (g2w != Transform.IDENTITY)) {
            for (int i=0; i < p.length; i++)
                p[i] = g2w.apply(p[i]);
        }
    }
    
    public int getIndex(int offset) {
        return vertexIndex[offset];
    }
    
    public Point getPoint(int offset) {
        return p[getIndex(offset)];
    }

    public AABB localBounds() {
        AABB bobj = AABB.EMPTY;
        
        for (int i = 0; i < p.length; i++)
            bobj = bobj.extend(w2g.apply(p[i]));
        
        return bobj;
    }
    
    public float getArea() {
        return 1;
    }
    
    @Override
    public boolean canIntersect() {
        return false;
    }
    
    @Override
    public AABB worldBounds() {
        AABB bobj = AABB.EMPTY;
        
        for (int i = 0; i < p.length; i++)
            bobj = bobj.extend(p[i]);
        
        return bobj;
    }
    
    @Override
    public void refine(final GeometryList glist) {
        for (int i = 0; i < nTris; ++i)
            glist.add(new Triangle(this, i));
    }
    
    @Override
    public void setTransform(final Transform toWorld) {
        final Transform oldToGeometry = w2g;
        super.setTransform(toWorld);
        if (oldToGeometry != Transform.IDENTITY)
            for (int i=0; i < p.length; ++i)
                p[i] = g2w.apply(oldToGeometry.apply(p[i]));
                
    }

    @Override
    public String toString() {
        return "TriangleMesh [tris="+nTris+", bounds=" + worldBounds() + "]";
    }

}
