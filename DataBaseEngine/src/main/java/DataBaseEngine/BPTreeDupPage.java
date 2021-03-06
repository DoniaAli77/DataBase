package DataBaseEngine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;


public class BPTreeDupPage implements Serializable {
	private static final long serialVersionUID = 1L;
	ArrayList<Comparable> dupl;   //array of duplicate keys
	//int duPptr;
	int order;                   //size of node   
	static int count;
	String  filename;          //leafnode filename
	//	int originalindex;         //to know the parent of this page 
	BPTreeDupPage nextptr;           //ptr to next dup page
	String nextFptr=null;          //ptr to next file
	ArrayList<BPTreeRefRecord> records;    //records
	int nofkeys;               // no. of keys
	String originalfilename;
	public BPTreeDupPage( int order,Comparable key,String orginalfilename) throws IOException {
		this.dupl = new ArrayList<Comparable>(order);
		this.records= new ArrayList<BPTreeRefRecord>(order);
		this.order = order;
		this.originalfilename=orginalfilename;
		this.filename =originalfilename.substring(0, orginalfilename.length()-4)+count+"key"+key+".ser";
		++count;
		FileOutputStream file = new FileOutputStream(filename);
		ObjectOutputStream out = new ObjectOutputStream(file);
		out.writeObject(this);
		out.close();
		file.close();
	}
	public String getNextFptr() {
		return nextFptr;
	}
	public void setNextFptr(String nextFptr) throws ClassNotFoundException, IOException {
		this.nextFptr = nextFptr;
		refresh();
	}
	public String getOriginalfilename() {
		return originalfilename;
	}
	public void setDupl(Comparable key) throws ClassNotFoundException, IOException {
		dupl.add(key);
		nofkeys++;
		refresh();
	}
	public BPTreeDupPage getNextptr() {
		return nextptr;
	}
	public void setNextptr(BPTreeDupPage nextptr) throws ClassNotFoundException, IOException {
		this.nextptr = nextptr;
		refresh();
	}
	public ArrayList<BPTreeRefRecord> getRecords() {
		return records;
	}
	public void setRecords(BPTreeRefRecord record ) throws ClassNotFoundException, IOException {
		records.add(record);
		refresh();
	}
	public int pageSize() {
		return nofkeys;
	}
//	public void deleteAllPages() throws ClassNotFoundException, IOException {
//		do{	
//			Files.deleteIfExists(Paths.get(this.filename));
//			if(this.nextFptr!=null) {
//				FileInputStream file = new FileInputStream(this.nextFptr);
//				ObjectInputStream in = new ObjectInputStream(file);
//				DupPage nextPage= (DupPage)in.readObject();
//				nextPage.deleteAllPages();
//				in.close();
//				file.close();
//			}
//
//		}while(this.nextFptr!=null);
//		//		if(this.nextFptr==null)
//		//			Files.deleteIfExists(Paths.get(this.filename));
//
//	}

	public ArrayList<BPTreeRefRecord> getallRecords() throws ClassNotFoundException, IOException{
		ArrayList<BPTreeRefRecord> allRecords = new ArrayList<BPTreeRefRecord>();

		return this.getallRecords(allRecords);

	}
	public ArrayList<BPTreeRefRecord> getallRecords(ArrayList<BPTreeRefRecord> allRecords) throws IOException, ClassNotFoundException{
		for (int i = 0; i < records.size(); i++) {
			allRecords.add(records.get(i));	
		}
		if(this.nextFptr!=null) {
			FileInputStream file = new FileInputStream(this.nextFptr);
			ObjectInputStream in = new ObjectInputStream(file);
			BPTreeDupPage nextPage= (BPTreeDupPage)in.readObject();
			nextPage.getallRecords(allRecords);
			in.close();
			file.close();
		}
		return allRecords;
	}
	public void deleteAllPages() throws ClassNotFoundException, IOException {
		ArrayList<String>allfiles = new ArrayList<>();
		ArrayList<String>deletedfiles=this.deleteAllPages(allfiles);
		for (int i = 0; i < deletedfiles.size(); i++) {
				Files.deleteIfExists(Paths.get(deletedfiles.get(i)));
		}
		
		
	}
	public  ArrayList<String> deleteAllPages(ArrayList<String> allfiles) throws ClassNotFoundException, IOException {
		     allfiles.add(this.filename);
		///		Files.deleteIfExists(Paths.get(this.filename));
				if(this.nextFptr!=null) {
					FileInputStream file = new FileInputStream(this.nextFptr);
					ObjectInputStream in = new ObjectInputStream(file);
					BPTreeDupPage nextPage= (BPTreeDupPage)in.readObject();
					nextPage.deleteAllPages(allfiles);
					in.close();
					file.close();
				}
		return allfiles;
				//		if(this.nextFptr==null)
				//			Files.deleteIfExists(Paths.get(this.filename));
		
			}

