/*
 * Statistics.java
 *
 * Created on 28. Februar 2006, 15:04
 */

package jay.utils.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author trem
 */
public class Statistics {
    
    static ArrayList<StatsObject> objects = new ArrayList<StatsObject>();
    
    public static void register(StatsObject s) {
        objects.add(s);
    }
    
    public static List<StatsObject> getStats() {
        return objects;
    }
    
    public static int getStatsCount() {
        return objects.size();
    }
    
}
