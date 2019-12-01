package structure;
import java.util.*;

public class Arrow<T> {
	public State<T> to;
	Callback callback = null; // ����ʱ�ص�
	public List<Object> demand = new ArrayList<>();
	public boolean reverse_demand = false; // Ϊtrueʱ������demand�Ĳ���

	
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
			if(callback!=null) callback.func(); //���ûص��ӿڷ���
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
