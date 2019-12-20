import java.util.*;

public class Parser {
   // Recursive descent parser that inputs a C++Lite program and
   // generates its abstract syntax. Each method corresponds to
   // a concrete syntax grammar rule, which appears as a comment
   // at the beginning of the method.

   Token token; // current token from the input stream
   Lexer lexer;
   Variable funcname; /////////// 고쳐야함
   Functions fs = new Functions(); ///// 11.20 함수 선언 배열
   Declarations parameters = new Declarations(); /////// 11.09 추가 파트
   Declarations locals = new Declarations(); /////// 11.09 추가 파트
   boolean forflag = false; //////// for문이면 true
   boolean returnflag = false; // return이 있다면 true;

   public Parser(Lexer ts) { // Open the C++Lite source program
      lexer = ts; // as a token stream, and
      token = lexer.next(); // retrieve its first Token
   }

   private String match(TokenType t) { // * return the string of a token if it matches with t *
      String value = token.value();
      if (token.type().equals(t))
         token = lexer.next();
      else
         error(t);
      return value;
   }

   private void error(TokenType tok) {
      System.err.println("Syntax error: expecting: " + tok + "; saw: " + token + " " + Lexer.count);
      System.exit(1);
   }

   private void error(String tok) {
      System.err.println("Syntax error: expecting: " + tok + "; saw: " + token + " " + Lexer.count);
      System.exit(1);
   }

   public Program program() {
      while (token.type().equals(TokenType.Identifier) || token.type().equals(TokenType.Main)) // 함수인 경우
      {
         Function f;
         Variable v; // 함수, 전역변수 이름
         Type T = null;
         if (token.type().equals(TokenType.Identifier)) {
            v = new Variable(match(TokenType.Identifier), null); // 함수 이름
            funcname = v;
            returnflag = false;
         } else {
            v = new Variable(match(TokenType.Main), null); // 메인 이름
            returnflag = false;
         }

         if (token.type().equals(TokenType.LeftParen)) ///// 함수
         {
            match(TokenType.LeftParen); // (
            //////////////////////////////////////////////////// parameter
            while (token.type().equals(TokenType.Identifier)) {
               Variable target = new Variable(match(TokenType.Identifier), null); // 변수 이름
               if (isDeclared_p(target.value())) // 파라미터 이름 겹치면 error
                  error("parameter name repeated");
               Declaration d = new Declaration(target, T); // 변수 이름과 타입 생성
               parameters.add(d); // parameters 배열에다가 추가
               if (token.type().equals(TokenType.Comma))
                  match(TokenType.Comma);
            } /// parameter 이름 겹쳐도 다 추가
               //////////////////////////////////////////////////// parameter
            match(TokenType.RightParen); // )
            Block b = statements(); // { body }
            Declarations param = parameters, local = locals;

            f = new Function(v, param, local, b, returnflag); // 함수 이름, 파라미터 변수들, 로컬 변수들, body
            fs.add(f); // fs 배열에다가 추가

            int count = 0;
            int[] tmp = new int[10];
            for (int i = 0; i < fs.size(); i++) {
               String str = new String("" + fs.get(i).v);
               if (str.equals(v.value())) {
                  tmp[count++] = fs.get(i).parameters.size();
                  if (count > 1) {
                     for (int j = 0; j < count - 1; j++) {
                        for (int k = j + 1; k < count; k++) {
                           if (tmp[j] == tmp[k]) {
                              error("overloading error!");
                           }
                        }
                     }
                  }
               }

            }

            parameters = new Declarations(); // 새 함수 받게 새로 초기화
            locals = new Declarations(); // 새 함수 받게 새로 초기화
         }
      }
      return new Program(fs);
   }

