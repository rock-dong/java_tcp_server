package com.tcp.server;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class tcpserver {

	InputStream is;
	OutputStream os = null;
	DataOutputStream osData = null;
	ServerSocket serverSocket;
	int count;
	byte serialNumLow = 0;
	byte serialNumHigh = 0;
	int receiveNum = 0;
	int passNum = 0;
	int failNum = 0;
	int repeatNum = 0;
	int lostNum = 0;
	int index = 0;
	
	public tcpserver() {
		// TODO Auto-generated constructor stub
		System.out.println("tcpserver construct");
		
        try {
        	//serverSocket =new ServerSocket(8088);
        	byte[] gprsData = new byte[100];
        	byte[] xorData = new byte[97];
        	
        	byte[] gprsResponse = new byte[24];
        	byte[] gprsResponseData = new byte[27];
        	byte[] iccid = new byte[20];
        	byte[] startTime = new byte[20];
        	byte[] endTime = new byte[20];
        	byte[] cell0 = new byte[17];
        	byte[] cell1 = new byte[17];
        	serverSocket =new ServerSocket(8088);
        	
            while(true){
            	
            	count = 0;
            	System.out.println("wait for connect..");
                Socket socket=serverSocket.accept();//侦听并接受到此套接字的连接,返回一个Socket对象
                System.out.println("connected");
            
                is = socket.getInputStream();//得到一个输入流，接收客户端传递的信息
            
                
                count = is.read(gprsData, 0, 100);

                System.out.println("client ip :"+ socket.getInetAddress().toString() + count + "bytes");
                
                receiveNum++;
                
                os = socket.getOutputStream();
                
                osData= new DataOutputStream(os);
                
                gprsResponseData[0] = (byte)0x55;
                gprsResponseData[1] = (byte)0xAA;
                gprsResponseData[2] = (byte)0x01;
                gprsResponseData[3] = gprsData[3];
                gprsResponseData[4] = gprsData[4];
                
                int tmp = gprsData[4]&0xFF + (gprsData[3]&0xFF)*256;
                
                if(tmp == index && index != 0){
                   System.out.println("repeat data");
                   repeatNum++;
                }else if( (index+1) != tmp ){
                   System.out.println("lost data");
                   lostNum ++;
                   index = tmp;
                }else{
                   index = tmp;
                }
                
                
                
                
                if(count != 100){
                	gprsResponseData[5] = (byte)0xbb;
                	failNum++;
                }else{
                	if((gprsData[0] & 0xFF) != 0x55 || (gprsData[1] & 0xFF) != 0xAA){
                		gprsResponseData[5] = (byte)0xbb;
                		System.out.println("preamble is wrong " + gprsData[0] + " " + gprsData[1]);
                		failNum++;
                	}else{
                	    if(((gprsData[2]&0xFF) & 0x0F) > 3){
                	       System.out.println("type is wrong");
                	       gprsResponseData[5] = (byte)0xbb;
                	       failNum++;
                	    }else {
                	    	
                	    	System.arraycopy(gprsData, 25, startTime, 0, 20);
                	    	String str = new String(startTime);
                	    	System.out.println(str);
                	    	System.arraycopy(gprsData, 45, endTime, 0, 20);
                	    	str = new String(endTime);
                	    	System.out.println(str);
                	    	System.arraycopy(gprsData, 65, cell0, 0, 17);
                	    	str = new String(cell0);
                	    	System.out.println(str);
                	    	System.arraycopy(gprsData, 82, cell1, 0, 17);
                	    	str = new String(cell1);
                	    	System.out.println(str);
                	    	
                	    	System.arraycopy(gprsData, 2, xorData, 0, 97);
                            if(gprsData[99] == xor(xorData)){
                        	    gprsResponseData[5] = (byte)0x77;
                        	    passNum++;
                            }else{
                            	System.out.println("xor verify fail");
                        	    gprsResponseData[5] = (byte)0xbb;
                        	    failNum++;
                            }	
                	    }
                        
                	}
                    
                   
                }
                
                Date now = new Date( );
                SimpleDateFormat ft = new SimpleDateFormat ("yy/MM/dd','hh:mm:ss+00");
                String timeNow = ft.format(now);
                System.out.println(timeNow);
                byte[] timeByte = timeNow.getBytes();
                //System.out.println("timeByte : " + timeByte.length);
                
                System.arraycopy(timeByte, 0, gprsResponseData, 6, timeByte.length);
                
                System.arraycopy(gprsResponseData, 2, gprsResponse, 0, 24);
                
                gprsResponseData[26] = xor(gprsResponse);
                
                osData.write(gprsResponseData, 0, 27);
            
                osData.flush();
                
                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                System.out.println("Summary Total:" + receiveNum + " Pass:" + passNum + " Fail:"+ failNum + " Repeat:" + repeatNum + " Lost:" + lostNum);
                
                is.close();
                os.close();
                socket.close();
                
            }
  
			
			
			//serverSocket.close();
			             
        } catch (IOException e) {
           e.printStackTrace();
      }
		
	}

	public static byte xor(byte[] b){
	   byte x = 0;
	   for(byte bb : b){
		   x = (byte) (x^bb);
	   }
	   
	   return x;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("tcp server main");
		new tcpserver();
	}

}
