public enum TokenType {
   Bool, String, Else, False, Float, If, Int, Main, True, While, For, Break, Null, YesIn, YesOut, In, Out, // 11.09.3 in
                                                                                 // : <-, out
                                                                                 // : ->
   Decrease, Increase, Return, // --, ++ eof �������� �տ� �ִ� ������ keyword�� ��

   Eof, LeftBrace, RightBrace, LeftBracket, RightBracket, LeftParen, RightParen, Semicolon, Comma, Assign,
   Equals, Less, LessEqual, Greater, GreaterEqual, Remainder, Not, NotEqual, Plus, Minus, Multiply, Divide, And, Or,
   Identifier, IntLiteral, FloatLiteral, StrLiteral, IntegerArray, FloatArray, StrArray, BoolArray // 11.09.2
                                                                                          // �߰�
}