   private Statement statement() {
      // Statement --> ; | Block | Assignment | IfStatement | WhileStatement | in/out
      // | ForStatement | NoAssignment | CallStatement | ReturnStatement
      Statement s = null;

      if (token.type().equals(TokenType.Semicolon)) { // 다음 토큰이 ; 이면 빈 statement -> skip
         s = new Skip();
         token = lexer.next();
      } else if (token.type().equals(TokenType.LeftBrace)) // block
         s = statements();
      else if (token.type().equals(TokenType.If)) // if
         s = ifStatement();
      else if (token.type().equals(TokenType.While)) // while
         s = whileStatement();
      else if (token.type().equals(TokenType.For))
         s = forStatement();
      else if (token.type().equals(TokenType.Identifier)) { // assign 11.09 수정
         Variable v = new Variable(token.value(), null);
         match(TokenType.Identifier);
         if (token.type().equals(TokenType.LeftParen)) { // 함수인 경우
            s = callStatement(v, "s");
         } else {
            s = assignment(v);
         }
      } else if (token.type().equals(TokenType.YesIn)) //////////////////////// 11.09.3
         s = inStatement();
      else if (token.type().equals(TokenType.YesOut))//////////////////////// 11.09.3
         s = outStatement();
      else if (token.type().equals(TokenType.Return))
         return returnStatement(funcname);
      else if (token.type().equals(TokenType.Break))
         s = breakStatement();
      else if (isIncrementOp() || isUnaryOp())
         s = noassignment();
      else
         error("Error in Statement construction");
      return s;
   }

   private Block statements() { // block
      // Block --> '{' Statements '}'
      Block b = new Block();
      Statement s;
      // student exercise

      match(TokenType.LeftBrace); // block의 시작은 {

      while (isStatement()) // block 안에서 ;, block, if, while, assign 반복
      {
         s = statement();
         b.members.add(s);
      }
      match(TokenType.RightBrace); // block의 끝은 }
      return b;
   }

   private Call callStatement(Variable v, String id) {
      match(TokenType.LeftParen);
      Stack<Expression> params = new Stack<Expression>();
      while (!(token.type().equals(TokenType.RightParen))) {
         params.push(expression());
         if (token.type().equals(TokenType.Comma)) {
            match(TokenType.Comma);
         }
      }
      match(TokenType.RightParen);

      for (int i = 0; i < fs.size(); i++) {
         String str = new String("" + fs.get(i).v);
         if (str.equals(v.value()) && params.size() == fs.get(i).parameters.size() && !fs.get(i).bool && id == "p") {
            error("function " + v.value() + " cannot use with Assign!");
         }
      }
      return new Call(v, params);
   }

   private Return returnStatement(Variable v) {
      match(TokenType.Return);
      if (token.type().equals(TokenType.Semicolon)) {
         match(TokenType.Semicolon);
         returnflag = false;
         return new Return(v, null);
      }
      Expression finalReturn = expression();
      match(TokenType.Semicolon);
      returnflag = true;
      return new Return(v, finalReturn);
   }

   private Break breakStatement() {
      match(TokenType.Break);
      match(TokenType.Semicolon);
      return new Break();
   }

   private NoAssignment noassignment() {

      Expression source = expression();
      match(TokenType.Semicolon);
      return new NoAssignment(source);
   }

