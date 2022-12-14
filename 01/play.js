"use strict"

/*
function sayHello() {
    println("Hello World!");
}
//调用刚才声明的函数
sayHello();
*/



// 1. Lexical analysis:
// tokenKind
var TokenKind;
(function (TokenKind) {
    TokenKind[TokenKind["Keyword"] = 0] = "Keyword";
    TokenKind[TokenKind["Identifier"] = 1] = "Identifier";
    TokenKind[TokenKind["StringLiteral"] = 2] = "StringLiteral";
    TokenKind[TokenKind["Seperator"] = 3] = "Seperator";
    TokenKind[TokenKind["Operator"] = 4] = "Operator";
    TokenKind[TokenKind["EOF"] = 5] = "EOF";
})(TokenKind || (TokenKind = {}));
;

// // given a token array after Lexical analysis
// let tokenArray = [
//     { kind: TokenKind.Keyword, text: 'function' },
//     { kind: TokenKind.Identifier, text: 'sayHello' },
//     { kind: TokenKind.Seperator, text: '(' },
//     { kind: TokenKind.Seperator, text: ')' },
//     { kind: TokenKind.Seperator, text: '{' },
//     { kind: TokenKind.Identifier, text: 'println' },
//     { kind: TokenKind.Seperator, text: '(' },
//     { kind: TokenKind.StringLiteral, text: 'Hello World!' },
//     { kind: TokenKind.Seperator, text: ')' },
//     { kind: TokenKind.Seperator, text: ';' },
//     { kind: TokenKind.Seperator, text: '}' },
//     { kind: TokenKind.Identifier, text: 'sayHello' },
//     { kind: TokenKind.Seperator, text: '(' },
//     { kind: TokenKind.Seperator, text: ')' },
//     { kind: TokenKind.Seperator, text: ';' },
//     { kind: TokenKind.EOF, text: '' }
// ];


/**
 * a char team with 3 operations
 */
class CharStream {
    constructor(data) {
        this.pos = 0;
        this.line = 1;
        this.col = 0;
        this.data = data;
    }
    // peek but no moving the point.
    peek() {
        return this.data.chatAt(this.pos);
    }
    // this order will move point next spot.
    next() {
        let ch = this.data.charAt(this.pos);
        this.pos++;
        // when encount a '\n', update line and col.
        if (ch == '\n') {
            this.line++;
            this.col = 0;
        }
        this.col++;
        return ch;
    }
    // tell if we arrive the end.
    eof() {
        return this.peek() == '';
    }
}

/**
 * Lexical analysis
 */
