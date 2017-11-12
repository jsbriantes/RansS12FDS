package com.ecca1.ranss12.ranss12fds;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class SimpleCrypto {
	
	// Modo DEBUG (D) = true. Poner a false para modo completo
	private static boolean DEBUG  = false;
	
	public static boolean lModoDepuracion() {
		return DEBUG;
	}
	
	public static String encrypt(String seed, String cleartext) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}
	
	public static String decrypt(String seed, String encrypted) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = null;
		if (android.os.Build.VERSION.SDK_INT >= 17) {
	        sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
	    } else {
	        sr = SecureRandom.getInstance("SHA1PRNG");
	    }
		sr.setSeed(seed);
	    kgen.init(128, sr); // 192 and 256 bits may not be available
	    SecretKey skey = kgen.generateKey();
	    byte[] raw = skey.getEncoded();
	    return raw;
	}

	
	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	    byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	    byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}
	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}
	
	public static byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2*buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}
	private final static String HEX = "0123456789ABCDEF";
	private static void appendHex(StringBuffer sb, byte b) {
		 sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
	
	// Verifica si la versi�n es Demo y si puede continuar utiliz�ndola
	// 0 - No puede usarse, 1 - Es Demo usable, 2 - Es completa operativa
	public static int nDemoOperativa(Context context) {
		int nResultado = 0;

		SharedPreferences parametros = context.getSharedPreferences("APPSETASTECMB", Activity.MODE_PRIVATE);
		String cid = parametros.getString("K1","ERROR");
		String cfec = parametros.getString("K4","ERROR");
        String cdemo = parametros.getString("K5", "ERROR");        
        
        try {
        
		        // Si est�n los par�metros verificaremos que son correctos
		        if ((cfec.compareTo("ERROR")!=0) && (cfec.compareTo("")!=0) && (cdemo.compareTo("ERROR")!=0) && (cdemo.compareTo("")!=0)) {
		        
		        	byte[] key2Bytes = new byte[] { 0x06, 0x0f, 0x03, 0x12, 0xd, 0x08, 0x0a,
		    				0x07, 0x07, 0x15, 0x0b, 0x0b, 0x0d, 0x0d, 0x0e, 0x0e, 0x10,
		    				0x0f, 0x05, 0x0f, 0x0f, 0x11, 0x16, 0x12 };
		        	String clave = new String(key2Bytes);
		        	String yfec="", ydemo="", yid="";
		        	
		        	// Obtenemos los datos desencriptados
		        	yid = decrypt(clave, cid).toUpperCase();
		        	yfec = decrypt(clave, cfec).toUpperCase();
		        	ydemo = decrypt(clave, cdemo).toUpperCase(); 
		        	
		        	// Comprobamos si es versi�n demo o completa
		        	if (lDemo(ydemo, yid)) {
		        		// Se trata de una versi�n demo
			    		// Tomamos los datos de fecha para comprobar si 
			    		Date fecha = new Date();
			    		Calendar calendar1 = Calendar.getInstance();
			    		calendar1.setTime(fecha);
			    		calendar1.add(Calendar.DAY_OF_YEAR, +7);
			    		Date nuevaFecha1 = calendar1.getTime();
			    		Calendar calendar2 = Calendar.getInstance();
			    		calendar2.setTime(fecha);
			    		calendar2.add(Calendar.DAY_OF_YEAR, -7);
			    		Date nuevaFecha2 = calendar2.getTime();
			    		
			    		// Convertimos el texto a fecha para efectuar las comprobaciones correspondientes
			    		SimpleDateFormat  format = new SimpleDateFormat("dd/MM/yyyy");  
		    		    Date feclic = format.parse(yfec);  			    		     
			    		
		    		    // Si la fecha en la que se grab� la licencia es inferior/superior a 7 d�as respecto de la fecha actual
		    		    // ya no podemos continuar usando la aplicaci�n
			    		if ((feclic.compareTo(nuevaFecha1)>0) || (feclic.compareTo(nuevaFecha2)<0)) {
			    			nResultado = 0;
			    			// Borramos la posible licencia que existiera
			    			SharedPreferences.Editor editor = parametros.edit();
			    			editor.remove("K1");
			    	    	editor.remove("K2");
			    	    	editor.remove("K3");
			    	    	editor.remove("K4");
			    	    	editor.remove("K5");
			    	    	editor.remove("K6");
			    	    	editor.remove("K7");
			    	    	editor.remove("K8");
			    	    	editor.remove("K9");
			    	    	editor.remove("K10");
			    	    	editor.commit();
			    		}
			    		else nResultado = 1;			    					    		
		        	}
		        	else nResultado = 2; // Se trata de la versi�n completa
		        }
        } catch (ParseException e) {
        	nResultado = 0;		        
        } catch (Exception e) {
        	nResultado = 0;
		}
         
		// Si modo DEBUG devolvemos 2 fijo. Si no, el resultado
        if (DEBUG) return 2;     // {$DEBUG}
        else return nResultado;  // {$RELEASE}
	}
	
	// Comprueba si un c�digo es demo o no
	public static Boolean lDemo(String cdemo, String cId) {
		boolean lDemo = true;
		
		// Extraemos los n�meros del identificador. Tomamos los primeros 4 n�meros que encontremos saltando 1 cada vez
		// Ese n�mero se restar� del n�mero incluido en la cdemo en formato m�dulo y se comprobar�
		// si el n�mero resultante suman sus cifras
		String aux ="";
		boolean lSaltar = true;
		int cuenta = 0;
		for (int i = 0; i<cId.length(); i++)
		{
			if (Character.isDigit(cId.charAt(i))) {
				if (!lSaltar) {
					aux = cId.charAt(i) + aux;
					cuenta = cuenta + 1;
					if (cuenta == 4) break;
				}
				lSaltar = !lSaltar;
			}
		}
		
		while (aux.length()<4) aux = aux + "0";
		
		cuenta = (Integer.parseInt(aux) * 56 ) + (Integer.parseInt(aux) * 36 ); 
		
		// Si el resultado de la operaci�n coindice con el c�digo de activaci�n, no es demo.
		if (String.valueOf(cuenta).substring(0, 4).compareTo(cdemo)==0) lDemo = false;
					    		
		return lDemo;
	}
	
	// Verifica si la aplicaci�n tiene licencia
	public static Boolean VerificaLicencia(Context context)  {
		Boolean flag = false;		
		SharedPreferences parametros;
		
		try
		{
				parametros = context.getSharedPreferences("APPSETASTECMB", Activity.MODE_PRIVATE);
		        String id = parametros.getString("K1","ERROR");
		        String lic = parametros.getString("K2", "ERROR");
		        String mac = parametros.getString("K3", "ERROR");
		        
		        if ((id.compareTo("ERROR")!=0) && (id.compareTo("")!=0) &&
		    		(lic.compareTo("ERROR")!=0) && (lic.compareTo("")!=0) &&
		    		(mac.compareTo("ERROR")!=0) && (mac.compareTo("")!=0)) {
		        	// Si hay datos guardaros
		        	byte[] key2Bytes = new byte[] { 0x06, 0x0f, 0x03, 0x12, 0xd, 0x08, 0x0a,
		    				0x07, 0x07, 0x15, 0x0b, 0x0b, 0x0d, 0x0d, 0x0e, 0x0e, 0x10,
		    				0x0f, 0x05, 0x0f, 0x0f, 0x11, 0x16, 0x12 };
		        	String clave = new String(key2Bytes);
		        	String aux1="", aux2="", aux3="";
		        	
		        	// Obtenemos los datos desencriptados
		        	id = decrypt(clave, id).toUpperCase();
		        	mac = decrypt(clave, mac).toUpperCase();
		        	lic = decrypt(clave, lic).toUpperCase();
		        	        	
		    		try {
		    			aux3 = makeMD5Hash(id).toString().toUpperCase();
		    		} catch (NoSuchAlgorithmException e) {
		    			aux3 = "";
		    		}
		    		    		
		    		int i=0;
		    		// tomar 1 de cada 3
		    		while ((i<31) && aux3.compareTo("")!=0)
		    		{
		    			aux1+=aux3.substring(i,i+1);
		    			i +=3;    		
		    		}
		    		
		    		// Invertir
		    		for(i=aux1.length()-1;i>=0;i--)
		    		{
		    			aux2+= aux1.substring(i,i+1);
		    		}    		    	
		    		
		    		// Aplicar MD5 de nuevo
		    		try {
		    			aux1= makeMD5Hash(aux2).toString().toUpperCase();
		    		} catch (NoSuchAlgorithmException e) {
		    			aux1="";
		    		}
		    		    		    		
		    		i=0;
		    		aux2="";
		    		// Tomar uno de cada 4
		    		while(i<31)
		    		{
		    			aux2+=aux1.substring(i,i+1);
		    			i +=4;		
		    		}
		    		
		    		// Si la licencia coincide ahora miramos la mac
		    		if (lic.compareTo(aux2.substring(0,4)+"-"+aux2.substring(4,8))==0) {
		    			String service = Context.WIFI_SERVICE;
		    			WifiManager wifi = (WifiManager)context.getSystemService(service);
		    			
		    			if (!wifi.isWifiEnabled())
		    				if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
		    						wifi.setWifiEnabled(true);
		    					
		    			WifiInfo info = wifi.getConnectionInfo();    		    
		    		    String macwifi =  info.getMacAddress().toUpperCase();
		    		    macwifi = macwifi.replace(':', '$');
		    		       		    
		    		    try {
		    		    	// Con el wifi cambiado, calculamos el MD5
		    				aux1 = makeMD5Hash(macwifi).toString().toUpperCase();
		    			} catch (NoSuchAlgorithmException e) {
		    				aux1 = "";
		    			}
		    		    
		    		    if (mac.compareTo(aux1)==0) flag = true;
		    		}        	        	        
		        }
		} catch (Exception e) {
		   return false;		
		}
		
		// Si modo DEBUG devolvemos true siempre. Si no el resultado
		if (DEBUG) return true;  // {$DEBUG}
		else return flag;        // {$RELEASE}
	}

	
	// Obtener el n�mero de serie para TPVP
	public static String NumeroSerieTPVP(Context context)  {
		String cnumserie = "";		
		SharedPreferences parametros;
		
		try
		{
				parametros = context.getSharedPreferences("APPSETASTECMB", Activity.MODE_PRIVATE);
		        String id = parametros.getString("K1","ERROR");
		        String lic = parametros.getString("K2", "ERROR");
		        String mac = parametros.getString("K3", "ERROR");
		        
		        if ((id.compareTo("ERROR")!=0) && (id.compareTo("")!=0) &&
		    		(lic.compareTo("ERROR")!=0) && (lic.compareTo("")!=0) &&
		    		(mac.compareTo("ERROR")!=0) && (mac.compareTo("")!=0)) {
		        	// Si hay datos guardaros
		        	byte[] key2Bytes = new byte[] { 0x06, 0x0f, 0x03, 0x12, 0xd, 0x08, 0x0a,
		    				0x07, 0x07, 0x15, 0x0b, 0x0b, 0x0d, 0x0d, 0x0e, 0x0e, 0x10,
		    				0x0f, 0x05, 0x0f, 0x0f, 0x11, 0x16, 0x12 };
		        	String clave = new String(key2Bytes);
		        	String aux1="", aux2="", aux3="";
		        	
		        	// Obtenemos los datos desencriptados
		        	id = decrypt(clave, id).toUpperCase();
		        	mac = decrypt(clave, mac).toUpperCase();
		        	lic = decrypt(clave, lic).toUpperCase();
		        	        	
		    		try {
		    			aux3 = makeMD5Hash(id).toString().toUpperCase();
		    		} catch (NoSuchAlgorithmException e) {
		    			aux3 = "";
		    		}
		    		    		
		    		int i=0;
		    		// tomar 1 de cada 4
		    		while ((i<31) && aux3.compareTo("")!=0)
		    		{
		    			aux1+=aux3.substring(i,i+1);
		    			i +=4;    		
		    		}
		    		
		    		// Invertir
		    		for(i=aux1.length()-1;i>=0;i--)
		    		{
		    			aux2+= aux1.substring(i,i+1);
		    		}    		    	
		    		
		    		// Aplicar MD5 de nuevo
		    		try {
		    			aux1= makeMD5Hash(aux2).toString().toUpperCase();
		    		} catch (NoSuchAlgorithmException e) {
		    			aux1="";
		    		}
		    		    		    		
		    		i=0;
		    		aux2="";
		    		// Tomar uno de cada 2
		    		while(i<31)
		    		{
		    			aux2+=aux1.substring(i,i+1);
		    			i +=2;		
		    		}
		    		
		    		cnumserie = aux2;
		        }
		        
		        return cnumserie;
		} catch (Exception e) {
		   return "";		
		}				
	}
	
	// Validar el c�digo que recibimos en la importaci�n
	public static boolean ValidarImportacion(String Terminal, String Fecha, String Lineas, String Codigo, Context context) {
		boolean flag = false;
		
		// Comprobamos si el c�digo que viene en la primera l�nea es correcto
		if ((Terminal.length()==4) && (Fecha.length()==6) && (Lineas.length()==4) && (Codigo.length()>0)) {
			try 
			{
				// Obtenemos el n�mero de serie asociado al TPVP
				String cnumserie = NumeroSerieTPVP(context);
				
				// Generamos la cadena a codificar con los datos recibidos
				String aux = cnumserie.substring(9) +
							 Terminal.substring(2) +
							 cnumserie.substring(0,4) +
							 Fecha.substring(2) +
							 Lineas +
							 Fecha.substring(0,2) +
							 Terminal.substring(0,2) +
							 cnumserie.substring(4,9);
				
				// Calculamos el hash
				aux = makeMD5Hash(aux).toUpperCase();
							 
				// Ahora veamos si coinciden
				if (aux.compareTo(Codigo)==0) flag = true;
			}
			catch(Exception e) {
				flag = false;
			}
		}		
		
		// Si modo DEBUG devolvemos true siempre, si no el resultado.
		if (DEBUG) return true;  // {$DEBUG}
		else return flag;        // {$RELEASE}
	}

	public static String makeMD5Hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		byte[] buffer = input.getBytes();
		md.update(buffer);
		byte[] digest = md.digest();

		String hexStr = "";
		for (int i = 0; i < digest.length; i++) {
			hexStr += Integer.toString((digest[i] & 0xff) + 0x100, 16)
					.substring(1);
		}
		return hexStr;
	}

	// Comprueba si una licencia corresponde con una id
	public static Boolean CompruebaLicencia(String id, String lic, String cDemo)  {				
		try
		{	        		        	
		        	String aux1="", aux2="", aux3="";
		        		        	        	
		    		try {
		    			aux3 = makeMD5Hash(id).toString().toUpperCase();
		    		} catch (NoSuchAlgorithmException e) {
		    			aux3 = "";
		    		}
		    		    		
		    		int i=0;
		    		// tomar 1 de cada 3
		    		while ((i<31) && aux3.compareTo("")!=0)
		    		{
		    			aux1+=aux3.substring(i,i+1);
		    			i +=3;    		
		    		}
		    		
		    		// Invertir
		    		for(i=aux1.length()-1;i>=0;i--)
		    		{
		    			aux2+= aux1.substring(i,i+1);
		    		}    		    	
		    		
		    		// Aplicar MD5 de nuevo
		    		try {
		    			aux1= makeMD5Hash(aux2).toString().toUpperCase();
		    		} catch (NoSuchAlgorithmException e) {
		    			aux1="";
		    		}
		    		    		    		
		    		i=0;
		    		aux2="";
		    		// Tomar uno de cada 4
		    		while(i<31)
		    		{
		    			aux2+=aux1.substring(i,i+1);
		    			i +=4;		
		    		}
		    		
		    		// Si la licencia coincide devolvemos correcto
		    		if (lic.compareTo(aux2.substring(0,4)+"-"+aux2.substring(4,8))==0) return true;
		    		else return false;
		} catch (Exception e) {
		   return false;		
		}
	}
	
	// Graba la licencia correcta en la BD de preferencias, calculando antes la MAC
	public static Boolean GrabaLicencia(String id, String lic, String cDemo, int terminal, String iplocal, String ippublica, int puerto, Context context)
	{			
		try
		{				
				String service = Context.WIFI_SERVICE;
				WifiManager wifi = (WifiManager)context.getSystemService(service);

	        	String macaux="", idaux="", licaux="", demo="";
	        	
				if (!wifi.isWifiEnabled())
					if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
							wifi.setWifiEnabled(true);
						
				WifiInfo info = wifi.getConnectionInfo();    		    
			    String macwifi =  info.getMacAddress().toUpperCase();
			    macwifi = macwifi.replace(':', '$');
	       		    
			    try {
			    	// Con el wifi cambiado, calculamos el MD5
					macaux = makeMD5Hash(macwifi).toString().toUpperCase();
				} catch (NoSuchAlgorithmException e) {
					macaux = "";
				}			    	        	
	        	
			    if (macaux.compareTo("")!=0) {			    				    
		        	// Obtenemos los datos encriptados	
					// Clave de grabaci�n 
		        	byte[] key2Bytes = new byte[] { 0x06, 0x0f, 0x03, 0x12, 0xd, 0x08, 0x0a,
		    				0x07, 0x07, 0x15, 0x0b, 0x0b, 0x0d, 0x0d, 0x0e, 0x0e, 0x10,
		    				0x0f, 0x05, 0x0f, 0x0f, 0x11, 0x16, 0x12 };
		        	String clave = new String(key2Bytes);
			    	
		        	idaux = encrypt(clave, id).toUpperCase();
		        	macaux = encrypt(clave, macaux).toUpperCase();
		        	licaux = encrypt(clave, lic).toUpperCase();
		        	demo = encrypt(clave, cDemo).toUpperCase();
		        	
		        	// Grabamos los datos en la BD de preferencias
		        	SharedPreferences parametros;
		    		SharedPreferences.Editor editor;
		    		parametros = context.getSharedPreferences("APPSETASTECMB", Activity.MODE_PRIVATE);
		        	editor = parametros.edit();
	            	editor.putString("K1", idaux);
	            	editor.putString("K2", licaux);
	            	editor.putString("K3", macaux);
	            	// Tomamos la fecha del d�a en que se graba la licencia	            	
	            	Date fecha = new Date();
	            	SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
	            	editor.putString("K4", encrypt(clave, formato.format(fecha)).toUpperCase());
	            	editor.putString("K5", demo);
	            	editor.putString("K7", String.valueOf(terminal));
	            	editor.putString("K8", iplocal);
	            	editor.putString("K9", ippublica);
	            	editor.putInt("K10", puerto);	            	
	            	editor.commit();
	            	
	            	DBAdapter dbHelper;
	            	//Abrimos nuestra base de datos (astegest)
	        		dbHelper = new DBAdapter(context);     
	        		dbHelper.open();
	        		ContentValues values = new ContentValues();
	        		values.put("NTERMINAL", terminal);
	        		values.put("CIPLOCAL", iplocal);
	        		values.put("CIPPUBLICA", ippublica);
	        		values.put("NPUERTO", puerto);
	        		dbHelper.InsertarRegistro("CONFIGURACION", values, "NPRIMARYKEY");	            	
	        		
	            	return true;
			    }
			    else return false;			    
		} catch (Exception e) {
			return false;
		}			
	}
	
	// Obtener el ID para comunicaciones
	public static String ObtenerId(Context context) {
								
		SharedPreferences parametros;
		
		try
		{
				parametros = context.getSharedPreferences("APPSETASTECMB", Activity.MODE_PRIVATE);
		        String id = parametros.getString("K1","ERROR");
		        String lic = parametros.getString("K2", "ERROR");
		        String mac = parametros.getString("K3", "ERROR");
		        
		        if ((id.compareTo("ERROR")!=0) && (id.compareTo("")!=0) &&
		    		(lic.compareTo("ERROR")!=0) && (lic.compareTo("")!=0) &&
		    		(mac.compareTo("ERROR")!=0) && (mac.compareTo("")!=0)) {
		        	// Si hay datos guardaros
		        	byte[] key2Bytes = new byte[] { 0x06, 0x0f, 0x03, 0x12, 0xd, 0x08, 0x0a,
		    				0x07, 0x07, 0x15, 0x0b, 0x0b, 0x0d, 0x0d, 0x0e, 0x0e, 0x10,
		    				0x0f, 0x05, 0x0f, 0x0f, 0x11, 0x16, 0x12 };
		        	String clave = new String(key2Bytes);
		        	String aux1="", aux2="", aux3="";
		        	
		        	// Obtenemos los datos desencriptados
		        	id = decrypt(clave, id).toUpperCase();
		        	        	
		    		try {
		    			aux3 = makeMD5Hash(id).toString().toUpperCase();
		    		} catch (NoSuchAlgorithmException e) {
		    			aux3 = "";
		    		}
		    		    		
		    		int i=0;
		    		// tomar 1 de cada 4
		    		while ((i<31) && aux3.compareTo("")!=0)
		    		{
		    			aux1+=aux3.substring(i,i+1);
		    			i +=4;    		
		    		}
		    		
		    		// Invertir
		    		for(i=aux1.length()-1;i>=0;i--)
		    		{
		    			aux2+= aux1.substring(i,i+1);
		    		}    			    		
		    		
		    		// Aplicar MD5 de nuevo
		    		try {
		    			aux1= makeMD5Hash(aux2).toString().toUpperCase();
		    		} catch (NoSuchAlgorithmException e) {
		    			aux1="";
		    		}
		    		    		    		
		    		i=0;
		    		aux2="";
		    		// Tomar uno de cada 2
		    		while(i<31)
		    		{
		    			aux2+=aux1.substring(i,i+1);
		    			i +=2;		
		    		}
		    		
		    		return aux2;

		        }
		        else return "";
		} catch(Exception e){
			return "";
		}			
	}
	
	// Decodifica una cadena de texto
	public static String cDecodificaCadena(String str)
	{
    	// Obtenemos los datos desencriptados	
		// Clave de codificaci�n
    	byte[] key2Bytes = new byte[] { 0x06, 0x0f, 0x03, 0x12, 0x0d, 0x08, 0x0a,
				0x07, 0x07, 0x15, 0x0b, 0x0b, 0x0d, 0x0d, 0x0e, 0x0e, 0x10,
				0x0f, 0x05, 0x0f, 0x0f, 0x11, 0x16, 0x12 };
    	String clave = new String(key2Bytes);    	
    	String idaux;
		try {
			idaux = decrypt(clave, str);
		} catch (Exception e) {		
			idaux = "";
		}
    	
    	return idaux;
	}
}
