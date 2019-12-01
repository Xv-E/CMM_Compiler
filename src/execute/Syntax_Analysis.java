package execute;

import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import structure.Arrow;
import structure.Callback;
import structure.Pattern;
import structure.State;
import structure.Token;
import structure.Tree_Node;
import tool.Constants;
import tool.Constants.TAG;

public class Syntax_Analysis {
	static List<Token> tokens; 
	static Token cur_token = null; // Ŀǰ�����token
	static int i = 0; // ��ѭ��i ��ʾ��ǰλ��
	static boolean accpect = true; // �������øñ���������token��û�б�����
	
	static Tree_Node root_node = new Tree_Node(); // ���ڵ�ĸ��ڵ�
	static Stack<Tree_Node> roots = new Stack<>(); // pattern�б�
	
	// �������͵�pattern
	static Pattern<Token> declare_pattern = declare_pattern();
	static Pattern<Token> assign_pattern = assign_pattern();
	static Pattern<Token> expression_pattern = expression_pattern();
	static Pattern<Token> semicolon_pattern = semicolon_pattern();
	static Pattern<Token> if_pattern = if_pattern();
	static Pattern<Token> for_pattern = for_pattern();
	static Pattern<Token> brace_pattern = brace_pattern();
	static Pattern<Token> while_pattern = while_pattern();
	static Pattern<Token> func_pattern = func_pattern();
	static Pattern<Token> array_pattern = array_pattern();
	static Pattern<Token> control_pattern = control_pattern();
	static Pattern<Token> array_index_pattern = array_index_pattern();
	
	static String error_msg = ""; // ������Ϣ
	static void set_error_msg(String msg) {
		Token pre_tokens = tokens.get(i-1);
		error_msg = pre_tokens.row+"��"+pre_tokens.culomn+"�У�"+msg;
	}
	static void set_error_msg(int r,int c,String msg) {
		error_msg = r+"��"+c+"�У�"+msg;
	}
	// ������Ϣ�ص�
	static class error_callback implements Callback{
		String error_msg;
		public error_callback(String error_msg){
			this.error_msg = error_msg;
		}
		public void func() {
			set_error_msg(error_msg);
		}
	}
	
