/*
 * StatsCounter.java
 *
 * Created on 28. Februar 2006, 15:04
 */

package jay.utils.statistics;

/**
 *
 * @author Matthias Treydte
 */
public class StatsCounter extends StatsObject {
    
    private int value;
    
    /** Creates a new instance of StatsCounter */
    public StatsCounter(String name) {
        super(name);
        this.value = 0;
    }
    
    public void increment() {
        value++;
    }

    public String getValue() {
        return Integer.toString(value);
    }

    public int get() {
        return value;
    }

    @Override
    public String toString() {
        return name + ": " +value;
    }
    
}
