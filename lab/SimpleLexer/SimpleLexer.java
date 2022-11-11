
import java.io.*;
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
//        Scanner sc = new Scanner(System.in);
//        String script;
        String outDir = "output.txt";
        File output = new File(outDir);
        if (!output.exists())
            output.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(outDir));

        String inDir = "testfile.txt";
        File file = new File(inDir);
        if (!file.exists())
            file.createNewFile();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line=br.readLine())!=null) {
            SimpleTokenReader tokenReader = lexer.tokenize(line);
            dump(tokenReader, bw);
        }
        br.close();
        bw.close();
//            script = sc.nextLine();
//            if (script.isEmpty()) {
//                break;
//            }
//            SimpleTokenReader tokenReader = lexer.tokenize(script);
//            dump(tokenReader);
//        }
//        sc.close();
    }

    /**
     * all states in parsing
     */
    private enum DfaState {
        Initial,
        INTCON,
        CHARCON,
        STRCON,
        IDENFR,

        Id_else1, Id_else2, Id_else3, Id_else4,
        Id_var1, Id_var2, Id_var3,
        Id_charOrConst1, Id_char2, Id_char3, Id_char4,
        Id_const2, Id_const3, Id_const4, Id_const5,
        Id_intOrIf1,Id_int2, Id_int3, Id_if2,
        Id_void1, Id_void2, Id_void3, Id_void4,
        Id_main1, Id_main2, Id_main3, Id_main4,
        Id_do1, Id_do2,
        Id_while1, Id_while2, Id_while3, Id_while4, Id_while5,
        Id_for1, Id_for2, Id_for3,
        Id_scanf1, Id_scanf2, Id_scanf3, Id_scanf4, Id_scanf5,
        Id_printf1, Id_printf2, Id_printf3, Id_printf4, Id_printf5, Id_printf6,
        Id_return1, Id_return2, Id_return3, Id_return4, Id_return5, Id_return6,
        // logi : ok
        GRE, GEQ, LSS, LEQ, EQL, NEQ,
        // arth :ok
        PLUS, MINU, MULT, DIV,ASSIGN,

        // symbol : ok
        SEMICN,COMMA,TAN,
        // Paren : ok
        LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE,
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
            newState = DfaState.INTCON;
            token.type = TokenType.INTCON;
            tokenText.append(ch);
        }
        if (ch == '"') {
            newState = DfaState.STRCON;
            token.type = TokenType.STRCON;
        }
        if (ch == '\'') {
            newState = DfaState.CHARCON;
            token.type = TokenType.CHARCON;
        }
        if (ch == '_') {
            newState = DfaState.IDENFR;
            token.type = TokenType.IDENFR;
            tokenText.append(ch);
        }
        if (isAlpha(ch)) {
            newState = DfaState.IDENFR;
            if (ch == 'c') {
                newState = DfaState.Id_charOrConst1;
            }
            if (ch == 'i') {
                newState = DfaState.Id_intOrIf1;
            }
            if (ch == 'v') {
                newState = DfaState.Id_void1;
            }
            if (ch == 'm') {
                newState = DfaState.Id_main1;
            }
            if (ch == 'e') {
                newState = DfaState.Id_else1;
            }
            if (ch == 'd') {
                newState = DfaState.Id_do1;
            }
            if (ch == 'w') {
                newState = DfaState.Id_while1;
            }
            if (ch == 'f') {
                newState = DfaState.Id_for1;
            }
            if (ch == 's') {
                newState = DfaState.Id_scanf1;
            }
            if (ch == 'p') {
                newState = DfaState.Id_printf1;
            }
            if (ch == 'r') {
                newState = DfaState.Id_return1;
            }
            token.type = TokenType.IDENFR;
            tokenText.append(ch);
        }
        // compare operators
        if (ch == '>') {
            newState = DfaState.GRE;
            token.type = TokenType.GRE;
            tokenText.append(ch);
        }
        if (ch == '<') {
            newState = DfaState.LSS;
            token.type = TokenType.LSS;
            tokenText.append(ch);
        }
        if (ch == '=') {
            newState = DfaState.ASSIGN;
            token.type = TokenType.ASSIGN;
            tokenText.append(ch);
        }
        if (ch == '!') {
            newState = DfaState.TAN;
            token.type = TokenType.TAN;
            tokenText.append(ch);
        }
        // arithmetic operator
        if (ch == '+') {
            newState = DfaState.PLUS;
            token.type = TokenType.PLUS;
            tokenText.append(ch);
        }
        if (ch == '-') {
            newState = DfaState.MINU;
            token.type = TokenType.MINU;
            tokenText.append(ch);
        }
        if (ch == '*') {
            newState = DfaState.MULT;
            token.type = TokenType.MULT;
            tokenText.append(ch);
        }
        if (ch == '/') {
            newState = DfaState.DIV;
            token.type = TokenType.DIV;
            tokenText.append(ch);
        }
        // paren and symbol
        if (ch == '(') {
            newState = DfaState.LPARENT;
            token.type = TokenType.LPARENT;
            tokenText.append(ch);
        }
        if (ch == ')') {
            newState = DfaState.RPARENT;
            token.type = TokenType.RPARENT;
            tokenText.append(ch);
        }
        if (ch == '[') {
            newState = DfaState.LBRACK;
            token.type = TokenType.LBRACK;
            tokenText.append(ch);
        }
        if (ch == ']') {
            newState = DfaState.RBRACK;
            token.type = TokenType.RBRACK;
            tokenText.append(ch);
        }
        if (ch == '{') {
            newState = DfaState.LBRACE;
            token.type = TokenType.LBRACE;
            tokenText.append(ch);
        }
        if (ch == '}') {
            newState = DfaState.RBRACE;
            token.type = TokenType.RBRACE;
            tokenText.append(ch);
        }
        if (ch == ';') {
            newState = DfaState.SEMICN;
            token.type = TokenType.SEMICN;
            tokenText.append(ch);
        }
        if (ch == ',') {
            newState = DfaState.COMMA;
            token.type = TokenType.COMMA;
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

                if (state == DfaState.INTCON) {
                    if (isDigit(ch)) {
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.STRCON) {
                    if (ch != '"') {
                        tokenText.append(ch);
                        continue;
                    }
                    token.type = TokenType.STRCON;
                    ich = reader.read();
                    ch = (char) ich;
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.CHARCON) {
                    if (ch != '\'') {
                        tokenText.append(ch);
                        continue;
                    }
                    token.type = TokenType.CHARCON;
                    ich = reader.read();
                    ch = (char) ich;
                    state = initToken(ch);
                    continue;
                }


                if (state == DfaState.SEMICN) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.COMMA) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.TAN) {
                    if (ch == '=') {
                        token.type = TokenType.NEQ;
                        state = DfaState.NEQ;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.GRE) {
                    if (ch == '=') {
                        token.type = TokenType.GEQ;
                        state = DfaState.GEQ;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.LSS) {
                    if (ch == '=') {
                        token.type = TokenType.LEQ;
                        state = DfaState.LEQ;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.ASSIGN) {
                    if (ch == '=') {
                        token.type = TokenType.EQL;
                        state = DfaState.EQL;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.LEQ) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.GEQ) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.NEQ) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.EQL) {
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.PLUS) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.MINU) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.MULT) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.DIV) {
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.LPARENT) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.RPARENT) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.LBRACK) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.RBRACK) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.LBRACE) {
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.RBRACE) {
                    state = initToken(ch);
                    continue;
                }

                if (state == DfaState.Id_charOrConst1) {
                    if (ch == 'h') {
                        state = DfaState.Id_char2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (ch == 'o') {
                        state = DfaState.Id_const2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_char2) {
                    if (ch == 'a') {
                        state = DfaState.Id_char3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_char3) {
                    if (ch == 'r') {
                        state = DfaState.Id_char4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_char4) {
                    if (isBlank(ch)) {
                        token.type = TokenType.CHARTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }
                if (state == DfaState.Id_const2) {
                    if (ch == 'n') {
                        state = DfaState.Id_const3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_const3) {
                    if (ch == 's') {
                        state = DfaState.Id_const4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_const4) {
                    if (ch == 't') {
                        state = DfaState.Id_const5;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_const5) {
                    if (ch == ' ') {
                        token.type = TokenType.CONSTTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_intOrIf1) {
                    if (ch == 'n') {
                        state = DfaState.Id_int2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (ch == 'f') {
                        state = DfaState.Id_if2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_if2) {
                    if (isBlank(ch) || ch == '(') {
                        token.type = TokenType.IFTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }
                if (state == DfaState.Id_int2) {
                    if (ch == 't') {
                        state = DfaState.Id_int3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_int3) {
                    if (isBlank(ch) || ch == '(') {
                        token.type = TokenType.INTTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
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
                        state = DfaState.IDENFR;
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
                        state = DfaState.IDENFR;
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
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_void1) {
                    if (ch == 'o') {
                        state = DfaState.Id_void2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_void2) {
                    if (ch == 'i') {
                        state = DfaState.Id_void3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_void3) {
                    if (ch == 'd') {
                        state = DfaState.Id_void4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_void4) {
                    if (isBlank(ch)) {
                        token.type = TokenType.VOIDTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_main1) {
                    if (ch == 'a') {
                        state = DfaState.Id_main2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_main2) {
                    if (ch == 'i') {
                        state = DfaState.Id_main3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_main3) {
                    if (ch == 'n') {
                        state = DfaState.Id_main4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_main4) {
                    if (ch == '(') {
                        token.type = TokenType.MAINTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_else1) {
                    if (ch == 'l') {
                        state = DfaState.Id_else2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_else2) {
                    if (ch == 's') {
                        state = DfaState.Id_else3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_else3) {
                    if (ch == 'e') {
                        state = DfaState.Id_else4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_else4) {
                    if (ch == ' ' || ch == '{') {
                        token.type = TokenType.ELSETK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_do1) {
                    if (ch == 'o') {
                        state = DfaState.Id_do2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_do2) {
                    if (isBlank(ch) || ch == '{') {
                        token.type = TokenType.DOTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_while1) {
                    if (ch == 'h') {
                        state = DfaState.Id_while2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_while2) {
                    if (ch == 'i') {
                        state = DfaState.Id_while3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_while3) {
                    if (ch == 'l') {
                        state = DfaState.Id_while4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_while4) {
                    if (ch == 'e') {
                        state = DfaState.Id_while5;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_while5) {
                    if (isBlank(ch) || ch == '(') {
                        token.type = TokenType.WHILETK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_scanf1) {
                    if (ch == 'c') {
                        state = DfaState.Id_scanf2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_scanf2) {
                    if (ch == 'a') {
                        state = DfaState.Id_scanf3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_scanf3) {
                    if (ch == 'n') {
                        state = DfaState.Id_scanf4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_scanf4) {
                    if (ch == 'f') {
                        state = DfaState.Id_scanf5;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_scanf5) {
                    if (ch == '(') {
                        token.type = TokenType.SCANFTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_printf1) {
                    if (ch == 'r') {
                        state = DfaState.Id_printf2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_printf2) {
                    if (ch == 'i') {
                        state = DfaState.Id_printf3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_printf3) {
                    if (ch == 'n') {
                        state = DfaState.Id_printf4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_printf4) {
                    if (ch == 't') {
                        state = DfaState.Id_printf5;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_printf5) {
                    if (ch == 'f') {
                        state = DfaState.Id_printf6;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_printf6) {
                    if (ch == '(') {
                        token.type = TokenType.PRINTFTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }


                if (state == DfaState.Id_return1) {
                    if (ch == 'e') {
                        state = DfaState.Id_return2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_return2) {
                    if (ch == 't') {
                        state = DfaState.Id_return3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_return3) {
                    if (ch == 'u') {
                        state = DfaState.Id_return4;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_return4) {
                    if (ch == 'r') {
                        state = DfaState.Id_return5;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_return5) {
                    if (ch == 'n') {
                        state = DfaState.Id_return6;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_return6) {
                    if (isBlank(ch) || ch == ';') {
                        token.type = TokenType.RETURNTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }

                if (state == DfaState.Id_for1) {
                    if (ch == 'o') {
                        state = DfaState.Id_for2;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_for2) {
                    if (ch == 'r') {
                        state = DfaState.Id_for3;
                        tokenText.append(ch);
                        continue;
                    }
                    if (isDigit(ch) || isAlpha(ch) || ch == '_') {
                        state = DfaState.IDENFR;
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
                    continue;
                }
                if (state == DfaState.Id_for3) {
                    if (isBlank(ch) || ch == '(') {
                        token.type = TokenType.FORTK;
                        state = initToken(ch);
                        continue;
                    }
                    state = DfaState.IDENFR;
                    tokenText.append(ch);
                    continue;
                }


                if (state == DfaState.IDENFR) {
                    if (isAlpha(ch) || isDigit(ch) || ch == '_') {
                        tokenText.append(ch);
                        continue;
                    }
                    state = initToken(ch);
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
    public static void dump(SimpleTokenReader tokenReader, BufferedWriter bw) throws IOException {
        Token token = null;
        while ((token = tokenReader.read()) != null) {
            bw.write(token.getType() + " " + token.getText() + "\n");
            System.out.println(token.getType() + "\t" + token.getText());
        }
        bw.flush();
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
