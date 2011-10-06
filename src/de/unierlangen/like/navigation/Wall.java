package de.unierlangen.like.navigation;


public class Wall extends Obstacle {
	
	private float x1_wall;
	private float y1_wall;
	private float x2_wall;
	private float y2_wall;
	private float gradient;
	//The angle of incline between X axe and a wall
	private double alpha;
	
	public Wall(float x1, float y1, float x2, float y2) {
		super();
		this.x1_wall = x1;
		this.y1_wall = y1;
		this.x2_wall = x2;
		this.y2_wall = y2;
		gradient = (y2_wall - y1_wall)/(x2_wall - x1_wall);
		alpha = Math.atan(gradient);
		
	}
	//Getters and setters
	public float getX1() {
		return x1_wall;
	}

	public float getY1() {
		return y1_wall;
	}
	
	public float getX2() {
		return x2_wall;
	}

	public float getY2() {
		return y2_wall;
	}
	
	public double getAlpha() {
		return alpha;
	}
	
	@Override
	public String toString() {
		return "Wall [x1_wall=" + x1_wall + ", y1_wall=" + y1_wall
				+ ", x2_wall=" + x2_wall + ", y2_wall=" + y2_wall + "]";
	}
	
	public boolean isBetween (Tag tag, float x, float y){
		//TODO implement math (isBetween, Wall)
		
		return false;
	}

}
