package structure;
import java.util.List;

public class Pattern<T> {
	public State<T> start;
	public State<T> cur_state;
	public boolean disable;
	
	public Pattern(State<T> s) {
		start = s;
		cur_state = start;
		disable = false;
	}
	
	public boolean next_state(Object t) {
		if(cur_state!=null) 
			cur_state = cur_state.find_arrow(t);
		if(cur_state==null) {
			disable = true;
			return false;
		}
		return true;
	}
	
	public State<T> get_next_state(Object t) {
		State<T> ret = null;
		if(cur_state!=null)
			ret = cur_state.have_arrow(t);
		return ret;
	}
	
	public void reset() {
		disable = false;
		cur_state = start;
	}

}
