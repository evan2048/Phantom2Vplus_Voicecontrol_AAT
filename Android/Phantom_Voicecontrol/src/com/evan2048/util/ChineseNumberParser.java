package com.evan2048.util;

public class ChineseNumberParser {
	
	public static int toIntNumber(String str)
	{
		if(str.equals("零"))
		{
			return 0;
		}
		else if(str.equals("十"))
		{
			return 10;
		}
		else if(str.equals("二十"))
		{
			return 20;
		}
		else if(str.equals("三十"))
		{
			return 30;
		}
		else if(str.equals("四十"))
		{
			return 40;
		}
		else if(str.equals("五十"))
		{
			return 50;
		}
		else if(str.equals("六十"))
		{
			return 60;
		}
		else if(str.equals("七十"))
		{
			return 70;
		}
		else if(str.equals("八十"))
		{
			return 80;
		}
		else if(str.equals("九十"))
		{
			return 90;
		}
		else
		{
			return 0;
		}
	}
}