class Tokenizer {
    constructor(stream) {
        this.nextToken = { kind: TokenKind.EOF, text: "" };
        this.stream = stream;
    }
    next() {
        // parse a token first
        if (this.nextToken.kind == TokenKind.EOF && !this.stream.eof()) {
            this.nextToken = this.getAToken();
        }
        let lastToken = this.nextToken;
        this.nextToken = this.getAToken();
        return lastToken;
    }
    peek() {
        if (this.nextToken.kind == TokenKind.EOF && !this.stream.eof()) {
            this.nextToken = this.getAToken();
        }
        return this.nextToken;
    }
    getAToken() {
        this.skipWhiteSpaces();
        if (this.stream.eof) {
            return { kind: TokenKind.EOF, text: "" };
        }
        let ch = this.stream.peek();
        if (this.isLetter(ch) || this.isDigit(ch)) {
            return this.parseIdentifer();
        }
        if (ch == '"') {
            return this.parseStringLiteral();
        }
        if (ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ';' || ch == ',') {
            this.stream.next();
            return { kind: TokenKind.Seperator, text: ch };
        }
        if (ch == '/') {
            this.stream.next();
            let ch1 = this.stream.peek();
            if (ch1 == '*') {
                this.skipMultipleLineComments();
                return this.getAToken();
            }
            if (ch == '/') {
                this.skipSingleLineComment();
                return this.getAToken();
            }
            if (ch == '=') {
                this.stream.next();
                return { kind: TokenKind.Operator, text: '/=' };
            }
            return { kind: TokenKind.Operator, text: '/' };
        }
        if (ch == '*') {
            this.stream.next();
            let ch1 = this.stream.peek();
            if (ch == '=') {
                this.stream.next();
                return { kind: TokenKind.Operator, text: '*=' };
            }
            return { kind: TokenKind.Operator, text: '*' };
        }
        if (ch == '+') {
            this.stream.next();
            let ch1 = this.stream.peek();
            if (ch1 == '+') {
                this.stream.next();
                return { kind: TokenKind.Operator, text: '++' };
            }
            if (ch == '=') {
                this.stream.next();
                return { kind: TokenKind.Operator, text: '+=' };
            }
            return { kind: TokenKind.Operator, text: '+' };
        }
        if (ch == '-') {
            this.stream.next();
            let ch1 = this.stream.peek();
            if (ch1 == '-') {
                this.stream.next();
                return { kind: TokenKind.Operator, text: '--' };
            }
            if (ch == '=') {
                this.stream.next();
                return { kind: TokenKind.Operator, text: '-=' };
            }
            return { kind: TokenKind.Operator, text: '-' };
        }
        console.log("Unrecognized pattern meeting ': " + ch + "', at" + this.stream.line + " col: " + this.stream.col);
        this.stream.next();
        return this.getAToken();
    }
    /**
     * skip the space
     */
    skipWhiteSpaces() {
        while (this.isWhiteSpace(this.stream.peek())) {
            this.stream.next();
        }
    }
    /** 
    * skip multiple line comment
    */
    skipMultipleLineComment() {
        // skip the '*' after '/'
        this.stream.next();
        if (!this.stream.eof()) {
            let ch1 = this.stream.next();
            while (!this.stream.eof()) {
                let ch2 = this.stream.next();
                if (ch1 == '*' && ch2 == '/') {
                    return;
                }
                ch1 = ch2;
            }
        }
        console.log("Failed to find matching */ for multiple line comments at ': " + this.stream.line + "col: " + this.stream.col);
    }
    /**
    * skip single line comment
    */
    skipSingleLineComment() {
        // skip the second '/'
        this.stream.next();
        while (this.stream.peek() != '\n' && !this.stream.eof()) {
            this.stream.next();
        }
    }
    /**
    * parse String-Literal
    */
    parseStringLiteral() {
        let token = { kind: TokenKind.StringLiteral, text: "" };
        this.stream.next();
        while (!this.stream.eof() && this.stream.peek() != '"') {
            token.next += this.stream.next();
        }
        if (this.stream.peek() == '"') {
            this.stream.next();
        }
        if (this.stream.peek() != '"') {
            console.log("Expecting an \" at line: " + this.stream.line + " col: " + this.stream.col);
        }
        return token;

    }
    /**
     * parse Indentifer
     */
    parseIdentifer() {
        let token = { kind: TokenKind.Identifier, text: "" };
        token.text += this.stream.next();
        while (!this.stream.eof() && this.isLetterDigitOrUnderScore(this.stream.peek())) {
            token.text += this.stream.next();
        }
        if (token.text == 'function') {
            token.kind = TokenKind.Keyword;
        }
        return token;
    }

    isLetter(ch) {
        if (ch >= 'a' && ch <= 'z') {
            return true;
        }
        if (ch >= 'A' && ch <= 'Z') {
            return true;
        }
        return false;
    }

    isDigit(ch) {
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        return false;
    }

    isWhiteSpace(ch) {
        if (ch == ' ' || ch == '\n' || ch == '\t') {
            return true;
        }
        return false;
    }
    isLetterDigitOrUnderScore(ch) {
        return this.isDigit(ch) || this.isLetter(ch) || (ch == '_');
    }
}
//todo: update syntax analysis, apply ll algorithm

// 2. syntax analysis
// data structure
// ASTNode: basic class
class AstNode {
}

// statement
class Statement extends AstNode {
    static isStatementNode(node) {
        if (!node) {
            return false;
        }
        return true;
    }
}

// root Node of AST 
class Prog extends AstNode {
    constructor(stmts) {
        // ES6 class 可以通过extends关键字实现继承，而同时子类必须在 constructor 方法中调用super方法，否则新建实例时会报错。
        super();
        this.stmts = stmts;
    }
    // do not understand:
    dump(prefix) {
        console.log(prefix + "Prog");
        this.stmts.forEach(x => x.dump(prefix + "\t"));
    }
}

// FunctionDecl node
class FunctionDecl extends Statement {
    constructor(name, body) {
        super();
        this.name = name;
        this.body = body;
    }
    dump(prefix) {
        console.log(prefix + "FunctionDecl " + this.name);
        this.body.dump(prefix + "\t");
    }
}

