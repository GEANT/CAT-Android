//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************

package uk.ac.swansea.eduroamcat;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Base64;


public class AuthenticationMethod {

	private boolean error=false;
	private String errorMessage="";
	private int outerEAPType = 0;
	private int innerEAPType = 0;
	private int innerNonEAPType = 0;
	private String CAencoding="",CAformat="";
	private ArrayList<String> serverIDs = new ArrayList<String>();
	private String clientCertEncoding="",clientCertFormat="";
	private String anonID="";
	private Boolean annonID_save=true;
	X509Certificate CAcert = null;
	X509Certificate usercert = null;
	String originalCACert = "";
	String originalClientCert = "";

	
	//set error message
	public void setConfigError(String errorMessage)
	{
		this.error=true;
		this.errorMessage=errorMessage;
	}
	
	//clear error message
	public void clearConfigError()
	{
		this.error=false;
		this.errorMessage="";
	}
	
	//Return if error or not
	public boolean isError()
	{
		return this.error;
	}
	
	//return error message if message exists
	public String getError()
	{
		if (errorMessage.length()>0) return errorMessage;
		else return "";
	}

	//set outer EAP Type
	public void setOuterEAPType(int eaptype)
	{
		if (eaptype>0){
			this.outerEAPType=eaptype;
			//checks on supported eap types?
		}
		else setConfigError(Resources.getSystem().getString(R.string.error_with)+"outerEAPType="+eaptype);
	}
	
	//return outer EAP Type
	public int getOuterEAPType()
	{
		return outerEAPType;
	}

	//set inner EAP Type
	public void setInnerEAPType(int eaptype)
	{
		if (eaptype>0){
			//checks on supported eap types?
			this.innerEAPType=eaptype;
		}
		else setConfigError(Resources.getSystem().getString(R.string.error_with)+"innerEAPType="+eaptype);
	}
	
	//return inner EAP Type
	public int getInnerEAPType()
	{
		return innerEAPType;
	}
	
	//set inner NonEAPAuthMethod
	public void setInnerNonEAPType(int eaptype)
	{
		if (eaptype>0){
			//checks on supported eap types?
			this.innerNonEAPType=eaptype;
		}
		else setConfigError(Resources.getSystem().getString(R.string.error_with)+"innerNonEAPType="+eaptype);
	}
	
	//return inner EAP Type
	public int getInnerNonEAPType()
	{
		return innerNonEAPType;
	}
	
	public String getCAFormat()
	{
		return CAformat;
	}
	
	public String getCAencoding()
	{
		return CAencoding; 
	}
	
	public String getOrignalCACert()
	{
		return originalCACert;
	}
	
	public String getOrignalClientCert()
	{
		return originalClientCert;
	}
	
	//test if a cert is an intermediate certificate or not
	public boolean isIntermediate(X509Certificate cert)
	{
		String subject = "";
		String issuer = "";
		subject = cert.getSubjectDN().toString();
		eduroamCAT.debug("CERT Subject="+subject+"\n");
		issuer = cert.getIssuerDN().toString();
		eduroamCAT.debug("CERT Issuer="+issuer+"\n");
		//CERT Subject=CN=StartCom Certification Authority, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL
		//CERT Issuer =CN=StartCom Certification Authority, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL
		if (subject.equals(issuer)) return false;
		return true;
	}
	
