import java.io.*; import java.util.ArrayList;
 
// парсер (містить по 1 процедурі
// (основаній на граматиці із вилученою лівою рекурсією 
// із початкової граматики) для кожного нетермінала)
public class Parser {
	private Token look;   // попередній перегляд
	Table top = null;    // поточна або верхня таблиця
	static int label = 0; // номер поточної мітки
	static int blockId = 0; // номер поточного блоку
	ArrayList<RenamedId> unPush = new ArrayList<RenamedId>(); // список переприсвоєних змінних у внутрішньому блоці
	public Lexer lex;    // лексичний аналізатор
	public FileWriter out;
	String dataseg = "MODEL SMALL\nSTACK 100h\n\nDATASEG\n";
	String codeseg = "ENDS\n\nCODESEG\nMain:\n\tMOV AX, @data\n\tMOV DS, AX\n\n\t";
	
	public Parser(String inFile, String outFile) throws IOException {
		lex =  new Lexer(inFile);
		out = new  FileWriter(outFile);
		move(); 
	}

	void move() throws IOException { look = lex.scan(); } // сканування чергового символа

	void error(String s) { throw new Error("near line "+Lexer.line+": "+s);  } 

	void match(int t) throws IOException { // перевірка чи сканований символ - t
		if( look.tag == t ) move();        // якщо так, сканування наступного символа
		else { error("syntax error");        // якщо ні, вивід про помилку
		System.exit(0); }
   }
	
	public void program() throws IOException {  
		block(); // program -> block
		out.write(dataseg+codeseg+"\n\tMOV AX,4C00h\n\tINT 21h\nEND Main\nEND"); // вивід асемблерного коду
	}

	void block() throws IOException { 
		blockId++;
		match('{');  
		Table savedTable = top;  
		top = new Table(top);
		while ( look.tag != '}' ) stmt();
		match('}');
		for(int i=unPush.size()-1; i>=0; i--){
			RenamedId thisId = unPush.get(i);
			if( blockId == thisId.blockNum ) {
				codeseg += "POP DX\n\tMOV "+thisId.id+", DL\n\t";
				unPush.remove(i);
			} else if ( blockId > thisId.blockNum ) break;	
		}
		top = savedTable;
		blockId--;
	}

	void decls() throws IOException { 
		while( look.tag == Tag.BASIC ) { 
			Id id = new Id();
			id.type = look.toString();
			match(Tag.BASIC);
			id.lexeme = look.toString();
			boolean initialized = false;
			if ( top.get(look) != null ) {
				codeseg += "MOV DL, "+id.lexeme+"\n\tPUSH DX\n\t";
				unPush.add(new RenamedId(id.lexeme,blockId));
				initialized = true;
			} 
			top.put( look, id );
			if(initialized == false) dataseg += "\t" + look.toString() + " DB ";
			match(Tag.ID);
			match('=');
			if(initialized == false) dataseg += look.toString() + "\n";
			else codeseg += "MOV DL, "+look.toString()+"\n\tMOV "+unPush.get(unPush.size()-1).id+", DL\n\t";
			match(Tag.NUM);
			match(';');
			
		}
	}
	
	void stmt() throws IOException {
		switch( look.tag ) {
	      case ';':
	         move(); break;  
	      case '{':
		     block(); break; 
	      case Tag.BASIC:
	    	  decls(); break;
	      case Tag.IF:
	    	  ifConstr(); break; 
	      case Tag.DO:
	    	  doWhileConstr(); break;  
	      case Tag.WHILE:
	    	  whileConstr(); break;
		  default:
			  rightConstr();	       
		}
	}
	
	void ifConstr() throws IOException {
		match(Tag.IF);
	      match('(');
	      logicExpr();
	      match(')');
	      stmt();
	      if( look.tag == Tag.ELSE ) {
	    	  codeseg += "JMP L"+(label+2)+"\n\t\nL"+(++label)+":\n\t";
	    	  move();
	    	  stmt();
	      }
	      codeseg += "\nL"+ (++label) + ":\n\t";
	}
	