// FunctionBody
class FunctionBody extends AstNode {
    constructor(stmts) {
        super();
        this.stmts = stmts;
    }
    static isFunctionBodyNode(node) {
        if (!node) {
            return false;
        }
        if (Object.getPrototypeOf(node) == FunctionBody.prototype) {
            return true;
        }
        return false;
    }

    dump(prefix) {
        console.log(prefix + "FunctionBody");
        this.stmts.forEach(x => x.dump(prefix + "\t"));
    }
}

// FunctionCall
class FunctionCall extends Statement {
    constructor(name, parameters) {
        super();
        this.definition = null; // point to functionDecl
        this.name = name;
        this.parameters = parameters;
    }
    static isFunctionCallNode(node) {
        if (!node) {
            return false;
        }
        if (Object.getPrototypeOf(node) == FunctionCall.prototype) {
            return true;
        }
        return false;
    }
    dump(prefix) {
        console.log(prefix + "FunctionCall " + this.name + (this.definition != null ? ", resolved" : ", not resolved"));
        this.parameters.forEach(x => console.log(prefix + "\t" + "Parameter: " + x));
    }
}


class Parser {
    constructor(tokenizer) {
        this.tokenizer = tokenizer;
    }
    /**
     * parse Prog
     * syntax rule : one program has 0 or more functionDecl and functionCall
     * prog = (functionDecl | functionCall)* ; // EBNF
     */
    parseProg() {
        let stmts = [];
        let stmt = null;
        while (true) {
            // just try statement is decl or call
            // if all not, then break the loop
            stmt = this.parseFunctionDecl();
            if (Statement.isStatementNode(stmt)) {
                stmts.push(stmt);
                continue;
            }

            stmt = this.parseFunctionCall();
            if (Statement.isStatementNode(stmt)) { // 02 question is here
                stmts.push(stmt);
                continue;
            }

            if (stmt == null) {
                break;
            }
        }
        return new Prog(stmts);
    }

    /**
     * parseFunction declaration
     * syntax rule : functionDecl: "function" Identifier "(" ")"  functionBody;
     */
    parseFunctionDecl() {
        let oldPos = this.tokenizer.position();
        let t = this.tokenizer.next();
        if (t.kind == TokenKind.Keyword && t.text == "function") {
            t = this.tokenizer.next();
            if (t.kind == TokenKind.Identifier) {
                // ( & )
                let t1 = this.tokenizer.next();
                if (t1.text == "(") {
                    let t2 = this.tokenizer.next();
                    if (t2.text == ")") {
                        let functionBody = this.parseFunctionBody();
                        if (FunctionBody.isFunctionBodyNode(functionBody)) {
                            // parse success
                            return new FunctionDecl(t.text, functionBody);
                        }
                    } else {
                        console.log("Expecting ')' in FunctionDecl, while we got a " + t.text);
                        return;
                    }
                } else {
                    console.log("Expecting '(' in FunctionDecl, while we got a " + t.text);
                    return;
                }
            }
        }
        this.tokenizer.traceBack(oldPos);
        return null;
    }
    /**
     * parse function body
     * rule : functionBody : '{' functionCall* '}' ;
     */
    parseFunctionBody() {
        let oldPos = this.tokenizer.position();
        let stmts = [];
        let t = this.tokenizer.next();
        if (t.text == "{") {
            let functionCall = this.parseFunctionCall();
            while (FunctionCall.isFunctionCallNode(functionCall)) {
                stmts.push(functionCall);
                functionCall = this.parseFunctionCall();
            }
            t = this.tokenizer.next();
            if (t.text == "}") {
                return new FunctionBody(stmts);
            } else {
                console.log("Expecting '}' in FunctionBody, while we got a " + t.text);
                return;
            }
        } else {
            console.log("Expecting '{' in FunctionBody, while we got a " + t.text);
            return;
        }
        this.tokenizer.traceBack(oldPos);
        return null;
    }
    /**
     * parse function call 
     * rule : Identifier '(' parameterList? ')' ;
     * parameterList : StringLiteral (',' StringLiteral)* ;
     */
    parseFunctionCall() {
        let oldPos = this.tokenizer.position();
        let params = [];
        let t = this.tokenizer.next();
        if (t.kind == TokenKind.Identifier) {
            let t1 = this.tokenizer.next();
            if (t1.text == "(") {
                let t2 = this.tokenizer.next();
                while (t2.text != ")") {
                    if (t2.kind == TokenKind.StringLiteral) {
                        params.push(t2.text);
                    } else {
                        console.log("Expecting parameter in FunctionCall, while we got a " + t2.text);
                        return;
                    }
                    t2 = this.tokenizer.next();
                    if (t2.text != ")") {
                        if (t2.text == ",") {
                            t2 = this.tokenizer.next();
                        } else {
                            console.log("Expecting a comma in FunctionCall, while we got a " + t2.text);
                            return;
                        }
                    }
                }
                t2 = this.tokenizer.next();
                if (t2.text == ";") {
                    return new FunctionCall(t.text, params);
                } else {
                    console.log("Expecting a comma in FunctionCall, while we got a " + t2.text);
                    return;
                }
            }
        }
        this.tokenizer.traceBack(oldPos);
        return null;
    }
}

