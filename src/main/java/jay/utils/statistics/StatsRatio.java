/*
 * StatsRatio.java
 *
 * Created on 5. März 2006, 17:14
 */

package jay.utils.statistics;

/**
 *
 * @author trem
 */
public class StatsRatio extends StatsObject {
    
    int tries;
    int succeeded;
    
    /** Creates a new instance of StatsRatio */
    public StatsRatio(String name) {
        super(name);
        tries = succeeded = 0;
    }
    
    public void add(int tries, int succeeded) {
        this.tries += tries;
        this.succeeded += succeeded;
    }
    
    public String getValue() {
        if (tries > 0) return Float.toString((float)succeeded / tries);
        else return "---";
    }
    
}
