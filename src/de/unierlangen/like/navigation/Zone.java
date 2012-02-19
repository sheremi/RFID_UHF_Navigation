package de.unierlangen.like.navigation;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.graphics.PointF;

public class Zone {
	private static final int AMOUNT_OF_POINTS_PER_ZONE = 72;
	private ArrayList<PointF> points;
	private static Zone instance;
	
	//Constructors
	public Zone(Tag tag, float radius) {
		points = new ArrayList<PointF>(AMOUNT_OF_POINTS_PER_ZONE);
		for (int i=0; i<AMOUNT_OF_POINTS_PER_ZONE; i++){
			float x = (float)(tag.getX()+radius*Math.cos((i*6.283f)/AMOUNT_OF_POINTS_PER_ZONE));
			float y = (float)(tag.getY()+radius*Math.sin((i*6.283f)/AMOUNT_OF_POINTS_PER_ZONE));
			points.add(new PointF(x, y));
		}
	}
	
	public Zone(Array x[], Array y[], float radius, int amountOfPoints) {
		super();
	}
	
	public static Zone obtainTestZone(Array x[], Array y[]){
		if (instance==null){
			Tag tag = new Tag ("dummy", 12, true, 0.0f, 0.0f);
			instance = new Zone(tag , 2.0f);
		}
		return instance;
	}
	
	public ArrayList<PointF> getPoints() {
		return points;
	}
}

