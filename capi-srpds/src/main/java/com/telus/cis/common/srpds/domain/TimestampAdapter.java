
package com.telus.cis.common.srpds.domain;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TimestampAdapter extends XmlAdapter<String, Timestamp>
{
	public String marshal(Timestamp value) throws Exception
	{      
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		int nanos = value.getNanos()/1000;		
		String nanoStr = nanos + "";
		String nanoAppend = "";
		
		if(nanoStr.length() < 6)
		{
			nanoAppend = "000000" + nanoStr;
			nanoStr = nanoAppend;
		}

		return sdf.format(value) + "." + nanoStr.substring(nanoStr.length()- 6, nanoStr.length());
	}

	public Timestamp unmarshal(String value) throws Exception
	{
		return convertStringToTimestamp(value);
	}
	  
	private Timestamp convertStringToTimestamp(String tsString) 
	{	
		String ts = tsString.substring(0, tsString.lastIndexOf("."));
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		Date date;
		Timestamp timest = null;

		try 
		{
			date = sdf.parse(ts);
			timest = new java.sql.Timestamp(date.getTime());
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}

		timest.setNanos(getNanoSeconds(tsString));
		
		return timest;
	}

	private int getNanoSeconds(String tsString) {
        int nanoSecs = 0;
        int fromIndex = tsString.lastIndexOf(".") + 1;
        int toIndex = tsString.length();
        String nanos = tsString.substring(fromIndex, toIndex);

        nanoSecs = Integer.parseInt(nanos)*1000;

        return nanoSecs;
    }

}
