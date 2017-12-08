package memory;

import java.util.ArrayList;

public class MemoryHierarchy {

	MainMem mainMem;
	int mainMemAccessTimeInCycles;
	//cachelevelsnumber must be at least 1!
	ArrayList <Cache> dataCacheLevels;
	ArrayList <Cache> instructionCacheLevels;
	int cacheLevelsNumber;
	static int totalNumCycles;
	
	public MemoryHierarchy(int mainMemAccessTimeInCycles, int cacheLevelsNumber){
		mainMem = new MainMem(mainMemAccessTimeInCycles);
		if(cacheLevelsNumber<1){
			System.out.println("you must enter at least one cache level");
		}
		else{
			this.cacheLevelsNumber = cacheLevelsNumber;
		}
		dataCacheLevels = new ArrayList<Cache>();
		instructionCacheLevels = new ArrayList<Cache>();
	}
	
	public void addDataCache(int s,int l,int m,int hitCycles){
		if(dataCacheLevels.size()>=cacheLevelsNumber){
			System.out.println("You can't enter more than "+cacheLevelsNumber+" levels of cache");
			return;
		}
		dataCacheLevels.add(new Cache(s, l, m, hitCycles));
	}
	public void addInstructionCache(int s,int l,int m,int hitCycles){
		if(instructionCacheLevels.size()>cacheLevelsNumber){
			System.out.println("You can't enter more than "+cacheLevelsNumber+" levels of cache");
			return;
		}
		instructionCacheLevels.add(new Cache(s, l, m, hitCycles));
	}
	
public Byte readDataByte(int address){
		Entry e;
		Byte b = null;
		boolean foundInCache = false;
		Byte data[];
		int level = 0;
		for(int i=0; i < cacheLevelsNumber; i++){
			totalNumCycles+=dataCacheLevels.get(i).hitCycles;
			if(dataCacheLevels.get(i).readByte(address)!=null){
				foundInCache = true;
				b = dataCacheLevels.get(i).readByte(address);
				level = i;
				break;
			}
		}
		if (!foundInCache){
			b = new Byte(mainMem.readByte(address).bits);
		}
		for(int i=0; i < level; i++){
			if(dataCacheLevels.get(i).readByte(address)==null){
				data = new Byte[dataCacheLevels.get(i).l];
				int tag = (address / dataCacheLevels.get(i).l) / (dataCacheLevels.get(i).s/dataCacheLevels.get(i).m); //get tag
				int setIndex = (address / dataCacheLevels.get(i).l) % (dataCacheLevels.get(i).s/dataCacheLevels.get(i).m);
				int startAddress = (setIndex)* dataCacheLevels.get(i).m; //Calculate from lecture
				for(int j=0; j<data.length;j++){
					data[j] = dataCacheLevels.get(level).readByte(startAddress + j);
				}
				e = new Entry(tag, data);
				writeEntry(i, setIndex, e);
			}
		}
		totalNumCycles+=mainMemAccessTimeInCycles;
		return b;
	}
	public Byte readInstructionByte(int address){
		Entry e;
		Byte b = null;
		boolean foundInCache = false;
		Byte data[];
		int level = 0;
		for(int i=0; i < cacheLevelsNumber; i++){
			totalNumCycles+=instructionCacheLevels.get(i).hitCycles;
			if(instructionCacheLevels.get(i).readByte(address)!=null){
				foundInCache = true;
				b = instructionCacheLevels.get(i).readByte(address);
				level = i;
				break;
			}
		}
		if (!foundInCache){
			b = new Byte(mainMem.readByte(address).bits);
		}
		for(int i=0; i < level; i++){
			if(instructionCacheLevels.get(i).readByte(address)==null){
				data = new Byte[instructionCacheLevels.get(i).l];
				int tag = (address / instructionCacheLevels.get(i).l) / (instructionCacheLevels.get(i).s/instructionCacheLevels.get(i).m); //get tag
				int setIndex = (address / instructionCacheLevels.get(i).l) % (instructionCacheLevels.get(i).s/instructionCacheLevels.get(i).m);
				int startAddress = (setIndex)* instructionCacheLevels.get(i).m; //Calculate from lecture
				for(int j=0; j<data.length;j++){
					data[j] = instructionCacheLevels.get(level).readByte(startAddress + j);
				}
				e = new Entry(tag, data);
				writeEntry(i, setIndex, e);
			}
		}
		totalNumCycles+=mainMemAccessTimeInCycles;
		return b;
	}
	
