/**
 * the type of ASTNode
 */
public enum ASTNodeType {
    Programm, // program entrance

    IntDEclaration, // int Variable declaration
    ExpressionStmt, // statement of expression with  a ';'
    AssignmentStmt, // statement of assignment expression

    Primary, // 
    Multiplicative,
    Additive,

    IDENFR,
    IntLiteral, IntDeclaration
}
