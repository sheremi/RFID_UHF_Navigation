package de.unierlangen.like.navigation;

import android.graphics.RectF;


public class Door extends Obstacle {
	//private static final String TAG = Door.class.getSimpleName();
	private float xAxle;
	private float yAxle;
	private float length;
	private float startAngle;
	private float sweepAngle;
	
	//Constructor
	public Door(float xAxle, float yAxle, float length, float startAngle, float sweepAngle) {
		super();
		this.xAxle = xAxle;
		this.yAxle = yAxle;
		this.length = length;
		this.startAngle = startAngle;
		this.sweepAngle = sweepAngle;
	}
		
	//Getters and setters
	public float get_xAxle() {
		return xAxle;
	}
	
	public float get_yAxle() {
		return yAxle;
	}
	
	public float getLength() {
		return length;
	}
	
	public float getSweepAngle() {
		return sweepAngle;
	}
	
	public float getStartAngle() {
		return startAngle;
	}
	public RectF getRectF() {
		return new RectF(xAxle-Math.abs(length), yAxle-Math.abs(length), xAxle+Math.abs(length), yAxle+Math.abs(length));
	}
	@Override
	public boolean isBetween(Tag tag, float x, float y) {
		// TODO Implement some math
		return false;
	}

	@Override
	public String toString() {
		return "Door [xAxle=" + xAxle + ", yAxle=" + yAxle + ", length="
				+ length + ", startAngle=" + startAngle + ", sweepAngle="
				+ sweepAngle + "]";
	}
}
