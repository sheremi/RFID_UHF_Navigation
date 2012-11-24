package de.unierlangen.like.navigation;

import android.graphics.PointF;

public class Wall extends Obstacle {

    private final float x1_wall;
    private final float y1_wall;
    private final float x2_wall;
    private final float y2_wall;
    private final float gradient;
    // The deviation's angle of the wall's line from the X axis
    private final double alpha;
    private PointF intersection;

    public Wall(float x1, float y1, float x2, float y2) {
        super();
        this.x1_wall = x1;
        this.y1_wall = y1;
        this.x2_wall = x2;
        this.y2_wall = y2;
        // the slope ("k" in the Linear equation "y=kx+b") or gradient of the
        // wall's line
        gradient = (y2_wall - y1_wall) / (x2_wall - x1_wall);
        alpha = Math.atan(gradient);

    }

    // Getters and setters
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
        return "Wall [x1_wall=" + x1_wall + ", y1_wall=" + y1_wall + ", x2_wall=" + x2_wall
                + ", y2_wall=" + y2_wall + "]";
    }

    /**
     * Finds out, if the wall is located between a tag and a possible position
     * of the reader. If yes, calculates intersections of the wall with the line
     * between tag and position.
     * 
     * @param x
     *            Coordinate x of the possible reader's position
     * @param y
     *            Coordinate y of the possible reader's position
     * @param tag
     *            Object of the regarded tag
     * @return Coordinates of the found intersection
     */
    @Override
    public PointF getIntersection(Tag tag, float x, float y) {
        float x_cross;
        float y_cross;
        if (Math.abs(x1_wall - x2_wall) < 0.0001f) {
            x_cross = x1_wall;
            float k = (tag.getY() - y) / (tag.getX() - x);
            float b = tag.getY() - k * tag.getX();
            y_cross = x_cross * k + b;
        } else {
            float k_wall = (y1_wall - y2_wall) / (x1_wall - x2_wall);
            // if (Math.abs(y1_wall - y2_wall)<0.001f) k_wall = 0;
            float b_wall = y1_wall - k_wall * x1_wall;
            float k = (tag.getY() - y) / (tag.getX() - x);
            float b = tag.getY() - k * tag.getX();
            x_cross = (b_wall - b) / (k - k_wall);
            y_cross = (k * b_wall - k_wall * b) / (k - k_wall);
        }
        float xWall_min = Math.min(x1_wall, x2_wall) - 0.1f;
        float xWall_max = Math.max(x1_wall, x2_wall) + 0.1f;
        float yWall_min = Math.min(y1_wall, y2_wall) - 0.1f;
        float yWall_max = Math.max(y1_wall, y2_wall) + 0.1f;
        float xTagPoint_min = Math.min(tag.getX(), x) - 0.1f;
        float xTagPoint_max = Math.max(tag.getX(), x) + 0.1f;
        float yTagPoint_min = Math.min(tag.getY(), y) - 0.1f;
        float yTagPoint_max = Math.max(tag.getY(), y) + 0.1f;
        if (xWall_min <= x_cross && xWall_max >= x_cross && xTagPoint_min <= x_cross
                && xTagPoint_max >= x_cross && yWall_min <= y_cross && yWall_max >= y_cross
                && yTagPoint_min <= y_cross && yTagPoint_max >= y_cross) {
            intersection = new PointF(x_cross, y_cross);
            return intersection;
        }
        return null;
    }
}
