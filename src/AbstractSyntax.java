// Abstract syntax for the language C++Lite,
// exactly as it appears in Appendix B.

import java.util.*;

class Program {
   Functions func;

   Program(Functions f) {
      func = f;
   }

   public void display() {
      System.out.println("Program(abstract syntax)");
      func.display();
   }
}

class Functions extends ArrayList<Function> {
   public void display() {
      for (int i = 0; i < size(); i++)
         get(i).display();
   }
}

class Function {
// Declaration = Variable v; Type t
   Variable v; // 함수 이름
   Declarations parameters, locals; // 파라미터, 지역변수
   Block body; // body
   boolean bool;

   Function(Variable n, Declarations p, Declarations l, Block b, boolean boo) {
      v = n;
      parameters = p;
      locals = l;
      body = b;
      bool = boo;
   }

   public Declarations getparams() {
      return parameters;
   }

   public void display() {
      System.out.println("\nFunction: " + v);
      System.out.print("Parameters: ");
      parameters.display();

      System.out.print("Local Variables: ");
      locals.display();
      body.display();
   }
}

class Array implements Expression {
   Variable target;
   Expression indexf, indexs;
   String select = "array";

   Array(Variable t, Expression i, Expression j) {
      target = t;
      indexf = i;
      indexs = j;
   }

   public Type type() {
      return target.type();
   }

   public void display() {
      target.display();
      indexf.display();
      if (indexs != null)
         indexs.display();
   }

   public String select() {
      return select;
   }
}

class Call implements Statement, Expression {
   Variable v;
   Stack<Expression> arguments;
   String select = "call";
   Type type;

   public Call(Variable v, Stack<Expression> arguments) {
      this.v = v;
      this.arguments = arguments;
   }

   public void display() {
      if (arguments.size() >= 0) {
         System.out.println("Call: " + v);
         System.out.println("Argument:");
         for (int a = 0; a < arguments.size(); a++) {
            arguments.get(a).display();
         }
      }
   }

   public Type type() {
      return type;
   }

   public String select() {
      return select;
   }
}

class Return implements Statement {
   Variable target;
   Expression result;
   String select = "return";

   public Return(Variable v, Expression e) {
      result = e;
      target = v;
   }

   public void display() {
      System.out.print("Return:\t");
      // target.display();
      if (result != null)
         result.display();
   }

   public String select() {
      return select;
   }
}

class Break implements Statement {
   String select = "break";

   public String select() {
      return select;
   }

   public Break() {

   }

   public void display() {

   }
}

class Declarations extends ArrayList<Declaration> {
   // Declarations = Declaration*
   // (a list of declarations d1, d2, ..., dn)
   public void display() {
      System.out.print("{ ");
      for (int i = 0; i < size(); i++)
         get(i).display();
      System.out.println("}");
   }
}

class Declaration {
   // Declaration = Variable v; Type t
   Variable v;
   Type t;
   int d = 0;
   // Expression source = null; // 일반 변수
   // Expressions sources = null; // 1차원배열
   // Expressionss sourcess = null; // 2차원배열

   Declaration(Variable var, Type type) { // parameter용
      v = var;
      t = type;
   }

   Declaration(Variable var, Type type, int d) { // parameter용
      v = var;
      t = type;
      this.d = d;
   }

   public void display() {
      System.out.print("<" + v + ", ");
      System.out.print(t + "> ");
   }
}

class Type {
   // Type = int | bool | string | float
   final static Type INT = new Type("int");
   final static Type BOOL = new Type("bool");
   final static Type STR = new Type("string");
   final static Type FLOAT = new Type("float");
   final static Type INTARRAY = new Type("intarray");
   final static Type BOOLARRAY = new Type("boolarray");
   final static Type STRARRAY = new Type("stringarray");
   final static Type FLOATARRAY = new Type("floatarray");
   final static Type NULL = new Type("null");

   private String id;

   private Type(String t) {
      id = t;
   }

   public String toString() {
      return id;
   }
}

interface Statement {
   // Statement = Skip | Block | Assignment | Conditional | Loop | in/out | For |
   // NoAssignment

   public void display();

   public String select();
}

class Skip implements Statement {

   String select = "skip";

