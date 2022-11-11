import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * craft a simple calculator
 * did not exactly correct cause of associative property
 */
public class Calculator {
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        String script = "int a = b+3;";
        System.out.println("解析变量声明语句: " + script);
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(script);
        try {
        SimpleASTNode node = calculator.intDeclare(tokens);
        calculator.dumpAST(node, "");
        } catch (Exception e) {
        System.out.println(e.getMessage());
        }

        // 测试表达式
        script = "2+3*5";
        System.out.println("\n计算: " + script + "，看上去一切正常。");
        calculator.evaluate(script);

        // 测试语法错误
        script = "2+";
        System.out.println("\n: " + script + "，应该有语法错误。");
        calculator.evaluate(script);

        script = "2+3+4";
        System.out.println("\n计算: " + script + "，结合性出现错误。");
        // calculator.evaluate(script);
        // Scanner sc = new Scanner(System.in);

        // String script = sc.nextLine();
        // System.out.println("parse variable declaration statement:" + script);
        // SimpleLexer lexer = new SimpleLexer();
        // TokenReader tokens = lexer.tokenize(script);
        // SimpleASTNode node = calculator.intDeclare(tokens);
        // calculator.dumpAST(node, "");
        // todo waiting for craft
        // calculator.evaluate(script);
    }

    /**
     * parse and return rootNode
     * 
     * @param code
     * @return
     * @throws Exception
     */
    public ASTNode parse(String code) throws Exception {
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(code);

        ASTNode rootNode = prog(tokens);

        return rootNode;
    }

    /**
     * parse rootNode
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private SimpleASTNode prog(TokenReader tokens) throws Exception {
        SimpleASTNode node = new SimpleASTNode(ASTNodeType.Programm, "Calculator");

        SimpleASTNode child = additive(tokens);

        if (child != null) {
            node.addChild(child);
        }
        return node;
    }

    public void evaluate(String script) {
        ASTNode tree;
        try {
            tree = parse(script); // rootNode
            // it is not ok.
            System.out.println("it is ok??");
            dumpAST(tree, ""); // print
            evaluate(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * evaluate ASTNode and print the procedure and answer
     * 
     * @param node
     * @param indent : tab
     * @return
     */
    private int evaluate(ASTNode node, String indent) {
        int ans = 0;
        System.out.println(indent + "Calculating: " + node.getType());
        ASTNodeType nodeType = node.getType();
        if (nodeType == ASTNodeType.Programm) {
            for (ASTNode child : node.getChildren()) {
                ans = evaluate(child, indent + "\t");
            }
        }
        if (nodeType == ASTNodeType.Additive) {
            ASTNode child1 = node.getChildren().get(0);
            int value1 = evaluate(child1, indent + "\t");
            ASTNode child2 = node.getChildren().get(1);
            int value2 = evaluate(child2, indent + "\t");
            if (node.getText().equals("+")) {
                ans = value1 + value2;
            }
            if (node.getText().equals("-")) {
                ans = value1 - value2;
            }
        }
        if (nodeType == ASTNodeType.Multiplicative) {
            ASTNode child1 = node.getChildren().get(0);
            int value1 = evaluate(child1, indent + "\t");
            ASTNode child2 = node.getChildren().get(1);
            int value2 = evaluate(child2, indent + "\t");
            if (node.getText().equals("*")) {
                ans = value1 * value2;
            }
            if (node.getText().equals("/")) {
                ans = value1 / value2;
            }
        }
        if (nodeType == ASTNodeType.IntLiteral) {
            ans = Integer.valueOf(node.getText()).intValue();
        }
        System.out.println(indent + "Answer: " + ans);
        return ans;
    }

    /**
     * additive -> multiplicative | multiplicative + additive
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private SimpleASTNode additive(TokenReader tokens) throws Exception {
        SimpleASTNode child1 = multiplicative(tokens);
        SimpleASTNode node = child1;

        Token token = tokens.peek();
        TokenType tokenType = token.getType();
        if (child1 != null && token != null) {
            if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU) {
                token = tokens.read();
                SimpleASTNode child2 = additive(tokens);
                if (child2 == null) {
                    throw new Exception("invalid additive expression, expecting the right part.");
                }
                if (child2 != null) {
                    node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                }
            }
        }
        return node;
    }

    /**
     * multiplicative -> primary | primary * multiplicative
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private SimpleASTNode multiplicative(TokenReader tokens) throws Exception {
        SimpleASTNode child1 = primary(tokens);
        SimpleASTNode node = child1;

        Token token = tokens.peek();
        TokenType tokenType = token.getType();
        if (token != null && node != null) {
            if (tokenType == TokenType.MULT || tokenType == TokenType.DIV) {
                token = tokens.read();
                SimpleASTNode child2 = multiplicative(tokens);
                if (child2 == null) {
                    throw new Exception("invalid multiplicative experssion, expecting the right part.");
                }
                if (child2 != null) {
                    node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
                    node.children.add(child1);
                    node.children.add(child2);
                }
            }
        }
        return node;
    }

    /**
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private SimpleASTNode primary(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        Token token = tokens.peek();
        TokenType tokenType = token.getType();
        String tokenText = token.getText();
        if (token != null) {
            if (tokenType == TokenType.INTCON) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IntLiteral, tokenText);
            }
            if (tokenType == TokenType.IDENFR) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IDENFR, tokenText);
            }
            if (tokenType == TokenType.LPARENT) {
                tokens.read();
                node = additive(tokens);
                if (node == null) {
                    throw new Exception("Expecting an additive expression inside parenthesis.");
                }
                if (node != null) {
                    token = tokens.peek();
                    if (token != null && tokenType == TokenType.RPARENT) {
                        tokens.read();
                    }
                    if (token == null || tokenType != TokenType.LPARENT) {
                        throw new Exception("Expecting right parenthesis.");
                    }
                }
            }
        }
        return node;
    }

    /**
     * Implement a ASTNode
     * get type, text, parent, children
     */
    private class SimpleASTNode implements ASTNode {
        SimpleASTNode parent = null;
        List<ASTNode> children = new ArrayList<ASTNode>();
        ASTNodeType nodeType = null;
        String text = null;

        public SimpleASTNode(ASTNodeType nodeType, String text) {
            this.nodeType = nodeType;
            this.text = text;
        }

        public ASTNode getParent() {
            return parent;
        }

        public List<ASTNode> getChildren() {
            return children;
        }

        public ASTNodeType getType() {
            return nodeType;
        }

        public String getText() {
            return text;
        }

        public void addChild(SimpleASTNode child) {
            children.add(child);
            child.parent = this;
        }
    }

    /**
     * print ASTTree
     * 
     * @param node
     * @param indent : tab|<-->|
     */
    private void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode child : node.getChildren()) {
            dumpAST(child, indent + " \t");
        }
    }

    /**
     * 整型变量声明语句，如：
     * int a;
     * int b = 2*3;
     *
     * @return
     * @throws Exception
     */
    private SimpleASTNode intDeclare(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        Token token = tokens.peek(); // 预读
        if (token != null && token.getType() == TokenType.INTTK) { // 匹配Int
            token = tokens.read(); // 消耗掉int
            if (tokens.peek().getType() == TokenType.IDENFR) { // 匹配标识符
                token = tokens.read(); // 消耗掉标识符
                // 创建当前节点，并把变量名记到AST节点的文本值中，这里新建一个变量子节点也是可以的
                node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
                token = tokens.peek(); // 预读
                if (token != null && token.getType() == TokenType.ASSIGN) {
                    tokens.read(); // 消耗掉等号
                    SimpleASTNode child = additive(tokens); // 匹配一个表达式
                    if (child == null) {
                        throw new Exception("invalide variable initialization, expecting an expression");
                    } else {
                        node.addChild(child);
                    }
                }
            } else {
                throw new Exception("variable name expected");
            }

            if (node != null) {
                token = tokens.peek();
                if (token != null && token.getType() == TokenType.SEMICN) {
                    tokens.read();
                } else {
                    throw new Exception("invalid statement, expecting semicolon");
                }
            }
        }
        return node;
    }
}
