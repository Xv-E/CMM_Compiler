package tool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Constants{
	public enum TAG {
		CONST_INT,
		CONST_REAL,
		CONST_CHAR,
		STRING,
		IDENTIFIER,
		/* 运算符 */
		AND,
		OR,
		BIT_AND,
		BIT_OR,
		PLUS, // +
		MINUS, // - 
		NEGATIVE,
		DIVIDE, // /
		TIMES, // *
		LT, // <
		GT, // >
		EQUAL, // =
		NEQUAL, // <>
		GT_EQUAL, 
		LT_EQUAL,
		ASSIGN, // = 
		/* 保留字 */
		READ,
		WRITE,
		WHILE,
		IF,
		FOR,
		ELSE,
		INT,
		REAL,
		CHAR,
		TRUE,
		FALSE,
		/* 分隔符*/
		SQ,
		DQ,
		RBRACE,
		LBRACE,
		RPAREN,
		LPAREN,
		RBRACKET,
		LBRACKET,
		COMMA,
		SEMICOLON,
		DOT,
		/* 注释符*/
		ROW,
		LEFT,
		RIGHT,
		/* 错误*/
		ERROR,
		/*空白符*/
		BLANK,
		
		ROOT,
		CONDITION,
		
		PRINT,
		BREAK,
		CONTINUE
		
	}
	
	//符号字典
	static HashMap<String, Constants.TAG> symbol_dict = new HashMap<>();
	static {
		symbol_dict.put("{",TAG.LBRACE);
		symbol_dict.put("}",TAG.RBRACE);
		symbol_dict.put("\"",TAG.DQ);
		symbol_dict.put("'",TAG.SQ);
		symbol_dict.put(")",TAG.RPAREN);
		symbol_dict.put("(",TAG.LPAREN);
		symbol_dict.put("]",TAG.RBRACKET);
		symbol_dict.put("[",TAG.LBRACKET);
		symbol_dict.put(",",TAG.COMMA);
		symbol_dict.put(";",TAG.SEMICOLON);
		symbol_dict.put(".",TAG.DOT);
		
		symbol_dict.put("&",TAG.BIT_AND);
		symbol_dict.put("|",TAG.BIT_OR);
		symbol_dict.put("+",TAG.PLUS);
		symbol_dict.put("-",TAG.MINUS);
		symbol_dict.put("*",TAG.TIMES);
		symbol_dict.put("/",TAG.DIVIDE);
		symbol_dict.put(">",TAG.GT);
		symbol_dict.put("<",TAG.LT);
		symbol_dict.put("==",TAG.EQUAL);
		symbol_dict.put("<>",TAG.NEQUAL);
		symbol_dict.put("=",TAG.ASSIGN);

		symbol_dict.put("//",TAG.ROW);
		symbol_dict.put("/*",TAG.LEFT);
		symbol_dict.put("*/",TAG.RIGHT);
	}
	
	public static Constants.TAG get_symbol_kind(String s){
		return symbol_dict.get(s);
	}
	
	//关键字字典
	static HashMap<String, Constants.TAG> reserved_dict = new HashMap<>();
	static {
		symbol_dict.put("read",TAG.READ);
		symbol_dict.put("write",TAG.WRITE);
		symbol_dict.put("while",TAG.WHILE);
		symbol_dict.put("print",TAG.PRINT);
		symbol_dict.put("if",TAG.IF);
		symbol_dict.put("for",TAG.FOR);
		symbol_dict.put("else",TAG.ELSE);
		symbol_dict.put("int",TAG.INT);
		symbol_dict.put("real",TAG.REAL);
		symbol_dict.put("char",TAG.CHAR);
		symbol_dict.put("true",TAG.TRUE);
		symbol_dict.put("false",TAG.FALSE);
		symbol_dict.put("break",TAG.BREAK);
		symbol_dict.put("continue",TAG.CONTINUE);
	}
	public static Constants.TAG get_reserved(String s){
		return symbol_dict.get(s);
	}
	
	//运算符优先级字典
	static HashMap<Constants.TAG,Integer> operator_priority = new HashMap<>();
	static {
		operator_priority.put(TAG.NEGATIVE,10); // 单目运算 负号优先运算
		
		operator_priority.put(TAG.PLUS,2);
		operator_priority.put(TAG.MINUS,2);
		operator_priority.put(TAG.DIVIDE,3);
		operator_priority.put(TAG.TIMES,3);
		operator_priority.put(TAG.LT,1);
		operator_priority.put(TAG.GT,1);
		operator_priority.put(TAG.LT_EQUAL,1);
		operator_priority.put(TAG.GT_EQUAL,1);
		operator_priority.put(TAG.EQUAL,1);
		operator_priority.put(TAG.NEQUAL,1);
		
		operator_priority.put(TAG.BIT_AND,0);
		operator_priority.put(TAG.BIT_OR,0);
		operator_priority.put(TAG.AND,0);
		operator_priority.put(TAG.OR,0);
		
		operator_priority.put(TAG.LPAREN,-1);
	}
	public static int get_operator_priority(TAG t){
		return operator_priority.get(t);
	}
	
	//常用集合
	static public List<TAG> SEPARARTOR = Arrays.asList(TAG.DQ,TAG.RBRACE,TAG.LBRACE,TAG.RPAREN,TAG.LPAREN,TAG.RBRACKET,TAG.LBRACKET,TAG.COMMA,TAG.SEMICOLON);
	// 双目
	static public List<TAG> OPERATOR = Arrays.asList(TAG.PLUS,TAG.MINUS,TAG.DIVIDE,TAG.TIMES,TAG.LT,TAG.GT,TAG.EQUAL,TAG.NEQUAL,TAG.GT_EQUAL,TAG.LT_EQUAL
			,TAG.AND,TAG.OR,TAG.BIT_AND,TAG.BIT_OR);
	// 单目
	static public List<TAG> MONOCULAR = Arrays.asList(TAG.NEGATIVE);
	static public List<TAG> COMMENT= Arrays.asList(TAG.ROW,TAG.LEFT,TAG.RIGHT);
	static public List<TAG> CONST_NUM = Arrays.asList(TAG.CONST_INT,TAG.CONST_REAL);
	static public List<TAG> COMPARISON = Arrays.asList(TAG.LT,TAG.GT,TAG.EQUAL,TAG.NEQUAL,TAG.GT_EQUAL,TAG.LT_EQUAL);
	
	static public List<TAG> CLASS = Arrays.asList(TAG.INT,TAG.REAL,TAG.CHAR);
	static public List<TAG> VAR = Arrays.asList(TAG.CONST_CHAR,TAG.STRING,TAG.CONST_INT,TAG.CONST_REAL,TAG.IDENTIFIER,TAG.READ);
	static public List<TAG> CONST = Arrays.asList(TAG.CONST_CHAR,TAG.STRING,TAG.CONST_INT,TAG.CONST_REAL);
	static public List<TAG> FUNC = Arrays.asList(TAG.PRINT,TAG.WRITE,TAG.READ);
	static public List<TAG> CONTROL = Arrays.asList(TAG.BREAK,TAG.CONTINUE);
	
	static public Set<Character> letters = new HashSet<>();
	static public Set<Character> lowercase = new HashSet<>();
	static public Set<Character> uppercase = new HashSet<>();
	static public Set<Character> numbers = new HashSet<>();
	static public List<Character> symbols = Arrays.asList('{','}','(',')','[',']','"',',','<','>','=','+','-','*','/',';','&','|','.');
	static public List<Character> blanks = Arrays.asList(' ','\n','\t','\0');
	static {
		for(int i = 0;i<26;i++){
			lowercase.add((char)(97+i));
			uppercase.add((char)(65+i));
		}
		letters.addAll(lowercase);
		letters.addAll(uppercase);
		for(int i = 0;i<10;i++) {
			numbers.add((char)(48+i));
		}
	}
	
	static HashMap<TAG, Class> class_dict = new HashMap<>();
	static {
		class_dict.put(TAG.INT,Integer.class);
		class_dict.put(TAG.CONST_INT,Integer.class);
		class_dict.put(TAG.CHAR,Integer.class);
		class_dict.put(TAG.CONST_CHAR,Integer.class);
		class_dict.put(TAG.REAL,Double.class);
		class_dict.put(TAG.CONST_REAL,Double.class);
		class_dict.put(TAG.READ,Integer.class);
		}
	public static Class get_class_by_kind(TAG kind){
		return class_dict.get(kind);
	}
	
	public static Method get_valueOf_by_kind(TAG kind){
		try {
			return class_dict.get(kind).getMethod("valueOf", String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}	