   @Override
   public void display() {
      // TODO Auto-generated method stub
   }

   public String select() {
      return select;
   }
}

class Block implements Statement {
   // Block = Statement*
   // (a Vector of members)
   public ArrayList<Statement> members = new ArrayList<Statement>();
   String select = "block";

   public void display() {
      for (int i = 0; i < members.size(); i++)
         members.get(i).display();
   }

   public String select() {
      return select;
   }
}

class NoAssignment implements Statement {
   Expression source;
   String select = "noassignment";

   NoAssignment(Expression e) {
      source = e;
   }

   public void display() {
      System.out.println("NoAssignment :");
      source.display();
   }

   public String select() {
      return select;
   }
}

class Assignment implements Statement {
   // Assignment = Variable target; Expression source
   Expression target;
   Expression source, first, second;

   Expressions sources;
   Expressionss sourcess;

   String select = "assignment";

   Assignment(Expression t, Expression f, Expression s, Expressionss ess, Expressions es, Expression e) {
      target = t;
      first = f;
      second = s;
      sourcess = ess;
      sources = es;
      source = e;
   }

   public String select() {
      return select;
   }

   public void display() {
      System.out.println("Assignment :");
      if (target != null) {
         target.display();
      }
      if (source != null) {
         source.display();
      }
      if (first != null) {
         first.display();
      }
      if (second != null) {
         second.display();
      }
      if (sources != null) {
         for (int i = 0; i < sources.size(); i++) {
            sources.get(i).display();
         }
      }
      if (sourcess != null) {
         for (int i = 0; i < sourcess.size(); i++) {
            for (int j = 0; j < sourcess.get(i).size(); j++) {
               sourcess.get(i).get(j).display();
            }
            break;
         }
      }
   }
}

class InStatement implements Statement {
   Expression source;
   String select = "instatement";

   InStatement(Expression t) {
      source = t;
   }

   public String select() {
      return select;
   }

   public void display() {
      System.out.println("yesin");
      source.display();
   }
}

class OutStatement implements Statement {
   ArrayList<Expression> source = null;
   String select = "outstatement";

   public OutStatement(ArrayList<Expression> v) {
      source = v;
   }

   public String select() {
      return select;
   }

   public void display() {
      System.out.println("yesout");
      if (source != null)
         for(int i = 0; i < source.size(); i++)
            source.get(i).display();
   }
}

class Elseif implements Statement {
   Expression test;
   Statement thenbranch;
   String select = "elseif";

   Elseif(Expression t, Statement b) {
      test = t;
      thenbranch = b;
   }

   public void display() {
      System.out.println("else if:");
      test.display();
      thenbranch.display();
   }

   public String select() {
      return select;
   }
}

class Conditional implements Statement {
   // Conditional = Expression test; Statement thenbranch, elsebranch
   Expression test;
   Statement thenbranch, elsebranch;
   ArrayList<Elseif> Elif = null;
   String select = "conditional";
   // elsebranch == null means "if... then"

   Conditional(Expression t, Statement tp, ArrayList<Elseif> elif, Statement el) { // if
      test = t;
      thenbranch = tp;
      Elif = elif;
      elsebranch = el;
   }

   public void display() {
      System.out.println("if:");
      test.display();
      thenbranch.display();
      if (Elif != null)
         for (int i = 0; i < Elif.size(); i++)
            Elif.get(i).display();
      if (elsebranch != null) {
         System.out.println("else:");
         elsebranch.display();
      }
   }

   public String select() {
      return select;
   }
}

class Loop implements Statement {
// Loop = Expression test; Statement body
   Expression test;
   Statement body;
   String select = "loop";

   Loop(Expression t, Statement b) {
      test = t;
      body = b;
   }

   public void display() {
      System.out.println("while:");
      test.display();
      body.display();
   }

   public String select() {
      return select;
   }
}

class For implements Statement {
   // For = Expression exp1, exp2, exp3, exp4; Statement body
   // Expression e1, e2, e3, e4;
   Statement body1, body3, body;
   Expression body2;
   String select = "for";

   For(Statement b1, Expression b2, Statement b3, Statement b) {
      body1 = b1;
      body2 = b2;
      body3 = b3;
      body = b;
   }