	void whileConstr() throws IOException {
		int doLabel = ++label;
		codeseg += "\nL"+ doLabel + ":\n\t";
		match(Tag.WHILE);
		match('(');
		logicExpr();
		match(')');
		stmt();
		codeseg += "JMP L"+doLabel+"\n\nL"+ (++label) + ":\n\t";	
	}
	
	void doWhileConstr() throws IOException {
		blockId++;
		int doLabel = ++label;
		codeseg += "\nL"+ doLabel + ":\n\t";
		match(Tag.DO);
		Table savedTable = top;  
		top = new Table(top);
		while ( look.tag != Tag.WHILE ) stmt();
		match(Tag.WHILE);
		match('(');
		logicExpr();
		match(')');
		for(int i=unPush.size()-1; i>=0; i--){
			RenamedId thisId = unPush.get(i);
			if( blockId == thisId.blockNum ) {
				codeseg += "POP DX\n\tMOV "+thisId.id+", DL\n\t";
			} else if ( blockId > thisId.blockNum ) break;	
		}
		codeseg += "JMP L"+doLabel+"\n\nL"+ (++label) + ":\n\t";
		for(int i=unPush.size()-1; i>=0; i--){
			RenamedId thisId = unPush.get(i);
			if( blockId == thisId.blockNum ) {
				codeseg += "POP DX\n\tMOV "+thisId.id+", DL\n\t";
				unPush.remove(i);
			} else if ( blockId > thisId.blockNum ) break;	
		}
		top = savedTable;
		blockId--;
	}
	
	void rightConstr() throws IOException {
		Id id = top.get(look);
		  if( id == null ) error(look.toString() + " undeclared");
			if ( id.type.equals("const") ) {
				  error(look.toString() + " is const. You can't redefine it!");
		  }
		  match(Tag.ID);
		  if ( look.tag == Tag.INC ) {
			  codeseg += "INC "+ id.lexeme + "\n\t";
			  move(); return;
		  } else { 
			  match('=');
			  arithmeticExpr(id);
			  codeseg += "MOV "+ id + ", AL\n\t";
			  match(';');
		  } 
	}
	
	void arithmeticExpr(Id id) throws IOException {
		  String op1 = "", op2 = "", op = "";
		  boolean bracket = false;
		  op1 = leftArithmeticExpr(id, op1);
		  rightArithmeticExpr(id, op, op1, op2, bracket);
	}
	
	String leftArithmeticExpr(Id id, String op1) throws IOException {
		boolean constant = false;
		Id lookId = top.get(look);
		if ( look.tag == Tag.NUM || look.tag == Tag.ID ) {
			if (look.tag == Tag.ID ) { 
				if( lookId == null ) error(look.toString() + " undeclared");
				if(lookId.type.equals("const")) constant=true;
			}
			op1 = look.toString(); move();
		} else if ( look.tag == Tag.BASIC ) {
			error("syntax error. You can't declare variable here! I'm tired of your obtusity. Goodbuy, loser!");
			System.exit(0);
		} else error("syntax error. Expected number, variable or constant");
		if ( look.tag == Tag.INC ) {
			if(constant) error(lookId.toString() + " is const. You can't redefine it!");
			op1 = id.lexeme; codeseg += "INC "+ op1 + "\n\t"; move();
		} codeseg += "MOV AL, "+ op1 + "\n\t";
		return op1;
	}
	
