import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles RSA Public/Private Key file generation and modification 
 *  
 */
public class KeyManager {
	
  /**
   * Initialize Public File
   * @param key the string representation of a Public Key
   * @return whether or not the operation succeeded
   */
	public boolean initPublicKeyFile(String key) {
	  
		//if file already existed, do nothing.
		if (new File("publicKey.xml").exists()) {
		  System.out.println("key exists already!");
			return false; 
		}
		
		try {
		  
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("key");
			doc.appendChild(rootElement);
					
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("publicKey.xml"));
			transformer.transform(source, result);
			
			updatePublicKey(key);
		}
		catch(Exception e){}
		System.out.println("Key Saved!");
		return true;
	}
	
	/**
	 * Create a Private Key File
	 * @param key the Private Key string
	 * @return the success of the operation
	 */
	public boolean initPrivateKeyFile(String key) {
	  
		//if file already existed, do nothing.
		if(new File("privateKey.xml").exists()) {
			updatePrivateKey(key);
			System.out.println("private key already exists!");
			return false;  //need not to initialize
		}
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("key");
			doc.appendChild(rootElement);
					
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("privateKey.xml"));
			transformer.transform(source, result);
			
			updatePrivateKey(key);
		}
		catch(Exception e){}
		System.out.println("Key Saved!");
		return true;
	}
	
	/**
	 * Update the Public Key entry in file
	 * @param key the Public Key strings
	 * @return the success of the operation
	 */
	public void updatePublicKey(String key) {
		Document d = null;
		try {
			d = XmlManager.parse("publicKey.xml");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// user elements
		Element keyElem = ((Element)d.getElementsByTagName("key").item(0));
		keyElem.setAttribute("value", key);
		
		
		// write the content into xml file
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(d);
			StreamResult result = new StreamResult(new File("publicKey.xml"));
	
			transformer.transform(source, result);
		}
		catch(Exception e){};
	}
	
	/**
	 * Update Private Key file entry
	 * 
	 * @param key the Private Key string
	 * @return the sucesss of the operation
	 */
	public void updatePrivateKey(String key) {
	  
		Document d = null;
		try {
			d = XmlManager.parse("privateKey.xml");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// user elements
		Element keyElem = ((Element)d.getElementsByTagName("key").item(0));
		keyElem.setAttribute("value", key);
		
		
		// write the content into xml file
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(d);
			StreamResult result = new StreamResult(new File("privateKey.xml"));
	
			transformer.transform(source, result);
		}
		catch(Exception e){};
	}
	
	/**
	 * Retrieve Public Key from file
	 * 
	 * @return the Public Key
	 */
	public String getPublicKey() {
		Document d = null;
		try {
			d = XmlManager.parse("publicKey.xml");
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed to open publicKey.xml");
			return null;
		}
		Element keyElement = (Element)(d.getElementsByTagName("key").item(0));
	
		return XmlManager.getAttribute(keyElement, "value");
	}
	
	/**
	 * Retrieve the Private Key from file
	 * 
	 * @return the Private Key
	 */
	public String getPrivateKey() {
		Document d = null;
		try {
			d = XmlManager.parse("privateKey.xml");
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed to open privateKey.xml");
			return null;
		}
		
		Element keyElement = (Element)(d.getElementsByTagName("key").item(0));
	
		return XmlManager.getAttribute(keyElement, "value");
	}
}
