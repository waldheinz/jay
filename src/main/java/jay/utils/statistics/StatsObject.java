/*
 * StatsObject.java
 *
 * Created on 28. Februar 2006, 15:06
 */

package jay.utils.statistics;

/**
 *
 * @author Matthias Treydte
 */
public abstract class StatsObject {
    
    protected String name;
    
    public StatsObject(String name) {
        this.name = name;
        Statistics.register(this);
    }
    
    public String getName() {
        return name;
    }
    
    public abstract String getValue();
}