	void rightArithmeticExpr(Id id, String op, String op1, String op2, boolean bracket) throws IOException {
		while( look.tag == '+' || look.tag == '-' ) {
			switch( look.tag ) {
				case '+':	
					op = "+"; move(); break;
				case '-':
					op = "-"; move(); break;
			}
			boolean constant = false;
			Id lookId = top.get(look);
			if ( look.tag == Tag.NUM || look.tag == Tag.ID ) {
				if (look.tag == Tag.ID ) { 
					if( lookId == null ) error(look.toString() + " undeclared");
					if(lookId.type.equals("const")) constant=true;
				}
				op2 = look.toString(); move();
			} else if ( look.tag == '(' ) {
				op = brackets(op, id); bracket = true;
			}  else if ( look.tag == Tag.BASIC ) {
				error("syntax error. You can't declare variable here!");
			} else error("syntax error. Expected bracket, number, variable or constant."); 
			if ( look.tag == Tag.INC ) {
				if(constant) error(lookId.toString() + " is const. You can't redefine it!");
				op1 = id.lexeme; codeseg += "INC "+ op1 + "\n\t"; move();
			}  
			switch( op ) {
				case "+":	
					if (bracket == false) 
						codeseg += "MOV BL, "+ op2 + "\n\t";
					else bracket = false;
					codeseg += "ADD AL, BL\n\t"; break;
				case "-":
					if (bracket == false)
						codeseg += "MOV BL, "+ op2 + "\n\t";
					else bracket = false;
					codeseg += "SUB AL, BL\n\t"; break;
			}  	
		}
	}
	
	String brackets(String op, Id id) throws IOException {
		String op1 = "", op2 = "", oldOp = op;
		boolean bracket = false;
		codeseg += "PUSH AX\n\t"; move();
		op1 = leftArithmeticExpr(id, op1);
		rightArithmeticExpr(id, op, op1, op2, bracket); 
		while ( look.tag != ')' ) {
			rightArithmeticExpr(id, op, op1, op2, bracket);	
		} move(); codeseg += "MOV BL, AL\n\tPOP AX\n\t"; return oldOp; 
	}
	
	void logicExpr() throws IOException {
		compareExpr();
	    while( look.tag == Tag.OR || look.tag == Tag.AND ) {
	    	if ( look.tag == Tag.OR ) {
	    		codeseg += "\n\tJMP L"+(label+2)+"\nL"+ (++label) + ":\n\t";
	    		move(); ++label; compareExpr();
	    		codeseg += "\nL"+ label + ":\n\t";
	    	} else {
	    		codeseg += "\n\t";
	    		move(); compareExpr();
	    	}
	    }
	}
	
	void compareExpr() throws IOException {
		String op1 = "", op2 = "";
		String op = "";
		boolean not = false;
		if ( look.tag == '!' ) { not = true; move(); }
		if ( look.tag == Tag.NUM || look.tag == Tag.ID ) {
			if (look.tag == Tag.ID ) { 
				Id lookId = top.get(look);
				if( lookId == null ) error(look.toString() + " undeclared");
			}
			op1 = look.toString(); move();
		} else error("syntax error. Expected number, variable or constant");
		switch( look.tag ) {
			case '>':
				if(not) op = "<="; else op = ">";	
				move(); break;
			case '<':
				if(not) op = ">="; else op = "<";	
				move(); break;
			case Tag.EQ:
				if(not) op = "!="; else op = "=="; 
				move(); break;
			default:
				error("syntax error. Expected compare operation");
		}
		if ( look.tag == Tag.NUM || look.tag == Tag.ID ) {
			if (look.tag == Tag.ID ) {
				Id lookId = top.get(look);
				if( lookId == null ) error(look.toString() + " undeclared");
			}
			op2 = look.toString(); move();
		} else error("syntax error. Expected number, variable or constant");
		codeseg += "MOV AL, " + op1 + "\n\tCMP AL, " + op2 + "\n\t";
		switch( op ) {
		case ">":
			codeseg += "JNG L" + (label+1) + "\n\t"; break;
		case "<":
			codeseg += "JNL L" + (label+1) + "\n\t"; break;
		case "==":
			codeseg += "JNE L" + (label+1) + "\n\t"; break;
		case "!=":
			codeseg += "JE L" + (label+1) + "\n\t"; break;
		case ">=":
			codeseg += "JNGE L" + (label+1) + "\n\t"; break;
		case "<=":
			codeseg += "JNLE L" + (label+1) + "\n\t"; break;
		}
	}
}

class RenamedId { 
	public String id;
	public int blockNum;
	public RenamedId(String myId, int myNum){ id = myId; blockNum = myNum; }
}