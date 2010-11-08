/*Trendy Fire Searcher Needle 1.0
Copyright 2010 Jeremiah B. O'Neal and distributed under the terms of the
GNU General Public License.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

/* Authors
Jeremiah O'Neal - Came up with the main concept for the code and created the first implementation. Currently working to refine and work on bugs.

Main
http://sourceforge.net/projects/projtfsn 

Mirrors
http://nuaitp.net/joneal/tfsn/ - Note, this is a very old repository so do not use unless you can't find the code anywhere else.
*/

//Imports

import java.net.*;
import java.io.*;
import java.util.*;

// Handles logging and displaying messages from the client/server
class Logger
{
 public static String[] report = new String[101]; // Holds messages here
 public static int repcount; // Number of messages stored and left in the above array.
 public static int status;
 Logger(String s)
 {
  if (status == 0)
  {
   repcount = 0;
   for(int i = 0; i <=100; i++)
   {	
    report[i] = ""; // Resets the report
   }
  }	
 }
 public static String reports() // This is used by the debug server to read error and fatal messages
 {
  if (repcount > 0 && status == 1)
  {
   repcount--;
   return report[repcount] + "\n";
  }
  return "";
 }
 // Different message types.
 public static void debug(String debug)
 {
  System.out.println("D: " + debug);
 }
 public static void info(String info)
 {
  System.out.println("I: " +info);
 }
 public static void warn(String warn)
 {
  System.out.println("W: " + warn);
 }
 public static void error(String error)
 {
  System.out.println("E: " + error);
  if (repcount < 100 && status == 1)
  {
   status = 2;
   report[repcount] = error;
   repcount++;
   status = 1;
  }
 }
/***********************************************************
Additional message types can be added using this template.
 public static void error(String error)
 {
  System.out.println("E: " + error);
  if (repcount < 100 && status == 1)
  {
   status = 2;
   report[repcount] = error;
   repcount++;
   status = 1;
  }
 }
************************************************************/

 public static void fatal(String fatal)
 {
  System.out.println("F: " + fatal);
  if (repcount < 100 && status == 1)
  {
   status = 3;
   report[repcount] = fatal;
   repcount++;
   status = 1;
  }
 }
}
//TFSNData contains a bunch of global varables that different classes needs access to.
class TFSNData
{
 // joneal a bunch of global varables
 public static String[] banned = new String[10];
 public static String[] addresses = new String[10];
 public static String[] port = new String[10];
 public static String[] remotebanned = new String[10];
 // function. Does not do anything.
 public TFSNData()
 {
 }
 public static int lr(int x, int y)
 {
  String customMsg = "TFSNData ";
  if(x<=0 || x >= 10 || y <= 0 || y >= 10)
  {
   Logger.debug(customMsg.toString() + "lr prevented reading a undefined portion of the array".toString());
   return 0;	
  }
  try
  {
   if(banned[x].equals(remotebanned[y]))
   {
    Logger.debug(customMsg.toString() + "Not adding bans because it already exist".toString());
    return 0;
   }
  }
  catch(NullPointerException e)
  {
   Logger.error(e.toString());
  }
  Logger.debug(customMsg.toString() + "Adding ban because it does not exist yet".toString());
  return 1;
  }
 }
