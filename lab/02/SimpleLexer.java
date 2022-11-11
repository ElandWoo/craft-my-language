import java.io.CharArrayReader;
import java.io.IOException;
import java.util.*;

/**
 * Simple Lexer
 */
public class SimpleLexer {
    private StringBuffer tokenText = null;
    private List<Token> tokens = null; // store tokens after lex parsing;
    private SimpleToken token = null; // token in parsing

    public static void main(String[] args) throws IOException {
        SimpleLexer lexer = new SimpleLexer();
        Scanner sc = new Scanner(System.in);
        String script;
        while (sc.hasNextLine()) {
            script = sc.nextLine();
            if (script.isEmpty()) {
                break;
            }
            System.out.println("parse :" + script);
            SimpleTokenReader tokenReader = lexer.tokenize(script);
            dump(tokenReader);
        }
        sc.close();
    }

    /**
     * all states in parsing
     */
    private enum DfaState {
        Initial,

        IntLiteral,

        Id,
        If, Id_if1, Id_if2,
        Else, Id_else1, Id_else2, Id_else3, Id_else4,
        Int, Id_int1, Id_int2, Id_int3,
        Var, Id_var1, Id_var2, Id_var3,

        GT, GE, LT, LE, EQ,

        Plus, Minus, Star, Slash,

        LeftParen, RightParen, SemiColon
    }

    private boolean isDigit(char ch) {
        return ch <= '9' && ch >= '0';
    }

    private boolean isAlpha(char ch) {
        if (ch <= 'z' && ch >= 'a') {
            return true;
        }
        if (ch <= 'Z' && ch >= 'A') {
            return true;
        }
        return false;
    }

    private boolean isBlank(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }

    /**
     * push dfa into begining
     * after init, state will changed directly
     * 
     * @param ch
     * @return
     */
    private DfaState initToken(char ch) {
        // update tokens : once we get a complete token, push it into tokens.
        if (tokenText.length() > 0) {
            token.text = tokenText.toString();
            tokens.add(token);

            tokenText = new StringBuffer();
            token = new SimpleToken();
        }

        DfaState newState = DfaState.Initial;
        if (isDigit(ch)) {
            newState = DfaState.IntLiteral;
            token.type = TokenType.IntLiteral;
            tokenText.append(ch);
        }
        if (isAlpha(ch)) {
            if (ch == 'i') {
                newState = DfaState.Id_int1;
            }
            if (ch == 'v') {
                newState = DfaState.Id_var1;
            }
            if (ch != 'v' && ch != 'i') {
                newState = DfaState.Id;
            }
            token.type = TokenType.Identifier;
            tokenText.append(ch);
        }
        // compare operators
        if (ch == '>') {
            newState = DfaState.GT;
            token.type = TokenType.GT;
            tokenText.append(ch);
        }
        if (ch == '<') {
            newState = DfaState.LT;
            token.type = TokenType.LT;
            tokenText.append(ch);
        }
        if (ch == '=') {
            newState = DfaState.EQ;
            token.type = TokenType.EQ;
            tokenText.append(ch);
        }
        // arithmetic operator
        if (ch == '+') {
            newState = DfaState.Plus;
            token.type = TokenType.Plus;
            tokenText.append(ch);
        }
        if (ch == '-') {
            newState = DfaState.Minus;
            token.type = TokenType.Minus;
            tokenText.append(ch);
        }
        if (ch == '*') {
            newState = DfaState.Star;
            token.type = TokenType.Star;
            tokenText.append(ch);
        }
        if (ch == '/') {
            newState = DfaState.Slash;
            token.type = TokenType.Slash;
            tokenText.append(ch);
        }
        // paren
        if (ch == '(') {
            newState = DfaState.LeftParen;
            token.type = TokenType.LeftParen;
            tokenText.append(ch);
        }
        if (ch == ';') {
            newState = DfaState.SemiColon;
            token.type = TokenType.SemiColon;
            tokenText.append(ch);
        }
        if (ch == ')') {
            newState = DfaState.RightParen;
            token.type = TokenType.RightParen;
            tokenText.append(ch);
        }
        // newState = DfaState.Initial; // skip unknown partterns
        return newState;
    }

    /**
     * a dfa to parse string to form token
     * jump from a state to another state
     * 
     * @param code
     * @return
     * @throws IOException
     */
    public SimpleTokenReader tokenize(String code) {
        tokens = new ArrayList<Token>();
        CharArrayReader reader = new CharArrayReader(code.toCharArray());
        tokenText = new StringBuffer();
        token = new SimpleToken();
        int ich = 0;
        char ch = 0;
        DfaState state = DfaState.Initial;
        try {
            while ((ich = reader.read()) != -1) {
                ch = (char) ich;
                if (state == DfaState.Initial) {
                    state = initToken(ch); // ensure the next state
                    continue;
                }

                if (state == DfaState.IntLiteral) {
                    if (isDigit(ch)) {
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.Id) {
                    if (isAlpha(ch) || isDigit(ch) || ch == '_') {
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.GT) {
                    if (ch == '=') {
                        token.type = TokenType.GE;
                        state = DfaState.GE;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.LT) {
                    if (ch == '=') {
                        token.type = TokenType.LE;
                        state = DfaState.LE;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.EQ) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.GE) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.LE) {
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.Plus) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Minus) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Star) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Slash) {
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.LeftParen) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.RightParen) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.SemiColon) {
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.Id_int1) {
                    if (ch == 'n') {
                        state = DfaState.Id_int2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.Id;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_int2) {
                    if (ch == 't') {
                        state = DfaState.Id_int3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.Id;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_int3) {
                    if (isBlank(ch)) {
                        token.type = TokenType.Int;
                        System.out.println("ok?");
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.Id;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_var1) {
                    if (ch == 'a') {
                        state = DfaState.Id_var2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.Id;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_var2) {
                    if (ch == 'r') {
                        state = DfaState.Id_var3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.Id;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_var3) {
                    if (isBlank(ch)) {
                        token.type = TokenType.Var;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.Id;
                    tokenText.append(ch);
                    continue;
                }
            }
            // push the last token
            if (tokenText.length() > 0) {
                initToken(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SimpleTokenReader(tokens);
    }

    /**
     * print all the tokens
     * 
     * @param tokenReader
     */
    public static void dump(SimpleTokenReader tokenReader) {
        System.out.println("text\ttype");
        Token token = null;
        while ((token = tokenReader.read()) != null) {
            System.out.println(token.getText() + "\t" + token.getType());
        }
    }

    /**
     * a simple token stream
     */
    private class SimpleTokenReader implements TokenReader {
        List<Token> tokens = null;
        int pos = 0;

        public SimpleTokenReader(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public Token read() {
            if (pos < tokens.size()) {
                return tokens.get(pos++);
            }
            return null;
        }

        @Override
        public Token peek() {
            if (pos < tokens.size()) {
                return tokens.get(pos);
            }
            return null;
        }

        @Override
        public void unread() {
            if (pos > 0) {
                pos--;
            }
        }

        @Override
        public int getPosition() {
            return pos;
        }

        @Override
        public void setPosition(int position) {
            if (position >= 0 && position < tokens.size()) {
                pos = position;
            }
        }

    }

    private final class SimpleToken implements Token {
        private TokenType type = null;
        private String text = null;

        @Override
        public TokenType getType() {
            return type;
        }

        @Override
        public String getText() {
            return text;
        }
    }

}