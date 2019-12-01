package structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import tool.Constants;

public class Tree_Node extends DefaultMutableTreeNode{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* ��ǰ������� */
	public 	String kind;
	/* ��ǰ�������*/
	public Token token;
	/* �ӽڵ��б�*/
	public List<Tree_Node> children = new ArrayList<>();
	
	//public int deep = 0;
	
	public Tree_Node(Token token) {
		super(token.content);
		this.setToken(token);
	}
	
	public Tree_Node() {
		super();
	}
	public void add(Tree_Node childNode) {
		super.add(childNode);
		children.add(childNode);
	}
	
	public void addAll(Collection<? extends Tree_Node> childNode) {
		for (Tree_Node t : childNode)
			super.add(t);
		children.addAll(childNode);
	}
	
	public Tree_Node getChildAt(int index) {
		return (Tree_Node) children.get(index);
	}
	
	public int Children_Size() {
		return children.size();
	}
	
	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		if(token!=null)
			setUserObject(token.content);
		this.token = token;
	}
	
}
