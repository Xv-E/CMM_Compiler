package execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import structure.Token;
import structure.Tree_Node;
import tool.Constants;
import tool.Constants.TAG;

public class Semantic_Analysis {
	static boolean looping = false;
	static boolean break_trigger = false;
	static boolean continue_trigger = false;
	
	static Tree_Node cur_tn =null;
	
	static class Var{
		String name;
		Object value;
		Constants.TAG kind;
		int index;
		List<Integer> range = null;
	}
	
	static Stack<List<Var>> var_stack = new Stack<>();
	
	static void error_msg(String msg) throws Exception{
		int r = cur_tn.getToken().row;
		int c = cur_tn.getToken().culomn;
		
		System.out.println("第"+r+"行:"+ msg);
		display_var_stack();
		throw new Exception("语义错误，分析中断");	
	}
	
	static void add_var_to_stack(Var var) {
		var_stack.peek().add(var);
	}
	
	static boolean assign_var(Var var1,Var var2) throws Exception {
		Class class1 = Constants.get_class_by_kind(var1.kind);
		Class class2 = Constants.get_class_by_kind(var2.kind);
		if(class1==class2) {
			var1.value = var2.value;	
		}
		else if(class1==Integer.class && class2==Double.class) {
			var1.value = ((Double)var2.value).intValue();
		}
		else if(class1==Double.class && class2==Integer.class) {
			var1.value = ((Integer)var2.value).doubleValue();
		}
		else {
			error_msg("不能将"+var2.kind+"赋给"+var1.kind);
			return false;
		}
		return true;
	}
	
	static Var find_var(String name,List<Integer> indexs) throws Exception {
		// 从栈顶开始
		for(int i=var_stack.size()-1;i>=0;i--) {
			List<Var> l = var_stack.get(i);
			for(Var v:l) {
				if(v.name.equals(name)) {
					// 不是数组
					if((indexs == null && v.range == null) || (indexs == null && v.range != null)) {
						return v;
					}
					else if(indexs != null && v.range == null)
						error_msg(v.name+"不是数组");
					// 取出数组元素
					else {
						int shift = 0;
						int step = 1;
						for(int j=indexs.size()-1;j>=0;j--) {
							int index = indexs.get(j);
							if(index>v.range.get(j)-1 || index<0)
								error_msg("数组越界");
							else {
								shift += index*step;
								step *= v.range.get(j);
							}
						}
						return l.get(l.indexOf(v)+shift);
					}
				}
			}
		}
		error_msg("变量未定义");
		return null;
	}
	