   public String select() {
      return select;
   }

   public void display() {
      System.out.println("for:");
      if (body1 != null)
         body1.display();
      if (body2 != null)
         body2.display();
      if (body3 != null)
         body3.display();
      if (body != null)
         body.display();
   }
}

class Expressionss extends ArrayList<Expressions> {

}

class Expressions extends ArrayList<Expression> {

}

interface Expression {
   // Expression = Variable | Value | Binary | Unary
   public void display();

   // abstract String termString(); //11.09 추가
   public Type type();

   public String select();
}

class SmallBlock implements Expression {
   public String select = "smallblock";
   Expression source;

   SmallBlock(Expression source) {
      this.source = source;
   }

   public void display() {
      source.display();
   }

   public String select() {
      return select;
   }

   public Type type() {
      return source.type();
   }
}

class Variable implements Expression {
   // Variable = String id
   private String id;
   private Type t;
   public String select = "variable";

   Variable(String s, Type t) {
      id = s;
      this.t = t;
   }

   public void setType(Type t) {
      this.t = t;
   }

   public Type type() {
      return t;
   }

   public String select() {
      return select;
   }

   public String toString() {
      return id;
   }
   // public String termString() {return null;} // 11.09 추가

   public boolean equals(Object obj) {
      String s = ((Variable) obj).id;
      return id.equals(s); // case-sensitive identifiers
   }

   public String value() {
      return id;
   }

   public int hashCode() {
      return id.hashCode();
   }

   public void display() {
      System.out.println("Variable " + id);
   }
}

abstract class Value implements Expression {
   // Value = IntValue | BoolValue |
   // CharValue | FloatValue
   protected Type type;
   protected boolean undef = true;
   public String select = "value";

   int intValue() {
      assert false : "should never reach here";
      return 0;
   } // implementation of this function is unnecessary can can be removed.

   boolean boolValue() {
      assert false : "should never reach here";
      return false;
   }

   String nullValue() {
      assert false : "should never reach here";
      return " ";
   }

   String strValue() {
      assert false : "should never reach here";
      return " ";
   }

   float floatValue() {
      assert false : "should never reach here";
      return 0.0f;
   }

   boolean isUndef() {
      return undef;
   }

   public Type type() {
      return type;
   }

   static Value mkValue(Type type) {
      if (type == Type.INT)
         return new IntValue();
      if (type == Type.BOOL)
         return new BoolValue();
      if (type == Type.FLOAT)
         return new FloatValue();
      if (type == Type.STR)
         return new StrValue();
      throw new IllegalArgumentException("Illegal type in mkValue");
   }
}

class IntValue extends Value {
   private int value = 0;

   IntValue() {
      type = Type.INT;
   }

   IntValue(int v) {
      this();
      value = v;
      undef = false;
   }

   int intValue() {
      assert !undef : "reference to undefined int value";
      return value;
   }

   public String select() {
      return select;
   }

   public String toString() {
      if (undef)
         return "undef";
      return "" + value;
   }

   public void display() {
      System.out.print("IntValue: ");
      System.out.println(value);
   }
}

class NullValue extends Value {
   private String value = "";

   NullValue() {
      type = Type.NULL;
   }

   public String select() {
      return select;
   }

   NullValue(String v) {
      this();
      value = v;
      undef = false;
   }

   String nullValue() {
      assert !undef : "reference to undefined int value";
      return value;
   }

   public String toString() {
      if (undef)
         return "undef";
      return "" + value;
   }

   public void display() {
      System.out.print("NullValue: ");
      System.out.println(value);
   }
}

class BoolValue extends Value {
   private boolean value = false;

   BoolValue() {
      type = Type.BOOL;
   }

   public String select() {
      return select;
   }

   BoolValue(boolean v) {
      this();
      value = v;
      undef = false;
   }

   boolean boolValue() {
      assert !undef : "reference to undefined bool value";
      return value;
   }

   int intValue() {
      assert !undef : "reference to undefined bool value";
      return value ? 1 : 0;
   }

   public String toString() {
      if (undef)
         return "undef";
      return "" + value;
   }

   public void display() {
      System.out.print("BoolValue: ");
      System.out.println(value);
   }
}

class StrValue extends Value {
   private String value = "";

