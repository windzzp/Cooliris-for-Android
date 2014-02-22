package com.gtx.cooliris.entity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class GirlImageSAXHandler extends DefaultHandler
{	
	private ImageGroup m_girl = new ImageGroup();
	
	private Image m_tempImage = null;
	private String m_tempString = "";
	
	private static final String TAG_GIRL_ID         = "girl_id";
	private static final String TAG_NAME_ID         = "name_id";
	private static final String TAG_IMAGE_ID        = "image_id";
	private static final String TAG_IMAGE_URL 		= "image_url";
	private static final String TAG_IMAGE_THUMB_URL	= "image_thumb_url";

	private static final String TAG_IMAGE 			= "image";
	
	public ImageGroup getParsedGirl()
	{
		return m_girl;
	}
	
	@Override
	public void startDocument() throws SAXException
	{
		System.out.println("--------- Start SAX parse file ---------");
	}

	@Override
	public void endDocument() throws SAXException
	{
		System.out.println("--------- End SAX parse file ---------");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
//		System.out.println("Start element: " + " uri = " + uri + ", localName = " + localName + ", qName = " + qName + ", attributes = " + attributes.toString());
		
		if (TAG_IMAGE.equals(qName))
		{
		    m_tempImage = new Image();
			m_girl.addImage(m_tempImage);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
//		System.out.println("End element: " + " uri = " + uri + ", localName = " + localName + ", qName = " + qName);
		
        if (TAG_GIRL_ID.equals(qName)) 
        {
            int girlId = Integer.parseInt(m_tempString);
            m_girl.setGrilId(girlId);
        }
        else if (TAG_NAME_ID.equals(qName)) 
        {
        }	    
		else if (TAG_IMAGE_ID.equals(qName))
		{
		    int imageId = Integer.parseInt(m_tempString);
		    m_tempImage.setImageId(imageId);
		}
		else if (TAG_IMAGE_URL.equals(qName)) 
		{
		    m_tempImage.setImageUrl(m_tempString);
		}
		else if (TAG_IMAGE_THUMB_URL.equals(qName)) 
		{
		    m_tempImage.setThumbUrl(m_tempString);
		}
		
		m_tempString = null;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		String tempCh = new String(ch, start, length);
		m_tempString = tempCh;
//		System.out.println("characters: " + " ch = " + tempCh + ", start = " + start + ", length = " + length);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException
	{
		e.printStackTrace();
	}
}
