"strict"

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
// given a token array after Lexical analysis
let tokenArray = [
    { kind: TokenKind.Keyword, text: 'function' },
    { kind: TokenKind.Identifier, text: 'sayHello' },
    { kind: TokenKind.Seperator, text: '(' },
    { kind: TokenKind.Seperator, text: ')' },
    { kind: TokenKind.Seperator, text: '{' },
    { kind: TokenKind.Identifier, text: 'println' },
    { kind: TokenKind.Seperator, text: '(' },
    { kind: TokenKind.StringLiteral, text: 'Hello World!' },
    { kind: TokenKind.Seperator, text: ')' },
    { kind: TokenKind.Seperator, text: ';' },
    { kind: TokenKind.Seperator, text: '}' },
    { kind: TokenKind.Identifier, text: 'sayHello' },
    { kind: TokenKind.Seperator, text: '(' },
    { kind: TokenKind.Seperator, text: ')' },
    { kind: TokenKind.Seperator, text: ';' },
    { kind: TokenKind.EOF, text: '' }
];

// Lexical analysisor
class Tokenizer {
    //token is a array
    constructor(tokens) {
        this.pos = 0;
        this.tokens = tokens;
    }

    next() {
        if (this.pos <= this.tokens.length) {
            return this.tokens[this.pos++];
        }
        // if till the end of token , always return EOF
        return this.tokens[this.pos];
    }

    position() {
        return this.pos;
    }
    // if parse failed, we need tarceback the start pos.
    traceBack(newPos) {
        this.pos = newPos;
    }
}

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
// FuntionDecl node
class FuntionDecl extends Statement {
    constructor(name, body) {
        super();
        this.name = name;
        this.body = body;
    }
    dump(prefix) {
        console.log(prefix + "FuntionDecl" + this.name);
        this.body.dump(prefix + "\t");
    }
}

// Functionbody
class FunctionBody extends AstNode {
    constructor(stmts) {
        super();
        this.stmts = stmts;
    }
    static isFunctionbodyName(node) {
        if (!node) {
            return false;
        }
        if (Object.getPrototypeOf(node) == FunctionBody.prototype) {
            return true;
        }
        return false;
    }
    
    dump(prefix) {
        console.log(prefix + "FuntionBody");
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
        console.log(prefix + "FunctionCall" + this.name + (this.definition != null ? ", resolved" : ", not resolved"));
        this.parameters.forEach(x => console.log(prefix + "\t" + "Parameter: " + x));
    }
}


class Parse {
    constructor(tokenizer) {
        this.tokenizer = tokenizer;
    }
    /**
     * parse Prog
     * syntax rule : one program has 0 or more funtiondecine and functioncall
     * prog = (functionDecl | functionCall)* ; // EBNF:扩展巴斯范式
     */
    parseProg() {
        let stmts = [];
        let stmt = null;
        while (true) {
            // just try statement is decl or call
            // if all not, then break the loop
            stmt = this.parseFuntionDecl();
            if (Statement.isStatementNode(stmt)) {
                stmts.push(stmt);
                continue;
            }
            stmt = this.parseFuntionCall();
            if (Statement.isFunctionCallNode(node)) {
                stmts.push(stmt);
                continue;
            }
            if (stmt == null) {
                break;
            }
        }
        return newProg(stmts);
    }
    
    /**
     * parseFunction declaration
     * syntax rule : functionDecl: "function" Identifier "(" ")"  funcitonBody;
     */
    parseFuntionDecl() {
        let oldPos = this.tokenizer.position();
        let t = this.tokenizer.next;
        if (t.kind == TokenKind.Keyword && t.text == "function") {
            t = this.tokenizer.next();
            if (t.kind == TokenKind.Identifier.next()) {
                // ( & )
                let t1 = this.tokenizer.next();
                if (t1.text == "(") {
                    let t2 = this.tokenizer.next();
                    if (t2.text == ")") {
                        let funcitonBody = this.parseFuncitonBody();
                        if (FunctionBody.isFunctionbodyName(funcitonBody)) {
                            // parse success
                            return new FuntionDecl(t.text, funcitonBody);
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
     * rule : functionBody : '{' funtionCall* '}' ;
     */
    parseFuncitonBody() {
        let oldPos = this.tokenizer.position();
        let stmts;
    }
    parseFuntionCall() {
    }
}



// 3. 语义分析

// 4. 解释器

// 5.main function
function complieAndRun() {
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
    // 3. 



}

// run!
complieAndRun();

// sayHello();


// //Parse :convert token to AST
// prog = parseProg();

// // 产生式
// prog -> statement prog
// prog ->
//     statement -> functionDecl
// statement -> functionCall
// // 一个Token数组，代表了下面这段程序做完词法分析后的结果：      33 /*
// 34 //一个函数的声明，这个函数很简单，只打印"Hello World!"
// 35 function sayHello() {
//     36     println("Hello World!");
//     37
// }
// 38 //调用刚才声明的函数
// 39 sayHello();
// 40 * /
