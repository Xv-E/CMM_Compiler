package structure;

import tool.Constants;

public class Token {
	/* token类型*/
	//private String kind;
	public Constants.TAG kind;
	/* token所在行*/
	public int row;
	/* token所在列*/
	public int culomn;
	/* token内容*/
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
            return true;//地址相等
        }

        if(obj == null){
            return false;//非空性：对于任意非空引用x，x.equals(null)应该返回false。
        }

        if(obj instanceof Token){
        	Token other = (Token) obj;
            //需要比较的字段相等，则这两个对象相等
            if(this.kind.equals(other.kind)){
                return true;
            }
        }

        return false;
    }
	
}
