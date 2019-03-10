package com.macrokeys;

import com.macrokeys.rendering.*;
import com.macrokeys.screen.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Screen of a {@link MacroSetup}
 */
public final class MacroScreen implements Cloneable, Serializable {

    /**
     * Serial for {@link Serializable}
     */
    private static final long serialVersionUID = 1L;

    /**
     * The List of keys of this screen
     */
    private List<MacroKey> keys;

    /**
     * Color of the background
     */
    private int colorBackground;

    /**
     * Text of the background
     */
    private String backgroundText;

    /**
     * Swipe to call this screen
     */
    private SwipeType swipeType;

    /**
     * Settings of the orientation for this screen
     */
    private Orientation orientation;

    /**
     * Instantiate an empty screen
     */
    public MacroScreen() {
        keys = new ArrayList<>();
        colorBackground = Color.WHITE;
        backgroundText = "";
        swipeType = SwipeType.Finger2_Up;
        orientation = Orientation.Horizontal;
    }

    /**
     * Render this at screen
     * @param r the Rendering object
     * @param s Screen where to render
     * @param drawArea the Area where to render
     * @param keyPress Keys actually pressed
     */
    public void render(@NonNull Renderer r, @NonNull Screen s, @NonNull RectF drawArea, @NonNull List<MacroKey> keyPress) {
        Objects.requireNonNull(r);
        Objects.requireNonNull(s);
        Objects.requireNonNull(drawArea);
        Objects.requireNonNull(keyPress);
        r.setTextSize(50);
        r.setAntiAlias(true);
        r.setTextAllign(TextAllign.Center);
        // Render backgraund color and text
        r.setPaintStyle(PaintStyle.Fill);
        r.setColor(getBackgroundColor());
        r.rect(drawArea);
        drawStringRect(r, getBackgroundText(), Color.BLACK, drawArea);
        for (MacroKey k : getKeys()) {
            int border;
            int fill;
            // CHeck if key was pressed (instance appears)
            if (contains(k, keyPress)) {
                border = k.getColorEdgePress();
                fill = k.getColorFillPress();
            } else {
                border = k.getColorEdge();
                fill = k.getColorFill();
            }
            RectF pix = k.getAreaPixel(s);
            if (k.getShape().getType().equals(KeyShape.Type.Ellipse)) {
                r.setPaintStyle(PaintStyle.Fill);
                r.setColor(fill);
                r.ellipse(pix);
                r.setPaintStyle(PaintStyle.Stroke);
                r.setColor(border);
                r.ellipse(pix);
            } else {
                r.setPaintStyle(PaintStyle.Fill);
                r.setColor(fill);
                r.rect(pix);
                r.setPaintStyle(PaintStyle.Stroke);
                r.setColor(border);
                r.rect(pix);
            }
            drawStringRect(r, k.getText(), Color.BLACK, pix);
        }
    }

    /**
     * @param m
     * @param l
     * @return True if the key {@code m} is present in {@code l}, false otherwise
     */
    private static boolean contains(MacroKey m, List<MacroKey> l) {
        assert m != null && l != null;
        for (MacroKey mm : l) {
            if (m == mm) {
                return true;
            }
        }
        return false;
    }

    /**
     * Render some text in the area, if possible
     * @param r Rendering
     * @param s Text to render
     * @param color color of the text
     * @param area the Area where to place the text
     */
    private static void drawStringRect(Renderer r, String s, int color, RectF area) {
        assert r != null && s != null && area != null;
        r.setColor(color);
        r.text(s, area);
    }

    /**
     * @return the List of keys contained vy this screen
     */
    @NonNull
    public List<MacroKey> getKeys() {
        assert keys != null;
        return keys;
    }

    /**
     * @return the Background color
     */
    public int getBackgroundColor() {
        return colorBackground;
    }

    /**
     * @return the Background tet
     */
    @NonNull
    public String getBackgroundText() {
        return backgroundText;
    }

    /**
     * @return Swipe type to call this screen
     */
    @NonNull
    public SwipeType getSwipeType() {
        return swipeType;
    }

    /**
     * @return the Orientation of this screen
     */
    @NonNull
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * @param swipeType Swipe that invokes this screen
     */
    public void setSwipeType(@NonNull SwipeType swipeType) {
        this.swipeType = swipeType;
    }

