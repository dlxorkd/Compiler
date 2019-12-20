import java.util.*;

public class TypeChecker {
   Program program;
   Functions fs;
   Declarations parameters, locals;
   Block body;
   Type returnType;
   Parser p = new Parser(new Lexer("testcase.txt"));

   private void error(String s) {
      System.err.println(s);
      System.exit(1);
   }

   public TypeChecker() {
      program = p.program();
      fs = program.func;
      if (!mainCheck()) // main이 없으면 error
         error("error : main function must exist;");
      for (int i = 0; i < fs.size(); i++) { // 함수 하나씩 돌면서 check
         Function f = fs.get(i);
         parameters = f.parameters;
         locals = f.locals;
         body = f.body; //// function 하나의 parameters, locals, body
         if (check(body))
            System.out.println("Complete Type Check for function " + f.v);
      }
      program.display();
   }

   public boolean check(Block b) { // 전체 체크 돌리는 함수, error 없으면 ok

      for (int n = 0; n < b.members.size(); n++) { //// statements를 돌면서 check
         Statement temp = b.members.get(n);
         if (temp.select().equals("skip")) {
            ;
         } else if (temp.select().equals("instatement")) {
            ;
         } else if (temp.select().equals("break")) {
            ;
         } else if (temp.select().equals("block")) {
            Block tempb = (Block) temp;
            check(tempb); // block전체를 다시 check함
            /////////// block 끝
         } else if (temp.select().equals("noassignment")) { // ++i, --i
            NoAssignment no = (NoAssignment) temp;
            expressionCheck(no.source);
            /////////// noassignment 끝
         } else if (temp.select().equals("assignment")) {
            Assignment assign = (Assignment) temp;

            if (assign.source != null) { // 일반 변수 or i++, i-- or array
               Expression target = assign.target;
               String select = target.select();
               Variable var = null;
               if(select == "variable")
                  var = (Variable) target;
               else {
                  Array arr = (Array) target;
                  var = arr.target;
               }
               if (isDeclared_p(var.value())) { // parameter인 경우
                  Declaration dd = new Declaration(var, getType(assign.source));
                  for (int i = 0; i < parameters.size(); i++) {
                     if (assign.target.equals(parameters.get(i).v)) {
                        parameters.set(i, dd);
                        break;
                     }
                  }
               }
               Type tt = expressionCheck(assign.source);
               if(select == "variable") {
                  Declaration d = new Declaration(var, tt);
                  for (int i = 0; i < locals.size(); i++) {
                     if (var.value().equals(locals.get(i).v.value())) {
                        locals.set(i, d);
                     }
                  }
               }
            } else if (assign.sources != null) { // 1차원 배열
               Expressions ex = assign.sources;
               Type type = null;
               for (int i = 0; i < ex.size(); i++) {
                  if (type == null)
                     type = expressionCheck(ex.get(i));
                  else {
                     if (type != expressionCheck(ex.get(i))) ///// int, float 허용하려면 여기 고치기
                        error("error : all array factors must have equal type;");
                  }
               }
            } else { // 2차원 배열
               Expressionss exx = assign.sourcess;
               Type type = null;
               for (int i = 0; i < exx.size(); i++) {
                  Expressions ex = exx.get(i);
                  for (int j = 0; j < ex.size(); j++) {
                     if (type == null)
                        type = expressionCheck(ex.get(j));
                     else {
                        if (type != expressionCheck(ex.get(j))) ///// int, float 허용하려면 여기 고치기
                           error("error : all array factors must have equal type;");
                     }
                  }
               }
            }
            /////////// assignment 끝
         } else if (temp.select().equals("outstatement")) {
            OutStatement out = (OutStatement) temp;
            ArrayList<Expression> ex = out.source;
            for (int a = 0; a < ex.size(); a++)
               expressionCheck(ex.get(a));
            /////////// outstatement 끝
         } else if (temp.select().equals("conditional")) {
            Conditional If = (Conditional) temp;
            Expression test = If.test;
            Statement then = If.thenbranch;
            Statement elseb = If.elsebranch;
            ArrayList<Elseif> elif = If.Elif;
            Block ifb = new Block();
            ifb.members.add(then);
            if (elseb != null)
               ifb.members.add(elseb);
            if (elif != null)
               for (int a = 0; a < elif.size(); a++)
                  ifb.members.add(elif.get(a));
            expressionCheck(test);
            check(ifb);
            /////////// if 끝
         } else if (temp.select().equals("elseif")) {
            Elseif elif = (Elseif) temp;
            Expression test = elif.test;
            Statement then = elif.thenbranch;
            Block elifb = new Block();
            elifb.members.add(then);
            expressionCheck(test);
            check(elifb);
            ////////// else if 끝
         } else if (temp.select().equals("loop")) {
            Loop loop = (Loop) temp;
            Block loopb = new Block();
            loopb.members.add(loop.body);
            expressionCheck(loop.test);
            check(loopb);
            /////////// while 끝
         } else if (temp.select().equals("for")) {
            For f = (For) temp;
            Expression body2;

            Block forb = new Block();
            if (f.body1 != null)
               forb.members.add(f.body1);
            if (f.body3 != null)
               forb.members.add(f.body3);
            if (f.body != null)
               forb.members.add(f.body);
            check(forb);
            if (f.body2 != null)
               expressionCheck(f.body2);
            //////////// for 끝
         } else if (temp.select().equals("call")) {
            Call c = (Call) temp;
            if (!isDeclared_f(c.v.value()))
               error("function " + c.v.value() + " is not declared!");
            int count = 0;
            boolean flag = true;
            int[] ttmp = new int[10];
            for (int i = 0; i < fs.size(); i++) {
               String str = new String("" + fs.get(i).v);
               if (str.equals(c.v.value())) {
                  ttmp[count++] = fs.get(i).parameters.size();
               }
            }
            for (int i = 0; i < count; i++) {
               if (c.arguments.size() == ttmp[i])
                  flag = false;
            }
            if (flag)
               error("Parameter size and Argument size must be the same!");

            ArrayList<Type> tmp = new ArrayList<>();
            String str = new String("" + c.v);
            Block bb = new Block();
            for (int i = 0; i < c.arguments.size(); i++) {
               tmp.add(expressionCheck(c.arguments.get(i)));
            }

            for (int i = 0; i < fs.size(); i++) {
               if (str.equals(fs.get(i).v.value()) && fs.get(i).parameters.size() == c.arguments.size()) {
                  for (int j = 0; j < fs.get(i).parameters.size(); j++) {
                     Declaration d = new Declaration(fs.get(i).parameters.get(j).v, tmp.get(j));
                     fs.get(i).parameters.set(j, d);
                  }
                  Declarations tempp, templ;
                  tempp = parameters;
                  templ = locals;
                  parameters = fs.get(i).parameters;
                  locals = fs.get(i).locals;
                  bb = fs.get(i).body;
                  check(bb);
                  parameters = tempp;
                  locals = templ;
               }
            }

         } else if (temp.select().equals("return")) {
            Return r = (Return) temp;
            expressionCheck(r.result);
            returnType = getType(r.result);
         }
      }
      return true;
   }