   private Assignment assignment(Variable v) { ///////////////// 11.09 수정
      // Assignment --> Identifier = Expression ;
      Expression source, first, second = null;
      Expressions sources = new Expressions();
      Expressionss sourcess = new Expressionss();
      Variable target = v;
      Type T = null;
      String id = v.value();
      ////////////////// 배열 []
      if (token.type().equals(TokenType.LeftBracket)) {
         // array a가 선언되어있고, a[3] = 1; 이런 경우
         if (!isDeclared(id)) // 선언 안되어있으면 error
            error("identifier is not declared");
         int dimension = getDimension(target);
         if (dimension == 0) {
            error("identifier is not array type");
         } // 선언도 되어있고 array인 경우
         int flag = 1;
         match(TokenType.LeftBracket); // [
         first = expression();
         match(TokenType.RightBracket); // ]

         if (token.type().equals(TokenType.LeftBracket)) { // 2차원 배열 a[2][2]
            flag = 2;
            match(TokenType.LeftBracket); // [
            second = expression();
            match(TokenType.RightBracket); // ]
         }
         match(TokenType.Assign); // =
         if (flag == 1) {
            //////// 1차원 배열에 값 넣는 경우 a[3] = 1;
            if (dimension == 1) {
               source = expression();
               Array temp = new Array(target, first, null);
               return new Assignment(temp, first, null, null, null, source);
            } else { //////// 2차원 배열에 값 넣는 경우 a[3] = {1, 2};
               match(TokenType.LeftBrace); // {
               source = expression();
               sources.add(source);
               while (isComma()) {
                  token = lexer.next();
                  source = expression();
                  sources.add(source);
               }
               match(TokenType.RightBrace); // }
               Array temp = new Array(target, first, null);
               return new Assignment(temp, first, null, null, sources, null);
            }
         } else { // 2차원 배열 a[3][3] = 1;
            source = expression();
            Array temp = new Array(target, first, second);
            return new Assignment(temp, first, second, null, null, source);
         }
      } /// 선언된 배열 바꾸는 부분
         //////////////////////////////////////////////////////////////
      else { ///////// 배열 x 일반 변수
         if (isIncrementOp()) {// i++, i--
            if (!isDeclared(target.value()))
               error("identifier is not declared");
            Operator op = new Operator(match(token.type()));
            source = new Increment(target, op);

            if (!forflag) // for body3이 아니면 match ;
               match(TokenType.Semicolon);
            return new Assignment(target, null, null, null, null, source);
         }
         match(TokenType.Assign); // i = 0;
         ////////////////////////////////////////////////// 수정, 배열 선언 파트
         if (token.type().equals(TokenType.LeftBrace)) {
            int flag = 1;
            match(TokenType.LeftBrace); // {

            if (token.type().equals(TokenType.LeftBrace)) ///// 2차원 배열
               flag = 2;

            if (flag == 1) // 1차원 배열인 경우
            {
               source = expression();
               T = getType(source);
               if (T == Type.INT)
                  T = Type.INTARRAY;
               else if (T == Type.FLOAT)
                  T = Type.FLOATARRAY;
               else if (T == Type.STR)
                  T = Type.STRARRAY;
               else if (T == Type.BOOL)
                  T = Type.BOOLARRAY;
               sources.add(source);
               while (isComma()) {
                  token = lexer.next();
                  source = expression();
                  sources.add(source);
               }

               match(TokenType.RightBrace); // }
               if (!forflag) // for body3이 아니면 match ;
                  match(TokenType.Semicolon);

               if (!isDeclared(id)) {/// 선언이 안된 변수인 경우만 추가
                  // Declaration d = new Declaration(target, T, sources);
                  Declaration d = new Declaration(target, T, 1);
                  locals.add(d);
               } else { // 선언이 된 경우
                  Declaration d = new Declaration(target, T, 1);
                  if (isDeclared_l(id)) { // local에 선언된 변수인 경우 수정
                     for (int i = 0; i < locals.size(); i++) {
                        if (target.equals(locals.get(i).v)) {
                           locals.set(i, d);
                           break;
                        }
                     }
                  } else { // parameter에 선언된 변수인 경우 수정
                     for (int i = 0; i < parameters.size(); i++) {
                        if (target.equals(parameters.get(i).v)) {
                           parameters.set(i, d);
                           break;
                        }
                     }
                  }
               } // 1차원 배열인 경우 끝
               return new Assignment(target, null, null, null, sources, null);
            } else // 2차원 배열인 경우
            {
               do {
                  if (token.type().equals(TokenType.Comma))
                     token = lexer.next();
                  match(TokenType.LeftBrace); // {
                  source = expression();
                  T = getType(source);
                  if (T == Type.INT)
                     T = Type.INTARRAY;
                  else if (T == Type.FLOAT)
                     T = Type.FLOATARRAY;
                  else if (T == Type.STR)
                     T = Type.STRARRAY;
                  else if (T == Type.BOOL)
                     T = Type.BOOLARRAY;
                  sources.add(source);
                  while (isComma()) {
                     token = lexer.next();
                     source = expression();
                     sources.add(source);
                  }
                  match(TokenType.RightBrace); // }
                  sourcess.add(sources);
               } while (token.type().equals(TokenType.Comma));
               match(TokenType.RightBrace); // }
               if (!forflag) // for body3이 아니면 match ;
                  match(TokenType.Semicolon);

               if (!isDeclared(id)) {/// 선언이 안된 변수인 경우만 추가
                  Declaration d = new Declaration(target, T, 2);
                  locals.add(d);
               } else { // 선언이 된 경우
                  Declaration d = new Declaration(target, T, 2);
                  if (isDeclared_l(id)) { // local에 선언된 변수인 경우 수정
                     for (int i = 0; i < locals.size(); i++) {
                        if (target.equals(locals.get(i).v)) {
                           locals.set(i, d);
                           break;
                        }
                     }
                  } else { // parameter에 선언된 변수인 경우 수정
                     for (int i = 0; i < parameters.size(); i++) {
                        if (target.equals(parameters.get(i).v)) {
                           parameters.set(i, d);
                           break;
                        }
                     }
                  }
               }
               return new Assignment(target, null, null, sourcess, null, null);
            } /////////////////// 배열 끝
         } ////////////////// 일반 변수 i = 0
         source = expression();
         T = getType(source);

         if (!isDeclared(id)) {/// 선언이 안된 변수인 경우만 추가
            Declaration d = new Declaration(target, T);
            locals.add(d);
         } else {
            if (isDeclared_p(id)) { // parameter인 경우
               Declaration d = new Declaration(target, T);
               for (int i = 0; i < parameters.size(); i++) {
                  if (target.equals(parameters.get(i).v)) {
                     parameters.set(i, d);
                     break;
                  }
               }
            } else { // local인 경우
               if (!isDeclared_l(id)) { // 선언이 된 경우 local에 됐는지 검사, 안된 변수면 추가
                  Declaration d = new Declaration(target, T);
                  locals.add(d);
               } else { // 선언이 된 경우 변경해줌
                  Declaration d = new Declaration(target, T);
                  for (int i = 0; i < locals.size(); i++) {
                     if (target.equals(locals.get(i).v)) {
                        locals.set(i, d);
                        break;
                     }
                  }
               }
            }
         }
         if (!forflag) // for body3이 아니면 match ;
            match(TokenType.Semicolon);
         return new Assignment(target, null, null, null, null, source);
      }
   }

