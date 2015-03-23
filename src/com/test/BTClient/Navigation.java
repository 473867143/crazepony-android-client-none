package com.test.BTClient;
 

import java.util.LinkedList;
import java.util.List;

public class Navigation {
	//private WayPoint wp1=new WayPoint("�̵�", 45443993,126373228); 
	public List<WayPoint> wpSave=new LinkedList<WayPoint>();		//���ڵص�
	public static List<WayPoint> route=new LinkedList<WayPoint>();	
	final String[] wpStr={"Home","��¥","����","����","�̵�","���¥","�Ǳ�","��¥A"};
	final int[][] pos={{1,1},{2,2},{3,3},{4,4},{5,5},{6,6},{7,7},{8,8},}; 
	public Navigation()	//initilize the kown wp
	{
		//����һЩ�̶��ص�
		 	int len ;
		for(len=0;len<wpStr.length;len++)
		{
			WayPoint wpT = new WayPoint(wpStr[len],pos[len][0], pos[len][1]); 
			wpSave.add(wpT);
		} 
	//	wpStr.length
		/*
		wpSave.add(new WayPoint("Home",1,2));
		wpSave.add(new WayPoint("��¥",45443993,126373228));
		wpSave.add(new WayPoint("����",3,4));
		wpSave.add(new WayPoint("����",3,4));
		wpSave.add(new WayPoint("�̵�",1,2));
		wpSave.add(new WayPoint("���¥",1,2));
		wpSave.add(new WayPoint("�Ǳ�",1,2));
		wpSave.add(new WayPoint("��¥A",1,2));*/
	}
	public int findWayPointIndex(String wpName)
	{
		int index=-1;
		for(int i=0;i<wpSave.size();i++)
		{
			if(wpSave.get(i).getWPName()==wpName)
				index=i;
		}
		return index;
	}
	public int findLat(String wpName)
	{
		int wpi=findWayPointIndex(wpName);
		if(wpi!=-1)
			return wpSave.get(wpi).getLat();
		else
			return -1;
	}
	public int findLon(String wpName)
	{
		int wpi=findWayPointIndex(wpName);
		if(wpi!=-1)
			return wpSave.get(wpi).getLon();
		else
			return -1;
	}
	public WayPoint findWayPoint(String wpName)
	{
		int wpi=findWayPointIndex(wpName);
		if(wpi!=-1)
			return wpSave.get(findWayPointIndex(wpName));
		else
			return null;
	}
	public void addWayPoint()
	{
		
	}
	public void addSavedWayPoint()
	{
		
	}
}
