package de.unierlangen.like.customviews;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import de.unierlangen.like.navigation.Door;
import de.unierlangen.like.navigation.Tag;
import de.unierlangen.like.navigation.Wall;
import de.unierlangen.like.navigation.Zone;
/**
 * View to represent map and tags graphically
 * @author Kate
 *
 */
public class MapView extends View {
	private static final String TAG = MapView.class.getSimpleName();
	// drawing tools
	private Paint debugRectPaint;
	private Paint tagPaint;
	private Paint wallsPaint;
	private Paint doorsPaint;
	private Paint zonePaintFilled;
	private Paint zonePaintBounder;
	//Items to draw
	private ArrayList<Wall> walls;
	private ArrayList<Door> doors;
	private ArrayList<Tag> tags;
	private RectF rectFTags;
	private ArrayList<Zone> zones;
	//
	private float minX;
	private float minY;
	private float maxX;
	private float maxY;
	private float padding = 3.0f;
	
	// Constructors
	public MapView(Context context) {
		super(context);
		init();
	}
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	// Methods
	private void init() {
		//initialize all fields as empty
		walls = new ArrayList<Wall>();
		doors = new ArrayList<Door>();
		tags = new ArrayList<Tag>();
		rectFTags = new RectF();
		zones = new ArrayList<Zone>();
		
		tagPaint = new Paint();
		tagPaint.setStyle(Paint.Style.STROKE);
		tagPaint.setColor(0xff00d000);
		tagPaint.setStrokeWidth(0.15f);
		tagPaint.setAntiAlias(true);
		
		zonePaintFilled = new Paint();
		zonePaintFilled.setStyle(Paint.Style.FILL_AND_STROKE);
		zonePaintFilled.setColor(0x3f1E90FF);
		zonePaintFilled.setStrokeWidth(0.05f);
		zonePaintFilled.setAntiAlias(true);
		
		zonePaintBounder = new Paint();
		zonePaintBounder.setStyle(Paint.Style.STROKE);
		zonePaintBounder.setColor(0x8f1E90FF);
		zonePaintBounder.setStrokeWidth(0.05f);
		zonePaintBounder.setAntiAlias(true);
		
		wallsPaint = new Paint();
		wallsPaint.setStyle(Paint.Style.STROKE);
		wallsPaint.setColor(0xffFFFAFA);
		wallsPaint.setStrokeWidth(0.2f);
		wallsPaint.setAntiAlias(true);
		
		doorsPaint = new Paint();
		doorsPaint.setStyle(Paint.Style.STROKE);
		doorsPaint.setColor(0xdf2E8B57);
		doorsPaint.setStrokeWidth(0.18f);
		doorsPaint.setAntiAlias(true);
		
		debugRectPaint = new Paint();
		debugRectPaint.setStyle(Paint.Style.FILL);
		debugRectPaint.setColor(0x5fff2318);
		debugRectPaint.setStrokeWidth(0.1f);
		debugRectPaint.setAntiAlias(true);
	}
	
	public float getPadding() {
		return padding;
	}
	
	//Methods used to control the view
	public void setAreaPadding(float newPadding) {
		padding = newPadding;
		invalidate();
	}
	public void setTags(ArrayList<Tag> tags) {
		this.tags = tags;
		invalidate();
	}
	public void setZones(ArrayList<Zone> zones) {
		this.zones = zones;
		invalidate();
	}
	public void setWalls(ArrayList<Wall> walls) {
		Log.d(TAG,"Received "+ walls.size() + " walls.");
		this.walls = walls;
		invalidate();
	}

	public void setDoors(ArrayList<Door> doors) {
		this.doors = doors;
		invalidate();
	}

	public void setRectFTags(RectF rectFTags) {
		this.rectFTags = rectFTags;
		invalidate();
	}
		
