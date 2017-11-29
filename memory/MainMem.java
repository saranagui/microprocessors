package memory;

public class MainMem {

	final int size = 64 * 1024;
	//BYTE ADDRESSABLE MEM AND CACHE! make it byte
	Byte [] data;
	int hitCycles;
	public MainMem(int hitCycles){
		data=new Byte[size/8];
		this.hitCycles=hitCycles;
	}
	public Byte readByte(int address){
		return data[address];
	}
	public void writeByte(int address, Byte newByte) {
		data[address] = newByte;
	}
}