   StrValue() {
      type = Type.STR;
   }

   public String select() {
      return select;
   }

   StrValue(String s) {
      this();
      value = s;
      undef = false;
   }

   String strValue() {
      assert !undef : "reference to undefined char value";
      return value;
   }

   public String toString() {
      if (undef)
         return "undef";
      return "" + value;
   }

   public void display() {
      System.out.print("StringValue: ");
      System.out.println(value);
   }
}

class FloatValue extends Value {
   private float value = 0;

   FloatValue() {
      type = Type.FLOAT;
   }

   public String select() {
      return select;
   }

   FloatValue(float v) {
      this();
      value = v;
      undef = false;
   }

   float floatValue() {
      assert !undef : "reference to undefined float value";
      return value;
   }

   public String toString() {
      if (undef)
         return "undef";
      return "" + value;
   }

   public void display() {
      System.out.print("FloatValue: ");
      System.out.println(value);
   }
}

class Binary implements Expression {
// Binary = Operator op; Expression term1, term2
   Operator op;
   Expression term1, term2;
   Type T;
   public String select = "binary";

   Binary(Operator o, Expression l, Expression r) {
      op = o;
      term1 = l;
      term2 = r;
   } // binary

   public String select() {
      return select;
   }

   public void display() {
      System.out.print("Binary: ");
      op.display();
      term1.display();
      term2.display();
   } // binary

   public String toString() {
      return ("Binary: op=" + op + " term1=" + term1 + " term2=" + term2);
   }

   public Type type() {
      return null;
   }
}

class Unary implements Expression {
   // Unary = Operator op; Expression term
   Operator op;
   Expression term;
   public String select = "unary";

   public String select() {
      return select;
   }

   Unary(Operator o, Expression e) {
      op = o;
      term = e;
   } // unary

   public void display() {
      System.out.print("Unary: ");
      op.display();
      term.display();
   }

   public Type type() {
      return term.type();
   }
}

class Increment implements Expression {
   Expression term;
   Operator op;
   String select = "increment";

   Increment(Expression e, Operator o) {
      term = e;
      op = o;
   }

   public String select() {
      return select;
   }

   public void display() {
      System.out.print("Increment : ");

      op.display();
      term.display();
   }

   public Type type() {
      return term.type();
   }
}

class Operator {
   // Operator = BooleanOp | RelationalOp | ArithmeticOp | UnaryOp
   // BooleanOp = && | ||
   final static String AND = "&&";
   final static String OR = "||";
   // RelationalOp = < | <= | == | != | >= | >
   final static String LT = "<";
   final static String LE = "<=";
   final static String EQ = "==";
   final static String NE = "!=";
   final static String GT = ">";
   final static String GE = ">=";
   // ArithmeticOp = + | - | * | /
   final static String PLUS = "+";
   final static String MINUS = "-";
   final static String TIMES = "*";
   final static String DIV = "/";
   // UnaryOp = !
   final static String NOT = "!";
   final static String NEG = "-";
   // CastOp = int | float | char
   final static String INT = "int";
   final static String FLOAT = "float";
   final static String CHAR = "char";
   // Typed Operators
   // RelationalOp = < | <= | == | != | >= | >
   final static String INT_LT = "INT<";
   final static String INT_LE = "INT<=";
   final static String INT_EQ = "INT==";
   final static String INT_NE = "INT!=";
   final static String INT_GT = "INT>";
   final static String INT_GE = "INT>=";
   // ArithmeticOp = + | - | * | /
   final static String INT_PLUS = "INT+";
   final static String INT_MINUS = "INT-";
   final static String INT_TIMES = "INT*";
   final static String INT_DIV = "INT/";
   // UnaryOp = !
   final static String INT_NEG = "-";
   // RelationalOp = < | <= | == | != | >= | >
   final static String FLOAT_LT = "FLOAT<";
   final static String FLOAT_LE = "FLOAT<=";
   final static String FLOAT_EQ = "FLOAT==";
   final static String FLOAT_NE = "FLOAT!=";
   final static String FLOAT_GT = "FLOAT>";
   final static String FLOAT_GE = "FLOAT>=";
   // ArithmeticOp = + | - | * | /
   final static String FLOAT_PLUS = "FLOAT+";
   final static String FLOAT_MINUS = "FLOAT-";
   final static String FLOAT_TIMES = "FLOAT*";
   final static String FLOAT_DIV = "FLOAT/";
   // UnaryOp = !
   final static String FLOAT_NEG = "-";
   // RelationalOp = < | <= | == | != | >= | >
   final static String CHAR_LT = "CHAR<";
   final static String CHAR_LE = "CHAR<=";
   final static String CHAR_EQ = "CHAR==";
   final static String CHAR_NE = "CHAR!=";
   final static String CHAR_GT = "CHAR>";
   final static String CHAR_GE = "CHAR>=";
   // RelationalOp = < | <= | == | != | >= | >
   final static String BOOL_LT = "BOOL<";
   final static String BOOL_LE = "BOOL<=";
   final static String BOOL_EQ = "BOOL==";
   final static String BOOL_NE = "BOOL!=";
   final static String BOOL_GT = "BOOL>";
   final static String BOOL_GE = "BOOL>=";
   // Type specific cast
   final static String I2F = "I2F";
   final static String F2I = "F2I";
   final static String C2I = "C2I";
   final static String I2C = "I2C";

