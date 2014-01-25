/*
 * PLYFileReader.java
 *
 * Created on 18. Juni 2007, 16:45
 */

package jay.fileio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Logger;
import jay.maths.Point;
import jay.maths.Transform;
import jay.scene.primitives.geometry.TriangleMesh;

/**
 * Reads PLY (polygon) files. See
 * http://local.wasp.uwa.edu.au/~pbourke/dataformats/ply/
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class PLYFileReader {
    
    /** The types of properties */
    public static enum PropertyType { SCALAR, LIST, STRING }
    
    /** Where we are reading from */
    protected final BufferedReader input;
    
    /** The line to be currently parsed, split into words. */
    protected String line[];
    
    /** The elements this PLY file containts */
    protected ArrayList<Element> elems =
            new ArrayList<Element>();
    
    private static Logger log = Logger.getLogger(PLYFileReader.class.getName());
    
    /**
     * Describes an element type in this PLY file.
     */
    protected static class Element {
        /** The number of elements of this kind in the file */
        public final int count;
        
        /** The name of this element */
        public final String name;
        
        /** Holds the properties of this element */
        private ArrayList<Property> props =
                new ArrayList<Property>();
        
        /**
         * Initializes a new element to the given
         * name and count.
         */
        public Element(final String name, int count) {
            this.count = count;
            this.name = name;
        }
        
        /**
         * Adds a new property to the end of the property list
         * of this element.
         */
        public void addProperty(Property p) { props.add(p); }
        
        public int getPropertyIdx(final String name) throws FileFormatException {
            for (int i=0; i < props.size(); i++)
                if (props.get(i).name.equals(name))
                    return i;
            
            throw new FileFormatException("Element \"" + this.name +
                    "\" has no property named \"" + name + "\"");
        }
    }
    
    /**
     * Describes a property of an element.
     */
    protected static class Property {
        /** The name of this property */
        public final String name;
        
        /** The type of this property */
        public final PropertyType type;
        
        public Property(final String name, final PropertyType type) {
            this.name = name;
            this.type = type;
        }
    }
    
    /** Creates a new instance of PLYFileReader */
    public PLYFileReader(final InputStream is) {
        try {
            this.input = new BufferedReader(new InputStreamReader(is, "US-ASCII"));
        } catch (UnsupportedEncodingException ex) {
           throw new RuntimeException("The PLYFileReader uses an unknown encoding?!", ex);
        }
    }
    
    /**
     * Reads the contents of the file and returns a mesh of
     * the contained object.
     *
     * @return The mesh stored in the file.
     */
    public TriangleMesh getObject() throws FileFormatException {
        try {
            log.info("reading PLY object");
            
            if (!checkHeader()) {
                throw new FileFormatException("can only read version 1.0 ascii ply files");
            }
            
            readHeader();
            return readContents();
            
        } catch (IOException ex) {
            throw new FileFormatException("error reading file", ex);
        }
        
    }
    
    /**
     * Checks if this is a PLY file we can understand.
     */
    protected boolean checkHeader() throws IOException {
        if (!input.readLine().equals("ply")) return false;
        if (!input.readLine().equals("format ascii 1.0")) return false;
        return true;
    }
    
    /**
     * Reads in the header of the file.
     */
    protected void readHeader() throws IOException {
        nextLine();
        
        while (true) {
            
            if (line[0].equals("comment")) {
                /* ignore comments */
                nextLine();
                continue;
            } else if (line[0].equals("element")) {
                readElementDesc();
                continue;
            } else if (line[0].equals("end_header")) {
                return;
            } else {
                throw new FileFormatException("unknown header entry \"" +
                        line[0] + "\"");
            }
        }
    }
    
    /**
     * Advance to the next line in the input file. The line
     * is split into words at the space character and stored
     * in the {@link #line} array.
     */
    protected void nextLine() throws IOException {
        line = input.readLine().split(" ");
    }
    
    /**
     * Reads the description of an element in the header.
     */
    protected void readElementDesc() throws IOException {
        Element e = new Element(line[1], Integer.parseInt(line[2]));
        
        nextLine();
        
        while (true) {
            
            if (line[0].equals("property")) {
                readPropertyDesc(e);
                nextLine();
            } else if (line[0].equals("comment")) {
                /* ignore comments */
                nextLine();
            } else {
                /* end of element */
                elems.add(e);
                return;
            } 
            
        }
    }

    /**
     * Reads in a property description for the given element.
     *
     * @param e The element to which the property should be added.
     */
    protected void readPropertyDesc(Element e) {
        if (line[1].equals("list")) {
            e.addProperty(new Property(line[4], PropertyType.LIST));
        } else if (line[1].equals("string")) {
            e.addProperty(new Property(line[2], PropertyType.STRING));
        } else {
            e.addProperty(new Property(line[2], PropertyType.SCALAR));
        }
    }
    
    /**
     * Reads the contents of the file and creates a {@link TriangleMesh}.
     *
     * @return The mesh read from the file.
     */
    protected TriangleMesh readContents() throws FileFormatException, IOException {
        final int vertexElem = getElementIdx("vertex");
        final int faceElem = getElementIdx("face");
        final int comp_x = elems.get(vertexElem).getPropertyIdx("x");
        final int comp_y = elems.get(vertexElem).getPropertyIdx("y");
        final int comp_z = elems.get(vertexElem).getPropertyIdx("z");
        
        /* Vertices */
        Point vertices[] = new Point[elems.get(vertexElem).count];
        int vidx = 0;
        
        int indices[] = new int[elems.get(faceElem).count * 3];
        int iidx = 0;
        
        int currentElem = 0;
        while (currentElem < elems.size()) {
            int linesToRead = elems.get(currentElem).count;
            
            while (linesToRead-- > 0) {
                nextLine();
                
                if (currentElem == vertexElem) {
                    vertices[vidx++] = new Point(
                            Float.parseFloat(line[comp_x]),
                            Float.parseFloat(line[comp_y]),
                            Float.parseFloat(line[comp_z]));
                } else if (currentElem == faceElem) {
                    /* FIXME: A fixed ordering of properties is assumed
                     * here, not sure if this is right.
                     */
                    if (Integer.parseInt(line[0]) == 3) {
                        indices[iidx++] = Integer.parseInt(line[1]);
                        indices[iidx++] = Integer.parseInt(line[2]);
                        indices[iidx++] = Integer.parseInt(line[3]);
                    } else {
                        log.warning("skipping non-triangle polygon");
                    }
                }
            }
            
            currentElem++;
        }
        
        return new TriangleMesh(
                Transform.IDENTITY,
                false,
                indices,
                vertices,
                null, null, null, null);
    }
    
    /**
     * Returns the index number of the element with the given name.
     *
     * @param name The name to search for.
     */
    private int getElementIdx(String name) throws FileFormatException {
        for (int i=0; i < elems.size(); i++)
            if (elems.get(i).name.equals(name))
                return i;
        
        throw new FileFormatException("PLY file has no element named \"" +
                name + "\"");
    }
}