    /**
     * @param colorBackground Background color
     */
    public void setBackgroundColor(int colorBackground) {
        this.colorBackground = colorBackground;
    }

    /**
     * @param backgroundText Text background
     */
    public void setBackgroundText(@NonNull String backgroundText) {
        this.backgroundText = backgroundText;
    }

    /**
     * @param orientation the Orientation of this screen
     */
    public void setOrientation(@NonNull Orientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Indicates if at the given position there is a key
     * @param x X position in pixels
     * @param y Y position in pixels
     * @param s Screen used
     * @return the Key found at the given position
     */
    public MacroKey keyAt(float x, float y, @NonNull Screen s) {
        Objects.requireNonNull(s);
        // Backword iteration to match the rendering order
        // (the first item in the queque is rendered first -> is overlapped by other keys)
        ListIterator<MacroKey> it = getKeys().listIterator(getKeys().size());
        while (it.hasPrevious()) {
            MacroKey k = it.previous();
            if (keyIntersect(k, s, x, y)) {
                return k;
            }
        }
        return null;
    }

    /**
     * Indicates if the key is intersected in the given point
     * @param m key to check
     * @param s the Screen where the key is rendered
     * @param x X position in pixels
     * @param y Y position in pixels
     * @return True-the key is intersected, false otherwise
     */
    private static boolean keyIntersect(@NonNull MacroKey m, @NonNull Screen s, float x, float y) {
        assert m != null && s != null;
        final RectF r = m.getAreaPixel(s);
        switch(m.getShape().getType()) {
            case Rectangle:
                return r.contains(x, y);
            case Ellipse:
                return pointInEllipse(r.centerX(), r.centerY(), // Dimezzo the diameter
                r.width() / 2, // Dimezzo the diameter
                r.height() / 2, x, y);
            default:
                assert false : "Unnkown case";
                return false;
        }
    }

    /**
     * Checks whether an ellipse intersect a pointTesta the intersection between an ellipse
     * @param cx the X position of the center
     * @param cy the Y poisition of the center of the ellipse
     * @param rx Radius on the X axis of the ellipse
     * @param ry Radius on the Y axis of the ellipse
     * @param x X position of the point
     * @param y Y position of the point
     * @return True if there is intersection, false otherwise
     * @see {@linkplain http://math.stackexchange.com/questions/76457/check-if-a-point-is-within-an-ellipse}
     */
    private static boolean pointInEllipse(float cx, float cy, float rx, float ry, float x, float y) {
        return ((x - cx) * (x - cx)) / (rx * rx) + ((y - cy) * (y - cy)) / (ry * ry) <= 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MacroScreen)) {
            return false;
        } else {
            MacroScreen s = ((MacroScreen) obj);
            List<MacroKey> l1 = getKeys();
            List<MacroKey> l2 = s.getKeys();
            if (l1.size() != l2.size() || getBackgroundColor() != s.getBackgroundColor() || !getBackgroundText().equals(s.getBackgroundText()) || !getOrientation().equals(s.getOrientation()) || !getSwipeType().equals(s.getSwipeType())) {
                return false;
            }
            boolean r = true;
            Iterator<MacroKey> it1 = l1.iterator();
            Iterator<MacroKey> it2 = l2.iterator();
            while (r && it1.hasNext()) {
                r = it1.next().equals(it2.next());
            }
            return r;
        }
    }

    @Override
    public int hashCode() {
        int sum = 0;
        for (MacroKey k : getKeys()) {
            sum += k.hashCode();
        }
        sum += getBackgroundColor();
        sum += getBackgroundText().hashCode();
        sum += getOrientation().hashCode();
        sum += getSwipeType().hashCode();
        return sum;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return JavaUtil.utilDeepClone(this);
    }

    /**
     * Swipe to call this screen
     */
    public enum SwipeType {

        Finger2_Up,
        Finger2_Right,
        Finger2_Down,
        Finger2_Left,
        Finger3_Up,
        Finger3_Right,
        Finger3_Down,
        Finger3_Left
    }

    /**
     * The Orientation of the screen
     */
    public enum Orientation {

        /**
         * Vertical
         */
        Vertical,
        /**
         * Horizontal
         */
        Horizontal,
        /**
         * Gets the rotation from the accelerometer
         */
        Rotate
    }
}