   public Type expressionCheck(Expression source) {
      String select = source.select();
      Type type = null;
      if (select == "variable") { // a = 변수 하나인 경우
         Variable temp = (Variable) source;
         type = declaredType(temp);
      } else if (select == "value") { // a = literal 하나인 경우
         Value temp = (Value) source;
         type = temp.type();
      } else if (select == "binary") { // binary
         type = binaryCheck((Binary) source);
      } else if (select == "call") { // call
         Call c = (Call) source;
         if (!isDeclared_f(c.v.value()))
            error("function " + c.v.value() + " is not declared!");
         int count = 0;
         boolean flag = true;
         int[] ttmp = new int[10];
         for (int i = 0; i < fs.size(); i++) {
            String str = new String("" + fs.get(i).v);
            if (str.equals(c.v.value())) {
               ttmp[count++] = fs.get(i).parameters.size();
            }
         }
         for (int i = 0; i < count; i++) {
            if (c.arguments.size() == ttmp[i])
               flag = false;
         }
         if (flag)
            error("Parameter size and Argument size must be the same!");

         ArrayList<Type> tmp = new ArrayList<>();
         String str = new String("" + c.v);
         Block bb = new Block();
         for (int i = 0; i < c.arguments.size(); i++) {
            tmp.add(expressionCheck(c.arguments.get(i)));
         }

         for (int i = 0; i < fs.size(); i++) {
            if (str.equals(fs.get(i).v.value()) && fs.get(i).parameters.size() == c.arguments.size()) {
               for (int j = 0; j < fs.get(i).parameters.size(); j++) {
                  Declaration d = new Declaration(fs.get(i).parameters.get(j).v, tmp.get(j));
                  fs.get(i).parameters.set(j, d);
               }
               Declarations tempp, templ;
               tempp = parameters;
               templ = locals;
               parameters = fs.get(i).parameters;
               locals = fs.get(i).locals;
               bb = fs.get(i).body;
               check(bb);
               parameters = tempp;
               locals = templ;
            }
         }

         type = returnType;
      } else if (select == "unary") { // unary
         Unary temp = (Unary) source;
         Variable v = (Variable) temp.term;
         Operator op = temp.op;
         type = getType(v);
         if (op.equals("++") || op.equals("--") || op.equals("-")) {
            if (v.type() == Type.STR || v.type() == Type.BOOL)
               error("error : string|bool type cannot use " + op + " operator; identifier " + v.value());
            if (type == Type.INTARRAY || type == Type.FLOATARRAY || type == Type.BOOLARRAY || type == Type.STRARRAY)
               error("error : array type cannot use increment operator; identifier " + v.value());
         } else if (op.equals("!")) {
            if (v.type() == Type.STR)
               error("error : string type cannot use " + op + " operator; identifier " + v.value());
         }
      } else if (select == "increment") { // increment
         Increment temp = (Increment) source;
         Variable v = (Variable) temp.term;
         type = getType(v);
         if (type == Type.STR)
            error("error : string type cannot use increment operator; identifier " + v.value());
         if (type == Type.BOOL)
            error("error : bool type cannot use increment operator; identifier " + v.value());
         if (type == Type.INTARRAY || type == Type.FLOATARRAY || type == Type.BOOLARRAY || type == Type.STRARRAY)
            error("error : array type cannot use increment operator; identifier " + v.value());
      } else if (select == "array") {
         Array temp = (Array) source;
         type = temp.type();
         Type t1 = expressionCheck(temp.indexf);
         if (t1 != Type.INT)
            error("index type must be integer");
         if (temp.indexs != null) {
            Type t2 = expressionCheck(temp.indexs);
            if (t2 != Type.INT)
               error("index type must be integer");
         }
      } else if (select == "smallblock") {
         SmallBlock temp = (SmallBlock) source;
         type = temp.type();
         expressionCheck(temp.source);
      }
      return type;
   }