   String val;

   Operator(String s) {
      val = s;
   }

   public String toString() {
      return val;
   }

   public boolean equals(Object obj) {
      return val.equals(obj);
   }

   boolean BooleanOp() {
      return val.equals(AND) || val.equals(OR);
   }

   boolean RelationalOp() {
      return val.equals(LT) || val.equals(LE) || val.equals(EQ) || val.equals(NE) || val.equals(GT) || val.equals(GE);
   }

   boolean ArithmeticOp() {
      return val.equals(PLUS) || val.equals(MINUS) || val.equals(TIMES) || val.equals(DIV);
   }

   boolean NotOp() {
      return val.equals(NOT);
   }

   boolean NegateOp() {
      return val.equals(NEG);
   }

   boolean intOp() {
      return val.equals(INT);
   }

   boolean floatOp() {
      return val.equals(FLOAT);
   }

   boolean charOp() {
      return val.equals(CHAR);
   }

   final static String intMap[][] = { { PLUS, INT_PLUS }, { MINUS, INT_MINUS }, { TIMES, INT_TIMES }, { DIV, INT_DIV },
         { EQ, INT_EQ }, { NE, INT_NE }, { LT, INT_LT }, { LE, INT_LE }, { GT, INT_GT }, { GE, INT_GE },
         { NEG, INT_NEG }, { FLOAT, I2F }, { CHAR, I2C } };

   final static String floatMap[][] = { { PLUS, FLOAT_PLUS }, { MINUS, FLOAT_MINUS }, { TIMES, FLOAT_TIMES },
         { DIV, FLOAT_DIV }, { EQ, FLOAT_EQ }, { NE, FLOAT_NE }, { LT, FLOAT_LT }, { LE, FLOAT_LE },
         { GT, FLOAT_GT }, { GE, FLOAT_GE }, { NEG, FLOAT_NEG }, { INT, F2I } };

   final static String charMap[][] = { { EQ, CHAR_EQ }, { NE, CHAR_NE }, { LT, CHAR_LT }, { LE, CHAR_LE },
         { GT, CHAR_GT }, { GE, CHAR_GE }, { INT, C2I } };

   final static String boolMap[][] = { { EQ, BOOL_EQ }, { NE, BOOL_NE }, { LT, BOOL_LT }, { LE, BOOL_LE },
         { GT, BOOL_GT }, { GE, BOOL_GE }, };

   final static private Operator map(String[][] tmap, String op) {
      for (int i = 0; i < tmap.length; i++)
         if (tmap[i][0].equals(op))
            return new Operator(tmap[i][1]);
      assert false : "should never reach here";
      return null;
   }

   final static public Operator intMap(String op) {
      return map(intMap, op);
   }

   final static public Operator floatMap(String op) {
      return map(floatMap, op);
   }

   final static public Operator charMap(String op) {
      return map(charMap, op);
   }

   final static public Operator boolMap(String op) {
      return map(boolMap, op);
   }

   public void display() {
      System.out.println(val);
   }
}