   private int getDimension(Variable v) {
      for (int i = 0; i < locals.size(); i++) {
         String s = new String("" + locals.get(i).v);
         if (s.equals(v.value()))
            return locals.get(i).d;
      }
      for (int i = 0; i < parameters.size(); i++) {
         String s = new String("" + parameters.get(i).v);
         if (s.equals(v.value()))
            return parameters.get(i).d;
      }
      return 0;
   }

   public Type binaryType(Binary source) {
      ArrayList<Type> t = new ArrayList<>();
      Type type = null;
      Binary temp = source;
      Operator op = temp.op;

      if (temp.term2.select() == "binary")
         t.add(binaryType((Binary) temp.term2));
      else
         t.add(0, temp.term2.type());
      if (temp.term1.select() == "binary")
         t.add(binaryType((Binary) temp.term1));
      else
         t.add(0, temp.term1.type());

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
            if (t.get(i) == Type.STR) {
               type = Type.STR;
               break;
            }
         }
      }
      if (opCheck(op))
         type = Type.BOOL;
      return type;
   }

   public Type getType(Expression source) {
      Type T = null;
      String select = source.select();
      if (select == "variable") { // a = 변수 하나인 경우
         Variable temp = (Variable) source;
         T = temp.type();
      } else if (select == "value") { // a = literal 하나인 경우
         Value temp = (Value) source;
         T = temp.type();
      } else if (select == "binary") { // binary
         Binary temp = (Binary) source;
         T = binaryType(temp);
      } else if (select == "call") { // call
         Call temp = (Call) source;
         T = temp.type();
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
         T = temp.type();
      }
      return T;
   }

   private InStatement inStatement() { // inStatement --> yesin <- identifier { <- identifier} ; --> assignment로 봄
      Type T = null;
      ArrayList<Expression> ex = new ArrayList<>();
      match(TokenType.YesIn); // yesin
      match(TokenType.In); // <-
      Variable v = new Variable(token.value(), T);
      match(TokenType.Identifier);

      if (token.type().equals(TokenType.LeftBracket)) { // 배열 input
         int dimension = getDimension(v);
         System.out.println(dimension);
         Array temp;
         if (dimension == 0)
            error("identifier is not array type");

         match(TokenType.LeftBracket); // [
         Expression source = expression(); // 1
         match(TokenType.RightBracket); // ]

         if (token.type().equals(TokenType.LeftBracket)) { // a = b[2][2];
            if (dimension == 1)
               error("array dimension error");
            match(TokenType.LeftBracket); // [
            Expression source2 = expression(); // 1
            match(TokenType.RightBracket); // ]
            temp = new Array(v, source, source2);
         } else { // a = b[2];
            if (dimension == 2)
               error("array dimension error");
            temp = new Array(v, source, null);
         }
         if (!isDeclared(temp.target.value())) {
            Declaration d = new Declaration(temp.target, T, 0);
            locals.add(d);
         }
         return new InStatement(temp);
      } else { // 일반 변수
         if (!isDeclared(v.value())) {
            Declaration d = new Declaration(v, T, 0);
            locals.add(d);
         }
         return new InStatement(v);
      }
   }

   private OutStatement outStatement() { // outStatement --> yesout -> (literal | identifier.value) { -> (literal |
      // identifier.value)} ;
      ArrayList<Expression> ex = new ArrayList<>();
      match(TokenType.YesOut); // yesout
      match(TokenType.Out); // ->
      Expression source = expression();
      ex.add(source);
      while (token.type().equals(TokenType.Out)) {
         match(TokenType.Out); // ->
         source = expression();
         ex.add(source);
      }
      return new OutStatement(ex);
   }

   private Conditional ifStatement() {
      // IfStatement --> if ( Expression ) Statement [ else Statement ]
      // IfStatement --> if ( Expression ) Statement { [else if | else] Statement}*
      ArrayList<Elseif> Elif = new ArrayList<>();
      Statement elsebranch = null;

      match(TokenType.If);
      match(TokenType.LeftParen);
      Expression test = expression();
      match(TokenType.RightParen);
      Statement thenbranch = statement(); // if

      System.out.println(token.value());
      while (token.type().equals(TokenType.Else)) {
         match(TokenType.Else);
         if (token.type().equals(TokenType.If)) { // else if
            match(TokenType.If);
            match(TokenType.LeftParen);
            Expression test2 = expression();
            match(TokenType.RightParen);
            Statement elseifbranch = statement();
            Elseif temp = new Elseif(test2, elseifbranch);
            Elif.add(temp);
         } else
            elsebranch = statement();
      }
      return new Conditional(test, thenbranch, Elif, elsebranch);
   }

   private Loop whileStatement() {
      // WhileStatement --> while ( Expression ) Statement
      Statement body;
      Expression test;

      match(TokenType.While); // while
      match(TokenType.LeftParen); // (
      test = expression(); // 조건
      match(TokenType.RightParen); // )
      body = statement(); // 실행부
      return new Loop(test, body); // student exercise
   }

   private For forStatement() {
      // ForStatement --> for([Expression]; [Expression]; [Expression]) Statement

      match(TokenType.For);
      match(TokenType.LeftParen);
      Variable v;
      Statement body1 = null;
      if (token.type().equals(TokenType.Semicolon)) // for( ; 공백인 경우
      {
         body1 = null;
         match(TokenType.Semicolon);
      } else if (token.type().equals(TokenType.Identifier)) // for(i; 또는 for(i=3;
      {
         Type type = null;
         v = new Variable(token.value(), type);
         for (int i = 0; i < locals.size(); i++) {
            if (v.equals(locals.get(i).v)) {
               type = locals.get(i).t;
               break;
            }
         }
         v = new Variable(token.value(), type);
         match(TokenType.Identifier);
         if (token.type().equals(TokenType.Semicolon)) // for(i;
         {
            match(TokenType.Semicolon);
            body1 = new Assignment(v, null, null, null, null, v);
         } else // for(i=1;
            body1 = assignment(v);
      }
      forflag = true;
      Expression body2;
      if (token.type().equals(TokenType.Semicolon))
         body2 = null;
      else
         body2 = expression();
      match(TokenType.Semicolon);

      Statement body3;
      if (token.type().equals(TokenType.RightParen))
         body3 = null;
      else
         body3 = statement();
      forflag = false;

      match(TokenType.RightParen);

      Statement body = statement();
      return new For(body1, body2, body3, body);
   }

   private Expression expression() {
      // Expression --> Conjunction { || Conjunction }
      Expression c = conjunction();
      while (token.type().equals(TokenType.Or)) {
         Operator op = new Operator(match(token.type()));
         Expression e = expression();
         c = new Binary(op, c, e);
      }
      return c; // student exercise
   }

   private Expression conjunction() {
      // Conjunction --> Equality { && Equality }
      Expression eq = equality();
      while (token.type().equals(TokenType.And)) {
         Operator op = new Operator(match(token.type()));
         Expression c = conjunction();
         eq = new Binary(op, eq, c);
      }
      return eq; // student exercise
   }

   private Expression equality() {
      // Equality --> Relation [ EquOp Relation ]
      Expression rel = relation();
      while (isEqualityOp()) {
         Operator op = new Operator(match(token.type()));
         Expression rel2 = relation();
         rel = new Binary(op, rel, rel2);
      }
      return rel; // student exercise
   }

   private Expression relation() {
      // Relation --> Addition [RelOp Addition]
      Expression a = addition();
      while (isRelationalOp()) {
         Operator op = new Operator(match(token.type()));
         Expression a2 = addition();
         a = new Binary(op, a, a2);
      }
      return a; // student exercise
   }

   private Expression addition() {
      // Addition --> Term { AddOp Term }
      Expression e = term();
      while (isAddOp()) {
         Operator op = new Operator(match(token.type()));
         Expression term2 = term();
         e = new Binary(op, e, term2);
      }
      return e;
   }

   private Expression term() {
      // Term --> Factor { MultiplyOp Factor }
      Expression e = factor();
      while (isMultiplyOp()) {
         Operator op = new Operator(match(token.type()));
         Expression term2 = factor();
         e = new Binary(op, e, term2);
      }
      return e;
   }

   private Expression factor() {
      // Factor --> [ UnaryOp | IncrementOp ] Primary [IncrementOp]
      if (isUnaryOp() || isIncrementOp()) {
         Operator op = new Operator(match(token.type()));
         Expression term = primary();
         return new Unary(op, term);
      }
      Expression term = primary();
      if (isIncrementOp()) {
         Operator op = new Operator(match(token.type()));
         return new Increment(term, op);
      } else
         return term;
   }

   private Expression primary() {
      // Primary --> Identifier | Literal | ( Expression )
      // | Type ( Expression )
      Expression e = null;
      if (token.type().equals(TokenType.Identifier)) { // 변수
         Type type = null;
         Variable v = new Variable(token.value(), type);
         token = lexer.next();
         type = declaredType(v);

         if (token.type().equals(TokenType.LeftParen)) // function call
            e = callStatement(v, "p");
         else if (token.type().equals(TokenType.LeftBracket)) { // 배열 넣는 경우 b = a[1];
            if (!isDeclared(v.value()))
               error("identifier is not declared");
            int dimension = getDimension(v);
            if (dimension == 0)
               error("identifier is not array type");

            match(TokenType.LeftBracket); // [
            Expression source = expression(); // 1
            match(TokenType.RightBracket); // ]

            if (token.type().equals(TokenType.LeftBracket)) { // a = b[2][2];
               if (dimension == 1)
                  error("array dimension error");
               match(TokenType.LeftBracket); // [
               Expression source2 = expression(); // 1
               match(TokenType.RightBracket); // ]
               if (type == Type.INTARRAY)
                  type = Type.INT;
               if (type == Type.FLOATARRAY)
                  type = Type.FLOAT;
               if (type == Type.BOOLARRAY)
                  type = Type.BOOL;
               if (type == Type.STRARRAY)
                  type = Type.STR;
               v = new Variable(v.value(), type);
               e = new Array(v, source, source2);
            } else { // a = b[2];
               if (dimension == 1) { // 1차원 배열인 경우
                  if (type == Type.INTARRAY)
                     type = Type.INT;
                  if (type == Type.FLOATARRAY)
                     type = Type.FLOAT;
                  if (type == Type.BOOLARRAY)
                     type = Type.BOOL;
                  if (type == Type.STRARRAY)
                     type = Type.STR;
                  v = new Variable(v.value(), type);
                  e = new Array(v, source, null);
               } else { // 2차원 배열인 경우
                  v = new Variable(v.value(), type);
                  e = new Array(v, source, null);
               }
            }
            //////////////////////// source를 넘겨줄 방법이 없당
         } else { // 일반 변수
            if (!isDeclared(v.value()))
               error("identifier is not declared");
            e = new Variable(v.value(), type);
         }
      } else if (isLiteral()) { // literal 상수
         e = literal();
      } else if (token.type().equals(TokenType.LeftParen)) { // ( expression )
         token = lexer.next();
         Expression t = expression();
         e = new SmallBlock(t);
         match(TokenType.RightParen);
      } else if (isType()) { //
         Operator op = new Operator(match(token.type()));
         match(TokenType.LeftParen);
         Expression term = expression();
         match(TokenType.RightParen);
         e = new Unary(op, term);
      } else
         error("Identifier | Literal | ( | Type");

      return e;
   }

   private Type declaredType(Variable id) {
      for (int i = 0; i < locals.size(); i++) {
         String v = new String("" + locals.get(i).v);
         if (v.equals(id.value()))
            return locals.get(i).t;
      }
      return null;
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

   private boolean isDeclared(String id) { ////// 11.09 현재 변수가 선언이 되었는가 판단하는 함수
      for (int i = 0; i < locals.size(); i++) {
         String v = new String("" + locals.get(i).v);
         if (v.equals(id))
            return true;
      }
      for (int i = 0; i < parameters.size(); i++) // 파라미터에 있으면 true
      {
         String v = new String("" + parameters.get(i).v);
         if (v.equals(id))
            return true;
      }
      for (int i = 0; i < fs.size(); i++) {
         String v = new String("" + fs.get(i).v);
         if (v.equals(id)) // 함수가 선언되어있으면 true
            return true;
      }
      return false; // for문을 전부 돌고 나와도 없으므로 선언되지 않음. -> false
   }

   private Value literal() { // 11.09 assignment가 literal일 때 type 설정해주는 코드 추가함
      Value value = null;
      String stval = token.value();

      if (token.type().equals(TokenType.IntLiteral)) {
         value = new IntValue(Integer.parseInt(stval));
         token = lexer.next();
      } else if (token.type().equals(TokenType.FloatLiteral)) {
         value = new FloatValue(Float.parseFloat(stval));
         token = lexer.next();
      } else if (token.type().equals(TokenType.StrLiteral)) {
         value = new StrValue(stval);
         token = lexer.next();
      } else if (token.type().equals(TokenType.True)) {
         value = new BoolValue(true);
         token = lexer.next();
      } else if (token.type().equals(TokenType.False)) {
         value = new BoolValue(false);
         token = lexer.next();
      } else if (token.type().equals(TokenType.Null)) {
         value = new NullValue(null);
         token = lexer.next();
      } else
         error("Error in literal value construction");
      return value; // student exercise
   }

   private boolean isAddOp() {
      return token.type().equals(TokenType.Plus) || token.type().equals(TokenType.Minus);
   }

   private boolean isIncrementOp() {
      return token.type().equals(TokenType.Decrease) || token.type().equals(TokenType.Increase);
   }

   private boolean isMultiplyOp() {
      return token.type().equals(TokenType.Multiply) || token.type().equals(TokenType.Divide)
            || token.type().equals(TokenType.Remainder);
   }

   private boolean isUnaryOp() {
      return token.type().equals(TokenType.Not) || token.type().equals(TokenType.Minus);
   }

   private boolean isEqualityOp() {
      return token.type().equals(TokenType.Equals) || token.type().equals(TokenType.NotEqual);
   }

   private boolean isRelationalOp() {
      return token.type().equals(TokenType.Less) || token.type().equals(TokenType.LessEqual)
            || token.type().equals(TokenType.Greater) || token.type().equals(TokenType.GreaterEqual);
   }

   private boolean isType() {
      return token.type().equals(TokenType.Int) || token.type().equals(TokenType.Bool)
            || token.type().equals(TokenType.Float) || token.type().equals(TokenType.String);
   }

   private boolean isLiteral() {
      return token.type().equals(TokenType.IntLiteral) || isBooleanLiteral()
            || token.type().equals(TokenType.FloatLiteral) || token.type().equals(TokenType.StrLiteral)
            || token.type().equals(TokenType.Null);
   }

   private boolean isBooleanLiteral() {
      return token.type().equals(TokenType.True) || token.type().equals(TokenType.False);
   }

   private boolean isComma() { // ,
      return token.type().equals(TokenType.Comma);
   }

   private boolean isSemicolon() { // ;
      return token.type().equals(TokenType.Semicolon);
   }

   private boolean isLeftBrace() { // {
      return token.type().equals(TokenType.LeftBrace);
   }

   private boolean isStatement() { // ;, {, if, while, assign
      return isSemicolon() || isLeftBrace() || token.type().equals(TokenType.If)
            || token.type().equals(TokenType.While) || token.type().equals(TokenType.For)
            || token.type().equals(TokenType.Identifier) || token.type().equals(TokenType.YesIn)
            || token.type().equals(TokenType.YesOut) || token.type().equals(TokenType.Return)
            || token.type().equals(TokenType.Break) || isIncrementOp();
   }

   public boolean opCheck(Operator op) { // == != > >= < <= && ||
      if (op.equals("==") || op.equals("!=") || op.equals(">") || op.equals(">=") || op.equals("<") || op.equals("<=")
            || op.equals("&&") || op.equals("||"))
         return true;
      return false;
   }

   public static void main(String args[]) {
      Parser parser = new Parser(new Lexer("testcase.txt"));
      Program prog = parser.program();
      prog.display(); // display abstract syntax tree
   } // main

} // Parser