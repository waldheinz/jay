/*
 * Visitor.java
 *
 * Created on 4. März 2006, 15:30
 */

package jay.utils.kdtree;

/**
 *
 * @author Matthias Treydte
 */
public abstract class Visitor<NodeData extends AbstractNodeData> {
    /**
     * @return neue max. Entfernung²
     */
    public abstract float visit(NodeData node, float dist2);
}