	//set CA cert
	public boolean setCAcert(String cacert, String format, String encoding) throws UnsupportedEncodingException
	{
		if (format.length()==0) format="X.509";
		if (encoding.length()==0) encoding="base64";
		if (cacert.length()>0 && format.equals("X.509") && encoding.equals("base64"))
		{
			//cacert=cacert.trim();
			this.originalCACert=cacert;
			eduroamCAT.debug("Cert to install =\n"+cacert);
			if (cacert.contains("-----BEGIN CERTIFICATE-----")==false) 
			{
				StringBuilder tmp = new StringBuilder();
				tmp.append("-----BEGIN CERTIFICATE-----\n");
				tmp.append(cacert);
				tmp.append("\n-----END CERTIFICATE-----");
				cacert=tmp.toString();
				eduroamCAT.debug("Cert="+cacert);
			}
			InputStream is = new ByteArrayInputStream(cacert.getBytes());
	        BufferedInputStream bis = new BufferedInputStream(is);
	        CertificateFactory cf = null;
	        try {
	            cf = CertificateFactory.getInstance("X.509");
	        } catch (java.security.cert.CertificateException e) {
	            e.printStackTrace();
	        }

	        X509Certificate cert = null;

	        try {
	            while (bis.available() > 0) {
	                cert = (X509Certificate) cf.generateCertificate(bis);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (java.security.cert.CertificateException e) {
	            e.printStackTrace();
	        }

	        try {
	            bis.close();
	            is.close();
	        } catch (IOException e) {
	            // If this fails, it isn't the end of the world.
	            e.printStackTrace();
	        }
	        
	        boolean intermediateCert = false;
	        intermediateCert = isIntermediate(cert);
			
			if (cert!=null && intermediateCert==false) 
			{
				eduroamCAT.debug((cert.toString()));
	    	//	checks on supported eap types?
				clearConfigError();
				this.CAcert=cert;
				this.CAformat=format;
				this.CAencoding=encoding;
				return true;
			}
			else {
				if (intermediateCert) setConfigError("Intermediate Certificate provided="+cacert);
				else setConfigError("Error with cert encoding="+cacert);
				return false;
			}
		}
		else {
			eduroamCAT.debug("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
			eduroamCAT.debug("CERT="+cacert);
			eduroamCAT.debug("originalCERT="+originalCACert);
			eduroamCAT.debug("format="+format);
			eduroamCAT.debug("encoding="+encoding);			
			//eduroamCAT.debug("Subject="+CAcert.getSubjectAlternativeNames().iterator().next().get(0));
			eduroamCAT.debug("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
			setConfigError(Resources.getSystem().getString(R.string.error_with)+"CA Cert="+originalCACert);
			return false;
		}
	}
	
	//return CA cert
	public Certificate getCAcert()
	{
		return CAcert;
	}
	
	public String getClientCertFormat()
	{
		return clientCertFormat;
	}
	
	public String getClientCertEncoding()
	{
		return clientCertEncoding; 
	}
	
	//set Client cert
	public boolean setClientCert(String cacert, String format, String encoding) throws UnsupportedEncodingException
	{
		if (format.length()==0) format="X.509";
		if (encoding.length()==0) encoding="base64";
		if (cacert.length()>0 && format.equals("X.509") && encoding.equals("base64"))
		{
			this.originalClientCert=cacert;
			eduroamCAT.debug("Cert to install =\n"+cacert);
			if (cacert.contains("-----BEGIN CERTIFICATE-----")==false) 
			{
				StringBuilder tmp = new StringBuilder();
				tmp.append("-----BEGIN CERTIFICATE-----\n");
				tmp.append(cacert);
				tmp.append("\n-----END CERTIFICATE-----");
				cacert=tmp.toString();
				eduroamCAT.debug("User Cert="+cacert);
			}
			InputStream is = new ByteArrayInputStream(cacert.getBytes());
	        BufferedInputStream bis = new BufferedInputStream(is);
	        CertificateFactory cf = null;
	        try {
	            cf = CertificateFactory.getInstance("X.509");
	        } catch (java.security.cert.CertificateException e) {
	            e.printStackTrace();
	        }

	        X509Certificate cert = null;

	        try {
	            while (bis.available() > 0) {
	                cert = (X509Certificate) cf.generateCertificate(bis);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (java.security.cert.CertificateException e) {
	            e.printStackTrace();
	        }

	        try {
	            bis.close();
	            is.close();
	        } catch (IOException e) {
	            // If this fails, it isn't the end of the world.
	            e.printStackTrace();
	        }
			
			if (cert!=null) 
			{
				eduroamCAT.debug((cert.toString()));
	    	//	checks on supported eap types?
				clearConfigError();
				this.usercert=cert;
				this.clientCertFormat=format;
				this.clientCertEncoding=encoding;
				return true;
			}
			else {
				setConfigError("Error with cert encoding="+usercert);
				return false;
			}
		}
		else {
			eduroamCAT.debug("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
			eduroamCAT.debug("CERT="+cacert);
			eduroamCAT.debug("originalCERT="+originalCACert);
			eduroamCAT.debug("format="+format);
			eduroamCAT.debug("encoding="+encoding);			
			//eduroamCAT.debug("Subject="+CAcert.getSubjectAlternativeNames().iterator().next().get(0));
			eduroamCAT.debug("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
			setConfigError(Resources.getSystem().getString(R.string.error_with)+"CA Cert="+originalCACert);
			return false;
		}
		
	}
	
	//return Client cert
	public Certificate getClientCert()
	{
		return usercert;
	}
	
	//add ServerID
	public void addServerID(String aServerID)
	{
		if (aServerID.length()>0) serverIDs.add(aServerID);
		else setConfigError(Resources.getSystem().getString(R.string.error_with)+"Server ID="+aServerID);
	}
	
	//get serverIDs
	public ArrayList<String> getServerIDs()
	{
		return serverIDs;
	}
	
	//set anon ID
	public void setAnonID(String anonID,Boolean save)
	{
		if (anonID.length()>0){
			this.anonID=anonID;
			this.annonID_save=save;
		}
		else setConfigError(Resources.getSystem().getString(R.string.error_with)+"anonymous ID="+anonID);
	}
	
	//return inner EAP Type
	public String getAnonID()
	{
		return anonID;
	}

}
