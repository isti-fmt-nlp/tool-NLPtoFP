package tirocinio;

import java.io.UnsupportedEncodingException;
import java.net.IDN;

public class PROVA {
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		String a = "•";//"�";
		String aa = "●";//"�";
		char bb = '●';
		byte [] A = a.getBytes("UTF8");
		byte [] B = a.getBytes();
		System.out.println(A + " " + B + " "+ (int)bb+"\n");
		
		String gg = Integer.toHexString(a.hashCode());
		System.out.println(IDN.toUnicode(a));
		String d = "'";
		int h = a.indexOf("\u2022");
		System.out.println(h);
	}

}
