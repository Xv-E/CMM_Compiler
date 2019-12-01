package execute;
import java.io.*;
import java.util.*;

import structure.Arrow;
import structure.Callback;
import structure.Pattern;
import structure.State;
import structure.Token;
import tool.Constants;
import tool.Constants.TAG;
public class Lexical_Analysis {
	static int back = 0;
	static Token cur_token;
	static int row = 0,col = 1;
	static Constants.TAG kind = null;
	static String error_msg = "";
	static List<Token> tokens = new ArrayList<>();
	static String buffers = "";
	static List<Pattern<Character>> pattens = new ArrayList<>();
	
	// 设置类型回调
	static class kind_callback implements Callback{
		TAG mykind;
		public kind_callback(TAG kind){
			mykind = kind;
		}
		public void func() {
			kind = mykind;
		}
	}
	
	//符号(单个)
	static Pattern<Character> symbol_partten() {
		State<Character> a = new State<Character>();
		State<Character> b = new State<Character>();
		
		Arrow<Character> a_b = new Arrow<Character>(b);
		a_b.demand.addAll(Constants.symbols);
		a.add_arrow(a_b);
		
		b.when_end(new Callback() {
			public void func() {
				//cur_token = new Token(buffers.toString(),Constants.get_symbol_kind(buffers),row,col);
				kind = Constants.get_symbol_kind(buffers);
			}
		});
				
		Pattern<Character> pattern = new Pattern<Character>(a);
		return pattern;
	}
	
	//符号(多个)
	static Pattern<Character> mulit_symbol_partten() {
		State<Character> a = new State<Character>();
		
		State<Character> b = new State<Character>();
		State<Character> c = new State<Character>();
		Arrow<Character> a_b = new Arrow<Character>(b);
		a_b.demand.add('=');
		a.add_arrow(a_b);
		Arrow<Character> b_c = new Arrow<Character>(c);
		b_c.demand.add('=');
		b.add_arrow(b_c);

		State<Character> d = new State<Character>();
		State<Character> e = new State<Character>();
		State<Character> f = new State<Character>();
		Arrow<Character> a_d = new Arrow<Character>(d);
		a_d.demand.add('<');
		a.add_arrow(a_d);
		Arrow<Character> d_e = new Arrow<Character>(e);
		d_e.demand.add('=');
		d.add_arrow(d_e);
		Arrow<Character> d_f = new Arrow<Character>(f);
		d_f.demand.add('>');
		d.add_arrow(d_f);
		
		State<Character> g = new State<Character>();
		State<Character> h = new State<Character>();
		Arrow<Character> a_g= new Arrow<Character>(g);
		a_g.demand.add('>');
		a.add_arrow(a_g);
		Arrow<Character> g_h = new Arrow<Character>(h);
		g_h.demand.add('=');
		g.add_arrow(g_h);
		
		State<Character> i = new State<Character>();
		State<Character> j = new State<Character>();
		Arrow<Character> a_i= new Arrow<Character>(i);
		a_i.demand.add('&');
		a.add_arrow(a_i);
		Arrow<Character> i_j = new Arrow<Character>(j);
		i_j.demand.add('&');
		i.add_arrow(i_j);
		
		State<Character> k = new State<Character>();
		State<Character> l = new State<Character>();
		Arrow<Character> a_k= new Arrow<Character>(k);
		a_k.demand.add('|');
		a.add_arrow(a_k);
		Arrow<Character> k_l = new Arrow<Character>(l);
		k_l.demand.add('|');
		k.add_arrow(k_l);
		
		c.when_end(new kind_callback(Constants.TAG.EQUAL));
		f.when_end(new kind_callback(Constants.TAG.NEQUAL));		
		e.when_end(new kind_callback(Constants.TAG.LT_EQUAL));		
		h.when_end(new kind_callback(Constants.TAG.GT_EQUAL));		
		j.when_end(new kind_callback(Constants.TAG.AND));		
		l.when_end(new kind_callback(Constants.TAG.OR));		
		Pattern<Character> pattern = new Pattern<Character>(a);
		return pattern;
	}
	
