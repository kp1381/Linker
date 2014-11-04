import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Linker {
	
	private static String symTable[]; //takes in all of the symbols
	private static int symValue[]; //takes in the value for each symbol
	private static boolean symUsed[]={false}; //used to check whether each symbol is used
	private static int position=0; //the number of symbols in the table
	private static boolean eof=false; //End Of File, used in reading through the input
	private static int moduleStart=0;//the start of the current module;
	private static String inputFile;
	
	//returns the next integer in the input file
	private static int readNextInt(BufferedReader br) throws IOException{ 
		int toReturn=0;
		char c=(char)br.read();
		while(Character.isWhitespace(c)){//skips all the whitespace
			c=(char)br.read();
		}
		toReturn+=c-'0';
		c=(char)br.read();
		while(Character.isDigit(c)){//assembles the integer digit by digit
			toReturn*=10;
			toReturn+=c-'0';
			c=(char)br.read();
		}
		if (c==(char)-1)eof=true;//checks for end of file
		return toReturn;
	}
	
	//returns the next string in the input file
	private static String readNextStr(BufferedReader br) throws IOException{
		String toReturn="";
		char c=(char)br.read();
		while(Character.isWhitespace(c)){//spips all the whitespace
			c=(char)br.read();
		}
		toReturn+=c;
		c=(char)br.read();
		while(!Character.isWhitespace(c)){//assembles the string digit by digit
			toReturn+=c;
			c=(char)br.read();
		}
		if (c==(char)-1)eof=true;//checks for end of file
		return toReturn;
	}
	//finds S in the symbolTable and returns its value, -1 if not found
	private static int findDef(String s){
		int i;
		for(i=0;i<position;i++){
			if(symTable[i].equals(s)){
				symUsed[i]=true;
				return symValue[i];
			}
		}
		return -1;//not found
	}
	//the actual running algorithm
	private static  void run() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		int i,count;
		while(!eof){//1st run
			count=readNextInt(br);//read in the number of symbols
			for(i=0;i<count;i++){//adds each 1 and its value to the symbol table
				String temp0=readNextStr(br);
				if(findDef(temp0)==-1){//it's not defined
				symTable[position]=temp0;
				symValue[position]=readNextInt(br)+moduleStart;
				position++;
				}
				else{//print error message
					System.out.print("Error: the variable ");
					System.out.print(temp0);
					System.out.println(" is multiply defined; first value used.");
				}
			}
			
			count=readNextInt(br);//skips lines that are not read in the 1st run
			for(i=0;i<count;i++){
				readNextStr(br);
			}
			
			count=readNextInt(br);
			moduleStart+=count;
			for(i=0;i<count;i++){
				readNextStr(br);
				readNextInt(br);
			}
		}//symbol table is assembled
		System.out.println("Symbol Table");
		for(i=0;i<position;i++){
			System.out.print(symTable[i]);
			System.out.print("=");
			System.out.println(symValue[i]);
		}//symbol table is printed
		System.out.println("Memory Map");
		eof=false;
		br = new BufferedReader(new FileReader(inputFile));
		moduleStart=0;
		while(!eof){//2nd run
			count=readNextInt(br);//read in the number of symbols
			for(i=0;i<count;i++){//skips the lines already read in the 1st run
				readNextStr(br);
				readNextInt(br);
			}
			count=readNextInt(br);
			String[] tempArr=new String[count];//use list
			boolean[] boolTempArr=new boolean[count];//checks whether everything in the use list is used
			for(i=0;i<count;i++){
				tempArr[i]=readNextStr(br);
			}
			count=readNextInt(br);
			int address=-1;;
			for(i=0;i<count;i++){
				String temp=readNextStr(br);
				if(temp.equals("I")){//immediate, ignore
					address=readNextInt(br);
				}
				if(temp.equals("A")){//absolute, check for correctness and ignore
					address=readNextInt(br);
					if((address%1000)>600){
						System.out.println("Error: Absolute address exceeds module size; zero used.");
						address/=1000;
						address*=1000;
					}
				}
				if(temp.equals("R")){//relative, check for errors and add the module start.
					address=readNextInt(br)+moduleStart;
					if(address%1000>moduleStart+count){
						address/=1000;
						address*=1000;
						System.out.println("Error: Relative address exceeds module size; zero used.");
					}
				}
				if(temp.equals("E")){//external, try to resolve, check for errors
					address=readNextInt(br);
					int temp1=address%1000;
					if(temp1<tempArr.length){
						address/=1000;
						address*=1000;
						if(findDef(tempArr[temp1])!=-1){//gets the value from the symbol table.
							address+=findDef(tempArr[temp1]);
							boolTempArr[temp1]=true;
						}
						else{
							System.out.print("Error: ");
							System.out.print(tempArr[temp1]);
							System.out.println(" is not defined; zero used.");
						}
					}
					else{
						 System.out.println("External address exceeds length of use list; treated as immediate.");
					}
				}
				System.out.print(moduleStart+i);//print the address
				System.out.print(":   ");
				System.out.println(address);
			}
			moduleStart+=count;
			for(i=0;i<tempArr.length;i++)//check if everything in the use list was used
			{
				if(!boolTempArr[i]){
					System.out.print("Warning: ");
					System.out.print(tempArr[i]);
					System.out.println(" appeared in the use list but was not actually used.");
				}
			}
		}
		for(i=0;i<position;i++)//check if everything in the definitions table is used.
		{
			if(!symUsed[i]){
				System.out.print("Warning: The following symbol was defined but not used: ");
				System.out.println(symTable[i]);
			}
		}
	}
	
	public static void main(String[] args) {
		symTable=new String[600];
		symValue=new int[600];
		symUsed=new boolean[600];
		inputFile=args[0];
		 try {
			run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}