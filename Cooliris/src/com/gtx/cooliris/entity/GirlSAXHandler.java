package com.gtx.cooliris.entity;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class GirlSAXHandler extends DefaultHandler
{
	private ArrayList<ImageGroup> m_girls = new ArrayList<ImageGroup>();
	private ImageGroup m_tempGirl = null;
	private String m_tempString = "";
	
	private static final String TAG_GIRLS_LIST     = "girls_list";
	private static final String TAG_GIRL_ROOT      = "girl";
	
	private static final String TAG_GIRL_ID        = "girl_id";
	private static final String TAG_NAME_ID        = "name_id";
	private static final String TAG_HOME_PAGE      = "home_page";
	private static final String TAG_NAME           = "name";
	private static final String TAG_TITLE_ABSTRACT = "title_abstract";
	private static final String TAG_TITLE          = "title";
	private static final String TAG_DESCRIPTION    = "description";
	private static final String TAG_THUMB_URL      = "thumb_url";
	private static final String TAG_THUMB_W        = "thumb_w";
	private static final String TAG_THUMB_H        = "thumb_h";
	//private static final String TAG_THUMBNAIL      = "thumbnail";
	
	public ArrayList<ImageGroup> getParsedGirls()
	{
		return m_girls;
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
		//System.out.println("Start element: " + " uri = " + uri + ", localName = " + localName + ", qName = " + qName + ", attributes = " + attributes.toString());
		
		if (TAG_GIRL_ROOT.equals(qName))
		{
		    m_tempGirl = new ImageGroup();
		    m_girls.add(m_tempGirl);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		//System.out.println("End element: " + " uri = " + uri + ", localName = " + localName + ", qName = " + qName);
		
	    if (TAG_GIRL_ID.equals(qName)) 
        {
	        int id = Integer.parseInt(m_tempString);
            m_tempGirl.setGrilId(id);
        }
        else if (TAG_NAME_ID.equals(qName)) 
        {
            //m_tempGirl.setNameId(m_tempString);
        }
        else if (TAG_HOME_PAGE.equals(qName)) 
        {
            m_tempGirl.setHomePage(m_tempString);
        }
        else if (TAG_NAME.equals(qName)) 
        {
            //m_tempGirl.setName(m_tempString);
        }
        else if (TAG_TITLE_ABSTRACT.equals(qName)) 
        {
            //m_tempGirl.setTitleAbstract(m_tempString);
        }
		if (TAG_TITLE.equals(qName))
		{
			m_tempGirl.setTitle(m_tempString);
		}
		else if (TAG_DESCRIPTION.equals(qName)) 
		{
			m_tempGirl.setDescription(m_tempString);
		}
		else if (TAG_THUMB_URL.equals(qName)) 
		{
			m_tempGirl.setThumbUrl(m_tempString);
		}
		else if (TAG_THUMB_W.equals(qName)) 
		{
			int w = Integer.parseInt(m_tempString);
			//m_tempGirl.setThumbWidth(w);
		}
		else if (TAG_THUMB_H.equals(qName)) 
		{
			int h = Integer.parseInt(m_tempString);
			//m_tempGirl.setThumbHeight(h);
		}
		else 
		{
		    // Do nothing
        }
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		String tempCh = new String(ch, start, length);
		m_tempString = tempCh;
		//System.out.println("characters: " + " ch = " + tempCh + ", start = " + start + ", length = " + length);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException
	{
		e.printStackTrace();
	}
}
