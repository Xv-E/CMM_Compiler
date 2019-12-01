package structure;
import java.util.ArrayList;
import java.util.List;

public class State<T> {
	List<Arrow<T>> arrows = new ArrayList<>();
	//Pattern<T> parent_pattern;
	private Callback end_callback; // �ڸ�״̬�½���
	
	public void add_arrow(Arrow<T> a){
		arrows.add(a);
	}
	
	public void when_end(Callback end_callback) {
		this.end_callback = end_callback;
	}
	
	//����get�õ��¸�״̬
	public State<T> find_arrow(Object get) {
		for (Arrow<T> a : arrows) {
            if(a.go_through(get)) {
            	return a.to;
            }
        }
		if (end_callback!=null) 
			end_callback.func();
		return null;
	}
	
	public State<T> have_arrow(Object get) {
		for (Arrow<T> a : arrows) {
            if(a.can_through(get)) {
            	return a.to;
            }
        }
		return null;
	}
	
}