	public void insertinPage(Comparable key ,BPTreeRefRecord record) throws IOException, ClassNotFoundException {
		if(this.pageSize()<order) {
			this.setDupl(key);// this.setDupl(key,dupl.size()-1);
			this.setRecords(record);//this.setRecords(record, dupl.size());
			refresh();
		}
		else {
			if(/*this.nextptr ==null&&*/this.nextFptr==null) { // to make sure that theres no procedding pages
				BPTreeDupPage newPage =new BPTreeDupPage(order, key, this.getOriginalfilename());
				//this.setNextptr(newPage);
				this.setNextFptr(newPage.filename);
				newPage.insertinPage(key, record);
				this.refresh();
				newPage.refresh();
			}
			else {
				FileInputStream file = new FileInputStream(this.nextFptr);
				ObjectInputStream in = new ObjectInputStream(file);
				BPTreeDupPage nextPage= (BPTreeDupPage)in .readObject();
				nextPage.insertinPage(key, record);
				nextPage.refresh();
				in.close();
				file.close();
				//this.nextptr.insertinPage(key, record);
			}
		}
	}

	public void deletefromPage(Comparable key ,BPTreeRefRecord record) throws ClassNotFoundException, IOException {
		if(this.pageSize()>=0) {
			boolean found =false;
			for (int i = 0; i < records.size(); i++) {
				if((records.get(i)).compareTTo(record)) {
					records.remove(i);
					dupl.remove(i);
					nofkeys--;
					found=true;
					refresh();
					break;	
				}
			}
			if(this.pageSize()==0) {
				Files.deleteIfExists(Paths.get(this.filename));// aftaker law page fadia lazem delete main key???  
			}
			if (!found/*&&this.nextptr!=null*/&&this.nextFptr!=null) {
				FileInputStream file = new FileInputStream(this.nextFptr);
				ObjectInputStream in = new ObjectInputStream(file);
				BPTreeDupPage nextPage = (BPTreeDupPage)in.readObject();
				in.close();
				file.close();
				nextPage.deletefromPage(key, record);
				//nextptr.deletefromPage(key, record);
				if(nextPage.pageSize()==0) {
					//this.setNextptr(nextptr.nextptr);
					this.setNextFptr(nextPage.nextFptr);
					//Files.deleteIfExists(Paths.get(nextptr.filename));
				}
				refresh();			

			}
		}
	}
	public BPTreeRefRecord searchinPage(Comparable key, BPTreeRefRecord record) throws IOException, ClassNotFoundException {
		boolean found= false;
		for (int i = 0; i < records.size(); i++) {
			if(records.get(i).compareTTo(record)&&dupl.get(i).compareTo(key)==0){
				found=true;
				return records.get(i);
			}
		}
		if (!found/*&&this.nextptr!=null*/&&this.nextFptr!=null) {
			FileInputStream file = new FileInputStream(this.nextFptr);
			ObjectInputStream in = new ObjectInputStream(file);
			BPTreeDupPage nextPage= (BPTreeDupPage)in .readObject();
			in.close();
			file.close();
			return nextPage.searchinPage(key, record);
			//return this.nextptr.searchinPage(key, record);
		}
		return null;
	}


	public void refresh() throws IOException, ClassNotFoundException {
		if (filename != null) {
			FileOutputStream file = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(file);
			out.writeObject(this);
			out.close();
			file.close();
		}
	}
	@Override
	public String toString() {
		String s =this.filename+"               ";	
		for (int i = 0; i < records.size(); i++) {
			s+=dupl.get(i)+" ";
			//s+=records.get(i)+" ";

		}
		return s;
	}
	public static void main(String[] args) {
		try {
			BPTreeDupPage p= new BPTreeDupPage(3, 4, "studentgpa0");

			BPTreeRefRecord r= new BPTreeRefRecord(4, 3);
			p.insertinPage(4,r );
			//System.out.println(p);	

			BPTreeRefRecord r1= new BPTreeRefRecord(5, 2);
			p.insertinPage(4,r1 );
			//System.out.println(p);

			BPTreeRefRecord r2= new BPTreeRefRecord(6, 7);
			p.insertinPage(4,r2 ); 
			System.out.println(p+"           hnaaaaaaaaaaa");

			BPTreeRefRecord r3= new BPTreeRefRecord(9,8);
			p.insertinPage(4,r3 ); 



			BPTreeDupPage next =p.nextptr;
			System.out.println(next);

			BPTreeRefRecord r4= new BPTreeRefRecord(10, 3);
			p.insertinPage(4,r4 );
			System.out.println(p);
			BPTreeRefRecord r5= new BPTreeRefRecord(10, 3);
			BPTreeRefRecord r6= new BPTreeRefRecord(10, 3);
			BPTreeRefRecord f= p.searchinPage(4, r5);
			System.out.println(f);
			p.insertinPage(4, r5);
			p.insertinPage(4, r6);
			System.out.println(p.nextptr);
			p.deletefromPage(4, r);
			BPTreeRefRecord ff= p.searchinPage(4, r5);
			System.out.println(ff);
			System.out.println(p);
			p.deletefromPage(4, r1);
			p.deletefromPage(4,r2 );
			p.deletefromPage(4, r3);
			System.out.println(p);
			p.deletefromPage(4, r4);
			p.deletefromPage(4, r5);
			System.out.println(p.nextptr);
			System.out.println(next.nextptr);



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
