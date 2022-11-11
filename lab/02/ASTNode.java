import java.util.List;

/**
 * ASTNode 
 */
public interface ASTNode {
    public ASTNode getParent();

    public List<ASTNode> getChildren();

    public ASTNodeType getType();

    public String getText();
}
