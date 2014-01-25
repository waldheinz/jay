/*
 * SpectrumCalc.java
 *
 * Created on 5. Januar 2006, 15:38
 */

package jay.utils;

/**
 * Ein veränderbares Spektrum. Macht interne Berechnungen
 * in Funktionen wesentlich übersichtlicher.
 *
 * z.B. kann man
 *
 * <pre>
 * L = L.scale(e.bsdf.eval(e.wi, e.wo). 
 *     scale(Utils.absdot(e.wo, e.ng) / 
 *     (e.bsdfWeight * e.rrWeight)));
 * </pre>
 *
 * umformen zu
 *
 * <pre> 
 * L.scale(e.bsdf.eval(e.wi, e.wo));
 * L.scale(Utils.absdot(e.wo, e.ng));
 * L.scale(1.0f / (e.bsdfWeight * e.rrWeight));
 * </pre>
 *
 * @author Matthias Treydze
 */
public class SpectrumCalc {
    
    /**
     * der aktuelle Wert
     */
    public Spectrum s;
    
    /** Erstellt eine neue Instanz von SpectrumCalc */
    public SpectrumCalc() {
        s = Spectrum.BLACK;
    }
    
    public SpectrumCalc(Spectrum value) {
        s = value;
    }
    
    public void scale(float f) {
        s = s.scale(f);
    }
    
    public void scale(Spectrum s) {
        this.s = this.s.scale(s);
    }
    
    public void add(Spectrum s) {
        this.s = this.s.add(s);
    }
    
}
