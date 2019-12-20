import java.io.*;
import java.util.*;
import javax.script.*;

public class CodeGenerator {
   ArrayList<String> funcname = new ArrayList<>(); // 함수 이름
   ArrayList<String> string = new ArrayList<>(); // 함수 내용

   public CodeGenerator(TypeChecker typeChecker) throws IOException {
      // TODO Auto-generated constructor stub
      Program program = typeChecker.program;
      Functions fs = program.func;

      File file = new File("test.js");
      FileWriter fw = new FileWriter(file);

      for (int i = 0; i < fs.size(); i++) {
         String str = "";
         Function f = fs.get(i);
         String v = f.v.value();

         for (int j = 0; j < funcname.size(); j++) {
            if (v.equals(funcname.get(j))) { // 오버로딩인 경우 -> string 가져와서 이어붙임
               str = string.get(j);
               str = str.substring(0, str.length() - 2);
               break;
            }
         }
         if (str == "") {

            str = "function " + v + "(){\n";
         }

         Declarations parameters = f.parameters;
         str += "if(arguments.length == " + parameters.size() + "){\n";

         for (int j = 0; j < parameters.size(); j++) {
            String temp = "" + parameters.get(j).v.value() + " = " + "arguments[" + j + "];\n";
            str += temp;
         }

         String bodies = "";
         Block body = f.body; //// function 하나의 parameters, locals, body

         bodies = check((Statement) body);

         if (v == "main") {
            str = bodies;
            if (str != "")
               str = str.substring(0, str.length() - 1);
         } else {
            str += bodies;
            str += "}\n}\n";
         }

         // string.add(str);
         boolean fl = true;
         for (int j = 0; j < funcname.size(); j++) {
            if (v.equals(funcname.get(j))) { // 오버로딩인 경우 -> string 가져와서 이어붙임
               string.set(j, str);
               fl = false;
               break;
            }
         }
         if (fl) {
            string.add(str);
         }
         funcname.add(v); // 함수 이름 추가
         // fw.write(str);
      }
      String fin = "";
      for (int i = 0; i < string.size(); i++)
         fin += string.get(i);
      fw.write(fin);
      fw.close();
   }

   public String check(Statement stmt) { // 전체 체크 돌리는 함수, error 없으면 ok
      String s = "";
      Statement temp = stmt;
      if (temp.select().equals("skip")) {
         //s += ";\n";
      } else if (temp.select().equals("instatement")) {
         InStatement tempin = (InStatement) temp;
         s += expressionCheck(tempin.source) + " = readLine();\n";
         s += "if(" + expressionCheck(tempin.source) + "[0] != '\"') {\n";
         s += "if(" + expressionCheck(tempin.source) + " == \"true\" ||" + expressionCheck(tempin.source) + " == \"false\" ||" + expressionCheck(tempin.source) + " == \"null\"){}\n";
         s += "else {\n";
         s += "flag = 0;\n for(ijk=0; ijk<" + expressionCheck(tempin.source) + ".length; ijk++) {\n";
         s += "if(" + expressionCheck(tempin.source) + "[ijk] == '.') {\n";
         s += "flag = 1;\n}\n}\n";
         s += "if(flag == 1) {\n";
         s += expressionCheck(tempin.source) + " = parseFloat(" + expressionCheck(tempin.source) + ");\n}\n";
         s += "if(flag == 0) {\n";
         s += expressionCheck(tempin.source) + " = parseInt(" + expressionCheck(tempin.source) + ");\n}\n}\n}";
         s += "else {\n";
         s += expressionCheck(tempin.source) + " = " + expressionCheck(tempin.source) + ".substring(1, "
               + expressionCheck(tempin.source) + ".length - 1);\n}\n";
      } else if (temp.select().equals("break")) {
         s += "break;\n";
      } else if (temp.select().equals("block")) {
         Block tempb = (Block) temp;
         s += "{\n";
         for (int i = 0; i < tempb.members.size(); i++) {
            Statement t = tempb.members.get(i);
            s += check(t); // block전체를 다시 check함
         }
         s += "}\n";
         /////////// block 끝
      } else if (temp.select().equals("noassignment")) { // ++i, --i
         NoAssignment no = (NoAssignment) temp;
         s += expressionCheck(no.source);
         /////////// noassignment 끝
      } else if (temp.select().equals("assignment")) {
         Assignment assign = (Assignment) temp;
         String t = "";
         if (assign.source != null) { // 일반 변수 or i++, i--
            t += expressionCheck(assign.source);
         } else if (assign.sources != null) { // 1차원 배열
            Expressions ex = assign.sources;
            t += "[";
            for (int i = 0; i < ex.size(); i++) {
               if (i == ex.size() - 1)
                  t += expressionCheck(ex.get(i));
               else
                  t = t + expressionCheck(ex.get(i)) + ",";
            }
            t += "]";
         } else { // 2차원 배열
            Expressionss exx = assign.sourcess;
            int fir = exx.size(), sec, count = 0, exj = 0; ////// 2 6
            t += "[";
            for (int i = 0; i < fir; i++) {
               t += "[";
               Expressions ex = exx.get(i);
               sec = ex.size();
               for (int j = count; j < exj + (sec / fir); j++) {
                  if (j == sec)
                     break;
                  t += expressionCheck(ex.get(j)) + ",";
               }
               count += sec / fir;
               exj += sec / fir;
               t = t.substring(0, t.length() - 1);
               t += "]";
               if (i != exx.size() - 1)
                  t += ",";
            }
            t += "]";
         }
         
         String tmp = "";
         if(assign.target.select() == "array") {
            Array arr = (Array) assign.target;
            tmp += arr.target.value() + "[" + expressionCheck(arr.indexf) + "]";
            if(arr.indexs != null) {
               tmp += "[" + expressionCheck(arr.indexs) + "]";
            }
         } else {
            Variable var = (Variable) assign.target;
            tmp += var.value();
         }
         if (assign.source != null) {
            if (assign.source.select() == "increment") {
               s += "" + t + ";\n";
            } else
               s += tmp + " = " + t + ";\n";
         } else
            s += tmp + " = " + t + ";\n";
         /////////// assignment 끝
      } else if (temp.select().equals("outstatement")) {
         OutStatement out = (OutStatement) temp;
         ArrayList<Expression> ex = out.source;
         s += "print(";
         for (int a = 0; a < ex.size(); a++) {
            s += expressionCheck(ex.get(a)) + ",";
         }
         s = s.substring(0, s.length() - 1);
         s += ");\n";
         /////////// outstatement 끝
      } else if (temp.select().equals("conditional")) {
         Conditional If = (Conditional) temp;
         Expression test = If.test;
         if (test != null)
            s += "if(" + expressionCheck(test) + ")\n";
         Statement then = If.thenbranch;
         if (then != null)
            s += check(then);
         ArrayList<Elseif> elif = If.Elif;
         if (elif != null) {
            for (int a = 0; a < elif.size(); a++)
               s += check(elif.get(a));
         }
         Statement elseb = If.elsebranch;
         if (elseb != null)
            s += "else\n" + check(elseb);
         /////////// if 끝
      } else if (temp.select().equals("elseif")) {
         Elseif elif = (Elseif) temp;
         Expression test = elif.test;
         if (test != null)
            s += "else if(" + expressionCheck(test) + ")\n";
         Statement then = elif.thenbranch;
         if (then != null)
            s += check(then);
         ////////// else if 끝
      } else if (temp.select().equals("loop")) {
         Loop loop = (Loop) temp;
         if (loop.test != null)
            s += "while(" + expressionCheck(loop.test) + ")";
         if (loop.body != null)
            s += check(loop.body);
         /////////// while 끝
      } else if (temp.select().equals("for")) {
         For f = (For) temp;
         s += "for(";
         if (f.body1 != null) {
            s += check(f.body1);
            s = s.substring(0, s.length() - 1);
         }

         else
            s += ";";
         if (f.body2 != null)
            s += expressionCheck(f.body2) + ";";
         else
            s += ";";
         if (f.body3 != null) {
            s += check(f.body3);
            s = s.substring(0, s.length() - 2);
         }
         s += ")\n";

         if (f.body != null)
            s += check(f.body);
         //////////// for 끝
      } else if (temp.select().equals("call")) {
         Call c = (Call) temp;
         String args = "";
         for (int i = 0; i < c.arguments.size(); i++) {
            if (i == c.arguments.size() - 1)
               args += expressionCheck(c.arguments.get(i));
            else
               args += expressionCheck(c.arguments.get(i)) + ", ";
         }
         s += c.v.value() + "(" + args + ")";
      } else if (temp.select().equals("return")) {
         Return r = (Return) temp;
         s += "return " + expressionCheck(r.result) + ";\n";
         ////////////// target은 뭘 해야하나?
      }
      return s;
   }

