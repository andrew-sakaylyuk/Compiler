//клас токена для зарезервованих слів та ідентифікаторів
public class Word extends Token {
	public String lexeme = "";
	public Word(String s, int tag) { super(tag); lexeme = s; }
	public String toString() { 
		return lexeme;
	}

	public static final Word
	and = new Word( "&&", Tag.AND ),  or = new Word( "||", Tag.OR ),
	eq  = new Word( "==", Tag.EQ  ),  inc = new Word( "++", Tag.INC ),
	Var = new Word( "var",   Tag.BASIC ), Const = new Word( "const", Tag.BASIC );
}
