#include <string.h>
#include <string>
#include <stdio.h>
#include <fstream>
#define MAX 22
#define RES_MAX 13 //关键词数量
#define MAXBUF 255

char ch = ' ';
int Line_NO;

struct keywords
{
    char lexptr[MAXBUF];
    int token;
};

struct keywords symtable[MAX];
char in[MAX][10] = {"const", "int", "main", "void", "char", "if", "else", "do", "while", "for", "scanf", "printf", "return"};
char out[MAX][10] = {"CONSTTK", "INTTK", "MAINTK", "VOIDTK", "CHARTK", "IFTK", "ELSETK", "DOTK", "WHILETK", "FORTK", "SCANFTK", "PRINTFTK", "RETURNTK"};

//将token序列赋初值，关键词对应编码
void init()
{
    int j;
    for (j = 0; j < MAX; j++)
    {
        strcpy(symtable[j].lexptr, in[j]);
        symtable[j].token = j;
    }
}

//关键词判断
int Iskeyword(char *is_res)
{
    int i;
    for (i = 0; i < MAX; i++)
    {
        //若关键词匹配则跳出循环，此时 i < MAX，则对应输出token编码
        if ((strcmp(symtable[i].lexptr, is_res)) == 0)
            break;
    }
    if (i < MAX)
        return symtable[i].token;
    else
        return 0;
}

//字符判断
int IsLetter(char c)
{
    if (((c <= 'z') && (c >= 'a')) || ((c <= 'Z') && (c >= 'A')) || (ch == '_'))
        return 1;
    else
        return 0;
}

//数字判断
int IsDigit(char c)
{
    if (c >= '0' && c <= '9')
        return 1;
    else
        return 0;
}

//词法分析
void analyse(FILE *fpin, FILE *fpout)
{
    char arr[MAXBUF];
    int j = 0;
    while ((ch = fgetc(fpin)) != EOF)
    {
        if (ch == ' ' || ch == '\t')
        {
            continue;
        }
        else if (ch == '\n')
        {
            Line_NO++;
        }
        else if (IsLetter(ch))
        {
            //这里判断是否为标识符（标识符：字母开头，后续跟数字或字母都可以）
            while (IsLetter(ch) || IsDigit(ch))
            {
                arr[j] = ch;
                j++;
                ch = fgetc(fpin);
                //将标识符逐字符的读入arr数组中
            }
            fseek(fpin, -1L, SEEK_CUR);
            arr[j] = '\0';
            j = 0;

            if (Iskeyword(arr))
            {
                int a;
                a = Iskeyword(arr);
                fprintf(fpout, "%s %s\n", out[a], arr);
            }
            else if (!strcmp(arr, "const"))
            {
                fprintf(fpout, "CONSTTK %s\n", arr);
            }
            else
            {
                fprintf(fpout, "IDENFR %s\n", arr);
            }
        }
        //若为单双引号，则须考虑的是引号中间是字符还是字符串
        else if (int(ch) == 34 || int(ch) == 39)
        {
            int s = 0;
            if (int(ch) == 34)
            {
                //双引号则为 1
                s = 1;
            }

            ch = fgetc(fpin);
            char arr[MAXBUF];
            int i, j = 0;
            while (int(ch) != 34 && int(ch) != 39)
            {
                arr[j] = ch;
                j++;
                ch = fgetc(fpin);
            }
            //这里不用指针往前倒，因为多读的那个是引号或者双引号，本身不用输出
            // fseek(fpin, -1L, SEEK_CUR);
            arr[j] = '\0';
            j = 0;
            int length = strlen(arr);

            if (s == 1)
            {
                fprintf(fpout, "STRCON %s\n", arr);
            }
            else
            {
                fprintf(fpout, "CHARCON %s\n", arr);
            }
            for (i = 0; i < length; i++)
                arr[i] = '\0';
        }

        //整型常量与整型变量的判断
        else if (IsDigit(ch))
        {
            //区别整形变量常量的标志位
            int s = 0;
            while (IsDigit(ch) || IsLetter(ch))
            {
                if (IsLetter(ch))
                {
                    arr[j] = ch;
                    j++;
                    ch = fgetc(fpin);
                    s = 1;
                }
                else if (IsDigit(ch))
                {
                    arr[j] = ch;
                    j++;
                    ch = fgetc(fpin);
                }
            }
            fseek(fpin, -1L, SEEK_CUR);
            arr[j] = '\0';
            j = 0;

            if (s == 0)
                fprintf(fpout, "INTCON %s\n", arr);
            else if (s == 1)
                fprintf(fpout, "INTTK %s\n", arr);
        }
        else
            switch (ch)
            {
            case '+':
                fprintf(fpout, "PLUS %s\n", "+");
                break;
            case '-':
                fprintf(fpout, "MINU %s\n", "-");
                break;
            case '*':
                fprintf(fpout, "MULT %s\n", "*");
                break;
            case '(':
                fprintf(fpout, "LPARENT %s\n", "(");
                break;
            case ')':
                fprintf(fpout, "RPARENT %s\n", ")");
                break;
            case '[':
                fprintf(fpout, "LBRACK %s\n", "[");
                break;
            case ']':
                fprintf(fpout, "RBRACK %s\n", "]");
                break;
            case ';':
                fprintf(fpout, "SEMICN %s\n", ";");
                break;
            case '/':
                fprintf(fpout, "DIV %s\n", "/");
                break;
            case ',':
                fprintf(fpout, "COMMA %s\n", ",");
                break;
            case ' ':
                fprintf(fpout, "NEQ %s\n", " ");
                break;
            case '{':
                fprintf(fpout, "LBRACE %s\n", "{");
                break;
            case '}':
                fprintf(fpout, "RBRACE %s\n", "}");
                break;

            //等号的话判断是等于号还是赋值号
            case '=':
            {
                ch = fgetc(fpin);
                if (ch == '=')
                    fprintf(fpout, "EQL %s\n", "==");
                else
                {
                    fprintf(fpout, "ASSIGN %s\n", "=");
                    fseek(fpin, -1L, SEEK_CUR);
                }
            }
            break;

            case '!':
            {
                ch = fgetc(fpin);
                if (ch == '=')
                    fprintf(fpout, "NEQ %s\n", "!=");
            }
            break;

            case '>':
            {
                ch = fgetc(fpin);
                if (ch == '=')
                    fprintf(fpout, "GEQ %s\n", ">=");
                else
                {
                    fprintf(fpout, "GRE %s\n", ">");
                    fseek(fpin, -1L, SEEK_CUR);
                }
            }
            break;

            case '<':
            {
                ch = fgetc(fpin);
                if (ch == '=')
                    fprintf(fpout, "LEQ %s\n", "<=");
                else
                {
                    fprintf(fpout, "LSS %s\n", "<");
                    fseek(fpin, -1L, SEEK_CUR);
                }
            }
            break;

            default:
                break;
            }
    }
}

int main()
{
    FILE *fpin, *fpout;
    fpin = fopen("testfile.txt", "r");
    fpout = fopen("output.txt", "w");
    init();
    analyse(fpin, fpout);
}