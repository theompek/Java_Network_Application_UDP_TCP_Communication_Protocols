/*
Ergasia Diktya 2 
Java DatagramSocket Programming
Onoma Foithth: Mpekiaris Theofanis AEM:8200
Etos: 2018
*/
package userApplication;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.lang.System;

public class userApplication {
	
public static void main(String[] args) throws IOException{
//Ports και κωδικοί απο την ιθάκη
  int clientPort = 48021;
  int serverPort = 38021;
  int ithakiCopterServerPort = 38048;
  int OBDserverPort = 29078 ;
  String echoRequestCode = "E1825";
  String imageRequestCode = "M9578";
  String soundRequestCode = "A6372";

//----- Echo----//
   echoFunction(echoRequestCode,serverPort,clientPort); //Πακέτα που έρχονται με κάποια καθυστέρηση
   echoFunction("E0000T00",serverPort,clientPort);         //Πακέτα χωρίς καθυστέρηση

//-----Image----//     
   String iRCode_PTZ = imageRequestCode + "CAM=PTZUDP=1024";
   String iRCode_FIX = imageRequestCode + "CAM=FIXUDP=1024";
 
   imageFunction(8200,iRCode_FIX,serverPort,clientPort);         //Default camera CAM=FIX
   imageFunction(8200,iRCode_PTZ+"DIR=C",serverPort,clientPort); //Camera CAM=PTZ center
   imageFunction(8200,iRCode_PTZ+"DIR=U",serverPort,clientPort); //Camera CAM=PTZ up
   imageFunction(8200,iRCode_PTZ+"DIR=C",serverPort,clientPort); //Camera CAM=PTZ center
   imageFunction(8200,iRCode_PTZ+"DIR=D",serverPort,clientPort); //Camera CAM=PTZ down
   imageFunction(8200,iRCode_PTZ+"DIR=C",serverPort,clientPort); //Camera CAM=PTZ center
   imageFunction(8200,iRCode_PTZ+"DIR=L",serverPort,clientPort); //Camera CAM=PTZ left
   imageFunction(8200,iRCode_PTZ+"DIR=C",serverPort,clientPort); //Camera CAM=PTZ center
   imageFunction(8200,iRCode_PTZ+"DIR=R",serverPort,clientPort); //Camera CAM=PTZ right
   
//----Sound----//
   int numOfSoundPackets = 100; //Αριθμός μουσικών πακέτων
   boolean AQ_DPCM = true;     //Επιλογή κωδικοποίησης AQ_DPCM ή DPCM
   int Qbits = 8;             //Αριθμός bit κβαντιστή για DPCM	
   String sRCodeΤ_DPCM = soundRequestCode + "T"+Integer.toString(numOfSoundPackets);          //Γεννήτρια Συχνοτήτων
   String sRCodeΤ_AQ_DPCM = soundRequestCode +"AQ" + "T"+Integer.toString(numOfSoundPackets); //Γεννήτρια Συχνοτήτων
   String sRCodeF1_AQ_DPCM = soundRequestCode +"AQ" +"L30"+ "F"+Integer.toString(numOfSoundPackets); //Mουσικό κομμάτι1(audio clip1)
   String sRCodeF2_AQ_DPCM = soundRequestCode +"AQ" +"L36"+"F"+Integer.toString(numOfSoundPackets); //Mουσικό κομμάτι2(audio clip2)
	
   soundDPCM_OR_AQDPCM(sRCodeΤ_DPCM,serverPort,clientPort,numOfSoundPackets,!AQ_DPCM,Qbits );    //DPCM κωδικοποίηση,από γεννήτρια συχν.
   soundDPCM_OR_AQDPCM(sRCodeΤ_AQ_DPCM,serverPort,clientPort,numOfSoundPackets,AQ_DPCM,Qbits );  //AQ_DPCM κωδικοποίηση,από γεννήτρια συχν.
   soundDPCM_OR_AQDPCM(sRCodeF1_AQ_DPCM,serverPort,clientPort,numOfSoundPackets,AQ_DPCM,Qbits ); //AQ_DPCM κωδικοποίηση,μουσικό κομμάτι1(audio clip1)
   soundDPCM_OR_AQDPCM(sRCodeF2_AQ_DPCM,serverPort,clientPort,numOfSoundPackets,AQ_DPCM,Qbits ); //AQ_DPCM κωδικοποίηση,μουσικό κομμάτι2(audio clip2)
 
//----IthakiCopter----//  
   int flightLevel_1 = 170;   //Επιθυμητό ύψος πτήσης του IthakiCopter
   
   int flightLevel_2 = 200;   //Επιθυμητό ύψος πτήσης του IthakiCopter
  
   ithakicopter(ithakiCopterServerPort,flightLevel_1);
   try{TimeUnit.SECONDS.sleep(20);}catch(Exception x) { System.out.println(x);}
   ithakicopter(ithakiCopterServerPort,flightLevel_2);

//----OBD-----//  
   OBDfunction(OBDserverPort);
//
}//telos main

static public void clientRequestDatagram(String requestCode,int serverPort) throws IOException{
	//Request προς τον server 
    byte[] txbuffer = requestCode.getBytes();
	
	//Δημιουργία socket για την αποστολή πακέτων προς τον server
	DatagramSocket s = new DatagramSocket();
	byte[] hostIP = { (byte)155,(byte)207,(byte)18,(byte)208 };
	InetAddress hostAddress = InetAddress.getByAddress(hostIP);
	DatagramPacket p = new DatagramPacket(txbuffer,txbuffer.length, hostAddress,serverPort);
	 try {
		 s.send(p);
	     s.close();
     } catch (IOException x) {System.out.println(x);}

}

static public DatagramPacket serverResponseDatagramPacket(int clientPort) throws IOException{
	   //Δημιουργεία socket για την παραλαβή των πακέτων απο τον εικονικό server της ιθάκης
	   DatagramSocket r = new DatagramSocket(clientPort);
	   r.setSoTimeout(10000);
	   byte[] rxbuffer = new byte[2048];
	   DatagramPacket packet = new DatagramPacket(rxbuffer,rxbuffer.length);	
	   try {
			r.receive(packet);
			} catch (Exception x) {
			System.out.println(x);
			}
	   r.close();
	return packet;
	}

static public void echoFunction(String requestCode,int serverPort,int clientPort) throws IOException{
File echoFileName  = new File("echoFile_" + requestCode + ".txt");
FileOutputStream echoFile = new FileOutputStream(echoFileName);
File echoThroughput8FileName  = new File("echoThroughput8File_" + requestCode + ".txt");
FileOutputStream echoThroughput8File = new FileOutputStream(echoThroughput8FileName);
File echoThroughput16FileName  = new File("echoThroughput16File_" + requestCode + ".txt");
FileOutputStream echoThroughput16File = new FileOutputStream(echoThroughput16FileName);
File echoThroughput32FileName  = new File("echoThroughput32File_" + requestCode + ".txt");
FileOutputStream echoThroughput32File = new FileOutputStream(echoThroughput32FileName);

try { //Perigrafh periexomenou arxeiwn
	   echoFile.write(("Mhnuma kai xronos ka8usterhshs se millis").getBytes());
	   echoFile.write((char) 13); //newline
	   echoThroughput8File.write(("Ari8mos paketwn ana 8sec kai h ru8mapososh pou prokuptei").getBytes());
	   echoThroughput8File.write((char) 13); //newline
	   echoThroughput16File.write(("Ari8mos paketwn ana 16sec kai h ru8mapososh pou prokuptei").getBytes());
	   echoThroughput16File.write((char) 13); //newline
	   echoThroughput32File.write(("Ari8mos paketwn ana 32sec kai h ru8mapososh pou prokuptei").getBytes());
	   echoThroughput32File.write((char) 13); //newline
    }
    catch (Exception x) {
	   	System.out.println(x);
     }


DatagramPacket packet = null;
String message = null;
long time4min = 4*60*1000; //4 λεπτα
long time8sec = 8*1000;    //8 sec
long time16sec = 16*1000;  //16 sec
long time32sec = 32*1000;  //32 sec
long timeTotalStart = 0, timeTotalEnd = 0, timePacketStart = 0, timePacketEnd = 0; //Συνολικός χρόνος,χρόνος λείψης πακέτου
long time8secStart = 0 , time16secStart = 0 , time32secStart = 0; //Χρόνοι για τον υπολογισμό της ρυθμαπόδοσης 
int numOfPackets8sec = 0; //Αριθμός πακέτων που λαμβάνονται ανά 8,16,32 seconds
int numOfPackets16sec = 0;
int numOfPackets32sec = 0;

timeTotalStart = System.currentTimeMillis();
time8secStart = timeTotalStart;
time16secStart = timeTotalStart;
time32secStart = timeTotalStart;

while(timeTotalEnd - timeTotalStart < time4min) 
  { 
	//Request
   clientRequestDatagram(requestCode,serverPort);  

   //Response
   timePacketStart = System.currentTimeMillis();  		
    packet = serverResponseDatagramPacket(clientPort); 
   timePacketEnd = System.currentTimeMillis();
  
   message = new String(Arrays.copyOfRange(packet.getData(),0,packet.getLength()));
   System.out.println(message + " " );
   System.out.println(timePacketEnd-timePacketStart);
  
   //Γράψε τα δεδομένα και τον χρόνο απόκρισης στο αρχείο για διαγραμμα G1
   try {
	   echoFile.write(Arrays.copyOfRange(packet.getData(),0,packet.getLength()));
	   echoFile.write((" " + String.valueOf(timePacketEnd-timePacketStart)).getBytes());
	   echoFile.write((char) 13); //newline
       }
       catch (Exception x) {
   	   	System.out.println(x);
        break;
        }
   
   //Ρυθμαπόδοση για 8sec
   if(timeTotalEnd-time8secStart < time8sec){
	   numOfPackets8sec++;
   }else{
	 //Γράψε τον αριθμό των πακέτων στο αρχείο και την ρυθμαπόδοση που προκύπτει
	   try {
		   echoThroughput8File.write(String.valueOf(numOfPackets8sec).getBytes());
		   //Throughput = 32byte*8bit*numOfPackets8sec*1000/time_se_millis
		   long Throughput = (32*8*numOfPackets8sec*1000)/(timeTotalEnd-time8secStart);
		   echoThroughput8File.write((" " + String.valueOf(Throughput) + " bps").getBytes());
		   echoThroughput8File.write((char) 13); //newline
	       }
	       catch (Exception x) {
	   	   	System.out.println(x);
	        break;
	        }
	   //Αρχικοποίηση μεταβλητών για την επόμενη μέτρηση των 8sec
	   time8secStart = System.currentTimeMillis();
	   numOfPackets8sec = 0;
   }//telos gia 8sec
   
  //Ρυθμαπόδοση για 16sec
   if(timeTotalEnd-time16secStart < time16sec){
	   numOfPackets16sec++;
   }else{
	 //Γράψε τον αριθμό των πακέτων στο αρχείο και την ρυθμαπόδοση που προκύπτει
	   try {
		   echoThroughput16File.write(String.valueOf(numOfPackets16sec).getBytes());
		   //Throughput = 32byte*8bit*numOfPackets8sec*1000/time_se_millis
		   long Throughput = (32*8*numOfPackets16sec*1000)/(timeTotalEnd-time16secStart);
		   echoThroughput16File.write((" " + String.valueOf(Throughput) + " bps").getBytes());
		   echoThroughput16File.write((char) 13); //newline
	       }
	       catch (Exception x) {
	   	   	System.out.println(x);
	        break;
	        }
	   //Αρχικοποίηση μεταβλητών για την επόμενη μέτρηση των 8sec
	   time16secStart = System.currentTimeMillis();
	   numOfPackets16sec = 0;
   }//telos gia 16sec
   
 //Ρυθμαπόδοση για 32sec
   if(timeTotalEnd-time32secStart < time32sec){
	   numOfPackets32sec++;
   }else{
	 //Γράψε τον αριθμό των πακέτων στο αρχείο και την ρυθμαπόδοση που προκύπτει
	   try {
		   echoThroughput32File.write(String.valueOf(numOfPackets32sec).getBytes());
		   //Throughput = 32byte*8bit*numOfPackets8sec*1000/time_se_millis
		   long Throughput = (32*8*numOfPackets32sec*1000)/(timeTotalEnd-time32secStart);
		   echoThroughput32File.write((" " + String.valueOf(Throughput) + " bps").getBytes());
		   echoThroughput32File.write((char) 13); //newline
	       }
	       catch (Exception x) {
	   	   	System.out.println(x);
	        break;
	        }
	   //Αρχικοποίηση μεταβλητών για την επόμενη μέτρηση των 8sec
	   time32secStart = System.currentTimeMillis();
	   numOfPackets32sec = 0;
    }//telos gia 32sec
   
  timeTotalEnd = System.currentTimeMillis();
  }//telos while	

//Κλείνουμε τα stream
echoFile.close();
echoThroughput8File.close();
echoThroughput16File.close();
echoThroughput32File.close();
}

static public void imageFunction(int imageId,String requestCode,int serverPort,int clientPort) throws IOException{
File imageFileName  = new File("imageFile"+imageId+"_" + requestCode + ".jpg");
FileOutputStream imageFile = new FileOutputStream(imageFileName);
requestCode += "FLOW=ON";
DatagramPacket packet = null;
boolean startFind = false; 
boolean endFind = false; 
byte[] beginning = { (byte)0xFF , (byte)0xD8 };   //Χαρακτήρες αρχής εικόνας
byte[] termination = { (byte)0xFF , (byte)0xD9 }; //Χαρακτήρες τέλους εικόνας
  
//Request
clientRequestDatagram(requestCode,serverPort);  
System.out.println("Wait Servo to move");

while(!startFind){ //Αναζήτηση των χαρακτήρων αρχής στα packets μέχρι να βρούμε την αρχή της εικόνας 
 packet = serverResponseDatagramPacket(clientPort); //Response 
 byte[] messegeBytes = Arrays.copyOfRange(packet.getData(),0,packet.getLength());
 
 for(int i = 0;i<messegeBytes.length-1;i++){ //Εύρεση χαρακτήρων αρχής
  if(messegeBytes[i] == beginning[0] && messegeBytes[i+1] == beginning[1]){
	  System.out.println("The image beggining was found");
	//Αποθήκευση πακέτων εικόνας απο τους χαρακτήρες αρχής και μετά
   try{
	  imageFile.write(Arrays.copyOfRange(messegeBytes, i, messegeBytes.length));	  
	  }
	  catch (Exception x) {System.out.println(x); }
	 startFind = true;
	 break;
   } 
  }
 
 clientRequestDatagram("NEXT",serverPort);
 }
 
System.out.println("The image transfer was started");

while(!endFind){
	//Response
	packet = serverResponseDatagramPacket(clientPort); //Response
	
	  byte[] messegeBytes = Arrays.copyOfRange(packet.getData(),0,packet.getLength());
	  for(int i = 0; i<messegeBytes.length-1;i++){ //Ψαξε τους χαρακτήρες τέλους 
	   if(messegeBytes[i] == termination[0] && messegeBytes[i+1] == termination[1]){
		   System.out.println("The end of the image was found");
			//Αποθήκευση πακέτου μέχρι και τους χαρακτήρες τέλους
		  try {
			  imageFile.write(Arrays.copyOfRange(messegeBytes,0,i+2));	    
		     }
			  catch (Exception x) {
			  	System.out.println(x);
			  }
		  endFind = true;
		  break;
		}   
	   }
	 // }
  
  //Αποθήκευση ενδιάμεσων πακέτων εικόνας
	if(!endFind)  
  try {
	  imageFile.write(Arrays.copyOfRange(packet.getData(),0,packet.getLength()));
      }
      catch (Exception x) {
  	   	System.out.println(x);
       } 
	clientRequestDatagram("NEXT",serverPort);


}

imageFile.close();
}

static public void soundDPCM_OR_AQDPCM(String requestCode,int serverPort,int clientPort,int size1,boolean AQ_DPCM,int Qbits ) throws IOException{
//Αρχικοποιούμε για την περίπτωση της DPCM κωδικοποίησης 
AudioFormat formatDPCM = new AudioFormat(8000, Qbits, 1, true, false);;
int size2 = 128;
int numofPackets = 0;
float b = (float) 1.6;

if(AQ_DPCM){ //Αν έχουμε AQ_DPCM κωδικοποίηση αρχικοποιούμε κατάλληλα τις μεταβλητές μας
 Qbits = 16;
 size2 = 132;
 formatDPCM = new AudioFormat(8000, Qbits, 1, true, false);
 System.out.println("AQ_DPCM");
}else{
 System.out.println("DPCM");
}

byte[][] packetBuffer = new byte[size1][size2];
SourceDataLine dataLine = null;
try {
    dataLine = AudioSystem.getSourceDataLine(formatDPCM);
    } catch (LineUnavailableException ex) {}

try {
    dataLine.open(formatDPCM, 32000);   //32000 audio buffer
    } catch (LineUnavailableException ex1) {}
dataLine.start();

 //Request
 clientRequestDatagram(requestCode,serverPort);

 //Αρχικοποίηση μεταβλητών για τα πακέτα που έρχονται απο τον server
 DatagramSocket r = new DatagramSocket(clientPort);
 r.setSoTimeout(300);  
 byte[] rxbuffer = new byte[2048];
 DatagramPacket packet = new DatagramPacket(rxbuffer,rxbuffer.length);	
 
 //Αποθήκευση δεδομένων σε έναν buffer size1(X)size2 = ari8mosPaketwn(X)mege8osPaketou
 for (int i = 0; i < size1; i++) {
  System.out.println("Size1 = "+ i);
  try {
	   r.receive(packet); //Παραλαβή πακέτου
	   numofPackets++;
	  } catch (Exception x) {i--; size1--; System.out.println(x); continue;}
  for (int j = 0; j < size2; j++)  packetBuffer[i][j] = packet.getData()[j];
  }
r.close(); //Κλείνουμε το Socket
 
System.out.println("numofPackets = "+ numofPackets);

byte[] bufferOut;

if (AQ_DPCM) {
bufferOut  = aq_dpcm(packetBuffer,requestCode,numofPackets);
} 
else{
bufferOut  = dpcm(packetBuffer,requestCode,numofPackets, b);
}

dataLine.write(bufferOut , 0, bufferOut.length);
dataLine.stop();
dataLine.close();

}

static public byte[] dpcm(byte[][] packetBuffer,String requestCode,int size1, float b) throws IOException{
File diafFileName  = new File("soundFilediaf_" + requestCode + ".txt"); 
FileOutputStream diafFile = new FileOutputStream(diafFileName); //Τα δεδομενα διαφορών οπως λαμβάνονται απο τον server
File deigFileName  = new File("soundFiledeig_" + requestCode + ".txt");
FileOutputStream deigFile = new FileOutputStream(deigFileName); //Τα δεδομενα του τραγουδιού μετά την αποκωδικοποίηση

byte[] bufferOut = new byte[128 * 2 * size1];
int tempData;
int nibbleLS;
int nibbleMS;
String string;
int size2 = packetBuffer[0].length;
int index = -1;

for (int i = 0; i < size1; i++) {
   for (int j = 0; j < size2; j++) {
	tempData   = (int) packetBuffer[i][j] ;   
    nibbleLS = (tempData      ) & 0b00001111; //Απομονώνουμε τα 2 δειγματα ηχου για καθε byte δεδομένων
    nibbleMS = (tempData >>> 4) & 0b00001111;     
    
    //Gia to MSB
    index++;
    tempData = Math.round((nibbleMS - 8) * b);  
    try {
        string = Integer.toString(tempData);
        diafFile.write(string.getBytes());
        diafFile.write((char) 13);
       } catch (IOException ex) {}
    if (j > 0)
        tempData = tempData + bufferOut[index - 1];
    try {
        string = Integer.toString(tempData);
        deigFile.write(string.getBytes());
        deigFile.write(13);
        deigFile.write(10);
        bufferOut[index] = (byte) tempData;
    } catch (IOException ex1) {}
    
    //Gia to LSB
    index++;
    tempData = Math.round((nibbleLS - 8) * b);
    try {
        string = Integer.toString(tempData);
        diafFile.write(string.getBytes());
        diafFile.write(13);
        diafFile.write(10);
    } catch (IOException ex2) {}
    tempData = tempData + bufferOut[index - 1];
    try {
        string = Integer.toString(tempData);
        deigFile.write(string.getBytes());
        deigFile.write(13);
        deigFile.write(10);
        bufferOut[index] = (byte) tempData;
    } catch (IOException ex3) {
    }

}
}

diafFile.close();
deigFile.close();
return bufferOut;
}

static public byte[] aq_dpcm(byte[][] packetBuffer,String requestCode,int size1) throws IOException{
File diafFileName  = new File("soundFilediaf_" + requestCode + ".txt"); 
FileOutputStream diafFile = new FileOutputStream(diafFileName); //Τα δεδομενα διαφορών οπως λαμβάνονται απο τον server
File deigFileName  = new File("soundFiledeig_" + requestCode + ".txt");
FileOutputStream deigFile = new FileOutputStream(deigFileName); //Τα δεδομενα του τραγουδιού μετά την αποκωδικοποίηση
File meanFileName  = new File("soundMeanFile_" + requestCode + ".txt"); 
FileOutputStream meanFile = new FileOutputStream(meanFileName); //Η τιμή του μέσου όρου που διαβάζεται από κάθε πακέτο
File stepFileName  = new File("soundStepFile_" + requestCode + ".txt");
FileOutputStream stepFile = new FileOutputStream(stepFileName); //Η τιμή του βήματος που διαβάζεται από κάθε πακέτο

String string;
byte[] bufferOut = new byte[2* (128 * 2) * size1]; 
int[][] samples = new int[size1][128 * 2];
int[] mean = new int[size1];
int[] step = new int[size1];
int tempData;
int nibbleLS;
int nibbleMS;
int index  = -1;
int bb;
int max = Integer.MIN_VALUE;
int min = Integer.MAX_VALUE;

//Για κάθε πακέτο που λάβαμε
for (int i = 0; i < size1; i++) { 
	
//Παίρνουμε τον μέσω όρο και το βήμα                                            
mean[i] = 256 * packetBuffer[i][1] + packetBuffer[i][0];   //msb+lsb
step[i] = 256 * packetBuffer[i][3] + packetBuffer[i][2];   //msb+lsb;

try { //Εγγραφη της μεσης τιμής και του βήματος σε αρχεία
    string = Integer.toString(mean[i]);
    meanFile.write(string.getBytes());
    meanFile.write(13);
    meanFile.write(10);
    
    string = Integer.toString(step[i]);
    stepFile.write(string.getBytes());
    stepFile.write(13);
    stepFile.write(10);
    } catch (IOException ex1) {}

index = -1;
//Αποκωδικοποίηση και αποθήκευση των δεδομένων ήχου(διαφορές xi-x(i-1))κάθε πακέτου στον πίνακα sample
for (int j = 4; j < packetBuffer[0].length; j++) {
bb = step[i];
tempData = (int) packetBuffer[i][j];       
nibbleLS = ( (  tempData & 0x0F )-8 )*bb;  
nibbleMS = ( (( tempData >>> 4) & 0x0F )-8 )*bb;
samples[i][++index] = nibbleMS;
samples[i][++index] = nibbleLS;
}
} 

//Aποθήκευση των δεδομένων σε αρχεία 
//----------------------------------------------//
for (int m=0; m<samples.length; m++){
 for (int n = 0; n < samples[0].length; n++) 
 {
  try {
       string = Integer.toString(samples[m][n]);
       diafFile.write(string.getBytes());
       diafFile.write(13);
       diafFile.write(10);
      } catch (IOException ex) { }

  if (n>0)  samples[m][n] += samples[m][n - 1]; //Προηγούμενο δειγμα τρεχοντος πακέτου 
 }
}

index  = -1;

for (int m=0; m<samples.length; m++)
 for (int n = 0; n < samples[0].length; n++) 
 {
  samples[m][n] += mean[m];
  try 
    {
     string = Integer.toString(samples[m][n]);
     deigFile.write(string.getBytes());
     deigFile.write(13);
     deigFile.write(10);
    }catch (IOException ex){ }

  bufferOut[++index] = (byte)  samples[m][n];
  bufferOut[++index] = (byte) (samples[m][n] >>> 8);
    
  if (samples[m][n]>max){
   max = samples[m][n];
  }else if (samples[m][n]<min){
   min = samples[m][n];
  }
}
System.out.println("max --> " + max);
System.out.println("min -->" + min);
System.out.println("aq-dpcm");
//----------------------------------------------//

//Κλείνουμε τα αρχεία
diafFile.close();
deigFile.close();
meanFile.close();
stepFile.close();

return bufferOut;
}

static public void ithakicopter(int serverPort,int fLevel) throws IOException{
File copterFileName  = new File("ithakiCopter_" + fLevel + ".txt");
FileOutputStream copterFile = new FileOutputStream(copterFileName);
File errorsPIDFileName  = new File("PIDCopterController_" + fLevel + ".txt");
FileOutputStream errorsPIDFile = new FileOutputStream(errorsPIDFileName);	


//Δημιουργία TCP socket για την επικοινωνία με τον Server
String hostIP ="155.207.18.208";
InetAddress hostAddress = InetAddress.getByName(hostIP);
Socket sock = new Socket(hostAddress,serverPort);
DataOutputStream copterStreamTCP_OUT = new DataOutputStream(sock.getOutputStream());
BufferedReader copterStreamTCP_IN = new BufferedReader(new InputStreamReader(sock.getInputStream()));

       
StringBuilder buffer = new StringBuilder();
String fileLabel = "Epi8umhtes times fLevel,lMotor,rMotor,            Times pou lambanoume gia fLevel,lMotor,rMotor\n";
buffer.append(fileLabel); 

StringBuilder errorsBuffer = new StringBuilder();
fileLabel = "PID controller errors\nProportial Integral Derivative\n";
errorsBuffer.append(fileLabel);

long startTime = System.currentTimeMillis();
long time3min = 3*60*1000; //3 λεπτα
int index=-1;
int MaxdDelay = 20; //Ορίζουμε καθυστέριση για το control των κινητήρων,
int delay = MaxdDelay; //δηλαδή καθυστέριση μεταξύ των αλλαγών της ταχύτητας των κινητήρων
int error =0;
int du_DIV_dt = 0; 
int sumError  = 0;
int prevAltitude = 74;
int lMotor = 178; //Αρχικές τιμές κινητήρων
int rMotor = 178;

while((System.currentTimeMillis()-startTime)< time3min-100){ //Λίγο λιγότερο απο 3λεπτά
 String strfLevel = String.valueOf(fLevel);
 String strlMotor = String.valueOf(lMotor);
 String strrMotor = String.valueOf(rMotor); 

 //Οι μεταβλητές LLL RRR και AAA πρέπει να είναι τριψήφιοι αριθμοί
 if(fLevel <100) strfLevel="0"+fLevel;
 if(lMotor <100) strlMotor="0"+lMotor;
 if(rMotor <100) strrMotor="0"+rMotor;
 if(fLevel <10) strfLevel="00"+fLevel;
 if(lMotor <10) strlMotor="00"+lMotor;
 if(rMotor <10) strrMotor="00"+rMotor;
 	
 String requestCode = "AUTO FLIGHTLEVEL="+strfLevel+" LMOTOR="+strlMotor+" RMOTOR="+strrMotor+" PILOT \r\n";
 //Request
 copterStreamTCP_OUT.writeBytes(requestCode);
 //Response
 String message = copterStreamTCP_IN.readLine();
 System.out.println(message);
//Πρεπει να βρουμε το ITHAKICOPTER και μετα να αποθηκέυσουμε τα απολεσματα
 try{
 if(message.indexOf("ITHAKICOPTER")!=0)  
 continue;	 
 else{
  index++;
  if(index==0) continue;
 }
 }catch(Exception x) { System.out.println(x); break;}
 
 String delimeter = " |=";
 String[] data = message.split(delimeter); //Διαχωρίζουμε τα δεδομένα και παίρνουμε τις τιμές τους
 buffer.append(index+"  "); 
 buffer.append(fLevel+"  ");  //FLIGHT LEVEL
 buffer.append(lMotor+"  ");  //LMOTOR
 buffer.append(rMotor+"  ");  //RMOTOR
 buffer.append("         ");
 int altitude = Integer.parseInt(data[6]);
 buffer.append(altitude+"  ");  //ALTITUDE
 int lmotor = Integer.parseInt(data[2]);
 buffer.append(lmotor+"  ");  //LMOTOR
 int rmotor = Integer.parseInt(data[4]);
 buffer.append(rmotor+"  ");  //RMOTOR
 float temperature = Float.parseFloat(data[8]); 
 buffer.append(temperature+"  ");  //TEMPERATURE
 float pressure = Float.parseFloat(data[10]);
 buffer.append(pressure+"\n");    //PRESSURE
  
 //---------------------PID Controller------------------//
 //PID controller for motor speed
 //Κατασκευάζουμε έναν controller που ελέγχει το επιθυμητό ύψος προσαρμόζοντας τις στροφές των κινητήρων.
 //Λόγο της καθυστέρησης που υπάρχει μεταξύ στην επικοινωνία εισάγουμε μέσω της μεταβλητής delay καθυστέρηση
 //στον βρόχο ανάδρασης. Με απλά λόγια,στέλνουμε το έτοιμα για τις στροφές των κινητήρων και περιμένουμε κάποιο 
 //χρονικό διάστημα(μέσω της Delay) μέρχι να σταλεί το έτοιμα και να κινηθεί κατάλληλα το copter και μέτα υπολογίζουμε το σφάλμα 
 //και προσαρμόζουμε την νέα ταχύτητα των κινητήρων
 delay++;
 if(delay>MaxdDelay){
  
   error = fLevel - altitude;			//P
   sumError += error;					//I
   du_DIV_dt = prevAltitude - altitude; //D
      
   if(sumError>15  || sumError <-15) sumError = 0; //Διότι το σφάλμα error είναι ικανό να διορθώση την θέση
  
   error = Math.round((float) 0.1*(float)error);				//P
   int tempSumError = Math.round((float) 0.05*(float)sumError);	//I
   du_DIV_dt = Math.round((float) 0.1*(float)du_DIV_dt);		//D
   
  
  int total_error = error + du_DIV_dt + tempSumError;
  
  System.out.println(error);
  System.out.println(du_DIV_dt);
  System.out.println(tempSumError);
  System.out.println(total_error);
  errorsBuffer.append(error+" "); 
  errorsBuffer.append(du_DIV_dt+" ");
  errorsBuffer.append(tempSumError+"\n");
  
  if((lMotor + total_error)<200 && (rMotor + total_error)<200 ){
  lMotor = lMotor + total_error;
  rMotor = rMotor + total_error;
  }else if(total_error>0){
	  lMotor = 200;
	  rMotor = 200;
  }
  
 delay = 0;
 prevAltitude = altitude;
 }
//---------------------PID End of code------------------//

}//Telos while()

//Εγγραφή στα αρχεία
try 
 { 
  copterFile.write(buffer.toString().getBytes()); 
  errorsPIDFile.write(errorsBuffer.toString().getBytes());
    
 } catch (Exception x) {System.out.println(x);}

copterFile.close();
errorsPIDFile.close();
sock.close();
       
}

static public void OBDfunction(int serverPort) throws IOException{
File OBDFileName  = new File("OBDData_" + ".txt");
FileOutputStream OBDFile = new FileOutputStream(OBDFileName);	

//Δημιουργία TCP socket για την επικοινωνία με τον Server
String hostIP ="155.207.18.208";
InetAddress hostAddress = InetAddress.getByName(hostIP);
Socket sock = new Socket(hostAddress,serverPort);
DataOutputStream OBDStreamTCP_OUT = new DataOutputStream(sock.getOutputStream());
BufferedReader OBDStreamTCP_IN = new BufferedReader(new InputStreamReader(sock.getInputStream()));

StringBuilder buffer = new StringBuilder();
String fileLabel = "EngineRunTime IntakeAirTemperature ThrottlePosition EngineRPM VehicleSpeed CoolantTemperature\n";
buffer.append(fileLabel); 
	

String delimeter = " ";
long startTime = System.currentTimeMillis();
long time4min = 4*60*1000; //4 λεπτα    
String mode = "01";
String[] PID = {"1F","0F","11","0C","0D","05"};
String message = "";
int x = 0;
int y = 0;

while (System.currentTimeMillis()-startTime < time4min){  //Για 4 λεπτά

for(int i = 0;i<6;i++){
OBDStreamTCP_OUT.writeBytes(mode+" "+PID[i] + "\r"); 
message = OBDStreamTCP_IN.readLine();	                
String[] received_data = message.split(delimeter);	

switch(i){
case 0:
	x = Integer.parseInt(received_data[2], 16);
	y = Integer.parseInt(received_data[3], 16);
	int engineRunTime=255*x + y;
	buffer.append(engineRunTime+"  ");
	System.out.print(engineRunTime+"  ");
	break;
case 1:
	x = Integer.parseInt(received_data[2], 16);
	int intakeAirTemperature=x-40;
	buffer.append(intakeAirTemperature+"  ");
	System.out.print(intakeAirTemperature+"  ");
	break;
case 2:
	x=Integer.parseInt(received_data[2], 16);
	int throttlePosition=x*100/255;
	buffer.append(throttlePosition+"  ");
	System.out.print(throttlePosition+"  ");
	break;
case 3:
	x = Integer.parseInt(received_data[2], 16);
	y = Integer.parseInt(received_data[3], 16);
	int engineRpm=(256*x + y)/4;
	buffer.append(engineRpm+"  ");
	System.out.print(engineRpm+"  ");
	break;
case 4:
	x = Integer.parseInt(received_data[2], 16);
	int vehicleSpeed=x;
	buffer.append(vehicleSpeed+"  ");
	System.out.print(vehicleSpeed+"  ");
	break;
case 5:
	x = Integer.parseInt(received_data[2], 16);
	int coolantTemperature=x-40;
	buffer.append(coolantTemperature+" \n");
	System.out.println(coolantTemperature+"  \n");
	break;
 }		
}

}//Telos while()
 
OBDFile.write(buffer.toString().getBytes());
OBDFile.close();
sock.close();

}

}//telos class Main
