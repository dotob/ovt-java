package tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Properties;

//import org.columba.ristretto.auth.AuthenticationException;
//import org.columba.ristretto.auth.AuthenticationFactory;
//import org.columba.ristretto.auth.NoSuchAuthenticationException;
//import org.columba.ristretto.composer.MimeTreeRenderer;
//import org.columba.ristretto.io.CharSequenceSource;
//import org.columba.ristretto.message.Address;
//import org.columba.ristretto.message.BasicHeader;
//import org.columba.ristretto.message.Header;
//import org.columba.ristretto.message.LocalMimePart;
//import org.columba.ristretto.message.MimeHeader;
//import org.columba.ristretto.message.MimeType;
//import org.columba.ristretto.parser.AddressParser;
//import org.columba.ristretto.parser.ParserException;
//import org.columba.ristretto.smtp.SMTPException;
//import org.columba.ristretto.smtp.SMTPProtocol;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class Mailer {
	
	private static String smtpserver         = SettingsReader.getString("MailSettings.smtpserver");
	private static String smtpuser           = SettingsReader.getString("MailSettings.smtpuser");
	private static String smtppass           = SettingsReader.getString("MailSettings.smtppasswort");
	private static String fromaddress        = SettingsReader.getString("MailSettings.fromaddress");
	
	public static void mailTo(String toaddress, String msgTxt, String attachTxt){
//		// Parse and check the given to- and from-address
//		Address fromAddress;
//		try {
//			fromAddress = AddressParser.parseAddress(fromaddress);
//		} catch (ParserException e) {
//			System.err.println("Invalid from-address : " + e.getSource());
//			return;
//		}
//		
//		Address toAddress;
//		try {
//			toAddress = AddressParser.parseAddress(toaddress);
//		} catch (ParserException e) {
//			System.err.println("Invalid to-address : " + e.getSource());
//			return;
//		}
//		
//		String subject = "MaFo-Manager is mailing!!";
//		
//		// PART 1 : Composing a message
//		
//		// Header is the actual header while BasicHeader wraps
//		// a Header object to give easy access to the Header.
//		Header header = new Header();
//		BasicHeader basicHeader = new BasicHeader(header);
//		
//		// Add the fields to the header
//		// Note that the basicHeader is only a convienience wrapper
//		// for our header object.
//		basicHeader.setFrom(fromAddress);
//		basicHeader.setTo(new Address[] { toAddress });
//		basicHeader.setSubject(subject, Charset.forName("ISO-8859-1"));
//		basicHeader.set("X-Mailer", "OVT MaFo-Manager / Ristretto API");
//		
//		// Now a mimepart is prepared which actually holds the message
//		// The mimeHeader is another convienice wrapper for the header
//		// object
//		MimeHeader mimeHeader = new MimeHeader(header);
//		mimeHeader.set("Mime-Version", "1.0");
//		LocalMimePart root = new LocalMimePart(mimeHeader);
//		
//		LocalMimePart textPart;
//		
//		// We have an attachment we must compose a multipart message
//		mimeHeader.setMimeType(new MimeType("multipart", "mixed"));
//		
//		textPart = new LocalMimePart(new MimeHeader());
//		root.addChild(textPart);
//		
//		// Now we can add some message text
//		MimeHeader textHeader = textPart.getHeader();
//		textHeader.setMimeType(new MimeType("text", "plain"));
//		root.setBody(new CharSequenceSource(msgTxt));
//		
//		// Now we compose the attachment
//		MimeHeader attachmentHeader  = new MimeHeader("application", "octet-stream");
//		attachmentHeader.setContentTransferEncoding("base64");
////		attachmentHeader.putDispositionParameter("filename", "mafodata.xml");
//		LocalMimePart attachmentPart = new LocalMimePart(attachmentHeader);
//		attachmentPart.setBody(new CharSequenceSource(attachTxt));
//		root.addChild(attachmentPart);
//		
//		InputStream messageSource;
//		try {
//			// Finally we render the message to an inputstream
//			messageSource = MimeTreeRenderer.getInstance().renderMimePart( root );
//		} catch (Exception e2) {
//			System.err.println(e2.getLocalizedMessage());
//			return;
//		}
//		
//		// Part 2 : Sending the message
//		
//		// Construct the protocol that is bound to the SMTP server
//		System.out.println(smtpserver+" : "+smtpuser+" : "+smtppass+" : "+fromaddress);
//		SMTPProtocol protocol = new SMTPProtocol(smtpserver);
//		try {
//			// Open the port
//			protocol.openPort();            
//			
//			// The EHLO command gives us the capabilities of the server
//			String capabilities[] = protocol.ehlo(InetAddress.getLocalHost());
//			
//			// Authenticate
//			if(smtpuser != null ) {
//				String authCapability = null;
//				for( int i=0; i<capabilities.length; i++) {
////					System.out.println("capabilities: "+capabilities[i]);
//					if( capabilities[i].startsWith("AUTH")) {
//						authCapability = capabilities[i];
//						break;
//					}
//				}
//				if( authCapability != null ) {
//					try {                        
//						System.out.println(AuthenticationFactory.getInstance().getSecurestMethod( authCapability )+" authCapability: "+authCapability);
////						protocol.auth( "PLAIN", smtpuser, smtppass.toCharArray() );
//						protocol.auth( AuthenticationFactory.getInstance().getSecurestMethod( authCapability ), smtpuser, smtppass.toCharArray() );
//						System.out.println(protocol.authReceive());
//					} catch (NoSuchAuthenticationException e3) {
//						System.err.println("error 1");
//						System.err.println(e3.getLocalizedMessage());
//						return;
//					} catch ( AuthenticationException e ) {
//						System.err.println("error 2");
//						System.err.println(e.getMessage());
//						System.err.println(e.getStackTrace());
//						return;
//					}
//				} else {
//					System.err.println("Server does not support Authentication!");
//					return;
//				}
//			}
//			
//			// Setup from and recipient
//			protocol.mail(fromAddress);
//			protocol.rcpt(toAddress);
//			
//			// Finally send the data
//			protocol.data(messageSource);
//			
//			// And close the session
//			protocol.quit();
//			
//		} catch (IOException e1) {
//			System.err.println("error 3");
//			System.err.println(e1.getLocalizedMessage());
//			return;
//		} catch (SMTPException e1) {
//			System.err.println("error 4");
//			System.err.println(e1.getMessage());
//			System.err.println(e1.getCode());
//			return;            
//		}
	}
	
	
