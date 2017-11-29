package memory;



public class Cache {

	Entry[] entries;
	int s; //size of cache in bytes
	int l; //block size in bytes
	int m; //m way associative
	int blocks; //blocks = s/l
	int hitCycles; //cycles taken when info is found in this level
	int hits; //counter of hits, static?
	int misses; //counter of misses, static?
	boolean hitWriteStratetgyWriteThrough; //strategy used for writing on cache hits
	//true = write through (also updates all next levels and the memory)
	//false = write allocation (write back: only updates the cache and uses a dirty bit!)
	boolean missWriteStratetgyWriteAllocate; //strategy used for writing on cache misses
	//true = write allocate (fetch block and treat the write miss as a read miss followed by a write hit)
	//false = write around (only update the next level of hierarchy without updating this cache
	
	public Cache(int s,int l,int m,int hitCycles){
		this.s=s;
		this.l=l;
		this.m=m;
		this.hitCycles=hitCycles;
		blocks=s/l;
		entries=new Entry[blocks];
		for (int i = 0; i <entries.length; i++) {
			entries[i]=new Entry();
		}
		hits=0;
		misses=0;
		
	}
	public Byte readByte(int address){
		//address is 16 bits 
		//ne7sebhom mel lecture..
		int setIndex = (address / l) % (s/m);
		int tag = (address / l) / (s/m);
		int offset = (address % l);
		
		Entry e=readEntry(setIndex,tag);
		if(e==null){
			return null;//means not in that cache
		}
		//bara nekteb Short s = readWord(); short word = s.getData();
		return new Byte(e.data[offset].bits);
	}
	public Entry readEntry(int setIndex,int tag){
		
		for(int i=0; i<m;i++){
			if(entries[setIndex * m + i].valid && entries[setIndex * m + i].tag == tag){
				hits++;
				return entries[setIndex * m + i];
			}
		}
		misses++;
		return null;
	}
	//NOTE: For the instruction caches there are no write operations!
	//For the data cache we have to assure that cache and
	//external memory are consistent
	public void writeByteHit(int address, Byte newByte){
		int setIndex = (address / l) % (s/m);
		int tag = (address / l) / (s/m);
		int offset = (address % l);
		
		Entry e=readEntry(setIndex,tag);
		e.data[offset] = newByte;
		if(!hitWriteStratetgyWriteThrough){
			//write allocation (write back: only updates the cache and uses a dirty bit!)
			e.data[offset].dirty = true;
		}
	}
	public void writeByte(int address, Byte newByte){
		int setIndex = (address / l) % (s/m);
		int tag = (address / l) / (s/m);
		int offset = (address % l);
		
		Entry e=readEntry(setIndex,tag);
		e.data[offset] = newByte;
	}
	public int getHits() {
		return hits;
	}
	public void setHits(int hits) {
		this.hits = hits;
	}
	public int getMisses() {
		return misses;
	}
	public void setMisses(int misses) {
		this.misses = misses;
	}
	
}