	//Override views methods
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
		Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		//if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {widthSize=200;}
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		//if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {heightSize=300;}
		setMeasuredDimension(widthSize, heightSize);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
		/** Calculate drawing area, using counted tags' position. */
		prepareDrawingArea(canvas, getWidth(), getHeight());
		/** Draw debug rectangle */		
		canvas.drawRect(rectFTags, debugRectPaint);
		/** TODO comment */
		if (!zones.isEmpty()){
			for (Zone zone: zones){
				drawZone(canvas, zonePaintFilled, zone);
				drawZone(canvas, zonePaintBounder, zone);
			}
		}
		/** Draw walls */
		for (Wall wall: walls)
		{
			canvas.drawLine(wall.getX1(), wall.getY1(), wall.getX2(), wall.getY2(), wallsPaint);
		}
		/** Draw doors */
		for (Door door: doors){
			float startAngle = door.getStartAngle();
			if (door.getLength()<0) {
				startAngle = startAngle + 180;
			}
			canvas.drawArc(door.getRectF(), startAngle, door.getSweepAngle(), true, doorsPaint);
		}
		/** Draw tags */
		for (Tag tag: tags){
			drawTag(canvas, tagPaint, tag);
		}
		/** Restore canvas state */
		canvas.restore();
	}

	private void prepareDrawingArea(Canvas canvas, int width, int height) {
		float scaleFactor;
		minX = rectFTags.left - padding;
		minY = rectFTags.top - padding;
		maxX = rectFTags.right + padding;
		maxY = rectFTags.bottom + padding;
		
		if ((maxX - minX)/(maxY - minY) > width/height){
			scaleFactor = width/(maxX - minX);
			canvas.scale(scaleFactor, scaleFactor);
			canvas.translate(-minX, -minY);
			canvas.translate(0, height/scaleFactor/2-(maxY-minY)/2);
		} else {
			scaleFactor = height/(maxY - minY);
			canvas.scale(scaleFactor, scaleFactor);
			canvas.translate(-minX, -minY);
			canvas.translate(width/scaleFactor/2-(maxX-minX)/2, 0);
		}
		
	}
	
	public void drawTag(Canvas canvas, Paint paint, Tag tag) {
		
		int filterColor = 0;
		/** Calculates value relative to range*/
		float position;
		int avarageRSSI = (int)((Tag.maxRSSI+Tag.minRSSI)/2);
		int currentRSSI = tag.getRssi(); 
		
		if (currentRSSI < avarageRSSI) {
			position = 1f*(avarageRSSI - currentRSSI) / (avarageRSSI - Tag.minRSSI);
			filterColor |= (int) ((0xf0) * position); // blue
		} else {
			position = 1f*(currentRSSI - avarageRSSI) / (Tag.maxRSSI - avarageRSSI);
			filterColor |= ((int) ((0xf0) * position)) << 16; // red	
		}
		
		paint.setColorFilter(new LightingColorFilter(0xff338822, filterColor));
		
		canvas.drawCircle(tag.getX(), tag.getY(), 0.2f, paint);
		
		// TODO implement new tag's view 
		/*canvas.save(Canvas.MATRIX_SAVE_FLAG);
		Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
		Matrix logoMatrix = new Matrix();
		logoScale = (1.0f / logo.getWidth()) * 0.3f;;
		logoMatrix.setScale(logoScale, logoScale);
		canvas.translate(x,y);
		canvas.drawBitmap(logo, logoMatrix, paint);
		canvas.restore();	*/
	}
	
	public void drawZone(Canvas canvas, Paint paint, Zone zone) {
		Path path = new Path();
		path.setLastPoint(zone.getPoints().get(0).x, zone.getPoints().get(0).y);
		for (PointF point: zone.getPoints()) {
			path.lineTo(point.x, point.y);
		}
		path.close();
		path.setFillType(FillType.WINDING);
		canvas.drawPath(path, paint);
		Log.d("drawZone","zone has been drawn");
	}
}






	

