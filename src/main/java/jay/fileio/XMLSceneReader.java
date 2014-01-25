/*
 * XMLSceneReader.java
 *
 * Created on 12. Juni 2007, 16:36
 */

package jay.fileio;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import javax.xml.stream.*;
import jay.cameras.*;
import jay.integrators.*;
import jay.integrators.photonmap.PhotonMapIntegrator;
import jay.lights.*;
import jay.lights.skylight.SkyLight;
import jay.materials.*;
import jay.materials.textures.*;
import jay.maths.*;
import jay.sampling.Film;
import jay.sampling.ImageFilm;
import jay.scene.Scene;
import jay.scene.primitives.*;
import jay.scene.primitives.geometry.*;
import jay.utils.Spectrum;

/**
 * 
 * For all of the readXXX() methods:
 * 
 * Precondition: The cursor is at the start element of
 *    the structure to read.
 *
 * Postcondition: The cursor is after the end element
 * of the structure just read.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class XMLSceneReader {
    
    protected InputStream input;
    protected XMLStreamReader r;
    protected Stack<Group> groups = new Stack<Group>();
    protected Scene s;
    protected Map<String, Material> materialLookup =
            new HashMap<String, Material>();
    
    private static Logger log = Logger.getLogger(XMLSceneReader.class.getName());
    
    public XMLSceneReader(final File file) throws XMLStreamException {
        try {
            init(new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException ex) {
            throw new XMLStreamException("Could not read file: ", ex);
        }
    }
    
    /** Creates a new instance of XMLSceneReader */
    public XMLSceneReader(final InputStream input) throws XMLStreamException {
        init(input);
    }
    
    protected void init(final InputStream input) throws XMLStreamException {
        this.input = input;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        this.r = factory.createXMLStreamReader(input);
    }
    
    public Scene getScene() throws XMLStreamException {
        
        /* find the start of the scene element */
        while (r.hasNext()) {
            r.next();
            if (r.isStartElement() && r.getLocalName().equals("scene"))
                return readScene();
        }
        
        throw new XMLStreamException("no scene found", r.getLocation());
    }
    
    /**
     * Skips to <em>after</em> the closing element of the current
     * tag.
     */
    protected void skipToClosingTag() throws XMLStreamException {
        int depth = 0;
        
        while (true) {
            if (r.isEndElement()) depth--;
            if (depth < 0) break;
            if (r.isStartElement()) depth++;
            r.next();
        }
        
        r.next();
    }
    
    protected Group readGroup() throws XMLStreamException {
        log.finer("reading a group");
        
        Group g = new Group();
        
        r.next();
        
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            final String name = r.getLocalName();
            if (name.equals("geometry")) {
                g.addChild(readGeometry());
            } else if (name.equals("group")) {
                g.addChild(readGroup());
            } else if (name.equals("light")) {
                s.addLight(readLight());
            } else if (name.equals("camera")) {
                s.setCamera(readCamera());
            } else {
                throw new XMLStreamException("unknown primitive \"" + name + "\"",
                        r.getLocation());
            }
            r.next();
        }
        
        return g;
    }
    
    protected Scene readScene() throws XMLStreamException {
        log.finer("reading the scene");
        boolean worldFound = false;
        s = new Scene();
        r.nextTag();
        
        while (r.hasNext()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("world")) {
                s.addChild(readGroup());
                worldFound=true;
            } else if (r.getLocalName().equals("settings")) {
                readSettings();
            } else {
                throw new XMLStreamException("unknown scene tag \"" +
                        r.getLocalName() + "\"", r.getLocation());
            }
            
            r.next();
        }
        
        if (!worldFound)
            throw new XMLStreamException("scene contains no world",
                    r.getLocation());
        
        return s;
    }

    protected Sphere readGeometrySphere() throws XMLStreamException {
        log.finer("reading a sphere");
        Sphere sp = new Sphere();
        r.next();
        
        while (!r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("radius")) {
                sp.setRadius(readFloatValue());
            } else {
                log.warning("skipping unknown tag \"" + 
                        r.getLocalName() + "\"");
            }
            
            r.nextTag();
        }
        
        return sp;
    }

    protected Point readPoint() throws XMLStreamException {
        log.finer("reading a point");
        try {
            r.next();
            String elems[] = r.getText().split(" ", 3);
            r.nextTag();
            return new Point(
                    Float.parseFloat(elems[0]),
                    Float.parseFloat(elems[1]),
                    Float.parseFloat(elems[2]));
            
        } catch (Exception ex) {
            skipToClosingTag();
            throw new XMLStreamException("could not read point at " +
                    r.getLocation(), ex);
        }

    }
    
    protected jay.maths.Vector readVector() throws XMLStreamException {
        try {
            return readPoint().vectorTo();
        } catch (XMLStreamException ex) {
            throw new XMLStreamException("could not read vector", ex);
        }
    }
    
    protected float readFloatValue() throws XMLStreamException {
        log.finer("reading a float value");
        
        try {
            r.next();
            float f = Float.parseFloat(r.getText());
            r.next();
            return f;
        } catch (XMLStreamException ex) {
            throw new XMLStreamException("expected float value", ex);
        }
    }
    
    protected Box readGeometryBox() throws XMLStreamException {
        log.finer("reading a box");
        Box b = new Box();
        r.next();
        
        while (!r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("size")) {
                jay.maths.Vector v = readVector();
                b.setExtents(v.x, v.y, v.z);
            } else {
                log.warning("ignoring unknown tag \"" + r.getLocalName() + "\"");
            }
            
            r.next();
        }
        
        return b;
    }
    
    protected GeometricPrimitive readGeometry() throws XMLStreamException {
        log.finer("reading a geometric object");

        r.next();

        Geometry g = null;
        Transform t = Transform.IDENTITY;
        Spectrum emit = null;
        ArrayList<Material> mats = new ArrayList<Material>();

        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }

            if (r.getLocalName().equals("sphere")) {
                g = readGeometrySphere();
            } else if (r.getLocalName().equals("box")) {
                g = readGeometryBox();
            } else if (r.getLocalName().equals("mesh")) {
                g = readGeometryMesh();
            } else if (r.getLocalName().equals("material")) {
                mats.add(readMaterial());
                r.next();
            } else if (r.getLocalName().equals("transform")) {
                t = readTransform();
            } else if (r.getLocalName().equals("emit")) {
                emit = readSpectrum();
            } else {
                throw new XMLStreamException("unknown geometry tag \"" +
                        r.getLocalName() + "\"", r.getLocation());
            }

            
        }

        if (g == null) {
            /* we did not read any geometry */
            throw new XMLStreamException("no geometry found while " +
                    "reading a geometric primitive", r.getLocation());
        }
        
        /* check if we really read a material */
        if (mats.size() == 0)
            mats.add(Material.DEFAULT);
        
        /* apply the gathered information */
        g.setTransform(t);
        GeometricPrimitive gp = new GeometricPrimitive(g);
        gp.makeLight(emit, s);
        gp.setMaterials(mats);
        
        return gp;
    }

    protected Transform readTransform() throws XMLStreamException {
        log.finer("reading a transformation");
        Transform t = Transform.IDENTITY;
        
        r.next();

        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }

            if (r.getLocalName().equals("translate")) {
                t = t.compose(Transform.translate(readVector()));
            } else if (r.getLocalName().equals("rotate_x")) {
                t = t.compose(Transform.rotateX(readFloatValue()));
            } else if (r.getLocalName().equals("rotate_y")) {
                t = t.compose(Transform.rotateY(readFloatValue()));
            } else if (r.getLocalName().equals("rotate_z")) {
                t = t.compose(Transform.rotateZ(readFloatValue()));
            } else if (r.getLocalName().equals("matrix")) {
                float[][] m = new float[4][4];
                for (int row=0; row < 4; row++) {
                    r.nextTag();
                    if (!r.getLocalName().equals("row"))
                        throw new XMLStreamException("expected a matrix <row>",
                              r.getLocation());
                    r.next();
                    m[row] = parseFloatVector(r.getText(), 4);
                    r.next();
                }

                Matrix mm = new Matrix(m);
                t = t.compose(new Transform(mm));
                r.next();
                r.next();
            } else {
                throw new XMLStreamException("unknown transformation tag \"" +
                      r.getLocalName() + "\"", r.getLocation());
            }

            r.nextTag();
        }
        
        r.next();
        
        return t;
    }

    protected Light readLight() throws XMLStreamException {
        log.finer("reading a light");
        String type = r.getAttributeValue(null, "type");
        if (type.equals("point")) {
            return readLightPoint();
        } else if (type.equals("sky")) {
            return readLightSky();
        } else {
            throw new XMLStreamException("unknown light type \"" + type + "\"");
        }
    }

    protected Light readLightPoint() throws XMLStreamException {
        log.finer("reading point light source");
        PointLight pl = new PointLight();
        
        r.next();
        
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("transform")) {
                pl.setTransform(readTransform());
            } else {
                throw new XMLStreamException("unknown point light tag \"" +
                      r.getLocalName() + "\"", r.getLocation());
            }
            
            r.next();
        }
        
        return pl;
    }

    protected Camera readCamera() throws XMLStreamException {
        log.finer("reading a camera");
        final String type = r.getAttributeValue(null, "type");
        
        if (type.equals("perspective")) {
            return readCameraPerspective();
        } else {
            throw new XMLStreamException("could not read camera of type \"" +
                    type + "\"", r.getLocation());
        }
    }

    protected PerspectiveCamera readCameraPerspective() throws XMLStreamException {
        log.finer("reading a perspective camera");
        
        r.next();
        PerspectiveCamera c = new PerspectiveCamera();
                
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("transform")) {
                /* the cameras expect world to camera transforms, so invert */
                c.setTransform(readTransform().getInverse());
            } else if (r.getLocalName().equals("fov")) {
                c.setFieldOfView(readFloatValue());
            } else {
                log.warning("unknown perspective camera tag \"" +
                        r.getLocalName() + "\""); 
            }
            
            r.next();
        }

        return c;
    }

    private Material readMaterial() throws XMLStreamException {
        log.finer("reading a material");
        
        String type = null;
        String name = null;
        
        for (int i=0; i < r.getAttributeCount(); i++) {
            final String a = r.getAttributeLocalName(i);
            if (a.equals("name"))
                name = r.getAttributeValue(i);
            else if (a.equals("type"))
                type = r.getAttributeValue(i);
            else
                throw new XMLStreamException("unknown material attribute \"" +
                        r.getAttributeLocalName(i), r.getLocation());
        }
        
        Material mat = null;
        
        if (type == null) {
            throw new XMLStreamException("missing material type", r.getLocation());
        }
        
        if (type.equals("phong")) {
            mat = readMaterialPhong();
        } else if (type.equals("blue_paint")) {
            mat = new BluePaint();
        } else if (type.equals("brushed_metal")) {
            mat = new BrushedMetal();
        } else if (type.equals("mat_felt")) {
            mat = new MaterialFelt();
        } else if (type.equals("mat_clay")) {
            mat = new MaterialClay();
        } else if (type.equals("mat_primer")) {
            mat = new MaterialPrimer();
        } else if (type.equals("glossy_paint")) {
            mat = readMaterialGlossyPaint();
        } else if (type.equals("glass")) {
            mat = new Glass();
        } else if (type.equals("plastic")) {
            mat = readMaterialPlastic();
        } else if (type.equals("ref")) {
            mat = materialLookup.get(name);
            if (mat == null) {
               throw new XMLStreamException("could not resolve material name\""
                       + name + "\"", r.getLocation());
            }
            
            
        } else {
            throw new XMLStreamException("unknown material type \"" +
                    type + "\"", r.getLocation());
        }
        
        r.next();
        
        if (name != null)
            materialLookup.put(name, mat);
        
        return mat;
    }

    protected Material readMaterialPhong() throws XMLStreamException {
        log.finer("reading a phong material");
        r.next();
        
        Texture<Spectrum> diffuse = null;
        Texture<Spectrum> specular = null;
        Texture<Float> exponent = null;
                
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("diffuse")) {
                diffuse = readTextureSpectrum();
            } else if (r.getLocalName().equals("specular")) {
                specular = readTextureSpectrum();
            } else if (r.getLocalName().equals("exponent")) {
                exponent = readTextureFloat();
            } else {
                log.warning("unknown phong material tag \"" +
                        r.getLocalName() + "\""); 
            }
            
            r.nextTag();
        }
        
        PhongMaterial p = new PhongMaterial();
        
        if (diffuse != null)
            p.setDiffuse(diffuse);
        
        if (specular != null)
            p.setSpecular(specular);
        
        if (exponent != null)
            p.setExponent(exponent);
        
        return p;
    }

    protected Texture<Spectrum> readTextureSpectrum() throws XMLStreamException {
        log.finer("reading a spectrum texture");
        r.nextTag();
        
        if (r.getLocalName().equals("spectrum")) {
            ConstantTexture<Spectrum> t = new ConstantTexture<Spectrum>(
                    readSpectrum());
            r.next();
            return t;
            
        } else if (r.getLocalName().equals("checkerboard")) {
            Texture<Spectrum> t = readTextureSpectrumCheckerboard();
            r.next();
            return t;
        } else {
            throw new XMLStreamException("unknown spectrum texture type " +
                    r.getLocalName() + "\"");
        }
    }

    protected Spectrum readSpectrumSampled() throws XMLStreamException {
        log.finer("reading a sampled spectrum");
        
        ArrayList<Float> wl = new ArrayList<Float>();
        ArrayList<Float> in = new ArrayList<Float>();
        
        r.next();
           
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("smp")) {
                wl.add(new Float(r.getAttributeValue(null, "w")));
                in.add(new Float(r.getAttributeValue(null, "i")));
            } else {
                log.warning("ignoring spectrum sample tag \"" +
                        r.getLocalName() + "\""); 
            }
            
            r.nextTag();
            r.next();
        }
        
        r.next();
        
        float wla[] = new float[wl.size()];
        float ina[] = new float[in.size()];
        
        for (int i=0; i < wla.length; i++) {
            wla[i] = wl.get(i).floatValue();
            ina[i] = in.get(i).floatValue();
        }
        
        return Spectrum.fromSamples(wla, ina);
    }

    protected Spectrum readSpectrum() throws XMLStreamException {
        log.finer("reading a spectrum");
        final String type = r.getAttributeValue(null, "type");
        
        if (type.equals("constant")) {
            return new Spectrum(readFloatValue());
        } else if (type.equals("sampled")) {
            return readSpectrumSampled();
        } else if (type.equals("rgb")) {
            Point rgb = readPoint();
            r.nextTag();
            return Spectrum.fromRGB(rgb.x, rgb.y, rgb.z);
        } else throw new XMLStreamException("unknown spectrum type \"" +
                type + "\" at " + r.getLocation());
    }

    protected TriangleMesh readGeometryMesh() throws XMLStreamException {
        log.finer("reading a triangle mesh");
        final String type = r.getAttributeValue(null, "type");
        
        if (type.equals("embedded")) {
            return readGeometryMeshEmbedded();
        } else if (type.equals("ply")) {
            String name = r.getAttributeValue(null, "file");
            try {
                PLYFileReader fr = new PLYFileReader(
                        new FileInputStream(new File(name)));
                r.nextTag();
                return fr.getObject();
            } catch (Exception ex) {
                throw new XMLStreamException("can't read external mesh", ex);
            }
            
        } else {
            throw new XMLStreamException("unkown triangle mesh type \"" +
                    type + "\"");
        }
    }

    protected TriangleMesh readGeometryMeshEmbedded() throws XMLStreamException {
        log.finer("reading an embedded triangle mesh");
        r.next();
        
        ArrayList<Point> p = new ArrayList<Point>();
        ArrayList<Normal> n = new ArrayList<Normal>();
        ArrayList<Integer> vidx = new ArrayList<Integer>();
        ArrayList<Integer> matIdx = new ArrayList<Integer>();
        
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("vertex")) {
                for (int i=0; i < r.getAttributeCount(); i++) {
                    final String aname = r.getAttributeLocalName(i);
                    if (aname.equals("pos")) {
                        String spos[] = r.getAttributeValue(i).split(" ", 3);
                        p.add(new Point(
                            Float.parseFloat(spos[0]),
                            Float.parseFloat(spos[1]),
                            Float.parseFloat(spos[2])));
                    } else if (aname.equals("normal")) {
                        String snorm[] = r.getAttributeValue(i).split(" ", 3);
                        n.add(new Normal(
                            Float.parseFloat(snorm[0]),
                            Float.parseFloat(snorm[1]),
                            Float.parseFloat(snorm[2])));
                    }
                }
                
                r.next();
            } else if (r.getLocalName().equals("tri")) {
                boolean rv = false;
                int midx = 1;
                
                for (int i=0; i < r.getAttributeCount(); i++) {
                    final String aname = r.getAttributeLocalName(i);
                    if (aname.equals("vidx")) {
                        String vid[] = r.getAttributeValue(i).split(" ", 3);
                        vidx.add(Integer.parseInt(vid[0]));
                        vidx.add(Integer.parseInt(vid[1]));
                        vidx.add(Integer.parseInt(vid[2]));
                        if (rv)
                            throw new XMLStreamException(
                                  "already read the vertex index");
                        else
                            rv = true;
                    } else if (aname.equals("material")) {
                        midx = Integer.parseInt(r.getAttributeValue(i));
                    }
                    
                   
                }
                
                if (!rv) throw new XMLStreamException("read no vindex");
                matIdx.add(midx);
                
                r.next();
            } else {
                log.warning("ignoring unknown embedded mesh tag \"" +
                        r.getLocalName() + "\"");
            }
            
            r.nextTag();
        }
        
        /* convert coordinates to pure array */
        Point pos[] = new Point[p.size()];
        p.toArray(pos);
        
        Normal norm [] = null;
        
        if (n.size() > 0) {
            if (n.size() != p.size())
                throw new XMLStreamException("count of vertex positions (" +
                    p.size() + ") does not equal normals count (" +
                    n.size() + ")");
                    
            norm = new Normal[n.size()];
            n.toArray(norm);
        }
        
        /* convert vertex indices to pure array */
        int vid[] = new int[vidx.size()];
        int i=0;
        for (int id : vidx) vid[i++] = id;
        
        /* convert material indices to pure array */
        int[] midx = new int[matIdx.size()];
        i=0;
        for (int mid : matIdx) midx[i++] = mid - 1;
        
        r.next();
        
        return new TriangleMesh(
              Transform.IDENTITY,
              false,
              vid,
              pos,
              norm,
              null,
              null,
              midx);
    }
    
    protected Texture<Float> readTextureFloat() throws XMLStreamException {
        log.finer("reading a spectrum texture");
        final String type = r.getAttributeValue(null, "type");
        
        if (type.equals("constant")) {
             ConstantTexture<Float> t = new ConstantTexture<Float>(
                    readFloatValue());
             r.next();
             return t;
        } else {
            throw new XMLStreamException("unknown float texture type " +
                    type + "\"");
        }
    }

    protected void readSettings() throws XMLStreamException {
        log.finer("reading scene settings");
        r.next();
        
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("mode")) {
                r.next();
                final String mode = r.getText();
                
                Film f = s.getFilm();
                
                if (mode.equals("direct")) {
                    s.setSurfaceIntegrator(new DirectLightingIntegrator(f));
                } else if (mode.equals("path")) {
                    s.setSurfaceIntegrator(new PathIntegrator(f));
                } else if (mode.equals("bidir_path")) {
                    s.setSurfaceIntegrator(new BidirPathIntegrator(f));
                } else if (mode.equals("debug")) {
                    s.setSurfaceIntegrator(new DebugIntegrator(f));
                } else if (mode.equals("photonmap")) {
                    s.setSurfaceIntegrator(new PhotonMapIntegrator(f));
                } else if (mode.equals("instantgi")) {
                    s.setSurfaceIntegrator(new InstantGIIntegrator(f));
                } else {
                    throw new XMLStreamException("unknown render mode \"" +
                            mode + "\"", r.getLocation());
                }
                
                r.next();
                r.nextTag();
            } else if (r.getLocalName().equals("resolution")) { 
                r.next();
                float res[] = parseFloatVector(r.getText(), 2);
                s.setFilm(new ImageFilm((int)res[0], (int)res[1]));
                r.next();
            } else {
                throw new XMLStreamException("unknown scene setting \"" +
                        r.getLocalName() + "\"", r.getLocation());
            }
        }
    }
    
    protected float[] parseFloatVector(final String toParse, int count)
            throws XMLStreamException {
        
        final String[] str = toParse.split(" ", count);
        float[] vals = new float[count];
        int i=0;
        
        try {
            for (i=0; i < count; i++)
                vals[i] = Float.parseFloat(str[i]);
        } catch (NumberFormatException ex) {
            throw new XMLStreamException("could not parse float value #" +
                    i+1, r.getLocation(), ex);
        }
        
        return vals;
    }

    protected GlossyPaint readMaterialGlossyPaint() throws XMLStreamException {
        log.finer("reading glossy paint material");
        r.next();
        
        Texture<Spectrum> diffuse = null;
        Texture<Spectrum> specular = null;
        Texture<Float> nu = null;
        Texture<Float> nv = null;
        
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("diffuse")) {
                diffuse = readTextureSpectrum();
            } else if (r.getLocalName().equals("specular")) {
                specular = readTextureSpectrum();
            } else if (r.getLocalName().equals("nu")) {
                nu = readTextureFloat();
            } else if (r.getLocalName().equals("nv")) {
                nv = readTextureFloat();
            } else {
                throw new XMLStreamException("unknown glossy paint material tag \"" +
                        r.getLocalName() + "\"", r.getLocation()); 
            }
            
            r.nextTag();
        }
        
        GlossyPaint gp = new GlossyPaint();
        
        if (diffuse != null)
            gp.setDiffuse(diffuse);
        
        if (specular != null)
            gp.setSpecular(specular);
        
        if (nu != null)
            gp.setRoughU(nu);
        
        if (nv != null)
            gp.setRoughV(nv);
        
        return gp;
    }

    private SkyLight readLightSky() throws XMLStreamException {
        r.nextTag();
        return new SkyLight();
    }
    
    private Texture<Spectrum> readTextureSpectrumCheckerboard()
            throws XMLStreamException {
        
        r.nextTag();
        TextureMapping2D m = null;
        Texture<Spectrum> t1 = null;
        Texture<Spectrum> t2 = null;
        
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("map")) {
                m = readTextureMapping2D();
                r.next();
            } else if (r.getLocalName().equals("t1")) {
                if (t1 != null) throw new
                        XMLStreamException("texture t1 already read",
                        r.getLocation());
                //r.next();
                t1 = readTextureSpectrum();
                r.next();
            } else if (r.getLocalName().equals("t2")) {
                if (t2 != null) throw new
                        XMLStreamException("texture t2 already read",
                        r.getLocation());
                //r.next();
                t2 = readTextureSpectrum();
                r.next();
            }
        }
        
        if (t1 == null) throw new
                XMLStreamException("no texture t1 found", r.getLocation());
        if (t2 == null) throw new
                XMLStreamException("no texture t2 found", r.getLocation());
        if (m == null) throw new
                XMLStreamException("no texture mapping found", r.getLocation());
        
        r.next();
        
        return new CheckerBoard<Spectrum>(m, t1, t2);
    }

    private TextureMapping2D readTextureMapping2D() throws XMLStreamException {
        final String type = r.getAttributeValue(null, "type");
        
        if (type.equals("uv")) {
            r.next();
            float[] v = parseFloatVector(r.getText(), 4);
            r.nextTag();
            return new TextureMapping2DUV(v[0], v[1], v[2], v[3]);
        } else throw new XMLStreamException("unknown 2D mapping \""+
                type + "\"", r.getLocation());
    }

    private MaterialPlastic readMaterialPlastic() throws XMLStreamException {
        r.next();
        
        MaterialPlastic m = new MaterialPlastic();
        
        while (r.hasNext() && !r.isEndElement()) {
            if (!r.isStartElement()) {
                r.next();
                continue;
            }
            
            if (r.getLocalName().equals("diffuse")) {
                m.setDiffuse(readTextureSpectrum());
            } else if (r.getLocalName().equals("specular")) {
                m.setSpecular(readTextureSpectrum());
            } else if (r.getLocalName().equals("roughness")) {
                m.setRoughness(readTextureFloat());
            } else {
                throw new XMLStreamException("unknown plastic material tag \"" +
                        r.getLocalName() + "\"", r.getLocation()); 
            }
            
            r.nextTag();
        }
        
        return m;
    }
}
