package structure;
import java.util.*;

public class Arrow<T> {
	public State<T> to;
	Callback callback = null; // 经过时回调
	public List<Object> demand = new ArrayList<>();
	public boolean reverse_demand = false; // 为true时，接受demand的补集

	
	public Arrow(State<T> t){
		to = t;
	}
	
	public Arrow(State<T> t,Callback cb){
		to = t;
		callback = cb;
	}

	public void set_callback(Callback cb) {
		callback = cb;
	}
	
	public boolean go_through(Object t) {
		boolean match = reverse_demand;
		for(Object d : demand) {
			if(t.equals(d)) {
				match = !match;
				break;
			}
		}
		if(match) {
			if(callback!=null) callback.func(); //调用回调接口方法
			return true;
		}
		return false;
	}

	public boolean can_through(Object t) {
		boolean match = reverse_demand;
		for(Object d : demand) {
			if(t.equals(d)) {
				match = !match;
				break;
			}
		}
		if(match) {
			return true;
		}
		return false;
	}
}
