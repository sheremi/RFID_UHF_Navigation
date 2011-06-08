package de.unierlangen.like.navigation;

import java.util.ArrayList;

import android.graphics.RectF;

public class Navigation {
	
	// Fields
	// Tools for navigation
	private ArrayList<Tag> arrayOfTags;
	// Geometry parameters
	private float areaWithTagsX2 = Float.MIN_VALUE;
	private float areaWithTagsY2 = Float.MIN_VALUE;
	private float areaWithTagsX1 = Float.MAX_VALUE;
	private float areaWithTagsY1 = Float.MAX_VALUE;
	
	// Constructor
	public Navigation(ArrayList<Tag> arrayOfTags) {
		super();
		this.arrayOfTags = arrayOfTags;
		for (Tag tag : this.arrayOfTags)
		{
			areaWithTagsX1 = Math.min(areaWithTagsX1, tag.getX());
			areaWithTagsY1 = Math.min(areaWithTagsY1, tag.getY());
			areaWithTagsX2 = Math.max(areaWithTagsX2, tag.getX());
			areaWithTagsY2 = Math.max(areaWithTagsY2, tag.getY());
		}
	}
	
	// Methods
	public RectF getAreaWithTags() {
		return new RectF(areaWithTagsX1, areaWithTagsY1, areaWithTagsX2, areaWithTagsY2);
	}
	
}