	// �������ʽ
	static Pattern<Token> expression_pattern(){
		State<Token> a = new State<>();	
		final Pattern<Token> pattern = new Pattern<Token>(a);
		
		// ��ʣ����ŵĳ�ջ��������
		final Callback end1 = new Callback() {
			public void func() {
				Stack<Tree_Node> expression_stack = tier_stack.peek().expression_stack;
				List<Tree_Node> expression_list = tier_stack.peek().expression_list;
				//��ʣ��ĳ�ջ
				while(expression_stack.size()>0) {
					if(expression_stack.peek().getToken().equals(Constants.TAG.LPAREN)) {
						set_error_msg("ȱ��������");
						break;
					}
					expression_list.add(expression_stack.pop());
				}
				// �����������ֶ���������
				Tree_Node tn = get_expression_tree(expression_list);
				tier_stack.peek().tree_node.addAll(tn.children);
				tier_stack.peek().tree_node.setToken(tn.getToken());
			}
		};
		// ���� var
		Arrow<Token> var = new Arrow<>(a,new Callback() {
			public void func() {
				List<Tree_Node> expression_list = tier_stack.peek().expression_list;
				Tree_Node tn = new Tree_Node(cur_token);
				//tn.deep = tier_stack.peek().tree_node.deep;
				expression_list.add(tn);
				// ����Ǳ�ʶ�� ʶ����������
				if(cur_token.kind == TAG.IDENTIFIER && tokens.get(i+1).kind == TAG.LBRACKET) {
					//ʶ����������
					Tier tier = new Tier(array_index_pattern,false);
					tier.tree_node = tn;
					tier_stack.push(tier);
				}
			}
		});
		var.demand.addAll(Constants.VAR);
		a.add_arrow(var);
		
		// ����operator
		Arrow<Token> operator = new Arrow<>(a,new Callback() {
			public void func() {
				Stack<Tree_Node> expression_stack = tier_stack.peek().expression_stack;
				List<Tree_Node> expression_list = tier_stack.peek().expression_list;
				//������ ֱ�ӽ�ջ
				if(cur_token.kind==Constants.TAG.LPAREN) {
					expression_stack.add(new Tree_Node(cur_token));
					tier_stack.peek().paren_court++;
				}
				//������ 
				else if(cur_token.kind==Constants.TAG.RPAREN) {
					tier_stack.peek().paren_court--;
					// ��ûƥ��������ţ��ֶ����˳����� �����Ϊ�˱���Ե�for if while �������� Ҳ����ԸĽ�
					if(tier_stack.peek().paren_court<0) {
						accpect = false;
						end1.func();
						//end2.func();
						return;
					}
					// ��ջ��������
					while(expression_stack.size()>0 && !expression_stack.peek().getToken().equals(Constants.TAG.LPAREN)) {
						expression_list.add(expression_stack.pop());
					}
					if(expression_stack.size()==0)
						set_error_msg("ȱ��������");
					else 
						expression_stack.pop();
				}
				//������������� ������ȼ�С�ڵ���ջ�����ȳ�ջ����ջ 
				else {
					//���� Ҳ�п����Ǹ����ĸ���
					if(cur_token.kind==Constants.TAG.MINUS) {
						//ǰһ��token������� ���� ���Ǳ���(������)  ʶ��Ϊ����
						if(Constants.OPERATOR.contains(tokens.get(i-1).kind) 
								|| (!Constants.VAR.contains(tokens.get(i-1).kind) && !tokens.get(i-1).kind.equals(TAG.RPAREN))) {
							cur_token.kind = Constants.TAG.NEGATIVE;
						}
					}
					while(expression_stack.size() > 0 && //!expression_stack.peek().equals(Constants.TAG.LPAREN) &&
							Constants.get_operator_priority(cur_token.kind)<=Constants.get_operator_priority(expression_stack.peek().getToken().kind)) {
						expression_list.add(expression_stack.pop());
					}
					expression_stack.add(new Tree_Node(cur_token));
				}
			}
		});
		operator.demand.addAll(Constants.OPERATOR);
		operator.demand.add(Constants.TAG.LPAREN);
		operator.demand.add(Constants.TAG.RPAREN);
		a.add_arrow(operator);

		a.when_end(end1);	
		return pattern;
	}
	
	static Tree_Node get_expression_tree(List<Tree_Node> list) {
		if(list.size()==0)
			return new Tree_Node();
		int index = 0;
		try {
			while(index<list.size()) {
				// ����� �������ڵ�
				if(Constants.OPERATOR.contains(list.get(index).getToken().kind)) {
					list.get(index).add(list.get(index-2));
					list.get(index).add(list.get(index-1));
					list.remove(index-1);
					list.remove(index-2);
					index--;
				}
				else if(list.get(index).getToken().kind == Constants.TAG.NEGATIVE) {
					list.get(index).add(list.get(index-1));
					list.remove(index-1);
				}
				else {
					index++;
				}
			}
		}catch(Exception e) {
			set_error_msg(list.get(index).getToken().row,list.get(index).getToken().culomn,"�޷�ƥ��������");
		}
		return list.get(0);
	}
	
	// ��ֵ���
	static Pattern<Token> assign_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		State<Token> c = new State<>();
		//State<Token> f = new State<>();
		//State<Token> g = new State<>();
		
		final Pattern<Token> pattern = new Pattern<Token>(a);
		
