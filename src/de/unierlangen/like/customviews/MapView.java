package de.unierlangen.like.customviews;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import de.unierlangen.like.navigation.Door;
import de.unierlangen.like.navigation.RoomsDatabase;
import de.unierlangen.like.navigation.Tag;
import de.unierlangen.like.navigation.Wall;
import de.unierlangen.like.navigation.Zone;
import de.unierlangen.like.ui.R;
/**
 * View to represent map and tags graphically
 * @author Kate
 *
 */
public class MapView extends View {
	private static final int SMOOTH_TRANSLATION_FRAMES = 30;
	private static final String TAG = MapView.class.getSimpleName();
	public static final int REQUEST_TRANSLATE = 1;
	// Drawing tools
	private Paint debugRectPaint;
	private Paint tagPaint;
	private Paint wallsPaint;
	private Paint doorsPaint;
	private Paint zonePaintFilled;
	private Paint zonePaintBounder;
	private Paint routePaint;
	private Paint readerPositionPaint;
	private Paint textPaint;
	private Picture preDrawnMap;
	//Items to draw
	private ArrayList<Wall> walls;
	private ArrayList<Door> doors;
	private ArrayList<Tag> tags;
	private RectF rectFTags;
	private ArrayList<Zone> zones;
	private Path routingPath;
	private PointF readerPosition;
	// Items to translate and scale
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
	
