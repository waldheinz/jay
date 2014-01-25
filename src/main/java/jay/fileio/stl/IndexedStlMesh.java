package jay.fileio.stl;


import java.util.List;
import java.util.TreeSet;
import jay.maths.Normal;
import jay.maths.Point;

class IndexedStlMesh {
	final Point[] points;
	final int[] triangles;		//We have triangles.length/3 triangles. The i'th triangle has 
							//x-coordinate points[triangles[3*i]], 
							//y-coordinate points[triangles[3*i+1] and
							//z-coordinate points[triangles[3*i+2]],
	final Normal[] normals;		//The normals according to the triangles (Some redundant information inside STL-Files)
	
	IndexedStlMesh(List<StlTriangle> triangles) {
		this.triangles = new int[3 * triangles.size()];
		normals=new Normal[triangles.size()];
		
		TreeSet<Point> p=new TreeSet<Point>();
        
		for(StlTriangle t:triangles) {
			p.add(t.v1);
			p.add(t.v2);
			p.add(t.v3);
		}
        
		points=p.toArray(new Point[0]);
		int i=0;
        
		for (StlTriangle t : triangles) {
			normals[i/3] = t.normal;
			this.triangles[i++] = java.util.Arrays.binarySearch(points, t.v1);
			this.triangles[i++] = java.util.Arrays.binarySearch(points, t.v2);
			this.triangles[i++] = java.util.Arrays.binarySearch(points, t.v3);
		}
	}
}