		Arrow<Token> a_b = new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.setToken(cur_token);
				//ʶ���������� ���������Ϊ�ӽڵ�
				if(tokens.get(i+1).kind == TAG.LBRACKET) {
					Tier tier = new Tier(array_index_pattern,false);
					tier.tree_node = tier_stack.peek().tree_node;
					//tier_stack.peek().tree_node.add(tier.tree_node);
					tier_stack.push(tier);
				}
			}
		});
		a_b.demand.add(Constants.TAG.IDENTIFIER);
		a.add_arrow(a_b);
		
		Arrow<Token> to_c = new Arrow<>(c,new Callback() {
			public void func() {
				Tree_Node assign = new Tree_Node(cur_token);
				assign.kind = "assign";
				tier_stack.peek().tree_node.add(assign);
				//��ʼ�������ʽʶ�� ���������Ϊ�ӽڵ�
				Tier tier = new Tier(expression_pattern,false);
				assign.add(tier.tree_node);
				tier_stack.pop();
				tier_stack.push(tier);
			}
		});
		to_c.demand.add(Constants.TAG.ASSIGN);
		b.add_arrow(to_c);
		//g.add_arrow(to_c);
		b.when_end(new error_callback("����ĸ�ֵ���"));
		return pattern;
	}
	
	//�������
	static Pattern<Token> declare_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		State<Token> c = new State<>();
		State<Token> d = new State<>();
		State<Token> e = new State<>();
		State<Token> f = new State<>();
		//State<Token> g = new State<>();
		final Pattern<Token> pattern = new Pattern<Token>(a);
		
		Arrow<Token> a_b = new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.setToken(cur_token);
			}
		});
		a_b.demand.addAll(Constants.CLASS);
		a.add_arrow(a_b);
		Arrow<Token> b_c = new Arrow<>(c,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.add(new Tree_Node(cur_token));
			}
		});
		b_c.demand.add(Constants.TAG.IDENTIFIER);
		b.add_arrow(b_c);

		// �Ⱥ� ʶ��ֵ���
		Arrow<Token> to_d = new Arrow<>(d,new Callback() {
			public void func() {
				Tier tier = null;
				// �������һ��token��{��ʶ������
				if(tokens.get(i+1).kind == Constants.TAG.LBRACE)
					tier = new Tier(array_pattern,false);
				else
					tier = new Tier(expression_pattern,false);
				Tree_Node assign = new Tree_Node(cur_token);
				//assign.deep = tier_stack.peek().tree_node.deep;
				assign.kind = "assign";
				assign.add(tier.tree_node);
				Tier declare_tier = tier_stack.peek();
				// ���һ��child���������ı���
				declare_tier.tree_node.getChildAt(declare_tier.tree_node.Children_Size()-1).add(assign);
				tier_stack.push(tier);
			}
		});
		to_d.demand.add(Constants.TAG.ASSIGN);
		c.add_arrow(to_d);
		f.add_arrow(to_d);
		// ������ ʶ������
		Arrow<Token> c_e = new Arrow<>(e);
		c_e.demand.add(Constants.TAG.LBRACKET);
		c.add_arrow(c_e);
		// ����������INT
		Arrow<Token> e_f = new Arrow<>(f,new Callback() {
			public void func() {
				Tier declare_tier = tier_stack.peek();
				declare_tier.tree_node.getChildAt(declare_tier.tree_node.Children_Size()-1).add(new Tree_Node(cur_token));
			}
		});
		e_f.demand.add(Constants.TAG.CONST_INT);
		e.add_arrow(e_f);
		// ����INT�������Ϊ������ʶ��������������һ�����ص�������Ϣ
		
		Arrow<Token> _e_f = new Arrow<>(f,new error_callback("�������������Ҫһ��CONST_INT"));
		_e_f.reverse_demand = true;
		_e_f.demand.add(Constants.TAG.CONST_INT);
		e.add_arrow(_e_f);

		Arrow<Token> f_c = new Arrow<>(c);
		f_c.demand.add(Constants.TAG.RBRACKET);
		f.add_arrow(f_c);
		
		
		// ���ն��ţ��ټ��һ��
		Arrow<Token> to_b = new Arrow<>(b);
		to_b.demand.add(Constants.TAG.COMMA);
		d.add_arrow(to_b);
		c.add_arrow(to_b);
		f.add_arrow(to_b);
		b.when_end(new error_callback("����������,��Ҫһ����ʶ��"));
		return pattern;
	}
	
	// ��������
	static Pattern<Token> array_index_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		//State<Token> c = new State<>();
		final Pattern<Token> pattern = new Pattern<Token>(a);
		Arrow<Token> a_b = new Arrow<>(b,new Callback() {
			public void func() {
				// ʶ��һ�����ʽ
				Tier tier = new Tier(expression_pattern,false);
				tier_stack.peek().tree_node.add(tier.tree_node);;
				tier_stack.push(tier);
			}
		});
		a_b.demand.add(Constants.TAG.LBRACKET);
		a.add_arrow(a_b);
		
		Arrow<Token> b_a = new Arrow<>(a);
		b_a.demand.add(Constants.TAG.RBRACKET);
		b.add_arrow(b_a);
		
		b.when_end(new error_callback("��Ҫ']'"));
		return pattern;
	}
	
	// ���鳣��
	static Pattern<Token> array_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		State<Token> c = new State<>();
		final Pattern<Token> pattern = new Pattern<Token>(a);
		
		Arrow<Token> a_b = new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.kind = "array";
				Tier tier = new Tier(expression_pattern,false);
				tier_stack.peek().tree_node.add(tier.tree_node);
				tier_stack.push(tier);
			}
		});
		a_b.demand.add(Constants.TAG.LBRACE);
		a.add_arrow(a_b);
		
		Arrow<Token> b_b = new Arrow<>(b,new Callback() {
			public void func() {
				Tier tier = new Tier(expression_pattern,false);
				tier_stack.peek().tree_node.add(tier.tree_node);
				tier_stack.push(tier);
			}
		});
		b_b.demand.add(Constants.TAG.COMMA);
		b.add_arrow(b_b);
		
		Arrow<Token> b_c = new Arrow<>(c,new Callback() {
			public void func() {
				tier_stack.pop();
			}
		});
		b_c.demand.add(Constants.TAG.RBRACE);
		b.add_arrow(b_c);
		
		b.when_end(new error_callback("��Ҫ'}'"));
		return pattern;
	}
	
	// �ֺ�
	static Pattern<Token> semicolon_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		final Pattern<Token> pattern = new Pattern<Token>(a);
		Arrow<Token> a_b = new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.pop();
			}
		});
		a_b.demand.add(Constants.TAG.SEMICOLON);
		a.add_arrow(a_b);
		
		a.when_end(new error_callback("���δ��������,��Ҫ';'"));
		return pattern;
	}
	
	// ���ƹؼ��� break continue return������
	static Pattern<Token> control_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		final Pattern<Token> pattern = new Pattern<Token>(a);
		Arrow<Token> a_b = new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.setToken(cur_token);
				tier_stack.pop();
			}
		});
		a_b.demand.addAll(Constants.CONTROL);
		a.add_arrow(a_b);
		return pattern;
	}
	// ��������
	static Pattern<Token> func_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		State<Token> c = new State<>();
		State<Token> d = new State<>();
		
		final Pattern<Token> pattern = new Pattern<Token>(a);
		Arrow<Token> a_b = new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.setToken(cur_token);
				tier_stack.peek().tree_node.kind = "func";
			}
		});
		a_b.demand.addAll(Constants.FUNC);
		a.add_arrow(a_b);
		
		Arrow<Token> b_c = new Arrow<>(c,new Callback() {
			public void func() {
				Tier tier = new Tier(expression_pattern,false);
				tier_stack.peek().tree_node.add(tier.tree_node);
				tier_stack.push(tier);
			}
		});
		b_c.demand.add(Constants.TAG.LPAREN);
		b.add_arrow(b_c);
		
		Arrow<Token> c_d = new Arrow<>(d,new Callback() {
			public void func() {
				tier_stack.pop();
			}
		});
		c_d.demand.add(Constants.TAG.RPAREN);
		c.add_arrow(c_d);
		
		b.when_end(new error_callback("��Ҫ'('"));
		c.when_end(new error_callback("��Ҫ')'"));
		return pattern;
	}
		
	// { }������
	static Pattern<Token> brace_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		State<Token> c = new State<>();
		
		final Pattern<Token> pattern = new Pattern<Token>(a);
		
		Arrow<Token> a_b = new Arrow<>(b,new Callback() {
			public void func() {
				//��ΪĿǰ�ĸ��ڵ�
				tier_stack.peek().tree_node.kind = "root";
				roots.push(tier_stack.peek().tree_node);
			}
		});
		a_b.demand.add(Constants.TAG.LBRACE);
		a.add_arrow(a_b);
		
		Arrow<Token> b_c = new Arrow<>(c,new Callback() {
			public void func() {
				//�˳�������
				roots.pop();
				tier_stack.pop();
			}
		});
		b_c.demand.add(Constants.TAG.RBRACE);
		b.add_arrow(b_c);
		b.when_end(new error_callback("��Ҫ'}'"));
		return pattern;
	}
	
	// IF���
	static Pattern<Token> if_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		State<Token> c = new State<>();
		State<Token> d = new State<>();
		State<Token> e = new State<>();
		final Pattern<Token> pattern = new Pattern<Token>(a);
		
		Arrow<Token> a_b =new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.setToken(cur_token);
				tier_stack.peek().tree_node.kind = "if";
			}
		});
		a_b.demand.add(Constants.TAG.IF);
		a.add_arrow(a_b);
		
		Arrow<Token> b_c = new Arrow<>(c,new Callback() {
			public void func() {
				Tier tier = new Tier(expression_pattern,false);
				tier.tree_node.kind = "condition";
				tier_stack.peek().tree_node.add(tier.tree_node);
				tier_stack.push(tier);
			}
		});
		b_c.demand.add(Constants.TAG.LPAREN);
		b.add_arrow(b_c);
		
		Arrow<Token> c_d =new Arrow<>(d,new Callback() {
			public void func() {
				Tier if_tier = tier_stack.peek();
				if(set_stack_by_head(tokens.get(i+1),false)) {
					if_tier.tree_node.add(tier_stack.peek().tree_node);
				}
			}
		});
		c_d.demand.add(Constants.TAG.RPAREN);
		c.add_arrow(c_d);
		
		Arrow<Token> d_e =new Arrow<>(e,new Callback() {
			public void func() {
				Tier if_tier = tier_stack.peek();
				tier_stack.pop();
				if(set_stack_by_head(tokens.get(i+1),false)) {
					if_tier.tree_node.add(tier_stack.peek().tree_node);
				}
			}
		});
		d_e.demand.add(Constants.TAG.ELSE);
		d.add_arrow(d_e);
		
		b.when_end(new error_callback("��Ҫ'('"));
		c.when_end(new error_callback("��Ҫ')'"));
		return pattern;
	}
	
	// WHILE���
	static Pattern<Token> while_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		State<Token> c = new State<>();
		State<Token> d = new State<>();
		final Pattern<Token> pattern = new Pattern<Token>(a);
		
		Arrow<Token> a_b =new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.setToken(cur_token);
				tier_stack.peek().tree_node.kind = "while";
			}
		});
		a_b.demand.add(Constants.TAG.WHILE);
		a.add_arrow(a_b);
		
		Arrow<Token> b_c = new Arrow<>(c,new Callback() {
			public void func() {
				Tier tier = new Tier(expression_pattern,false);
				tier.tree_node.kind = "condition";
				tier_stack.peek().tree_node.add(tier.tree_node);
				tier_stack.push(tier);
			}
		});
		b_c.demand.add(Constants.TAG.LPAREN);
		b.add_arrow(b_c);
		
		Arrow<Token> c_d =new Arrow<>(d,new Callback() {
			public void func() {
				// ������һ��tokenͷ��tier
				Tier while_tier = tier_stack.peek();
				tier_stack.pop();
				if(set_stack_by_head(tokens.get(i+1),false))
					while_tier.tree_node.add(tier_stack.peek().tree_node);

			}
		});
		c_d.demand.add(Constants.TAG.RPAREN);
		c.add_arrow(c_d);
		
		b.when_end(new error_callback("��Ҫ'('"));
		c.when_end(new error_callback("��Ҫ')'"));
		return pattern;
	}
	
	// FOR���
	static Pattern<Token> for_pattern() {
		State<Token> a = new State<>();
		State<Token> b = new State<>();
		State<Token> c = new State<>();
		State<Token> d = new State<>();
		State<Token> e = new State<>();
		State<Token> f = new State<>();
		final Pattern<Token> pattern = new Pattern<Token>(a);
		
		Arrow<Token> a_b =new Arrow<>(b,new Callback() {
			public void func() {
				tier_stack.peek().tree_node.setToken(cur_token);
				tier_stack.peek().tree_node.kind = "for";
				//��ǰ���ڵ�
				//roots.add(tier_stack.peek().tree_node);
			}
		});
		a_b.demand.add(Constants.TAG.FOR);
		a.add_arrow(a_b);
		
		// ʶ��һ����ֵ���
		final Callback add_front_assign = new Callback() {
			public void func() {
				Tier tier;
				// ʶ��һ����ֵ�������������
				if(Constants.CLASS.contains(tokens.get(i+1).kind)) 
					tier = new Tier(declare_pattern,false);
				else
					tier = new Tier(assign_pattern,false);
				tier_stack.peek().tree_node.add(tier.tree_node);
				tier.tree_node.kind = "front";		
				tier_stack.push(tier);
			}
		};
		// after_assign
		final Callback add_after_assign = new Callback() {
			public void func() {
				// ʶ��һ����ֵ���
				Tier tier = new Tier(assign_pattern,false);
				tier_stack.peek().tree_node.add(tier.tree_node);
				tier.tree_node.kind = "after";
				tier_stack.push(tier);
			}
		};
		
		Arrow<Token> b_c = new Arrow<>(c,add_front_assign);
		b_c.demand.add(Constants.TAG.LPAREN);
		b.add_arrow(b_c);
		
		Arrow<Token> c_d =new Arrow<>(d,new Callback() {
			public void func() {
				// ʶ��һ���������ʽ
				Tier tier = new Tier(expression_pattern,false);
				tier_stack.peek().tree_node.add(tier.tree_node);
				tier.tree_node.kind = "condition";
				tier_stack.push(tier);
			}
		});
		c_d.demand.add(Constants.TAG.SEMICOLON);
		c.add_arrow(c_d);
		
		Arrow<Token> d_e =new Arrow<>(e,add_after_assign);
		d_e.demand.add(Constants.TAG.SEMICOLON);
		d.add_arrow(d_e);
		
		Arrow<Token> e_f =new Arrow<>(f,new Callback() {
			public void func() {
				Tier for_tier = tier_stack.peek();
				tier_stack.pop();
				if(set_stack_by_head(tokens.get(i+1),false))
					for_tier.tree_node.add(tier_stack.peek().tree_node);
			}
		});
		e_f.demand.add(Constants.TAG.RPAREN);
		e.add_arrow(e_f);
		
		b.when_end(new error_callback("��Ҫ'('"));
		c.when_end(new error_callback("��Ҫ';'"));
		d.when_end(new error_callback("��Ҫ';'"));
		e.when_end(new error_callback("��Ҫ')'"));
		
		return pattern;
	}
	
	
	//stack��Ĳ�ṹ��
	static class Tier{	
		Tree_Node tree_node;
        Pattern<Token> pattern;
        State<Token> cur_state;
        int deep;
        
        // �������ʽ��
		int paren_court=0;
		Stack<Tree_Node> expression_stack = new Stack<>();
		List<Tree_Node> expression_list = new ArrayList<>();
		
        Tier(Pattern<Token> pattern,boolean add_to_root){
        	this.pattern = pattern;
        	this.cur_state = pattern.start;
        	tree_node = new Tree_Node();
        	if(add_to_root)
        		roots.peek().add(tree_node);
        	//tree_node.deep = roots.size();
        	this.deep = roots.size();
        }
    }
	
	// ���ݿ�ͷtoken ��ջ�����Tier
	static Stack<Tier> tier_stack = new Stack<>();
	static public boolean set_stack_by_head(Token head,boolean add_to_root) {
		if(head.kind == Constants.TAG.IDENTIFIER) {
			tier_stack.push(new Tier(semicolon_pattern,false));
			tier_stack.push(new Tier(assign_pattern,add_to_root));
		}
		else if(head.kind == Constants.TAG.SEMICOLON) {
			tier_stack.push(new Tier(semicolon_pattern,false));
		}
		else if(Constants.FUNC.contains(head.kind)) {
			tier_stack.push(new Tier(semicolon_pattern,false));
			tier_stack.push(new Tier(func_pattern,add_to_root));
		}
		else if(Constants.CONTROL.contains(head.kind)) {
			tier_stack.push(new Tier(semicolon_pattern,false));
			tier_stack.push(new Tier(control_pattern,add_to_root));
		}
		else if(Constants.CLASS.contains(head.kind)) {
			tier_stack.push(new Tier(semicolon_pattern,false));
			tier_stack.push(new Tier(declare_pattern,add_to_root));
		}
		else if(head.kind == Constants.TAG.LBRACE) {
			tier_stack.push(new Tier(brace_pattern,add_to_root));
		}
		else if(head.kind == Constants.TAG.IF) {
			tier_stack.push(new Tier(if_pattern,add_to_root));
			//roots.push(tier_stack.peek().tree_node);
		}
		else if(head.kind == Constants.TAG.WHILE) {
			tier_stack.push(new Tier(while_pattern,add_to_root));
		}
		else if(head.kind == Constants.TAG.FOR) {
			tier_stack.push(new Tier(for_pattern,add_to_root));
		}
		else {
			return false;
		}
		return true;
	}
	
	
	static public Tree_Node execute() throws IOException {
		tokens = Lexical_Analysis.execute();
		// ��������һ���հ�token����ѭ��
		tokens.add(new Token(null, Constants.TAG.BLANK, tokens.get(tokens.size()-1).row, tokens.get(tokens.size()-1).culomn)); 

		root_node.kind = "root";
		roots.add(root_node);		
		System.out.println("�﷨��������");
		// ѭ����
		boolean wrong_head = false;
		boolean be_handle = false;
		// ����tokens
		while(i<tokens.size()-1) {
			if(be_handle) i++;
			be_handle = false;
			cur_token = tokens.get(i);
			// ����tokenΪstack���pattern
			if(tier_stack.size()==0) {
				set_stack_by_head(cur_token,true);
			}
			//
			else if(tier_stack.peek().deep < roots.size()) {
				Tier tier = tier_stack.peek();
				Pattern<Token> cur_p = tier.pattern;
				cur_p.cur_state = tier.cur_state;
				if(cur_p.get_next_state(cur_token.kind)!=null) {
					tier_stack.peek().pattern.next_state(cur_token.kind);
					// ������� ��һ��token
					tier.cur_state = cur_p.cur_state;
					if(accpect)	{
						be_handle = true;
						continue;
					}
					accpect = true;
				}
				else {
					if(!set_stack_by_head(cur_token,true))
						be_handle=true;
					// ���ǺϷ���tokenͷ ��wrong_headΪtrue ֱ���ҵ��Ϸ���tokenͷ ��������һ�����Ƿ�ͷ���һ��
					if(!wrong_head) {
						if(tier_stack.size()==0) {
							wrong_head = true;
							System.out.println(cur_token.row+"��"+cur_token.culomn+"�У�"+"�޷�ͨ���﷨����");
						}
						else wrong_head = false;
					}
				}
			}
			// ֻ��ջ��tier�Ĵ�
			boolean error = false;
			while(tier_stack.size()>0) {
				Tier tier = tier_stack.peek();
				Pattern<Token> cur_p = tier.pattern;
				cur_p.cur_state = tier.cur_state;
				if(cur_p.next_state(cur_token.kind)) {
					// ������� ��һ��token
					tier.cur_state = cur_p.cur_state;
					if(accpect)	{
						be_handle = true;
						break;
					}
					accpect = true;
				}
				// �������ջ
				if(error_msg.length()>0 && !error) {
					error = true;
					System.out.println(error_msg);
					error_msg = "";
				}
				tier_stack.remove(tier);
				if(tier_stack.size()==0||tier_stack.peek().deep < roots.size())
					break;
			}
		}
		
		// ������㣺����еȴ�}��tier�����ﱨ��
		boolean error = false;
		while(tier_stack.size()>0) {
			Tier tier = tier_stack.peek();
			Pattern<Token> cur_p = tier.pattern;
			cur_p.cur_state = tier.cur_state;
			if(cur_p.next_state(cur_token.kind)) {
				// ������� ��һ��token
				tier.cur_state = cur_p.cur_state;
				if(accpect)	{
					be_handle = true;
					break;
				}
				accpect = true;
			}
			// �������ջ
			if(error_msg.length()>0 && !error) {
				error = true;
				System.out.println(error_msg);
				error_msg = "";
			}
			tier_stack.remove(tier);
			if(tier_stack.size()==0||tier_stack.peek().deep < roots.size())
				break;
		}
		
		Frame f = new Frame();
		f.setSize(300, 300);
		//��������JTree���
		JTree jTree = new JTree(root_node);
		//���JTree���������
		f.add(jTree); 
		 //���ô��ڿɼ�
		f.setVisible(true);
		System.out.println("========================");
		return root_node;
	    
	}
	
}
