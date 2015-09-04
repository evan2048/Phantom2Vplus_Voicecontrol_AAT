package com.evan2048.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RecognizeResultXmlParser {

	//获取识别结果
	public static String getResult(String xml) 
	{
		StringBuffer buffer = new StringBuffer();
		try
		{
			//DOM builder
			DocumentBuilder domBuilder = null;
			//DOM doc
			Document domDoc = null;	
			//init DOM
			DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
			domBuilder = domFact.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			domDoc = domBuilder.parse(is);
			//获取根节点
			Element root = (Element) domDoc.getDocumentElement();
			Element raw = (Element)root.getElementsByTagName("rawtext").item(0);
			//获取识别结果
			buffer.append(raw.getFirstChild().getNodeValue());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		};
		return buffer.toString();
	}
	
	//获取识别结果置信度
	public static int getResultConfidence(String xml) 
	{
		int confidenceValue=0;
		StringBuffer buffer = new StringBuffer();
		try
		{
			//DOM builder
			DocumentBuilder domBuilder = null;
			//DOM doc
			Document domDoc = null;	
			//init DOM
			DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
			domBuilder = domFact.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			domDoc = domBuilder.parse(is);
			//获取根节点
			Element root = (Element) domDoc.getDocumentElement();
			Element confidence = (Element)root.getElementsByTagName("confidence").item(0);
			//获取识别结果置信度
			buffer.append(confidence.getFirstChild().getNodeValue());
			confidenceValue=Integer.parseInt(buffer.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		};
		return confidenceValue;
	}
}