// This is the server socket
class TFSNServerSoc implements Runnable
{
 Thread TSN;
 Socket socket;
 TFSNServerSoc()
 {
  TFSNServerSocGo();
 }
 public void TFSNServerSocGo()
 {
  String customMsg = "TFSNClientSoc "; // A custom message to help the user know where the log is reporting from
  while(TFSNData.port[0] == "0")
   Logger.debug(customMsg.toString() + "Reading server port".toString());
  int port = Integer.parseInt(TFSNData.port[0].trim());
  while(true) //inf loop. This will prevent added threads in the main statement from running.
  {
   try
   {
    Thread.sleep(5000);
    TSN = new Thread(this, "TFSN ServerSoc");
    ServerSocket srv = new ServerSocket(port);
    Logger.debug(customMsg.toString() + "Listening".toString());
    socket = srv.accept();
    Logger.debug(customMsg.toString() + "Connection accepted".toString());
    TSN.start();
   }
   catch (IOException e)
   {
    Logger.error(customMsg.toString() + e.toString());
   }
   catch (InterruptedException e)
   {
    Logger.error(customMsg.toString() + e.toString());
   }
  }
 }
 public void run()
 {
  String customMsg = "TFSNClientSoc thread ";
  Logger.debug(customMsg.toString() + "Creating Client Socket thread".toString());
  try
  {
   BufferedReader rd = new BufferedReader (new InputStreamReader(socket.getInputStream()));
   BufferedWriter wr = new BufferedWriter (new OutputStreamWriter(socket.getOutputStream()));
   String str = ".";
   String cmdp = "n";
   String cmds = "n";
   int stage = 0;
   while(str != null)
   {
    Thread.sleep(1000);
    try
    {
     str = rd.readLine();
    }
    catch(IOException e)
    {
     Logger.error(e.toString());
    }
    if(stage == 0)
    {
     Logger.debug(customMsg.toString() + "Stage 0".toString());
     if(str.equals("[") || str.equals("("))
     {
      cmdp = str;
      Logger.debug(customMsg.toString() + "Received commmand staring with".toString());
     }
     if(str.equals("]") || str.equals(")"))
     {
      cmds = str;
      Logger.debug(customMsg.toString() + "Received command ending with".toString());
     }
     if(str.equals(cmdp + "yes" + cmds))
     {
      Logger.debug(customMsg.toString() + "Validated".toString());
      stage = 1;
     }
    }
    if(stage == 1)
    {
    Logger.debug(customMsg.toString() + "Stage 1".toString());
    wr.write(cmdp + "good" + cmds + "\n");
    wr.flush();
    stage = 2;
    }
    if(stage == 2)
    {
     Logger.debug(customMsg.toString() + "Stage 2".toString());
     wr.write(cmdp + "down" + cmds + "\n");
     wr.flush();
     Logger.debug(customMsg.toString() + "Telling the client to throttle down".toString());
     stage = 3;
    }
    if(stage >= 3 && stage <= 8)
    {
     wr.write(cmdp + "GA" + cmds + "\n");
     wr.flush();
     Logger.debug(customMsg.toString() + "Waiting...".toString());
     stage++;
    }
    if(stage == 8)
    {
     Logger.debug(customMsg.toString() + "Stage 8".toString());
     wr.write(cmdp + "hold" + cmds + "\n");
     wr.flush();
     wr.write(cmdp + "log1" + cmds + "\n");
     wr.flush();
     wr.write("Welcome!\n");
     wr.flush();
     wr.write(cmdp + "log0" + cmds + "\n");
     wr.flush();
     wr.write(cmdp + "GA" + cmds + "\n");
     wr.flush();
     stage = 9;
    }
    if(stage == 9)
    {
     if(str.equals(cmdp + "refresh" + cmds))
     {
      wr.write(cmdp + "banned" + cmds + "\n");
      wr.flush();
      for(int i = 0; i < TFSNData.banned.length; i++)
      {
       wr.write(TFSNData.banned[i] + "\n");
       wr.flush();
      }
      wr.write(cmdp + "GA" + cmds + "\n");
      wr.flush();
     }
     else
     {
      wr.write(cmdp + "GA" + cmds + "\n");
      wr.flush();
     }
    
    }
   }
  }
  catch (IOException e)
  {
   Logger.debug(e.toString());
  }
  catch(InterruptedException e)
  {
   Logger.debug(e.toString());
  }
 }
}
// This is the client socket that connects to remote servers and queries them of information
class TFSNClientSoc implements Runnable
{
 Thread TSN;
 Socket sock;
 int nndnum;
 TFSNClientSoc()
 {
  TFSNClientSocGo();
 }
 public void TFSNClientSocGo()
 {
  String customMsg = "TFSNClientSoc "; // A custom message to help the user know where the log is reporting from
  Logger.debug(customMsg.toString() + "Client socket started".toString());
  nndnum = 1;
  Logger.debug(customMsg.toString() + "Reading properties file".toString());
  // Properties file is a file which contains all the addresses that TFSN needs to connect to. It can be edited by the user in notepad.
  Properties properties = new Properties();
  try
  {
   properties.load(new FileInputStream("tfsn.ini"));
  }
  catch (IOException e)//This happens if tfsn.ini is missing
  {
   Logger.info(customMsg.toString() + "Could not find tfsn.ini. Please create that file and restart this program.".toString());
   Logger.error(e.toString());
   Logger.fatal(customMsg.toString() + "Client Socket failed because tfsn.ini does not exist".toString());
  }
  String connections = properties.getProperty("connections"); //joneal connections is the number of addresses in the tfsn.ini file
  int x = Integer.parseInt(connections.trim());
  Logger.debug(customMsg.toString() + "Reading addr1..10s from tfsn.ini file".toString());
  // This reads all of the addresses
  TFSNData.addresses[0] = properties.getProperty("address");
  TFSNData.port[0] = properties.getProperty("port");
  if (x >= 1)
  {
   TFSNData.addresses[1] = properties.getProperty("addr1");
   TFSNData.port[1] = properties.getProperty("port1");
   Logger.debug(customMsg.toString() + "Adding addr1".toString());
  }
  if (x >= 2)
  {
   TFSNData.addresses[2] = properties.getProperty("addr2");
   TFSNData.port[2] = properties.getProperty("port2");
   Logger.debug(customMsg.toString() + "Adding addr2".toString());
  }
  if (x >= 3)
  {
   TFSNData.addresses[3] = properties.getProperty("addr3");
   TFSNData.port[3] = properties.getProperty("port3");
   Logger.debug(customMsg.toString() + "Adding addr3".toString());
  }
  if (x >= 4)
  {
   TFSNData.addresses[4] = properties.getProperty("addr4");
   TFSNData.port[4] = properties.getProperty("port4");
   Logger.debug(customMsg.toString() + "Adding addr4".toString());
  }
  if (x >= 5)
  {
   TFSNData.addresses[5] = properties.getProperty("addr5");
   TFSNData.port[5] = properties.getProperty("port5");
   Logger.debug(customMsg.toString() + "Adding addr5".toString());
  }
  if (x >= 6)
  {
   TFSNData.addresses[6] = properties.getProperty("addr6");
   TFSNData.port[6] = properties.getProperty("port6");
   Logger.debug(customMsg.toString() + "Adding addr6".toString());
  }
  if (x >= 7)
  {
   TFSNData.addresses[7] = properties.getProperty("addr7");
   TFSNData.port[7] = properties.getProperty("port7");
   Logger.debug(customMsg.toString() + "Adding addr7".toString());
  }
  if (x >= 8)
  {
   TFSNData.addresses[8] = properties.getProperty("addr8");
   TFSNData.port[8] = properties.getProperty("port8");
   Logger.debug(customMsg.toString() + "Adding addr8".toString());
  }
  if (x >= 9)
  {
   TFSNData.addresses[9] = properties.getProperty("addr9");
   TFSNData.port[9] = properties.getProperty("port9");
   Logger.debug(customMsg.toString() + "Adding addr9".toString());
  }
  if (x >= 10)
  {
   TFSNData.addresses[10] = properties.getProperty("addr10");
   TFSNData.port[10] = properties.getProperty("port10");
   Logger.debug(customMsg.toString() + "Adding addr10".toString());
  }
  /************************************************************************
  More addresses can be added by adding this line and changing 10 to 11.
  if (x >= 10)
  {
   TFSNData.addresses[10] = properties.getProperty("addr10");
   logger.debug(customMsg.toString() + "Adding addr10".toString());
  }
  *************************************************************************/
  // This loops through all of the addresses starting them in their own thread.
  for(int i = 0; i < x; i++)
  {
   try
   {        
    TSN = new Thread(this, "TFSN ClientSoc Description");
    TSN.start();
    Thread.sleep(500);
   }
   catch (InterruptedException e)
   {
    Logger.error(e.toString());
   }
  }
 }	
 // This is the thread for each individual address.
 public void run()
 {
  String customMsg = "TFSNClientSoc thread ";
  Logger.debug(customMsg.toString() + "Creating Client Socket thread".toString());
  try
  {
   InetAddress addr = InetAddress.getByName(TFSNData.addresses[nndnum]);
   int port = Integer.parseInt(TFSNData.port[nndnum].trim());
   Logger.debug(customMsg.toString() + "Creating new client socket".toString());
   Socket sock = new Socket(addr, port);
   TFSNData.remotebanned[0] = "open"; // This tells anything that's reading this array that this thread is not using it.
   Logger.debug(customMsg.toString() + "Adding command prefix and command suffix".toString());
   // Command prefix and command suffix are attached to any commands that this client sends and receives. The client is in charge of telling the
   // server what command prefix and suffix it wants to use. This can be changed to cmdp [, (. cmds ], ).
   String cmdp = "[";
   String cmds = "]";
   String str = cmdp + "new" + cmds;
   Logger.debug(customMsg.toString() + "Intilizing varables".toString());
   // Varables that are initalize
   int i = 0;
   int throttle = 0;
   int logging = 0;
   int kill = 0;
   int setthrottle = 1000;
   BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
   BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream()));
   Logger.debug(customMsg.toString() + "Creating buffers".toString());
   // This loops until the server sends [good] after the server receives [yes] from this client.
   while (! str.equals(cmdp + "good" + cmds))
   {
    Logger.debug(customMsg.toString() + "Running pre suf checker".toString());
    try
    {
     sock.setSoTimeout(5000);
     str = rd.readLine();
    }
    catch(IOException e)// A IOException will occur when the client isn't receiving any data from the server for 5sec.
    {
     Logger.error(e.toString());
    }
    if(str.equals(cmdp + "new" + cmds)) // This continues to loop until [good] is received.
    {
     Logger.debug(customMsg.toString() + "Sending command prefix".toString());
     wr.write(cmdp + "\n"); // The prefix that the client tells the server to use
     wr.flush();
     Logger.debug(customMsg.toString() + "Sending command suffix".toString());
     wr.write(cmds + "\n"); // The suffix that the client tells the server to use
     wr.flush();
     Logger.debug(customMsg.toString() + "Sending prefix suffix and yes to accept the prefix and suffix for future commands".toString());
     wr.write(cmdp + "yes" + cmds + "\n"); /* A combination of the prefix and suffix. If the server understands this as a command then the server will
                                              reply with prefix good suffix or [good]*/
     wr.flush();
    }
   }
   while (str != null && kill == 0)
   {					
    Logger.debug(customMsg.toString() + "Reseting string".toString());
    Logger.info(customMsg.toString() + "Client started".toString());
    str = "[CA]"; // This clears the string with the CA command.
    Logger.debug(customMsg.toString() + "Refresh code sent".toString());
    wr.write(cmdp + "refresh" + cmds + "\n"); // The client will send [refresh] to the server until the server sends a command or the banlist.
    wr.flush();
    try
    {
     Thread.sleep(setthrottle); //This is a throttle to slow the client down by every second
     if (throttle >= 1 && throttle < 10000) // Throttle should stay inbetween 1 and 10sec per loop.
     {
      Logger.debug(customMsg.toString() + "Throttling connection".toString());
      setthrottle = setthrottle + throttle; // This increases the throttle by what throttle is set to.
     }
     i = -1; // Makes sure that the next if statement does not run
     while (! str.equals(cmdp+ "GA" + cmds)) // The client will expect more data from the server until it receives [GA]
     {
      Logger.debug(customMsg.toString() + "Waiting for server to go ahead - Looping".toString());
      if (i != -1) // This will not be -1 if the banlist is coming through.
      {
       i++;
       Logger.debug(customMsg.toString() + "Testing to see whether remotebanned is being accessed".toString());
       //This checks to make sure that nothing else is using this array including another thread of this.
       if(TFSNData.remotebanned[0] == "unlock" || TFSNData.remotebanned[0] == "adding" )
       {
        Logger.debug(customMsg.toString() + "Testing to see whether remotebanned is being accessed by this own thread".toString());
        if(TFSNData.remotebanned[0] == "unlock")
        {
         Logger.debug(customMsg.toString() + "Locking remotebanned array".toString());
         TFSNData.remotebanned[0] = "adding"; // This locks the array so nothing else tries to access it.
        }
        Logger.debug(customMsg.toString() + "Checking remotebanned length".toString());
        if(i < TFSNData.remotebanned.length)
        {
         Logger.debug(customMsg.toString() + "Writing a ban to remotebanned array".toString());
         TFSNData.remotebanned[i] = str; // This is writing the banlist from the server to remotebanned array which contains the servers banlist
        }
        else
        {
         Logger.debug(customMsg.toString() + "Unlocking remotebanned array".toString());
         TFSNData.remotebanned[0] = "unlock"; // Once done, the array is unlocked
        }
       }
      }
      if (str.equals(cmdp+ "banned"+ cmds)) // This is sent by the server to tell the client that the next data sent is the servers banlist
      {
       Logger.debug(customMsg.toString() + "Banned command received".toString());
       i++;
      }
      if (str.equals(cmdp + "down" + cmds)) // This is a throttle down command sent by the server to tell the client to slow down
      {
       Logger.debug(customMsg.toString() + "Throttle down command received".toString());
       throttle = 1000;
      }
      if (str.equals(cmdp + "hold" + cmds)) // This is a hold command sent a few rotatations after [down] to tell the client to stop slowing down. 
      {
       Logger.debug(customMsg.toString() + "Throttle hold command received".toString());
       throttle = 0;
      }
      if (str.equals(cmdp + "log1" + cmds)) // This tells the client that the server wants to send text messages that can be read by the user.
      {
       Logger.debug(customMsg.toString() + "Server message log enabled".toString());
       logging = 1;
      }
      if (str.equals(cmdp + "log0" + cmds)) // This tells the client that the server no longer wants to send text messages.
      {
       Logger.debug(customMsg.toString() + "Server message log disabled".toString());
       logging = 0;
      }
      if (str.equals(cmdp + "kill" + cmds)) // This is a remote kill command that tells the client to disconnect from the server.
      {
       Logger.debug(customMsg.toString() + "Kill command received".toString());
       Logger.info(customMsg.toString() + "Killed by server".toString());
       kill = 1;
      }
      /*
      Additional commands can be added simply by using this as a template
      if (str.equals(cmdp + "command" + cmds)) // This is a remote kill command that tells the client to disconnect from the server.
      {
       logger.debug(customMsg.toString() + "Kill command received".toString());
       logger.info(customMsg.toString() + "Killed by server".toString());
       add anything you want the command to do here.
      }
      */
      if (logging == 1) // When the client receives [log1]. The server can then talk to the user here.
      {
       Logger.info(customMsg.toString() + "By server: " + str.toString());
      }
      Logger.debug(customMsg.toString() + "Waiting for server to send a command".toString());
      str = rd.readLine();
     }
    }
    catch(IOException e) // This is a timeout for the readline command. This may happen frequently.
    {
     Logger.error(e.toString());
    }
    catch(InterruptedException e)
    {
     Logger.debug(customMsg.toString() + "This is not an error. A timeout was sent because nothing was received.".toString());
     Logger.error(e.toString());
    }
   }
   Logger.debug(customMsg.toString() + "Closing connection".toString());
   Logger.info(customMsg.toString() + "Client disconnected".toString());
   rd.close();
  }
  catch (IOException e)
  {
   Logger.error(e.toString());
  }      
 }
}
// This class manages the Reading and writing of the banned.txt file
class TFSNRWFile implements Runnable
{
 Thread TSN;
 TFSNRWFile()
 {
  TFSNRWFileGo();
 }
 public void TFSNRWFileGo()
 {
  String customMsg = "TFSNRWFile ";
  // This is clearing the banned and remotebanned array.
  for (int x = 0; x < TFSNData.banned.length; x++)
  {
   Logger.fatal(customMsg.toString() + "Defining banned array".toString());
   TFSNData.banned[x] = "myname"; // This is the default name used to clear the banned and remotebanned array.
  }
  for (int x = 0; x < TFSNData.remotebanned.length; x++)
  {
   Logger.fatal(customMsg.toString() + "Defining remote banned array".toString());
   TFSNData.remotebanned[x] = "myname";
  }	
  // This starts the thread.
  TSN = new Thread(this, "TFSN Readwritefile Description");
  TSN.start();
 }
 public void run()
 {
  String customMsg = "TFSNRWFile thread ";
  Logger.debug(customMsg.toString() + "Read Write thread started".toString());
  Logger.debug(customMsg.toString() + "Unlocking ban array".toString());
  // This unlocks the array so other classes can use them.
  TFSNData.remotebanned[0] = "open"; // joneal
  TFSNData.remotebanned[0] = "unlock";
  // This sets in and out as buffered.
  // try //joneal?
  BufferedReader in;
  BufferedWriter out; 
  while(true) // Continues to loop this area.
  {
   Logger.debug(customMsg.toString() + "Looping".toString());
   try
   {
    Thread.sleep(1500);
    in = new BufferedReader(new FileReader("banned.txt")); // This opens banned.txt for reading
    Logger.debug(customMsg.toString() + "Creating buffer".toString());
    Logger.debug(customMsg.toString() + "Initalizing varables".toString());
    // This initalizes some varables
    String str;
    int i = 0;
    while(TFSNData.remotebanned[0] == "adding")
     Logger.debug(customMsg.toString() + "Locked".toString()); // This is looped if another class function is using this array.
    Logger.debug(customMsg.toString() + "Locking".toString());
    TFSNData.remotebanned[0] = "lock"; // This tells all other class functions that this function will be using the array.
    while ((str = in.readLine()) != null) // This reads the entire banned.txt file.
    {
     Logger.debug(customMsg.toString() + "Reading file".toString());
     if(i < TFSNData.banned.length)
     {
      TFSNData.banned[i] = str; // This takes the banned.txt file and places it into the local banned array from this client/server.
      i = i + 1;
      for(int x = 0; x < TFSNData.remotebanned.length; x++)
      {
       Logger.debug(customMsg.toString() + "Comparing banned to remote banned".toString());
       if(TFSNData.lr(i,x) == 0) // This is telling TFSNData.lr to read the banned and remotebanned array and see if any are the same
       {
        Logger.debug(customMsg.toString() + "Duplicate found".toString());
        TFSNData.remotebanned[x] = "myname"; // If this client/server already has the banned name then it renames the remotebanned name to myname.
       }
      }
     }
    }
    for (; i < TFSNData.banned.length; i++)
    {
     Logger.debug(customMsg.toString() + "Clearing the rest".toString());
     TFSNData.banned[i] = "myname"; // Any additional banned names from previous uses of this section are changed to myname
    }
    Logger.debug(customMsg.toString() + "Closing file".toString());
    in.close(); // The file is closed
    out = new BufferedWriter(new FileWriter("banned.txt", true)); // This opens the file banned.txt from appending (true)
    for (int x = 0; x < TFSNData.remotebanned.length; x++)
    {
     Logger.debug(customMsg.toString() + "Found a new ban candidate".toString());
     if(TFSNData.remotebanned[x] != "myname") // Checks to see if this is not a duplicate or a name that already exist. myname.
     {
      System.out.println(TFSNData.remotebanned[x]);
      out.write(TFSNData.remotebanned[x] + "\n");// Writes the name to banned.txt
      Logger.info(customMsg.toString() + TFSNData.remotebanned[x].toString());
      TFSNData.remotebanned[x] = "myname";
     }
    }
    out.close(); // Closes banned.txt file
    Logger.debug(customMsg.toString() + "Unlocking array".toString());
    TFSNData.remotebanned[0] = "unlock"; // Unlocks the array so other functions can use it.
   }
   catch (InterruptedException e)
   {
    Logger.error(e.toString());
   }
   catch (IOException e)
   {
    Logger.error(e.toString());
   } // joneal: add to finally, in.close() and out.close()
  }
 }
}
class TFSNLogclient implements Runnable
{
 Thread TSN;
 Socket client;
 BufferedWriter wr;
 TFSNLogclient()
 {
  TFSNLogclientGo();
 }
 // This is the log client that sends error and fatal messages to the debug server.
 public void TFSNLogclientGo()
 {
  TSN = new Thread(this, "TFSN Log Description");
  TSN.start();
 }
 public void run()
 {
  try
  {
   InetAddress addr = InetAddress.getByName("we6jbo.mine.nu"); // This connects to we6jbo.mine.nu, a remote debug server.
   client = new Socket(addr, 1965);
   wr = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
   while(true){ wr.write(Logger.reports()); wr.flush();} // This requests error messages from the logger and sends them to the server.
  }
  /* A debug server can be created with the following code
     (Add debug server code here)
  */
  catch(IOException e)
  {
  }
 }
}

// TFSN starts here
public class tfsn
{
 // Main function for TFSN
 public static void main(String[] args)
 {
  Logger.status = 1; // joneal This gets all the functions in the logger class to work.
  TFSNData.port[0] = "0";
  new TFSNData(); // joneal This is a class containing a bunch of globals. Nothing should happen here.
  new TFSNClientSoc(); // This starts the client socket
  new TFSNRWFile();
  new TFSNLogclient();
  new TFSNServerSoc();
try
{
Thread.sleep(1000);
}
catch (InterruptedException e)
{
}
}
}