/**
 * traverse AST
 * basic class,
 */
class AstVisitor {
    visitProg(prog) {
        let retVal;
        for (let x of prog.stmts) {
            if (typeof x.body === 'object') {
                retVal = this.visitFunctionDecl(x);
            } else {
                retVal = this.visitFunctionCall(x);
            }
        }
        return retVal;
    }
    visitFunctionDecl(functionDecl) {
        return this.visitFunctionBody(functionDecl.body);
    }
    visitFunctionBody(functionBody) {
        let retVal;
        for (let x of functionBody.stmts) {
            retVal = this.visitFunctionCall(x);
        }
        return retVal;
    }
    // 函数调用内容为空， 仅仅返回undefined值
    visitFunctionCall(functionCall) {
        return undefined;
    }
}


/**
 * // 3. 语义分析
 */
class RefResolver extends AstVisitor {
    constructor() {
        super(...arguments);
        this.prog = null;
    }
    visitProg(prog) {
        this.prog = prog;
        for (let x of prog.stmts) {
            let functionCall = x;
            if (typeof functionCall.parameters === 'object') {
                this.resolveFunctionCall(prog, functionCall);
            }
            else {
                this.visitFunctionDecl(x);
            }
        }
    }
    visitFunctionBody(functionBody) {
        if (this.prog != null) {
            for (let x of functionBody.stmts) {
                return this.resolveFunctionCall(this.prog, x);
            }
        }
    }
    resolveFunctionCall(prog, functionCall) {
        let functionDecl = this.findFunctionDecl(prog, functionCall.name);
        if (functionDecl != null) {
            functionCall.definition = functionDecl;
        }
        else {
            if (functionCall.name != "println") { //系统内置函数不用报错
                console.log("Error: cannot find definition of function " + functionCall.name);
            }
        }
    }
    findFunctionDecl(prog, name) {
        for (let x of prog === null || prog === void 0 ? void 0 : prog.stmts) {
            let functionDecl = x;
            if (typeof functionDecl.body === 'object' && functionDecl.name == name) {
                return functionDecl;
            }
        }
        return null;
    }
}

/**
 * // 4. 解释器
 * traverse AST, produce function
 */
class Intepretor extends AstVisitor {
    visitProg(prog) {
        let retVal;
        for (let x of prog.stmts) {
            let functionCall = x;
            if (typeof functionCall.parameters === 'object') {
                retVal = this.runFunction(functionCall);
            }
        }
        ;
        return retVal;
    }

    visitFunctionBody(functionBody) {
        let retVal;
        for (let x of functionBody.stmts) {
            retVal = this.runFunction(x);
        }
        ;
    }

    runFunction(functionCall) {
        if (functionCall.name == "println") { // 内置函数
            if (functionCall.parameters.length > 0) {
                console.log(functionCall.parameters[0]);
            } else {
                console.log();
            }
            return 0;
        } else {
            if (functionCall.definition != null) {
                this.visitFunctionBody(functionCall.definition.body);
            }
        }
    }
}

// 5.main function
function compileAndRun() {
    // 1. lexical analysis:
    let tokenizer = new Tokenizer(tokenArray);
    console.log("\nToken which program used:");
    for (let token of tokenArray) {
        console.log(token);
    }
    // 2. syntax analysis
    let prog = new Parser(tokenizer).parseProg();
    console.log("\nAST after syntax analysis:")
    prog.dump("");
    // 3. 语义分析
    new RefResolver().visitProg(prog);
    console.log("\nAST after 语义分析， 自定义函数的调用已经消解：");
    prog.dump("");
    // 4. run the program
    console.log("\nRun currant program now.");
    let retVal = new Intepretor().visitProg(prog);
    console.log("retVal: " + retVal);
}

// run!
compileAndRun();