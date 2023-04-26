package com.bw.game.download.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.winner.casino.wheel.R;

public class StrokeTextView extends TextView {

    public TextView borderText;///用于描边的TextView
    private int colors;
    private int strokeWidth;

    public StrokeTextView(Context context) {
        this(context,null);
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public StrokeTextView(Context context, AttributeSet attrs,int defStyle) {
        super(context, attrs, defStyle);
        borderText = new TextView(context,attrs);
        init(attrs);
    }

    public void init(AttributeSet attrs){

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
        colors = ta.getColor(R.styleable.StrokeTextView_stroke_color,0xFFFFFF);
        strokeWidth = ta.getDimensionPixelSize(R.styleable.StrokeTextView_stroke_width, 0);
        ta.recycle();

        TextPaint tp1 = borderText.getPaint();                //new
        tp1.setStrokeWidth(strokeWidth);                                  //设置描边宽度
        tp1.setStyle(Paint.Style.STROKE);                             //对文字只描边
        borderText.setTextColor(colors);                        //设置描边颜色
        borderText.setGravity(getGravity());
    }

    @Override
    public void setLayoutParams (ViewGroup.LayoutParams params){
        super.setLayoutParams(params);
        borderText.setLayoutParams(params);
    }

    /**
     * onMeasure通过父View传递过来的大小和模式，
     * 以及自身的背景图片的大小得出自身最终的大小，
     * 然后通过setMeasuredDimension()方法设置给mMeasuredWidth和mMeasuredHeight.
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CharSequence tt = borderText.getText();

        //两个TextView上的文字必须一致
        if(tt== null || !tt.equals(this.getText())){
            borderText.setText(getText());
            this.postInvalidate();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        borderText.measure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 该方法是View的放置方法，在View类实现。
     * 调用该方法需要传入放置View的矩形空间左上角left、top值和右下角right、bottom值。
     * 这四个值是相对于父控件而言的。
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    protected void onLayout (boolean changed, int left, int top, int right, int bottom){
        super.onLayout(changed, left, top, right, bottom);
        borderText.layout(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        borderText.draw(canvas);
        super.onDraw(canvas);
    }

}