	//数
	static Pattern<Character> number_partten() {
		State<Character> a = new State<Character>();
		State<Character> c = new State<Character>();
		State<Character> d = new State<Character>();
		State<Character> e = new State<Character>();
		State<Character> f = new State<Character>();

		Arrow<Character> to_c = new Arrow<Character>(c);
		to_c.demand.addAll(Constants.numbers);
		a.add_arrow(to_c);
		c.add_arrow(to_c);
		
		Arrow<Character> c_d = new Arrow<Character>(d);
		c_d.demand.add('_');
		c_d.demand.addAll(Constants.letters);
		c.add_arrow(c_d);
		
		Arrow<Character> to_d = new Arrow<Character>(d);
		to_d.demand.add('.');
		to_d.demand.add('_');
		to_d.demand.addAll(Constants.letters);
		f.add_arrow(to_d);
		e.add_arrow(to_d);
		
		Arrow<Character> d_d = new Arrow<Character>(d);
		d_d.demand.addAll(Constants.numbers);
		d_d.demand.addAll(Constants.letters);
		d_d.demand.add('_');
		d_d.demand.add('.');
		d.add_arrow(d_d);
		
		Arrow<Character> c_e = new Arrow<Character>(e);
		c_e.demand.add('.');
		c.add_arrow(c_e);
		
		Arrow<Character> to_f = new Arrow<Character>(f);
		to_f.demand.addAll(Constants.numbers);
		e.add_arrow(to_f);
		f.add_arrow(to_f);
		
		c.when_end(new kind_callback(Constants.TAG.CONST_INT));
		e.when_end(new kind_callback(Constants.TAG.CONST_REAL));
		f.when_end(new kind_callback(Constants.TAG.CONST_REAL));
		d.when_end(new Callback() {
			public void func() {
				error_msg = "错误的数字格式";
				kind = Constants.TAG.ERROR;
			}
		});
		Pattern<Character> pattern = new Pattern<Character>(a);
		return pattern;
	}
	
	//标识符
	static Pattern<Character> identifier_partten() {
		State<Character> a = new State<Character>();
		State<Character> b = new State<Character>();
		State<Character> c = new State<Character>();
		State<Character> d = new State<Character>();

		Arrow<Character> a_b = new Arrow<Character>(b);
		a_b.demand.addAll(Constants.letters);
		a.add_arrow(a_b);
		
		Arrow<Character> a_c = new Arrow<Character>(c,new Callback() {
			public void func() {
				error_msg = "标识符以下划线开头，必须以字母开头！";
				kind = Constants.TAG.ERROR;
			}
		});
		a_c.demand.add('_');
		a.add_arrow(a_c);
		
		Arrow<Character> to_d = new Arrow<Character>(d);
		to_d.demand.addAll(Constants.numbers);
		b.add_arrow(to_d);
		c.add_arrow(to_d);
		d.add_arrow(to_d);
		
		Arrow<Character> to_c = new Arrow<Character>(c);
		to_c.demand.add('_');
		b.add_arrow(to_c);
		c.add_arrow(to_c);
		d.add_arrow(to_c);
		
		Arrow<Character> to_b = new Arrow<Character>(b);
		to_b.demand.addAll(Constants.letters);
		b.add_arrow(to_b);
		c.add_arrow(to_b);
		d.add_arrow(to_b);
		
		//在c状态退出 == 下划线结束
		c.when_end(new Callback() {
			public void func() {
				error_msg = "标识符不能以下划线结束！";
				kind = Constants.TAG.ERROR;
			}
		});
		
		b.when_end(new kind_callback(Constants.TAG.IDENTIFIER));
		d.when_end(new kind_callback(Constants.TAG.IDENTIFIER));
		
		Pattern<Character> pattern = new Pattern<Character>(a);
		return pattern;
	}
	
	//char常量
	static Pattern<Character> char_partten() {
		State<Character> a = new State<Character>();
		State<Character> b = new State<Character>();
		State<Character> c = new State<Character>();
		State<Character> d = new State<Character>();
		State<Character> e = new State<Character>();
		
		Arrow<Character> a_b = new Arrow<Character>(b);
		a_b.demand.add('\'');
		a.add_arrow(a_b);
		
		Arrow<Character> to_c = new Arrow<Character>(c);
		to_c.reverse_demand = true;
		to_c.demand.add('\'');
		b.add_arrow(to_c);
		e.add_arrow(to_c);
		
		Arrow<Character> b_e = new Arrow<Character>(e);
		b_e.demand.add('\\');
		b.add_arrow(b_e);
		
		Arrow<Character> c_d = new Arrow<Character>(d,new kind_callback(Constants.TAG.CONST_CHAR));
		c_d.demand.add('\'');
		c.add_arrow(c_d);
		
		b.when_end(new Callback() {
			public void func() {
				error_msg = "CHAR为空";
				kind = Constants.TAG.ERROR;
			}
		});
		c.when_end(new Callback() {
			public void func() {
				error_msg = "无效的CHAR";
				kind = Constants.TAG.ERROR;
			}
		});
		
		Pattern<Character> pattern = new Pattern<Character>(a);
		return pattern;
	}
	
