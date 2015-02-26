

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is used to read XML file
 */
public class XmlManager {
	
	/**
	 * get the attribute of a node 
	 * @param element	 the node
	 * @param attributeName	 the name of attribute
	 * @return	value of the attribute
	 */
	public static String getAttribute( Element element, String attributeName ) {
		return element.getAttribute( attributeName );
	}
	
   /**
	* get text in the specified node 
	* @param element	the node
	* @return	the text
	*/
	public static String getText( Element element ) {
		return element.getFirstChild().getNodeValue();
	}
	
	/**
	 * read the xml file£¬and set up its DOM tree in memory
	 * @param xmlFile	the XML file (full path name)
	 * @return	the Document after reading XML file
	 * @throws Exception	XML file does not exist
	 */
	public static Document parse( String xmlFile ) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document domTree = db.parse( xmlFile );
		return domTree;
	}
	
	/**
	 * get a sub node in a specified node (sub-nod was specified by an attribute and its value)
	 * OR: get sub-node under parentElement childName£¬with attributeName attributeValue
	 * @param parentElement	 the parent Element
	 * @param childName	 name of sub-element
	 * @param attributeName	 the specified attribute name
	 * @param attributeValue	attribute value
	 * @return	the eligible sub-node
	 * @throws Exception	sub-node does not exist or there are more than one eligible sub-node
	 */
	public static Element getChildElement( Element parentElement, String childName, String attributeName, String attributeValue ) throws Exception {
		NodeList list = parentElement.getElementsByTagName( childName );
		int count = 0;
		Element curElement = null;
		for ( int i = 0 ; i < list.getLength() ; i ++ ) {
			Element child = ( Element )list.item( i );
			String value = child.getAttribute( attributeName );
			if ( true == value.equals( attributeValue ) ) {
				curElement = child;
				count ++;
			}
		}
		if (count == 0 ) {
			return null;
		} else if ( 1 < count ) {
			throw new Exception( "there are more than one eligible sub-node!" );
		}
		
		return curElement;
	}
	
	/**
	 * get a sub node in a specified node (sub-nod was specified by its name)
	 * get sub-node under parentElement named childName
	 * @param parentElement	  parent element
	 * @param childName	  name of sub-node
	 * @return	eligible sub-node
	 * @throws Exception	sub-node does not exist or there are more than one eligible sub-node
	 */
	public static Element getChildElement( Element parentElement, String childName ) throws Exception {
		NodeList list = parentElement.getElementsByTagName( childName );
		Element curElement = null;
		if ( 1 == list.getLength()  ) {
			curElement = ( Element )list.item( 0 );
		} else if ( 0 == list.getLength() ) {
			return null;
		} else {
			throw new Exception( "there are more than one eligible sub-node!" );
		}
		return curElement;
	}
}

