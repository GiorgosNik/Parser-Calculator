import java.io.InputStream;
import java.io.IOException;

/*The grammar of the problem, it's FIRST and FOLLOW sets
and the derived LOOKAHEAD table*/

/*
*   #1  exp    -> term exp2
*   #2  exp2   -> ^ term exp2
*   #3         | ε
*   #4  term   -> factor term2
*   #5  term2  -> & factor term2
*   #6         | ε
*   #7  factor -> (exp)
*   #8         | num
*   #9  num    -> 0
*   #10        | 1
*   #11        | 2
*   #12        | 3
*   #13        | 4
*   #14        | 5
*   #15        | 6
*   #16        | 7
*   #17        | 8
*   #18        | 9
*
*   ###FIRST SETS###
*   FIRST(exp) = {(, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
*   FIRST(exp2) = {^, ε}
*   FIRST(term) = {(, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
*   FIRST(term2) = {&, ε}
*   FIRST(factor) = {(, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
*   FIRST(num) = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
*
*   ### FOLLOW SETS###
*   FOLLOW(exp) = {), $ }
*   FOLLOW(exp2) = {), $}
*   FOLLOW(term) = {^, ), $}
*   FOLLOW(term2) = {^, ), $}
*   FOLLOW(factor) = {&, ^, ), $}
*   FOLLOW(num) = {&, ^, ), $}
*
*   LOOKUP TABLE
*                0-9     ^      &      (     )       $
*   exp         #1      ERR    ERR    #1    ERR     ERR
*   exp2        ERR     #2     ERR    ERR   #3      #3
*   term        #4      ERR    ERR    #4    ERR     ERR
*   term2       ERR     #6     #5     ERR   #6      #6
*   factor      #8      ERR    ERR    #7    ERR     ERR
*   num         #9-18   ERR    ERR    ERR   ERR     ERR
*
* ---------------------------------------------------------------------------------------------------
* 	        |     '0' .. '9'     |  '^'         |       '&'       |    '('      |   ')'     |   $   |
* ---------------------------------------------------------------------------------------------------
* 	        |		             |	            |	              |             |           |       |
* exp       |    term exp2       |  error       |     error       |  term exp2  |   error   | error |
*           | 	   	             |	            |    	          |             |           |       |
* ---------------------------------------------------------------------------------------------------
* 	        |		             |	            |	              |             |           |       |
* exp2      |     error          | ^ term exp2  |     error       |    error    |     ε     |   ε   |
*           | 	   	             |	            |    	          |             |           |       |
* ---------------------------------------------------------------------------------------------------
* 	        |		             |	            |	              |             |           |       |
* term      |   factor term2     |    error     |     error       |factor term2 |   error   | error |
*           | 	   	             |	            |    	          |             |           |       |
* ---------------------------------------------------------------------------------------------------
* 	        |		             |	            |	              |             |           |       |
* term2     |      error         |      ε       | & factor term2  |    error    |     ε     |   ε   |
*           | 	   	             |	            |    	          |             |           |       |
* ---------------------------------------------------------------------------------------------------
* 	        |		             |	            |	              |             |           |       |
* factor    |       num          |    error     |     error       |   ( exp )   |  error    | error |
*           | 	   	             |	            |    	          |             |           |       |
* ---------------------------------------------------------------------------------------------------
* 	        |		             |	            |	              |             |           |       |
* num       |    '0' .. '9'      |    error     |     error       |    error    |  error    | error |
*           | 	   	             |	            |    	          |             |           |       |
* ---------------------------------------------------------------------------------------------------

*/
class AND_XOR_Evaluator {
    private final InputStream in;
    private int lookahead;

    private int exp() throws IOException, ParseError {
        int value;
        if (isDigit(lookahead) || lookahead == '(') {   // If we get a number or a parentheses continue
            value = term();
            return exp2(value);
        }       // Else we throw error
        throw new ParseError();
    }

    private int exp2(int value) throws IOException, ParseError {
        if(lookahead == ('^')){ //If we get XOR operator, get right-hand side term and calculate result
            consume(lookahead);
            int rightPart = term();
            return value ^ exp2(rightPart);
        }else if(lookahead == -1 || lookahead == '\n'|| lookahead == ')'){  // If the expression is empty return
            return value;
        }
        throw new ParseError();
    }

    private int term() throws IOException, ParseError {
        int value;
        if (isDigit(lookahead) ||lookahead == '(') {    // If we get a number or a parentheses continue
            value = factor();
            return term2(value);
        }
        throw new ParseError();
    }

    private int term2(int value) throws IOException, ParseError {
        if(lookahead == ('&')){ //If we get AND operator, get right-hand side term and calculate result
            consume(lookahead);
            int rightPart = factor();
            return value & term2(rightPart);
        }else if(lookahead == -1 || lookahead == '\n' || lookahead =='^' || lookahead == ')'){  // If the expression is empty return
            return value;
        }
        throw new ParseError();
    }

    private int factor() throws IOException, ParseError {
        if (isDigit(lookahead)) {   //If we get number continue
            return num();
        }else if(lookahead == '('){ //Else if a parentheses open, calculate internal expression
            consume(lookahead);
            int value = exp();
            if(lookahead == ')'){   //Check for closing parentheses
                consume(lookahead);
                return value;
            }
        }
        throw new ParseError();
    }

    private int num() throws IOException, ParseError {
        if (isDigit(lookahead)) {   // Return numerical value
            int number = evalDigit(lookahead);
            consume(lookahead);
            return number;
        }
        throw new ParseError();
    }

    public AND_XOR_Evaluator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    public int eval() throws IOException, ParseError {
        int value = exp();

        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();

        return value;
    }

}
