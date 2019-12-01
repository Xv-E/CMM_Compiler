package structure;

import tool.Constants;

public class Token {
	/* token����*/
	//private String kind;
	public Constants.TAG kind;
	/* token������*/
	public int row;
	/* token������*/
	public int culomn;
	/* token����*/
	public String content;
	
	public Token(String content,Constants.TAG kind,int row,int culomn) {
		this.kind = kind;
		this.row = row;
		this.culomn = culomn;
		this.content = content;
	}
	

	
	public boolean equals(Constants.TAG kind) {
		if(this.kind.equals(kind)){
			return true;
			}
		return false;
    }
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj){
            return true;//��ַ���
        }

        if(obj == null){
            return false;//�ǿ��ԣ���������ǿ�����x��x.equals(null)Ӧ�÷���false��
        }

        if(obj instanceof Token){
        	Token other = (Token) obj;
            //��Ҫ�Ƚϵ��ֶ���ȣ����������������
            if(this.kind.equals(other.kind)){
                return true;
            }
        }

        return false;
    }
	
}