	//string常量
	static Pattern<Character> string_partten() {
		State<Character> a = new State<Character>();
		State<Character> b = new State<Character>();
		State<Character> c = new State<Character>();
		State<Character> d = new State<Character>();

		Arrow<Character> a_b = new Arrow<Character>(b);
		a_b.demand.add('"');
		a.add_arrow(a_b);
		

		Arrow<Character> b_b = new Arrow<Character>(b);
		b_b.reverse_demand = true;
		b_b.demand.add('"');
		b.add_arrow(b_b);
		
		Arrow<Character> b_c = new Arrow<Character>(c,new kind_callback(Constants.TAG.STRING));
		b_c.demand.add('"');
		b.add_arrow(b_c);

		Pattern<Character> pattern = new Pattern<Character>(a);
		return pattern;
	}
	
	//注释
	static Pattern<Character> comment_partten() {
		State<Character> a = new State<Character>();
		//多行注释 状态
		State<Character> b = new State<Character>();
		State<Character> c = new State<Character>();
		State<Character> d = new State<Character>();
		State<Character> e = new State<Character>();
		//单行注释 状态
		State<Character> f = new State<Character>();
		
		Arrow<Character> a_b = new Arrow<Character>(b);
		a_b.demand.add('/');
		a.add_arrow(a_b);
		
		Arrow<Character> b_c = new Arrow<Character>(c);
		b_c.demand.add('*');
		b.add_arrow(b_c);
		
		Arrow<Character> c_d = new Arrow<Character>(d);
		c_d.demand.add('*');
		c.add_arrow(c_d);
		
		Arrow<Character> d_e = new Arrow<Character>(e);
		d_e.demand.add('/');
		d.add_arrow(d_e);
		
		Arrow<Character> to_c = new Arrow<>(c);
		to_c.demand.add('*');
		to_c.reverse_demand = true;
		d.add_arrow(to_c);
		c.add_arrow(to_c);
		
		Arrow<Character> b_f = new Arrow<>(f);
		b_f.demand.add('/');
		b.add_arrow(b_f);
		
		Arrow<Character> f_f = new Arrow<>(f);
		f_f.demand.add('\n');
		f_f.reverse_demand = true;
		f.add_arrow(f_f);
		
		c.when_end(new kind_callback(Constants.TAG.LEFT));
		
		e.when_end(new kind_callback(Constants.TAG.RIGHT));
		
		f.when_end(new kind_callback(Constants.TAG.ROW));
		Pattern<Character> pattern = new Pattern<Character>(a);
		return pattern;
	}
	
	public static List<Token> execute() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("D:\\test.txt"));
		String str;
		
		pattens.add(mulit_symbol_partten());
		pattens.add(symbol_partten());
		pattens.add(comment_partten());
		pattens.add(identifier_partten());
		pattens.add(number_partten());
		pattens.add(char_partten());
		pattens.add(string_partten());
		
		System.out.println("词法分析错误：");
		int end_pattern = 0;
		boolean begin = false; // pattern还未开始循环
		//按行读取文件
		while((str = br.readLine())!=null){
			row++;
			str+='\n';
			//遍历行字符串
			for(int i=0;i<str.length();i++) {
				//如果pattern还没开始遍历 跳过空白符
				if(!begin&&(str.charAt(i)==' '||Constants.blanks.contains(str.charAt(i)))) {
					if(str.charAt(i) == '\n')
						col=1;
					continue;
				}
				begin = true;
				boolean match = false;
				//遍历patterns
				for(Pattern<Character> pattern : pattens) {
					if(!pattern.disable)
						if(!pattern.next_state(str.charAt(i)))
							end_pattern++;
					//所有map都结束了，取最后一个识别的token
					if(end_pattern==pattens.size()) {
						//kind为空是无法识别的情况
						if(kind!=null) {
							//注释 空白符跳过
							if(kind==Constants.TAG.ROW||kind==Constants.TAG.RIGHT
									||kind==Constants.TAG.LEFT||kind==Constants.TAG.BLANK);
							else if(error_msg.length()>0) {
								System.out.println(row+"行"+col+"列："+error_msg+" "+buffers);
								error_msg="";
							}
							else {
								// 检测保留字
								if(kind == TAG.IDENTIFIER) {
									Constants.TAG _kind = Constants.get_reserved(buffers);
									kind = _kind==null?kind:_kind;
								}
								// 去引号
								else if(kind == TAG.CONST_CHAR || kind == TAG.STRING) {
									buffers = buffers.substring(1,buffers.length()-1);
								}
								Token token = new Token(buffers, kind, row, col);
								tokens.add(token);
								//cur_token = null;
							}
							i--;
						}
						else {
							buffers+=str.charAt(i);
							System.out.println(row+"行"+col+"列："+"无法识别的字符:"+" "+buffers);
						}
						//为下次循环做初始化工作
						kind = null;
						match = true;
						begin = false;
						col = i+2;
						buffers = "";
						end_pattern = 0;
						for(Pattern<Character> m :pattens) {
							m.reset();
						}
						break;
					}
				}
				//字符已经被识别
				if(!match)
					buffers+=str.charAt(i);
			}
		}
		System.out.println("========================");
		return tokens;

	}
}
