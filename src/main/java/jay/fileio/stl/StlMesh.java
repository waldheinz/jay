package jay.fileio.stl;

import java.io.*;
import java.util.regex.*;
import java.io.FileReader;
import java.util.zip.DataFormatException;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.ByteOrder;
import java.util.List;
import jay.maths.Normal;
import jay.maths.Point;
import jay.scene.primitives.geometry.TriangleMesh;

public class StlMesh {
	static public List<StlTriangle> read(File f) throws FileNotFoundException,
            IOException, DataFormatException {
        
		FileReader fr = new FileReader(f);
		if(f.length()>Integer.MAX_VALUE)
			throw new IOException("File size exceeds " + Integer.MAX_VALUE);
		CharBuffer cb=CharBuffer.allocate(5);
		fr.read(cb);
		fr.close();
		cb.rewind();
		List<StlTriangle> triangles = null;
        
		if(String.valueOf(cb).compareTo("solid")==0) {
			try {
				triangles = readASCII(f);
			} catch (Exception E) {
				System.err.println("Unable to read " + f.getName() +
                        " as ASCII-File, retry to read it as binary one.");
			}
		}

		if (triangles == null)
			triangles = readBinary(f);
		return triangles;
	}

    public static TriangleMesh getTriangleMesh(List<StlTriangle> triangles) {
        IndexedStlMesh mtMesh = new IndexedStlMesh(triangles);
        System.err.println("We have " + mtMesh.points.length +
                " different points (out of " + mtMesh.triangles.length +
                " points) in " + mtMesh.normals.length + " triangles.");

        return new TriangleMesh(mtMesh.triangles, mtMesh.points);
    }

	static List<StlTriangle> readASCII(File f) throws FileNotFoundException,
            IOException, DataFormatException {
        
		//:TODO: What about the solid's name?
		
		FileReader fr=new FileReader(f);
		CharBuffer cb=CharBuffer.allocate((int)f.length());
		System.err.println("Try to read STL-ASCII file of length "+fr.read(cb));
		fr.close();
		cb.rewind();

		Pattern pSolid = Pattern.compile(
                "solid\\s+(\\S+)\\s+(.+?)\\s+endsolid", Pattern.DOTALL);
		String sVertex = "vertex\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+";
		String sNormal = "normal\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+";
		Pattern pFacet = Pattern.compile(
                "facet\\s+"+sNormal+"outer loop\\s+" +
                sVertex + sVertex + sVertex +
                "endloop\\s+endfacet\\s+", Pattern.DOTALL);
		
		Matcher match = pSolid.matcher(cb);

		if(!match.find()) {
			throw new DataFormatException(
                    "Unable to find the solid's boundaries.");
        }
        
		int start=match.start(2);
		final int end=match.end(2);
		System.err.println("Found solid: "+match.group(1) +"("+start+"-"+end+")");
		match.usePattern(pFacet);
		final List<StlTriangle> triangles = new ArrayList<StlTriangle>();
        
		while (start < end) {
			if(!match.find(start))
				break;
            
			StlTriangle t = new StlTriangle();
			t.normal = new Normal(
                    Float.parseFloat(match.group(1)),
                    Float.parseFloat(match.group(2)),
                    Float.parseFloat(match.group(3)));
			t.v1 = new Point(
                    Float.parseFloat(match.group(4)),
                    Float.parseFloat(match.group(5)),
                    Float.parseFloat(match.group(6)));
			t.v2 = new Point(
                    Float.parseFloat(match.group(7)),
                    Float.parseFloat(match.group(8)),
                    Float.parseFloat(match.group(9)));
			t.v3 = new Point(
                    Float.parseFloat(match.group(10)),
                    Float.parseFloat(match.group(11)),
                    Float.parseFloat(match.group(12)));
			triangles.add(t);
//			System.out.println(match.group(0));
			start=match.end(0);
		}
        
		System.err.println("Found " + triangles.size() +
                " triangles in file " + f.getName());
		return triangles;
	}
	
	static List<StlTriangle> readBinary(File f) throws FileNotFoundException,IOException, DataFormatException{
		//:TODO: What about the ASCII prefix?
		
		FileChannel fc=new FileInputStream(f).getChannel();
		ByteBuffer bb=ByteBuffer.allocate((int)f.length());
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int l=fc.read(bb);
		fc.close();
		bb.rewind();
		System.err.println("Try to read STL-binary file of length "+l);
		byte[] prefix=new byte[80];
		bb.get(prefix);
		bb.position(80);
		
		long facets=bb.getInt()&(long)0xFFFFFFF;
		System.err.println("Start reading of binary file containing "+facets+" facets with prefix "+new String(prefix));
		final List<StlTriangle> triangles = new ArrayList<StlTriangle>();

		for(int i=0;i<facets;i++) {
			FloatBuffer fb=bb.asFloatBuffer();
			StlTriangle t=new StlTriangle();
			triangles.add(t);
			t.normal=new Normal(fb.get(),fb.get(),fb.get());
			t.v1=new Point(fb.get(),fb.get(),fb.get());
			t.v2=new Point(fb.get(),fb.get(),fb.get());
			t.v3=new Point(fb.get(),fb.get(),fb.get());
			bb.position(bb.position()+4*12);
			short attCount=bb.getShort();
			if(attCount!=0)
				throw new DataFormatException("Attribute count is not zero.");
		}
		System.err.println("Found "+triangles.size()+" triangles in file "+f.getName());
		return triangles;
	}
}