   private Type binaryCheck(Binary source) {
      ArrayList<Type> t = new ArrayList<>();
      Type type = null;
      Binary temp = source;
      Operator op = temp.op;

      t.add(expressionCheck(temp.term1));
      t.add(expressionCheck(temp.term2));
      if (t.get(0) == null || t.get(1) == null)
         type = null;
      else {
         /////////////////////////////////////////// + - * / % 에 대한 예외처리
         if (CalOpCheck(op)) {
            if (t.contains(Type.STR) || t.contains(Type.BOOL) || t.contains(Type.INTARRAY)
                  || t.contains(Type.FLOATARRAY) || t.contains(Type.STRARRAY) || t.contains(Type.BOOLARRAY))
               error("error : string | bool | array type cannot use " + op);
         }
         if (op.equals("+")) {
            if (t.contains(Type.BOOL) || t.contains(Type.INTARRAY) || t.contains(Type.FLOATARRAY)
                  || t.contains(Type.STRARRAY) || t.contains(Type.BOOLARRAY))
               error("error : string | bool | array type cannot use " + op);
            if(t.contains(Type.STR))
               type = Type.STR;
         }

         /////////////////////////////////////////// > >= < <= 에 대한 예외처리
         if (sizeRelOpCheck(op)) {
            if (t.contains(Type.STR) || t.contains(Type.BOOL) || t.contains(Type.INTARRAY)
                  || t.contains(Type.FLOATARRAY) || t.contains(Type.STRARRAY) || t.contains(Type.BOOLARRAY))
               error("error : string | bool | array type cannot use " + op);
         }

         /////////////////////////////////////////// == != 에 대한 예외처리
         if (eqRelOpCheck(op)) {
            if (t.get(0).equals(Type.INT) || t.get(0).equals(Type.FLOAT)) {
               if (t.get(1).equals(Type.INT) || t.get(1).equals(Type.FLOAT))
                  ;
               else
                  error("operator " + op + " not exist; " + t.get(0) + " " + op + " " + t.get(1));
            } else if (t.get(0).equals(Type.BOOL)) {
               if (!t.get(1).equals(Type.BOOL))
                  error("operator " + op + " not exist; " + t.get(0) + " " + op + " " + t.get(1));
            } else if (t.get(0).equals(Type.STR)) {
               if (!t.get(1).equals(Type.STR))
                  error("operator " + op + " not exist; " + t.get(0) + " " + op + " " + t.get(1));
            } else if (t.contains(Type.INTARRAY) || t.contains(Type.FLOATARRAY) || t.contains(Type.STRARRAY)
                  || t.contains(Type.BOOLARRAY))
               error("operator " + op + " not exist; " + t.get(0) + " " + op + " " + t.get(1));
         }
      }

      for (int i = 0; i < t.size(); i++) {
         if (type == null)
            type = t.get(i);
         else {
            if (type == Type.INT && t.get(i) == Type.FLOAT)
               type = t.get(i);
            if (t.get(i) == null) { // type중에 null이 있는 경우는 무조건 null type을 가짐
               type = null;
               break;
            }
         }
      }
      if (opCheck(op))
         type = Type.BOOL;
      return type;
   }

