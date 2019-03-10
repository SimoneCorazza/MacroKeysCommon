package com.macrokeys;

import java.io.Serializable;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import com.macrokeys.rendering.Color;
import com.macrokeys.rendering.RectF;
import com.macrokeys.screen.Screen;
import com.macrokeys.screen.ScreenUtility;

/**
 * Key present in the {@link MacroScreen}
 */
public final class MacroKey implements Cloneable, Serializable, Comparable<MacroKey> {

    /**
     * Serial for {@link Serializable}
     */
    private static final long serialVersionUID = 2L;

    /**
     * Identifier of the key in the {@link MacroSetup}
     */
    private int id = -1;

    /**
     * Sequence of keys associated with this macro key
     */
    private LimitedKeySequence macroSeq;

    /**
     * The Area occupied by this key at screen in millimiters
     */
    private RectF area;

    /**
     * The Shape of the key
     */
    private KeyShape shape;

    /**
     * Color of the border
     */
    private int colorEdge;

    /**
     * Color of the fill
     */
    private int colorFill;

    /**
     * Color of the border when the key is pressed
     */
    private int colorEdgePress;

    /**
     * Color of the fill when the key is pressed
     */
    private int colorFillPress;

    /**
     * String of the test to render
     */
    private String text;

    /**
     * Type of key
     */
    private MacroKeyType type;

    /**
     * Instance a new key with default property
     */
    public MacroKey() {
        setArea(new RectF(0, 0, 1, 1));
        setShape(new KeyShape());
        setColorEdge(Color.BLACK);
        setColorFill(Color.GRAY);
        setColorEdgePress(Color.BLACK);
        setColorFillPress(Color.DKGRAY);
        setText("Macro key");
        setKeySeq(new LimitedKeySequence());
        setType(MacroKeyType.Normal);
    }

    /**
     * @return Identifier of the key
     */
    public int getId() {
        return id;
    }

    /**
     * @param Identifier of the key
     */
    void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Parameter id must be >= 0");
        }
        this.id = id;
    }

    /**
     * @return the Area (not editable) of the key at screen in millimiters
     */
    public RectF getArea() {
        return new RectF(area);
    }

    /**
     * @param s Screen for which obtein the area of this key
     * @return the Area of the key at screen in pixels
     * @throws NullPointerException If {@code s} is null
     */
    @NonNull
    public RectF getAreaPixel(@NonNull Screen s) {
        Objects.requireNonNull(s);
        return ScreenUtility.mmtopx(area, s);
    }

    /**
     * @return Shape of the key
     */
    @NonNull
    public KeyShape getShape() {
        return shape;
    }

    /**
     * @return Color of the border
     */
    public int getColorEdge() {
        return colorEdge;
    }

    /**
     * @return Color of the filling of the key
     */
    public int getColorFill() {
        return colorFill;
    }

    /**
     * @return the Text of the key
     */
    @NonNull
    public String getText() {
        return text;
    }

    /**
     * @return Color of the border of the key when it is pressed
     */
    public int getColorEdgePress() {
        return colorEdgePress;
    }

    /**
     * @return Color of the filling of the key when it is pressed
     */
    public int getColorFillPress() {
        return colorFillPress;
    }

    /**
     * @param c Color of the filling
     */
    public void setColorFill(int c) {
        colorFill = c;
    }

    /**
     * @param area area of the key at screen in millimiters
     */
    public void setArea(@NonNull RectF area) {
        Objects.requireNonNull(area);
        if (area.width() < 0 || area.height() < 0) {
            throw new IllegalArgumentException("The width and heigth must be >= 0");
        }
        this.area = new RectF(area);
    }

    /**
     * @param shape the Shape of the key
     */
    public void setShape(@NonNull KeyShape shape) {
        Objects.requireNonNull(shape);
        this.shape = shape;
    }

    /**
     * @param colorEdge Color of the edge
     */
    public void setColorEdge(int colorEdge) {
        this.colorEdge = colorEdge;
    }

    /**
     * @param colorEdgePress Color of the edge when the key is pressed
     */
    public void setColorEdgePress(int colorEdgePress) {
        this.colorEdgePress = colorEdgePress;
    }

    /**
     * @param colorFillPress Color of the fiiling when the key is pressed
     */
    public void setColorFillPress(int colorFillPress) {
        this.colorFillPress = colorFillPress;
    }

    /**
     * @param text text of the key
     */
    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    /**
     * @return Sequence of keys associeted with the key
     */
    @NonNull
    public LimitedKeySequence getKeySeq() {
        return macroSeq;
    }

    /**
     * @param keySeq the Sequence of keys associeted with this
     */
    public void setKeySeq(@NonNull LimitedKeySequence keySeq) {
        Objects.requireNonNull(keySeq);
        this.macroSeq = keySeq;
    }

    /**
     * @return Key type
     */
    public MacroKeyType getType() {
        return type;
    }

    /**
     * @param keystrokeOnUp Key type
     */
    public void setType(MacroKeyType type) {
        this.type = type;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return JavaUtil.utilDeepClone(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MacroKey)) {
            return false;
        } else {
            // Executing the cheks for every field not only the ID
            MacroKey k2 = (MacroKey) obj;
            return getId() == k2.getId() && getType() == k2.getType() && getArea().equals(k2.getArea()) && getColorEdge() == k2.getColorEdge() && getColorEdgePress() == k2.getColorEdgePress() && getColorFill() == k2.getColorFill() && getColorFillPress() == k2.getColorFillPress() && getKeySeq().equals(k2.getKeySeq()) && getShape().equals(k2.getShape()) && getText().equals(k2.getText());
        }
    }

    @Override
    public int hashCode() {
        return getId() + getArea().hashCode() + getType().ordinal() + getColorEdge() + getColorEdgePress() + getColorFill() + getColorFillPress() + getKeySeq().hashCode() + getShape().hashCode() + getText().hashCode();
    }

    @Override
    public int compareTo(MacroKey m) {
        if (m == null) {
            return 0;
        } else {
            return Integer.compare(getId(), m.getId());
        }
    }
}