	static Var get_node_var(Tree_Node tn) throws Exception {
		Var var = null;
		Token t = tn.getToken();
		if(Constants.OPERATOR.contains(t.kind) || Constants.MONOCULAR.contains(t.kind)) {
			switch(t.kind) {
			case GT:var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),1);break;
			case LT:var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),2);break;
			case GT_EQUAL: var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),3);break;
			case LT_EQUAL: var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),4);break;
			case EQUAL: var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),5);break;
			case NEQUAL: var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),0);break;
			
			case PLUS:var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),6);break;
			case MINUS:var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),7);break;
			case TIMES:var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),8);break;
			case DIVIDE:var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),9);break;

			case BIT_AND:var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),10);break;
			case BIT_OR:var = binary_operator(get_node_var(tn.getChildAt(0)),get_node_var(tn.getChildAt(1)),11);break;
			
			case AND:{
				Var var1 = get_node_var(tn.getChildAt(0));
				if(Constants.get_class_by_kind(var1.kind)==Integer.class) {
					if((Integer)var1.value!=0)
						var = binary_operator(var1,get_node_var(tn.getChildAt(1)),12);
					else 
						var = binary_operator(var1,var1,12);
				}
				else if(Constants.get_class_by_kind(var1.kind)==Double.class) {
					if((Double)var1.value!=0.0)
						var = binary_operator(var1,get_node_var(tn.getChildAt(1)),12);
					else 
						var = binary_operator(var1,var1,12);
				}}
				break;
			case OR:{
				Var var1 = get_node_var(tn.getChildAt(0));
				if(Constants.get_class_by_kind(var1.kind)==Integer.class) {
					if((Integer)var1.value==0)
						var = binary_operator(var1,get_node_var(tn.getChildAt(1)),13);
					else 
						var = binary_operator(var1,var1,13);
				}
				else if(Constants.get_class_by_kind(var1.kind)==Double.class) {
					if((Double)var1.value==0.0)
						var = binary_operator(var1,get_node_var(tn.getChildAt(1)),13);
					else 
						var = binary_operator(var1,var1,13);
				}}
			break;
			case NEGATIVE:var = monocular_operation(get_node_var(tn.getChildAt(0)), 0);
			}
			//System.out.println("临时变量"+var.value);
		}
		else if(t.kind == Constants.TAG.IDENTIFIER) {
			List<Integer> indexs = null;
			for(Tree_Node child :tn.children) {
				if(indexs==null) indexs = new ArrayList<>();
				indexs.add((Integer)get_node_var(child).value);
			}
			var = find_var(t.content, indexs);
//			if(tn.children.isEmpty())
//				var = find_var(t.content, null);
//			else {
//				Var v = get_node_var(tn.getChildAt(0));
//
//				if(Constants.get_class_by_kind(v.kind)!=Integer.class)
//					error_msg("数组下标应该为INT类型");
//				else	
//					var = find_var(t.content,(Integer)v.value);			
//			}
		}
			else if(Constants.CONST.contains(t.kind)) {
			var = new Var();
			var.kind = t.kind;
			if(t.kind==TAG.CONST_CHAR) {
				var.value = (int)t.content.charAt(0);
			}
			else {
				Method m = Constants.get_valueOf_by_kind(t.kind);
				if(m!=null) {
					try {
						var.value = m.invoke(null,t.content);
						}
					catch(InvocationTargetException e) {
						error_msg("数据格式错误");
						//e.printStackTrace();
					}
				}
				else
					error_msg("类型错误");
			}
			
		}
		else if(t.kind == Constants.TAG.READ) {
			var = read_stm();
			
		}
		return var;
	}

	static void display_var_stack() {
		// 输出变量栈的情况
		System.out.println("变量栈情况:");
		for(List<Var> l :var_stack) {
			for(Var v:l) {
				if(v.kind==TAG.CHAR)
					System.out.println(v.name+'\t'+v.index+'\t'+(char)((int)v.value)+'\t'+v.kind);
				else
					System.out.println(v.name+'\t'+v.index+'\t'+v.value+'\t'+v.kind);
			}
		}
		System.out.println("========================");
	}
	
	// 单目运算
	public static Var monocular_operation(Var var,int flag) throws Exception {
		Var ret = new Var();
		Class c = Constants.get_class_by_kind(var.kind);

		if(c==Integer.class) {
			ret.kind = TAG.INT;
			switch(flag) {
			case 0:ret.value = -(Integer)var.value;break;
			}
		}
		else if(c==Double.class) {
			ret.kind = TAG.REAL;
			switch(flag) {
			case 0:ret.value = -(Double)var.value;break;
			}
		}
		return ret;
	}
	
	// 双目运算
	public static Var binary_operator(Var var1,Var var2 ,int flag) throws Exception {
		Var var = new Var();
		Class class1 = Constants.get_class_by_kind(var1.kind);
		Class class2 = Constants.get_class_by_kind(var2.kind);
		if(class1 ==null ||class2==null) {
			error_msg("类型错误");
			return null;
		}
		else if(class1 == Integer.class && class2 == Integer.class) {
			var.kind = TAG.INT;
			Integer val1,val2;
			val1 = (Integer)var1.value;
			val2 = (Integer)var2.value;
			if(val1==null||val2==null) {
				error_msg("变量未初始化");
			}
			switch(flag) {
			case 0:var.value = val1 != val2?1:0;break;
			case 1:var.value = val1 > val2?1:0;break;
			case 2:var.value = val1 < val2?1:0;break;
			case 3:var.value = val1 >= val2?1:0;break;
			case 4:var.value = val1 <= val2?1:0;break;
			case 5:var.value = val1 == val2?1:0;break;
			
			case 6:var.value = val1 + val2;break;
			case 7:var.value = val1 - val2;break;
			case 8:var.value = val1 * val2;break;
			case 9:var.value = val1 / val2;break;
			
			case 10:var.value = val1 & val2;break;
			case 11:var.value = val1 | val2;break;
			
			case 12:var.value = val1!=0 && val2!=0?1:0;break;
			case 13:var.value = val1!=0 || val2!=0?1:0;break;
			}
		}
		else if(class1 == Double.class || class2==Double.class){
			var.kind = TAG.REAL;
			Double val1,val2;
			// 第一个参数
			if(class1 == Integer.class)
				val1 = ((Integer)var1.value).doubleValue();
			else
				val1 = (Double)var1.value;
			// 第二个参数
			if(class2 == Integer.class)
				val2 = ((Integer)var2.value).doubleValue();
			else
				val2 = (Double)var2.value;
			if(val1==null||val2==null) {
				error_msg("变量未初始化");
			}
			switch(flag) {
			case 0:var.value = val1 != val2?1:0;break;
			case 1:var.value = val1 > val2?1:0;break;
			case 2:var.value = val1 < val2?1:0;break;
			case 3:var.value = val1 >= val2?1:0;break;
			case 4:var.value = val1 <= val2?1:0;break;
			case 5:var.value = val1 == val2?1:0;break;
			
			case 6:var.value = val1 + val2;break;
			case 7:var.value = val1 - val2;break;
			case 8:var.value = val1 * val2;break;
			case 9:var.value = val1 / val2;break;
			
			case 10:error_msg("REAL不可以进行&运算");break;
			case 11:error_msg("REAL不可以进行|运算");break;
			
			case 12:var.value = val1!=0 && val2!=0?1:0;var.kind = TAG.INT;break;
			case 13:var.value = val1!=0 || val2!=0?1:0;var.kind = TAG.INT;break;
			}
		}
		else {
			System.out.println("类型错误");
			return null;
		}
		return var;
	}
	static void assign_stm(Tree_Node head) throws Exception {
		Token t = head.getToken();
		Var var1,var2;
		
		List<Integer> indexs = null;
		Tree_Node assign = null;
		for(Tree_Node child :head.children) {
			if(!"assign".equals(child.kind)) {
				if(indexs == null) indexs = new ArrayList<>();
				indexs.add((Integer)get_node_var(child).value);
			}
			else 
				assign = child;
		}
		Var v1 = find_var(t.content,indexs);
		Var v2 = get_node_var(assign.getChildAt(0));
		assign_var(v1, v2);
//		// 数组元素赋值
//		if(head.children.size()==2) {
//			Var index = get_node_var(head.getChildAt(0));
//			if(Constants.get_class_by_kind(index.kind)!=Integer.class) {
//				error_msg("数组下标应该为整型");
//			}
//			var1 = find_var(t.content,(Integer)index.value);
//			var2 = get_node_var(head.getChildAt(1).getChildAt(0));
//		}
//		// 不是数组
//		else {
//			var1 = find_var(t.content,-1);
//			var2 = get_node_var(head.getChildAt(0).getChildAt(0));
//		}

	}
	
	static void declare_stm(Tree_Node head) throws Exception {
		// tn里的token是被声明变量
		for(Tree_Node tn :head.children) {
			for(List<Var> l:var_stack) {
				for(Var v :l) {
					if(v.name.equals(tn.getToken().content)) {
					 error_msg("不能重复声明变量");
						return;
					}
				}
			}
			// 只是声明变量
			if(tn.children.size()==0) {
				Var var = new Var();
				var.name = tn.getToken().content;
				var.kind = head.getToken().kind;
				var.index = -1;
				add_var_to_stack(var);
			}
			else {
				int d_court = 1;
				List<Integer> indexs = null;
				Tree_Node assign = null;
				for(Tree_Node child :tn.children) {
					if(!"assign".equals(child.kind)) {
						if(indexs==null)
							indexs = new ArrayList<>();
						d_court *= (Integer)get_node_var(child).value;
						indexs.add((Integer)get_node_var(child).value);
					}
					else 
						assign = child;
				}
				Var[] d_vars = new Var[d_court];
				for(int i=0;i<d_court;i++) {		
					Var var = new Var();
					var.name = tn.getToken().content;
					var.kind = head.getToken().kind;
					var.index = i;
					d_vars[i]=var;
					add_var_to_stack(var);
					if(i==0)
						var.range = indexs;
				}
				
				if(assign!=null) {
					// 非数组赋值
					if(tn.Children_Size()==1) {
						assign_var(d_vars[0],get_node_var(assign.getChildAt(0)));
					}
					// 数组赋值
					else {
						Tree_Node const_array = assign.getChildAt(0); 
						if(!"array".equals(const_array.kind)) {
							// 字符串看作const_array
							if(const_array.getToken().kind==TAG.STRING) {
								String str = const_array.getToken().content;
								if(str.length() > d_court) {
									error_msg("数组初始化项太多");
								}
								for(int i=0;i<str.length();i++) {
									Var init_var = new Var();
									init_var.value = (int)str.charAt(i); 
									init_var.kind = TAG.CONST_CHAR;
									assign_var(d_vars[i],init_var);
								}
							}
							else
								error_msg("应使用{...}初始化数组");
						}
						else {
							int init_court = const_array.children.size();
							if(init_court > d_court) {
								error_msg("数组初始化项太多");
							}
							for(int i=0;i<init_court;i++) {
								Var init_var = get_node_var(const_array.getChildAt(i));
								assign_var(d_vars[i],init_var);
							}
						}
					}
					
				}
			}
		}
			// 声明数组并赋值
//			if(tn.children.size()==2) {
//				Var index = get_node_var(tn.getChildAt(0));
//				if(Constants.get_class_by_kind(index.kind)!=Integer.class) {
//					error_msg("数组下标应该为整型");
//				}
//				Var[] declare_vars = new Var[(Integer)index.value];
//				for(int i=0;i<(Integer)index.value;i++) {
//					Var var = new Var();
//					var.name = tn.getToken().content;
//					var.kind = head.getToken().kind;
//					var.index = i;
//					declare_vars[i]=var;
//					add_var_to_stack(var);
//				}
//				// 赋值部分
//				Tree_Node const_array = tn.getChildAt(1).getChildAt(0); //子节点是赋值项
//				if(!"array".equals(const_array.kind)) {
//					// 字符串看作const_array
//					if(const_array.getToken().kind==TAG.STRING) {
//						String str = const_array.getToken().content;
//						if(str.length() > (Integer)index.value) {
//							error_msg("数组初始化项太多");
//						}
//						for(int i=0;i<str.length();i++) {
//							Var init_var = new Var();
//							init_var.value = (int)str.charAt(i); 
//							init_var.kind = TAG.CONST_CHAR;
//							assign_var(declare_vars[i],init_var);
//						}
//					}
//					else
//						error_msg("应使用{...}初始化数组");
//				}
//				else {
//					int init_court = const_array.children.size();
//					if(init_court > (Integer)index.value) {
//						error_msg("数组初始化项太多");
//					}
//					for(int i=0;i<init_court;i++) {
//						Var init_var = get_node_var(const_array.getChildAt(i));
//						assign_var(declare_vars[i],init_var);
//					}
//				}
//			}
//			else if(tn.children.size()==1) {
//				Tree_Node tn1 = tn.getChildAt(0);
//				// 声明数组
//				if(tn1.getToken().kind!=TAG.ASSIGN) {
//					Var index = get_node_var(tn.getChildAt(0));
//					if(Constants.get_class_by_kind(index.kind)!=Integer.class) {
//						error_msg("数组下标应该为整型");
//					}
//					Var[] declare_vars = new Var[(Integer)index.value];
//					for(int i=0;i<(Integer)index.value;i++) {
//						Var var = new Var();
//						var.name = tn.getToken().content;
//						var.kind = head.getToken().kind;
//						var.index = i;
//						declare_vars[i]=var;
//						add_var_to_stack(var);
//					}
//				}
//				// 声明并赋值
//				else {
//					Var var = new Var();
//					var.name = tn.getToken().content;
//					var.kind = head.getToken().kind;
//					var.index = -1;
//					assign_var(var,get_node_var(tn1.getChildAt(0)));
//					add_var_to_stack(var);
//				}
//			}
//			else if(tn.children.size()==0) {
//				Var var = new Var();
//				var.name = tn.getToken().content;
//				var.kind = head.getToken().kind;
//				var.index = -1;
//				add_var_to_stack(var);
//			}
//		}
	}
	
	static void print_stm(Tree_Node head) throws Exception {
		Var v = get_node_var(head.getChildAt(0));
		if(v.kind==TAG.CHAR)
			System.out.println((char)((int)v.value));
		else
			System.out.println(v.value);
	}
	
	static Var read_stm() throws Exception {
		Var v = new Var();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String str = br.readLine();
		if(Pattern.compile("^[-\\+]?[.\\d]*$").matcher(str).matches()) {
			v.kind = TAG.CONST_REAL;
			v.value = Double.valueOf(str);
		}
		else if(str.matches("[0-9]+")) {
			v.kind = TAG.CONST_INT;
			v.value = Integer.valueOf(str);
		}
		else {
			v.kind = TAG.CONST_CHAR;
			v.value = (int)str.charAt(0);
		}
		return v;
	}
	
	static void if_stm(Tree_Node head) throws Exception {
		Tree_Node tn = head.getChildAt(0);
//		if(!"condition".equals(tn.kind)) {
//			System.out.println("怎么肥事？");
//		}
		Var condition = get_node_var(tn);
		if((Integer)condition.value!=0) {
			execute_local(head.getChildAt(1));
		}
		else {
			if(head.Children_Size()==3)
				execute_local(head.getChildAt(2));
		}

	}
	
	static void while_stm(Tree_Node head) throws Exception {
		looping =true;
		Tree_Node tn = head.getChildAt(0);
//		if(!"condition".equals(tn.kind)) {
//			System.out.println("怎么肥事？");
//		}
		while((Integer)get_node_var(tn).value!=0) {
			execute_local(head.getChildAt(1));
			if(break_trigger) {
				break_trigger = false;
				break;
			}
			if(continue_trigger) {
				continue_trigger = false;
				continue;
			}
		}
		looping =false;
	}

	static void for_stm(Tree_Node head) throws Exception {
		looping =true;
		Tree_Node front = null;
		Tree_Node condition =null;
		Tree_Node after =null;
		List<Var> local_vars = new ArrayList<>();
		var_stack.push(local_vars);
		// 安排！
		for(Tree_Node tn:head.children) {
			if (tn.getToken()==null)
				continue;
			else if("front".equals(tn.kind)) 
				front=tn;
			else if("after".equals(tn.kind))
				after = tn;
			else if("condition".equals(tn.kind))
				condition = tn;
		}
		// for 循环
		if(front!=null)
			func_by_head(front);
		while((Integer)get_node_var(condition).value!=0) {
			// 最后一个节点是循环体
			execute_local(head.getChildAt(head.Children_Size()-1));
			if(break_trigger) {
				break_trigger = false;
				break;
			}
			if(after!=null)
				func_by_head(after);
			if(continue_trigger) {
				continue_trigger = false;
				continue;
			}
		}
		looping =false;
		var_stack.pop();
	}
	
	static void break_stm(Tree_Node head) throws Exception{
		if(looping)
			break_trigger = true;
		else
			error_msg("break只能用在循环体里");
	}
	
	static void continue_stm(Tree_Node head) throws Exception {
		if(looping)
			continue_trigger = true;
		else
			error_msg("continue只能用在循环体里");
	}
	
	// 根据树节点来选择执行函数
	static void func_by_head(Tree_Node head) throws Exception {
		if (head.getToken()==null) {
			if(head.kind == "root")
				execute_local(head);
		}
		else if(head.getToken().kind==Constants.TAG.IDENTIFIER)
			assign_stm(head);
		else if(Constants.CLASS.contains(head.getToken().kind)) 
			declare_stm(head);
		else if(head.getToken().kind==Constants.TAG.IF) 
			if_stm(head);
		else if(head.getToken().kind==Constants.TAG.WHILE) 
			while_stm(head);
		else if(head.getToken().kind==Constants.TAG.FOR) 
			for_stm(head);
		else if(head.getToken().kind==Constants.TAG.BREAK) 
			break_stm(head);
		else if(head.getToken().kind==Constants.TAG.CONTINUE) 
			continue_stm(head);
		else if(head.getToken().kind==Constants.TAG.PRINT) 
			print_stm(head);
		else if(head.getToken().kind==Constants.TAG.READ) 
			read_stm();
	}
	
	static void execute_local(Tree_Node root) throws Exception {
		List<Var> local_vars = new ArrayList<>();
		var_stack.push(local_vars);
		try {
			// 如果是作用域 执行所有语句
			if("root".equals(root.kind))
				for (Tree_Node tn :root.children) {
					cur_tn = tn;
					func_by_head(tn);
					// break continue;
					if(break_trigger || continue_trigger)
						break;
				}
			// 一条语句的情况
			else func_by_head(root);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		display_var_stack();
		//System.out.println();
		var_stack.pop();
	}
	
	static public void execute() throws Exception {
		Tree_Node root = Syntax_Analysis.execute();
		//List<Var> global_var = new ArrayList<>();
		//var_stack.add(global_var);
		System.out.println("语义分析:");	
		execute_local(root);	
		
		System.out.println("========================");
	} 
}
