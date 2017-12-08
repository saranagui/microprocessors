package memory;

public class Entry {

	boolean valid;
	//boolean dirty;
	int tag;
	Byte [] data;
	
	
	public Entry(){
		this.valid=false;
	}
	public Entry(int tag, Byte data[]){
		this.tag=tag;
		this.data=data;
		this.valid=true;
	}
	
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public int getTag() {
		return tag;
	}
	public void setTag(int tag) {
		this.tag = tag;
	}
	public Byte[] getData() {
		return data;
	}
	public void setData(Byte[] data) {
		this.data = data;
	}
	
}
