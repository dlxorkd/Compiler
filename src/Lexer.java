import java.io.*;

public class Lexer {

    private boolean isEof = false;
    private char ch = ' '; 
    private BufferedReader input;
    private String line = "";
    private int lineno = 0;
    private int col = 1;
    
    public static int count = 1;
    
    private final String letters = "abcdefghijklmnopqrstuvwxyz"
        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String digits = "0123456789";
    
    private final String specials = "$_";
    private final String blank = " =+-*/%`~!@#^&()[]{}\\|;:,.?";
    
    private final char eolnCh = '\n';
    private final char eofCh = '\004';
    

    public Lexer (String fileName) { // source filename
        try {
            input = new BufferedReader (new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            System.exit(1);
        }
    }

    private char nextChar() { // Return next char
        if (ch == eofCh)
            error("Attempt to read past end of file");
        col++;
        if (col >= line.length()) {
            try {
                line = input.readLine( );
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            } // try
            if (line == null) // at end of file
                line = "" + eofCh;
            else {
                // System.out.println(lineno + ":\t" + line);
                lineno++;
                line += eolnCh;
            } // if line
            col = 0;
        } // if col
        return line.charAt(col);
    }

    public Token next( ) { // Return next token
        do {
            if (isLetter(ch)) { // ident or keyword
                String spelling = concat(letters + digits + specials);
                return Token.keyword(spelling);
            } else if (isDigit(ch)) { // int or float literal
                String number = concat(digits);
                if (ch != '.')  // int Literal
                    return Token.mkIntLiteral(number);
                number += concat(digits);
                return Token.mkFloatLiteral(number);
            } else if(ch == '"') {
               ch = nextChar();
               while(ch != '"') {
                  String spelling = concat(letters + digits + specials + blank);
                  ch = nextChar();
                  return Token.mkStrLiteral(spelling);
               }
            }
            else if(ch == '\'') {
                ch = nextChar();
                while(ch != '\'') {
                   String spelling = concat(letters + digits + specials + blank);
                   ch = nextChar();
                   return Token.mkStrLiteral(spelling);
                }
             }
            else switch (ch) {
            case ' ': case '\t': case '\r':
                ch = nextChar();
                break;
            
            case '/':  // divide or comment
                ch = nextChar();
                if (ch != '/')  return Token.divideTok;
                // comment
                do {
                    ch = nextChar();
                } while (ch != eolnCh);
                ch = nextChar();
                count++;
                //return Token.commentTok;
                break;
                
            case eolnCh:
               ch = nextChar();
               count++;
               //return Token.newlineTok;
               break;
                
            case eofCh: return Token.eofTok;
                
              // - * ( ) { } ; ,  student exercise
            case '*': ch = nextChar();
               return Token.multiplyTok;
            case '%': ch = nextChar();
               return Token.remainderTok;
            
            case ';': ch = nextChar();
               return Token.semicolonTok;
            case ',': ch = nextChar();
               return Token.commaTok;
               
            case '{': ch = nextChar();
               return Token.leftBraceTok;
            case '}': ch = nextChar();
               return Token.rightBraceTok;
               
            case '[': ch = nextChar();
               return Token.leftBracketTok;
            case ']': ch = nextChar();
               return Token.rightBracketTok;
               
            case '(': ch = nextChar();
               return Token.leftParenTok;
            case ')': ch = nextChar();
               return Token.rightParenTok;

            case '&': check('&'); return Token.andTok;
            case '|': check('|'); return Token.orTok;

            case '=':
                return chkOpt('=', Token.assignTok, Token.eqeqTok);
                // < > !  student exercise 
            case '<':
               return chkOpt(Token.ltTok, Token.inTok, Token.lteqTok);
               
            case '>':
               return chkOpt('=', Token.gtTok, Token.gteqTok);
               
            case '!':
               return chkOpt('=', Token.notTok, Token.noteqTok);

            case '+':
               return chkOpt('+', Token.plusTok, Token.increaseTok);
           
            case '-':
               return chkOpt(Token.minusTok, Token.decreaseTok, Token.outTok);
                

            default:  error("Illegal character " + ch); 
            } // switch
        } while (true);
    } // next


    private boolean isLetter(char c) {
        return (c>='a' && c<='z' || c>='A' && c<='Z' || c=='$' || c=='_');
    }
  
    private boolean isDigit(char c) {
        return (c>='0' && c<='9');  // student exercise
    }

    private void check(char c) {
        ch = nextChar();
        if (ch != c) 
            error("Illegal character, expecting " + c);
        ch = nextChar();
    }

    private Token chkOpt(char c, Token one, Token two) {
       ch = nextChar();
       if(ch == c) {
          ch = nextChar();
          return two;
       }
       return one;
    }
    
    private Token chkOpt(Token one, Token two, Token three) {
       ch = nextChar();
       if(ch == '-') {
          ch = nextChar();
          return two;
       }
       else if(ch == '=' || ch == '>') {
          ch = nextChar();
          return three;
       }
       else
          return one;
    }

    private String concat(String set) {
        String r = "";
        do {
            r += ch;
            ch = nextChar();
        } while (set.indexOf(ch) >= 0);
        return r;
    }

    public void error (String msg) {
        System.err.print(line);
        System.err.println("Error: column " + col + " " + msg);
        System.exit(1);
    }

    static public void main ( String[] argv ) {
        Lexer lexer = new Lexer("testcase.txt");
        Token tok = lexer.next();
        while (tok != Token.eofTok) {
           System.out.println("Line " + Lexer.count);
           System.out.println("\t" + tok.toString());
            tok = lexer.next();
            System.out.println();
        } 
    } // main

}