	// This handles handles a request to make a new translate 
	// (when the view should be moved because of the changing of reader's position)
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REQUEST_TRANSLATE:
				rectFTags = (RectF) msg.obj;
				invalidate();
				break;
			default:
				break;
			}
		}
	};
	private int viewWidth;
	private int viewHeight;

	// Methods
	private void init() {
		//initialize all fields as empty
		walls = new ArrayList<Wall>();
		doors = new ArrayList<Door>();
		tags = new ArrayList<Tag>();
		rectFTags = new RectF();
		zones = new ArrayList<Zone>();
		routingPath = new Path();
		readerPosition = new PointF();
		
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
		
		routePaint = new Paint();
		routePaint.setStyle(Paint.Style.STROKE);
		routePaint.setColor(0xdfEE0000);
		routePaint.setStrokeWidth(0.18f);
		routePaint.setAntiAlias(true);
		
		readerPositionPaint = new Paint();
		readerPositionPaint.setStyle(Paint.Style.FILL);
		readerPositionPaint.setColor(0xffFF00FF);
		readerPositionPaint.setAntiAlias(true);
		
		textPaint = new Paint();
		textPaint.setStyle(Paint.Style.STROKE);
		textPaint.setColor(0xffFFD700);
		textPaint.setStrokeWidth(0.05f);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(1.0f);
	}
	
	public float getPadding() {
		return padding;
	}
	
	// Methods, which are used to control the view
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
		float diffLeft = rectFTags.left - this.rectFTags.left;
		float diffTop = rectFTags.top - this.rectFTags.top;
		float diffRight = rectFTags.right - this.rectFTags.right;
		float diffBottom = rectFTags.bottom - this.rectFTags.bottom;
		//TODO Here we can optimize the speed of translate
		for (int i=1; i<=SMOOTH_TRANSLATION_FRAMES; i++){
			RectF rectF = new RectF(this.rectFTags);
			rectF.left += (diffLeft/SMOOTH_TRANSLATION_FRAMES)*i;
			rectF.top += (diffTop/SMOOTH_TRANSLATION_FRAMES)*i;
			rectF.right += (diffRight/SMOOTH_TRANSLATION_FRAMES)*i;
			rectF.bottom += (diffBottom/SMOOTH_TRANSLATION_FRAMES)*i;
			Message msg = Message.obtain(handler, REQUEST_TRANSLATE, rectF);
			handler.sendMessageDelayed(msg, 50*i);
		}
	}
	
	public void setRoute(Path routingPath){
		this.routingPath = routingPath;
		invalidate();
	}
	
	public void setReaderPosition(PointF readerPosition){
		this.readerPosition = readerPosition;
		invalidate();
	}
	
	private void prepareDrawingArea(Canvas canvas)	{
		float minX = rectFTags.left - padding;
		float minY = rectFTags.top - padding;
		float maxX = rectFTags.right + padding;
		float maxY = rectFTags.bottom + padding;
		float scaleFactor;
		float positionX = -minX;
		float positionY = -minY;
		if ((maxX - minX)/(maxY - minY) > getWidth()/ getHeight()){
			scaleFactor = getWidth()/(maxX - minX);
			positionY += getHeight()/scaleFactor/2-(maxY-minY)/2;
		} else {
			scaleFactor = getHeight()/(maxY - minY);
			positionX += getWidth()/scaleFactor/2-(maxX-minX)/2;
		}
		canvas.scale(scaleFactor, scaleFactor);
		canvas.translate(positionX, positionY);
	}
	
	private void drawTag(Canvas canvas, Paint paint, Tag tag) {
		
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
	
	private void drawZone(Canvas canvas, Paint paint, Zone zone) {
		Path path = new Path();
		path.setLastPoint(zone.getPoints().get(0).x, zone.getPoints().get(0).y);
		for (PointF point: zone.getPoints()) {
			path.lineTo(point.x, point.y);
		}
		path.close();
		path.setFillType(FillType.WINDING);
		canvas.drawPath(path, paint);
		//Log.d("drawZone","zone has been drawn");
	}
	
	private void drawRoute(Canvas canvas, Paint paint, Path routingPath){
		canvas.drawPath(routingPath, paint);
	}
	/**
	 * @param canvas
	 */
	private void drawPosition(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		Paint logoPaint = new Paint();
		logoPaint.setFilterBitmap(true);
		// Make the icon more green
		logoPaint.setColorFilter(new LightingColorFilter(0x1f338822, 0));
		Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.android);
		Matrix logoMatrix = new Matrix();
		// Set correct size of the icon
		float bitmapScale = 0.025f;
		logoMatrix.setScale(bitmapScale, bitmapScale);
		// Calculate the offset of the icon's position
		float offset = - (icon.getHeight() + icon.getWidth()) * bitmapScale / 4;
		canvas.translate(readerPosition.x + offset,readerPosition.y + offset);
		canvas.drawBitmap(icon, logoMatrix, logoPaint);
		canvas.restore();
	}
	/**
	 * @param canvas
	 */
	private void drawRoomName(Canvas canvas) {
		RoomsDatabase roomsDatabase = RoomsDatabase.getRoomsDatabase();
		for (String roomName : roomsDatabase.getRoomsNamesArray()){
			if (roomName.contains("3")){
			PointF roomCoordinates = roomsDatabase.getRoomCoordinates(roomName);
			canvas.drawText(roomName, roomCoordinates.x, roomCoordinates.y, textPaint);
			}
		}
	}

	/**
	 * 
	 */
	private void drawMap(Canvas canvas) {
		for (Wall wall: walls) {
			canvas.drawLine(wall.getX1(), wall.getY1(), wall.getX2(), wall.getY2(), wallsPaint);
		}
		for (Door door: doors){
			float startAngle = door.getStartAngle();
			if (door.getLength()<0) {
				startAngle = startAngle + 180;
			}
			canvas.drawArc(door.getRectF(), startAngle, door.getSweepAngle(), true, doorsPaint);
		}
		drawRoomName(canvas);
	}

	//Override view's methods
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		viewWidth = widthMeasureSpec;
		viewHeight = heightMeasureSpec;
		Log.d(TAG, "Width spec: " + MeasureSpec.toString(viewWidth));
		Log.d(TAG, "Height spec: " + MeasureSpec.toString(viewHeight));
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		//if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {widthSize=200;}
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		//if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {heightSize=300;}
		setMeasuredDimension(widthSize, heightSize);
		// 
		preDrawnMap = new Picture();
		Canvas mapCanvas = preDrawnMap.beginRecording(widthSize, heightSize);
		drawMap(mapCanvas);
		preDrawnMap.endRecording();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
		/** Calculate drawing area, using counted tags' position. */
		prepareDrawingArea(canvas);
		/** Draw debug rectangle */		
		canvas.drawRect(rectFTags, debugRectPaint);
		/** Draw zones around tags */
		if (!zones.isEmpty()){
			for (Zone zone: zones){
				drawZone(canvas, zonePaintFilled, zone);
				drawZone(canvas, zonePaintBounder, zone);
			}
		}
		/** Draw map */
		canvas.drawPicture(preDrawnMap);
		/** Draw tags */
		for (Tag tag: tags){
			drawTag(canvas, tagPaint, tag);
		}
		/** Draw current reader's position */
		drawPosition(canvas);
		/** Draw route*/
		drawRoute(canvas, routePaint, routingPath);
		/** Restore canvas state */
		canvas.restore();
	}
	public int getViewWidth() {
		return viewWidth;
	}
	public int getViewHeight() {
		return viewHeight;
	}
	
}