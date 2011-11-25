package de.unierlangen.like.navigation;

import android.graphics.PointF;


public abstract class Obstacle {
	public abstract PointF getIntersection (Tag tag,float x, float y);
}
