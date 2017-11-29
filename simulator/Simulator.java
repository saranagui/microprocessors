package simulator;

import java.util.ArrayList;
import java.util.Scanner;

import hardware.HardwareOrganization;
import memory.MemoryHierarchy;

public class Simulator {

	static MemoryHierarchy memoryHierarchy;
	static HardwareOrganization hardwareOrganization;
	static ArrayList<String> program;
	static ArrayList<Data> data;
	static Scanner sc;
	
	static int instructionsExecuted;
	static int totalCyclesToAccessMemories;
	
	public static void main(String[]args){
		
		System.out.println("Welcome to our Microprocessor Simulator\n");
		System.out.println("*** Please set up the memory hierarchy! ***");
		System.out.println("Please specify the main memory access time (in cycles)!");
		
		sc = new Scanner(System.in);

		//set up memHierarchy
		int mainMemAccessTimeInCycles = sc.nextInt();
		System.out.println("Please enter the number of cache levels.");
		int cacheLevelsNumber = sc.nextInt();
		while(cacheLevelsNumber<1){
			System.out.println("The number of cache levels must be at least 1!");
			System.out.println("Please enter the number of cache levels.");
			cacheLevelsNumber = sc.nextInt();
		}
		memoryHierarchy = new MemoryHierarchy(mainMemAccessTimeInCycles, cacheLevelsNumber);
		
		System.out.println();
		System.out.println();
		
		//set up instruction caches
		System.out.println("*** Let's add some instruction caches ***");
		for(int i=0; i<cacheLevelsNumber; i++){
			System.out.println("To add a cache level, please enter the size of cache in bytes, the block size in bytes, m way associative, and the hit cycles.");
			int s = sc.nextInt();
			int l = sc.nextInt();
			int m = sc.nextInt();
			int hitCycles = sc.nextInt();
			memoryHierarchy.addInstructionCache(s, l, m, hitCycles);
			System.out.println("----------------------------");
		}
		
		System.out.println();
		System.out.println();
		
		//set up data caches
		System.out.println("*** Let's add some data caches ***");
		for(int i=0; i<cacheLevelsNumber; i++){
			System.out.println("To add a cache level, please enter the size of cache in bytes, the block size in bytes, m way associative, and the hit cycles.");
			int s = sc.nextInt();
			int l = sc.nextInt();
			int m = sc.nextInt();
			int hitCycles = sc.nextInt();
			memoryHierarchy.addDataCache(s, l, m, hitCycles);
			System.out.println("----------------------------");
		}
		
		System.out.println();
		System.out.println();

		//take program
		System.out.println("*** Please enter the assembly program ***");
		System.out.println("To end the program enter an 'end'");
		program = new ArrayList<String>();
		String line;
		do{
			line = sc.nextLine();
			program.add(line);
		}while(!line.equals("end"));
		System.out.println("----------------------------");
		System.out.println("Please specify the starting address of the program");
		int programStartingAddress = sc.nextInt();
		
		System.out.println();
		System.out.println();
		
		//take data
		System.out.println("*** Please enter the program data ***");
		System.out.println("To end the data entries enter an 'end'");
		//line = sc.nextLine();
		data = new ArrayList<Data>();
		int value;
		int address;
		do{
			value=sc.nextInt();
			System.out.println("value "+value);
			if(!sc.hasNextInt()){
				System.out.println("Please enter the address");
			}
			address = sc.nextInt();
			System.out.println("address "+address);
			data.add(new Data(value, address));
		}while (sc.hasNextInt());
		System.out.println("----------------------------");
		System.out.println("Thank you for entering all inputs!");
		
		System.out.println();
		System.out.println();
		
		hardwareOrganization = new HardwareOrganization();
		hardwareOrganization.runProgram(program, data);
		
		//after accessing the memory and running the program, te output will be displayed
		System.out.println("*** The Simulator's output! ***");
		System.out.println(instructionsExecuted+" were executed in total.");
		totalCyclesToAccessMemories = memoryHierarchy.getTotalNumCycles();
		System.out.println(totalCyclesToAccessMemories+" cycles spent to access memories");
		for(int i=0; i<cacheLevelsNumber; i++){
			System.out.println("Instruction cache level "+i+":");
			System.out.println("Hits: "+memoryHierarchy.getInstructionCacheLevels().get(i).getHits());
			System.out.println("Misses: "+memoryHierarchy.getInstructionCacheLevels().get(i).getMisses());
			
			System.out.println("Data cache level "+i+":");
			System.out.println("Hits: "+memoryHierarchy.getDataCacheLevels().get(i).getHits());
			System.out.println("Misses: "+memoryHierarchy.getDataCacheLevels().get(i).getMisses());
		}
	}
}