   public boolean outCheck(String id) { // yesout -> id인 경우 검사
      if (isDeclared_p(id) || isDeclared_l(id)) // 둘 다 false면 선언 안된 변수이므로 error
         return true;
      return false;
   }

   public boolean mainCheck() {
      for (int i = 0; i < fs.size(); i++) {
         Function f = fs.get(i);
         if (f.v.value().equals("main"))
            return true;
      }
      return false;
   }

   public Type getType(Expression source) {
      Type T = null;
      String select = source.select();
      if (select == "variable") { // a = 변수 하나인 경우
         Variable temp = (Variable) source;
         T = declaredType(temp);
      } else if (select == "value") { // a = literal 하나인 경우
         Value temp = (Value) source;
         T = temp.type();
      } else if (select == "binary") { // binary
         Binary temp = (Binary) source;
         T = binaryCheck(temp);
      } else if (select == "call") { // call
         expressionCheck(source);
         T = returnType;
      } else if (select == "unary") { // unary
         Unary temp = (Unary) source;
         T = temp.type();
      } else if (select == "increment") { // increment
         Increment temp = (Increment) source;
         T = temp.type();
      } else if (select == "array") {
         Array temp = (Array) source;
         T = temp.type();
      } else if (select == "smallblock") {
         SmallBlock temp = (SmallBlock) source;
         T = temp.source.type();
      }
      return T;
   }

   private Type declaredType(Variable id) {
      for (int i = 0; i < parameters.size(); i++) {
         String v = new String("" + parameters.get(i).v);
         if (v.equals(id.value()))
            return parameters.get(i).t;
      }
      for (int i = 0; i < locals.size(); i++) {
         String v = new String("" + locals.get(i).v);
         if (v.equals(id.value()))
            return locals.get(i).t;
      }
      return null;
   }

   public boolean opCheck(Operator op) { // == != > >= < <= && ||
      if (op.equals("==") || op.equals("!=") || op.equals(">") || op.equals(">=") || op.equals("<") || op.equals("<=")
            || op.equals("&&") || op.equals("||"))
         return true;
      return false;
   }

   private boolean isDeclared_l(String id) {
      for (int i = 0; i < locals.size(); i++) {
         String v = new String("" + locals.get(i).v);
         if (v.equals(id))
            return true;
      }
      return false;
   }

   private boolean isDeclared_p(String id) {
      for (int i = 0; i < parameters.size(); i++) {
         String v = new String("" + parameters.get(i).v);
         if (v.equals(id))
            return true;
      }
      return false;
   }

   private boolean isDeclared_f(String id) {
      for (int i = 0; i < fs.size(); i++) {
         String v = new String("" + fs.get(i).v);
         if (v.equals(id))
            return true;
      }
      return false;
   }

   private boolean CalOpCheck(Operator op) { // + 제외 arithmetic operator
      if (op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%"))
         return true;
      return false;
   }

   private boolean sizeRelOpCheck(Operator op) { // > >= < <=
      if (op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">="))
         return true;
      return false;
   }

   private boolean eqRelOpCheck(Operator op) { // == !=
      if (op.equals("==") || op.equals("!="))
         return true;
      return false;
   }

   public static void main(String[] args) {
      TypeChecker typechecker = new TypeChecker();
   }
}