//	public static void mailToSun(String toaddress, String msgTxt, String attachTxt){
//		String to = toaddress;
//		String from = fromaddress;
//		String host = smtpserver;
//		boolean debug = false;
//
//		// create some properties and get the default Session
//		Properties props = new Properties();
//		props.put("mail.smtp.host", host);
//
//		Session session = Session.getInstance(props, null);
//		session.setDebug(debug);
//		
//		try {
//		    // create a message
//		    MimeMessage msg = new MimeMessage(session);
//		    msg.setFrom(new InternetAddress(from));
//		    InternetAddress[] address = {new InternetAddress(to)};
//		    msg.setRecipients(Message.RecipientType.TO, address);
//		    msg.setSubject("JavaMail APIs Multipart Test");
//		    msg.setSentDate(new Date());
//
//		    // create and fill the first message part
//		    MimeBodyPart mbp1 = new MimeBodyPart();
//		    mbp1.setText(msgText1);
//
//		    // create and fill the second message part
//		    MimeBodyPart mbp2 = new MimeBodyPart();
//		    // Use setText(text, charset), to show it off !
//		    mbp2.setText(msgText2, "us-ascii");
//
//		    // create the Multipart and its parts to it
//		    Multipart mp = new MimeMultipart();
//		    mp.addBodyPart(mbp1);
//		    mp.addBodyPart(mbp2);
//
//		    // add the Multipart to the message
//		    msg.setContent(mp);
//		    
//		    // send the message
//		    Transport.send(msg);
//		} catch (MessagingException mex) {
//		    mex.printStackTrace();
//		    Exception ex = null;
//		    if ((ex = mex.getNextException()) != null) {
//			ex.printStackTrace();
//		    }
//		}
//	}
}