	//NOTE: For the instruction caches there are no write operations!
	//For the data cache we have to assure that cache and
	//external memory are consistent
	public void writeByte(int address, Byte newByte, int startCacheLevel){
		
		boolean foundInCache = false;
		int level = 0;
		for(int i=startCacheLevel; i < cacheLevelsNumber; i++){
			//totalNumCycles+=cacheLevels.get(i).hitCycles;
			if(dataCacheLevels.get(i).readByte(address)!=null){
				foundInCache = true;
				level = i;
				break;
			}
		}
		//in case of a hit
		if (foundInCache){
			dataCacheLevels.get(level).writeByteHit(address, newByte); //takes into consideration the dirty bit in case of write back
			if(dataCacheLevels.get(level).hitWriteStratetgyWriteThrough){
				//write through (also updates all next levels and the memory)
				for(int i=level+1; i < cacheLevelsNumber; i++){
					dataCacheLevels.get(i).writeByteHit(address, newByte);
				}
				mainMem.writeByte(address, newByte);
			}
		}
		//in case of a miss
		else{
			if(dataCacheLevels.get(level).missWriteStratetgyWriteAllocate){
				//write allocate (fetch block and treat the write miss as a read miss followed by a write hit)
				readDataByte(address);
				writeByte(address, newByte, 0);
			}
			else{
				//write around (only update the next level of hierarchy without updating this cache
				for(int i=level+1; i < cacheLevelsNumber; i++){
					if(dataCacheLevels.get(i).readByte(address)!=null)
						dataCacheLevels.get(i).writeByte(address, newByte);
				}
				mainMem.writeByte(address, newByte);
			}
		}
	}

	public void writeEntry(int cacheLevel, int setIndex, Entry newEntry) {
		//if a block will be replaced, if its dirty, update the memory 
		//and the lower levels of cache!!! also use random replacement policy
		if(cacheLevel==cacheLevelsNumber){
			return;
		}
		Entry[] entries = dataCacheLevels.get(cacheLevel).entries;
		boolean entryAdded = false;
		for(int i=0; i<dataCacheLevels.get(cacheLevel).m && !entryAdded;i++){
			if(!entries[setIndex * dataCacheLevels.get(cacheLevel).m + i].valid){
				entries[setIndex * dataCacheLevels.get(cacheLevel).m + i] = newEntry;
				entryAdded = true;
			}
		}
		if(!entryAdded){
			//using a random position in the set
			int i = (int)Math.random()*dataCacheLevels.get(cacheLevel).m;
			//if it is dirty
			for(int j=0; j<entries[setIndex * dataCacheLevels.get(cacheLevel).m + i].data.length; j++){
				if(entries[setIndex * dataCacheLevels.get(cacheLevel).m + i].data[j].dirty){
					//update lower levels of cache and the main memory of this entry before replacing it
					//address = tag, setindex, offset
					int address = (entries[setIndex * dataCacheLevels.get(cacheLevel).m + i].tag * (dataCacheLevels.get(cacheLevel).s/dataCacheLevels.get(cacheLevel).m) * dataCacheLevels.get(cacheLevel).l) + (setIndex * dataCacheLevels.get(cacheLevel).l) + (j);
					writeByte(address, entries[setIndex * dataCacheLevels.get(cacheLevel).m + i].data[j], cacheLevel+1);
					//update memory
				}
			}
			entries[setIndex * dataCacheLevels.get(cacheLevel).m + i] = newEntry;
			entryAdded = true;
		}
	}

	public ArrayList<Cache> getDataCacheLevels() {
		return dataCacheLevels;
	}

	public void setDataCacheLevels(ArrayList<Cache> dataCacheLevels) {
		this.dataCacheLevels = dataCacheLevels;
	}

	public ArrayList<Cache> getInstructionCacheLevels() {
		return instructionCacheLevels;
	}

	public void setInstructionCacheLevels(ArrayList<Cache> instructionCacheLevels) {
		this.instructionCacheLevels = instructionCacheLevels;
	}

	public int getCacheLevelsNumber() {
		return cacheLevelsNumber;
	}

	public void setCacheLevelsNumber(int cacheLevelsNumber) {
		this.cacheLevelsNumber = cacheLevelsNumber;
	}

	public static int getTotalNumCycles() {
		return totalNumCycles;
	}

	public static void setTotalNumCycles(int totalNumCycles) {
		MemoryHierarchy.totalNumCycles = totalNumCycles;
	}

}
