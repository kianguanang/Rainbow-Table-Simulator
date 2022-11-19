import java.io.*;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList; //Import the arraylist class
import java.math.BigInteger;
import java.security.MessageDigest; //Import the hash functions
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Rainbow{

  private static ArrayList<String> passwords = new ArrayList<String>();
  private static BigInteger m;

  public static void main (String[] args)
  {
    //Read the password file and store password in an arraylist
    //Code referenced from https://www.w3schools.com/java/java_files_create.asp 
    try {
      File myObj = new File(args[0]);
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        passwords.add(data);
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    
    //Count number of passwords read and report
    System.out.println("Number of passwords read: " + passwords.size());

    //initialise reduction factor
    m = BigInteger.valueOf(passwords.size());
    System.out.println("Reduction factor: " + m);

    RainbowTable rb = new RainbowTable(passwords);
    HackPassword hp = new HackPassword(rb);
  }

  public static BigInteger getM(){return m;} //getter function for reduction mod factor
}

class MD5 {
    
    //function to hash password using MD5
    //Code referenced from https://www.geeksforgeeks.org/md5-hash-in-java/
    public static String getMd5(String input)
    {
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
 
            // digest() method is called to calculate message digest
            // of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());
 
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
 
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
 
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static BigInteger reduction(String hex)
    {
      BigInteger no = new BigInteger(hex, 16);
      return no.mod(Rainbow.getM());
    }
}

class RainbowTable
{
  ArrayList<String> list = new ArrayList<String>();
  HashMap<String, String> initial = new HashMap<String, String>();
  HashMap<String, String> rainbow = new HashMap<String, String>();

  RainbowTable(ArrayList<String> input)
  {
    list = input;
    
    //Create Rainbow Table
    for (int i=0; i<list.size(); i++)
    {
      String tempWord = list.get(i);
      if (isNewWord(tempWord))
      {
        do5(tempWord);
      }
    }

    //Sort Rainbow Table
    rainbow = sortByValue(rainbow);

    //Write to Rainbow.txt
    writeFile(rainbow, "Rainbow.txt");

    //Print number of entries
    System.out.println("Number of entries in Rainbow Table: " + rainbow.size());
  }

  //Function to check if word has been used, if not, mark word as used.
  private boolean isNewWord(String w)
  {
    if(initial.containsKey(w)==false) //if word is new, mark it and return hashvalue for further reduction
    {
      String tempHash = MD5.getMd5(w);
      initial.put(w, tempHash);
      return true;
    }
    return false;
  }

  //Function to conduct reduction, chain the passwords and store in Rainbow Table
  private void do5(String w)
  {
    String currPw = w;
    String currHash = initial.get(currPw);
    for (int i=0; i<4; i++)
    {
      int nextIndex = MD5.reduction(currHash).intValue();
      currPw = list.get(nextIndex);
      isNewWord(currPw);
      currHash = initial.get(currPw);
    }
    rainbow.put(w, currHash);
  }

  // function to sort hash by values
  // code referenced from https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
  private HashMap<String, String> sortByValue(HashMap<String, String> hm)
  {
      // Create a list from elements of HashMap
      List<Map.Entry<String, String> > list =
              new LinkedList<Map.Entry<String, String> >(hm.entrySet());

      // Sort the list
      Collections.sort(list, new Comparator<Map.Entry<String, String> >() {
          public int compare(Map.Entry<String, String> o1,
                              Map.Entry<String, String> o2)
          {
              return (o1.getValue()).compareTo(o2.getValue());
          }
      });
        
      // put data from sorted list to hashmap
      HashMap<String, String> temp = new LinkedHashMap<String, String>();
      for (Map.Entry<String, String> aa : list) {
          temp.put(aa.getKey(), aa.getValue());
      }
      return temp;
  }

  //Function to write Hashmap entries to txt file
  public static void writeFile(HashMap<String, String> map, String filename)
  {
    //Code referenced from https://www.w3schools.com/java/java_files_create.asp
    try {
      File myObj = new File(filename);
      if (myObj.createNewFile()) {
        System.out.println("File created: " + myObj.getName());
      } else {
        System.out.println("File already exists.");
      }
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }

    try {
      FileWriter myWriter = new FileWriter(filename);
      map.forEach((k,v) -> {
        try {
          myWriter.write(k + "," + v + "\n");
        }
        catch (IOException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
      });
      myWriter.close();
      System.out.println("Successfully wrote to the file.");
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  //getter functions for all lists and tables in this class
  public ArrayList<String> getList() {return list;} //getter for password list
  public HashMap<String, String> getInitial() {return initial;}//getter for full hash table
  public HashMap<String, String> getRainbow() {return rainbow;}//getter for rainbow hash table
}

//Class to check if password exists
class HackPassword
{
  ArrayList<String> searchList = new ArrayList<String>();// to store all hashes from the 5 reductions
  ArrayList<String> result = new ArrayList<String>(); // to store all password matches in the rainbow table
  RainbowTable rb; //copy of rainbow table
  String inputHash = ""; //to store the user inputhash for comparison

  HackPassword(RainbowTable rb)
  {
    //initialise variables
    this.rb = rb;
    Scanner s = new Scanner(System.in);  // Create a Scanner object
    System.out.println("Enter hash to check: ");

    //userinput cannot be NULL or will introduce exception
    //read user input and store it
    while(inputHash=="")
    {
    inputHash = s.nextLine();  // Read user input
    }

    //find all hashes from 5 rounds of reduction and store in search list for subsequent processing
    reduce4(inputHash);

    //hack the password and print it out
    String res = hack();
    System.out.println("The pre-image password is: "+ res);
  }

  //function to add all 5 possible reduced hashes into a list for search later
  private void reduce4 (String inputHash)
  {
    searchList.add(inputHash);
    String tempHash = inputHash;
    for(int i=0; i<4; i++)
    {
      try
      {
        int tempIndex = MD5.reduction(tempHash).intValue();//reduce hash to get index to retrieve next password
        String tempPW = rb.getList().get(tempIndex);//retrieve next password
        tempHash = rb.getInitial().get(tempPW);//retrieve corresponding hash of the password
        searchList.add(tempHash);//add to searchlist
      }
      catch (RuntimeException e)
      {
        System.out.println("An error occurred during reduction.");
        e.printStackTrace();
      }
    }
  }

  //Function to hack the password
  private String hack()
  {
    //get all the list from Rainbow class
    ArrayList<String> pwList = rb.getList();
    HashMap<String, String> fullTable = rb.getInitial();
    HashMap<String, String> rainbowTable = rb.getRainbow();
    
    //For the user input hash and each of the 4 hash from subsequent reduction, conduct a search on the rainbow table
    for(String s:searchList)
    {
      //search the rainbow table
      if(rainbowTable.containsValue(s))
      {
        //if exists, find all passwords that matches the hash and store it in a result arraylist
        getKeys(rainbowTable, s);//get all matching pw from rainbow table

        //for each password in the result arraylist, do a search across all 5 reductions to see if a match can be found for the hash
        for(String pw: result)
        {
          String tempPW = pw;
          for (int i=0; i<5; i++)
          {
            String tempHash = fullTable.get(tempPW);
                  
            if(inputHash.compareTo(tempHash)==0) //compare pw hash with user input hash
            {
              return tempPW; //hash match is found, return password
            }
            else //reduce again, get the next password in the chain and try again
            {
              int tempIndex = MD5.reduction(tempHash).intValue();
              tempPW = pwList.get(tempIndex);
            }
          }
        }
      }
    }
    return "Password not in list"; //finally conclude that password is not in the list
  }

  //function to find and return key of hash
  //reference from https://mkyong.com/java/java-get-keys-from-value-hashmap/#get-all-keys-from-hashmap-keyset
  private ArrayList<String> getKeys(HashMap<String, String> map, String value) 
  {
    if (map.containsValue(value)) 
    {
        for (Map.Entry<String, String> entry : map.entrySet()) 
        {
            if (Objects.equals(entry.getValue(), value)) 
            {
                result.add(entry.getKey());
            }
        }
    }
    return result;
  }
}


