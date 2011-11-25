package de.unierlangen.like.navigation;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.graphics.PointF;

public class Zone {
	private ArrayList<PointF> points;
	private static Zone instance;
	
	//Constructors
	public Zone(Tag tag, float radius, int amountOfPoints) {
		points = new ArrayList<PointF>(amountOfPoints);
		for (int i=0; i<amountOfPoints; i++){
			float x = (float)(tag.getX()+radius*Math.cos((i*6.283f)/amountOfPoints));
			float y = (float)(tag.getY()+radius*Math.sin((i*6.283f)/amountOfPoints));
			points.add(new PointF(x, y));
		}
	}
	
	public Zone(Array x[], Array y[], float radius, int amountOfPoints) {
		super();
	}
	
	public static Zone obtainTestZone(Array x[], Array y[]){
		if (instance==null){
			Tag tag = new Tag ("dummy", 12, true, 0.0f, 0.0f);
			instance = new Zone(tag , 2.0f, 10);
		}
		return instance;
	}
	
	public ArrayList<PointF> getPoints() {
		return points;
	}
}