   public String expressionCheck(Expression source) {
      String select = source.select();
      String s = "";
      if (select == "variable") { // a = 변수 하나인 경우
         Variable temp = (Variable) source;
         s += temp.value();
      } else if (select == "value") { // a = literal 하나인 경우
         Value temp = (Value) source;
         if (temp.type() == Type.STR)
            s += "\"" + temp.toString() + "\"";
         else
            s += temp.toString();
      } else if (select == "binary") { // binary
         s += binaryCheck((Binary) source);
      } else if (select == "call") { // call
         Call c = (Call) source;
         String args = "";
         for (int i = 0; i < c.arguments.size(); i++) {
            if (i == c.arguments.size() - 1)
               args += expressionCheck(c.arguments.get(i));
            else
               args += expressionCheck(c.arguments.get(i)) + ", ";
         }
         s += c.v.value() + "(" + args + ")";
      } else if (select == "unary") { // unary
         Unary temp = (Unary) source;
         Variable v = (Variable) temp.term;
         Operator op = temp.op;
         s += temp.op.toString() + expressionCheck(temp.term);
      } else if (select == "increment") { // increment
         Increment temp = (Increment) source;
         Variable v = (Variable) temp.term;
         s += expressionCheck(temp.term) + temp.op.toString();
      } else if (select == "array") {
         Array temp = (Array) source;
         s += temp.target.value() + "[" + expressionCheck(temp.indexf) + "]";
         if (temp.indexs != null) {
            s += "[" + expressionCheck(temp.indexs) + "]";
         }
      } else if (select == "smallblock") {
         SmallBlock temp = (SmallBlock) source;
         s += "(" + expressionCheck(temp.source) + ")";
      }
      return s;
   }

   private String binaryCheck(Binary source) {
      Binary temp = source;
      Operator op = temp.op;
      String s = "";

      if(op.val == "/") {
         s += "parseInt(" + expressionCheck(temp.term1) + op + expressionCheck(temp.term2) + ")";
      } else {
         s += expressionCheck(temp.term1) + op + expressionCheck(temp.term2);
      }
      return s;
   }

   public static void main(String[] args) throws ScriptException, IOException {
      // TODO Auto-generated method stub
      CodeGenerator codegenerator = new CodeGenerator(new TypeChecker());

      ScriptEngine e = new ScriptEngineManager().getEngineByName("nashorn");
      e.eval(new FileReader("test.js"));
   }

}