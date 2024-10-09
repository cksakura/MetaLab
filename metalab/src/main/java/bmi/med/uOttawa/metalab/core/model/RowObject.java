/**
 * 
 */
package bmi.med.uOttawa.metalab.core.model;

/**
 * @author Kai Cheng
 *
 */
public class RowObject {

	private Object[] objs;

	public RowObject(Object[] objs) {
		this.objs = objs;
	}

	public Object[] getObjs() {
		return objs;
	}

	public void setObjs(Object[] objs) {
		this.objs = objs;
	}

	public Object getObject(int id) {
		return objs[id];
	}

}
