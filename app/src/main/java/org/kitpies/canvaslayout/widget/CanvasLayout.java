/**
 * Copyright 2016 YuHao-KitPies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kitpies.canvaslayout.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.kitpies.canvaslayout.R;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * <h1>Description:</h1>
 * A canvas layout for handle multiple screen size and density.<br/>
 * You can use the canvas layout in your java code or in your xml hierarchy as other layout widget.<br/>
 * It observe the principle：The size and density is various, but the scaling is constant.
 * <h1>How does it work:</h1>
 * Let's define several term first.<br/>
 * <br/>
 * A virtual window is a scaled fixed aspect ratio rectangle of the design drawing.<br/>
 * The virtual window is fit and inside the physics window.<br/>
 * The physics window is named stretch window too.<br/>
 * <br/>
 * When we design a combine widget, the available space in the android ui hierarchy for <br/>
 * the combine widget is the stretch window.<br/>
 * <br/>
 * When we decide the widgets size, we have two choices. One is scaling with the virtual window.<br/>
 * The other is scaling with the stretch window.<br/>
 * <br/>
 * When we layout the widgets in the combine widget, we have two choices. One is layout widgets<br/>
 * in the virtual window. The other is layout widgets int the stretch window.<br/>
 *
 * <h1>How to use:</h1>
 * <code>
 *      &lt;org.kitpies.canvaslayout.widget.CanvasLayout<br/>
 *        	&nbsp;android:layout_width="match_parent"<br/>
 *        	&nbsp;android:layout_height="wrap_content"<br/>
 *        	&nbsp;app:design_width="100"<br/>
 *        	&nbsp;app:design_height="100"&gt;<br/>
 *
 *        	&nbsp;&lt;ImageView<br/>
 *         	&nbsp; 	&nbsp;android:layout_width="wrap_content"<br/>
 *         	&nbsp; 	&nbsp;android:layout_height="wrap_content"<br/>
 *         	&nbsp; 	&nbsp;app:layout_design_width="50"<br/>
 *         	&nbsp; 	&nbsp;app:layout_design_height="50"<br/>
 *         	&nbsp; 	&nbsp;android:background="#ff556678"/&gt;<br/>
 *
 *        	&nbsp;&lt;ImageView<br/>
 *         	&nbsp; 	&nbsp;android:layout_width="wrap_content"<br/>
 *         	&nbsp; 	&nbsp;android:layout_height="wrap_content"<br/>
 *         	&nbsp; 	&nbsp;app:layout_design_x="30"<br/>
 *         	&nbsp; 	&nbsp;app:layout_design_y="30"<br/>
 *         	&nbsp; 	&nbsp;app:layout_zDepth="15"<br/>
 *         	&nbsp; 	&nbsp;app:layout_design_width="50"<br/>
 *         	&nbsp; 	&nbsp;app:layout_design_height="50"<br/>
 *         	&nbsp; 	&nbsp;android:background="#ff776678"/&gt;<br/>
 *       	&nbsp;&lt;/org.kitpies.canvaslayout.widget.CanvasLayout&gt;<br/>
 * </code>
 *
 * The layout_zDepth accept a float value to specify the widget position in z-axis. A widget with a larger<br/>
 * layout_zDepth value will be layout more front than another.<br/>
 */
public class CanvasLayout extends ViewGroup implements Comparator<View>{

    private TreeSet<View> mDrawList = new TreeSet<View>(this);

    private int mDesignWidth = 0;
    private int mDesignHeight = 0;

    public CanvasLayout(Context context) {
        super(context);
    }

    public CanvasLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.CanvasLayout);
        mDesignWidth = a.getInt(R.styleable.CanvasLayout_design_width,0);
        mDesignHeight = a.getInt(R.styleable.CanvasLayout_design_height,0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int pWidth = MeasureSpec.getSize(widthMeasureSpec);
        int pHeight = MeasureSpec.getSize(heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int tPWidth = 0, tPHeight = 0;
        float stretchXS;
        float stretchYS;
        float virtualXS, virtualYS;
        int virtualXPadding, virtualYPadding;
        //To handle different measure type.
        if(wMode==MeasureSpec.EXACTLY){
            if(hMode==MeasureSpec.EXACTLY){
                stretchXS = mDesignWidth!=0?pWidth/(float)mDesignWidth:0;
                stretchYS = mDesignHeight!=0?pHeight/(float)mDesignHeight:0;
                virtualXS = virtualYS = Math.min(stretchXS,stretchYS);
                virtualXPadding = (int) ((pWidth-virtualXS*mDesignWidth)/2);
                virtualYPadding = (int) ((pHeight-virtualYS*mDesignHeight)/2);
                //verify scaling
                if(stretchXS==0&&stretchYS!=0)stretchYS=0;
                if(stretchXS!=0&&stretchYS==0)stretchXS=0;
                //measure every child
                measureEveryChild(stretchXS,stretchYS,virtualXS,virtualYS,virtualXPadding,virtualYPadding);
                tPWidth = pWidth;
                tPHeight = pHeight;
            }else if(hMode==MeasureSpec.AT_MOST){
                stretchXS = mDesignWidth!=0?pWidth/(float)mDesignWidth:0;
                int tempH = (mDesignHeight!=0&&mDesignWidth!=0)?(int)(pWidth/(float)mDesignWidth*mDesignHeight):0;
                stretchYS = mDesignHeight!=0?Math.min(tempH,pHeight)/(float)mDesignHeight:0;
                virtualXS = virtualYS = Math.min(stretchXS,stretchYS);
                virtualXPadding = (int) ((pWidth-virtualXS*mDesignWidth)/2);
                virtualYPadding = tempH!=0?(int) ((Math.min(tempH,pHeight)-virtualYS*mDesignHeight)/2):0;
                //verify scaling
                if(stretchXS==0&&stretchYS!=0)stretchYS=0;
                if(stretchXS!=0&&stretchYS==0)stretchXS=0;
                //measure every child
                int[] tSize = measureEveryChild(stretchXS,stretchYS,virtualXS,virtualYS,virtualXPadding,virtualYPadding);
                tPWidth = pWidth;
                tPHeight =Math.min(pHeight, tempH!=0?tempH:tSize[1]);
            }else if(hMode==MeasureSpec.UNSPECIFIED){
                stretchXS = mDesignWidth!=0?pWidth/(float)mDesignWidth:0;
                stretchYS = stretchXS;
                virtualXS = virtualYS = stretchXS;
                virtualXPadding = (int) ((pWidth-virtualXS*mDesignWidth)/2);
                virtualYPadding = 0;
                //measure every child
                int[] tSize = measureEveryChild(stretchXS,stretchYS,virtualXS,virtualYS,virtualXPadding,virtualYPadding);
                tPWidth = pWidth;
                int tempH = (mDesignHeight!=0&&mDesignWidth!=0)?(int)(tPWidth/(float)mDesignWidth*mDesignHeight):0;
                tPHeight =tempH!=0?tempH:tSize[1];
            }
        }else if(wMode==MeasureSpec.AT_MOST){
            if(hMode==MeasureSpec.EXACTLY){
                stretchYS = mDesignHeight!=0?pHeight/(float)mDesignHeight:0;
                int tempW = (mDesignHeight!=0&&mDesignWidth!=0)?(int)(pHeight/(float)mDesignHeight*mDesignWidth):0;
                stretchXS = mDesignWidth!=0?Math.min(tempW,pWidth)/(float)mDesignWidth:0;
                virtualXS = virtualYS = Math.min(stretchXS,stretchYS);
                virtualXPadding = tempW!=0?(int) ((Math.min(tempW,pWidth)-virtualXS*mDesignWidth)/2):0;
                virtualYPadding = (int) ((pHeight-virtualYS*mDesignHeight)/2);
                //verify scaling
                if(stretchXS==0&&stretchYS!=0)stretchYS=0;
                if(stretchXS!=0&&stretchYS==0)stretchXS=0;
                //measure every child
                int[] tSize = measureEveryChild(stretchXS,stretchYS,virtualXS,virtualYS,virtualXPadding,virtualYPadding);
                tPHeight = pHeight;
                tPWidth = Math.min(pWidth, tempW!=0?tempW:tSize[0]);
            }else if(hMode==MeasureSpec.AT_MOST){
                stretchXS = mDesignWidth!=0?Math.min(pWidth,mDesignWidth)/(float)mDesignWidth:0;
                stretchYS = mDesignHeight!=0?Math.min(pHeight,mDesignHeight)/(float)mDesignHeight:0;
                virtualXS = virtualYS = Math.min(stretchXS,stretchYS);
                virtualXPadding = (int) ((Math.min(pWidth,mDesignWidth)-virtualXS*mDesignWidth)/2);
                virtualYPadding = (int) ((Math.min(pHeight,mDesignHeight)-virtualYS*mDesignHeight)/2);
                //verify scaling
                if(stretchXS==0&&stretchYS!=0)stretchYS=0;
                if(stretchXS!=0&&stretchYS==0)stretchXS=0;
                //measure every child
                int[] tSize = measureEveryChild(stretchXS,stretchYS,virtualXS,virtualYS,virtualXPadding,virtualYPadding);
                tPWidth = Math.min(pWidth,mDesignWidth);
                tPHeight = Math.min(pHeight,mDesignHeight);
            }else if(hMode==MeasureSpec.UNSPECIFIED){
                stretchXS = mDesignWidth!=0?Math.min(pWidth,mDesignWidth)/(float)mDesignWidth:0;
                int tempH = (mDesignHeight!=0&&mDesignWidth!=0)?(int)(Math.min(pWidth,mDesignWidth)/(float)mDesignWidth*mDesignHeight):0;
                stretchYS = stretchXS;
                virtualXS = virtualYS = stretchXS;
                virtualXPadding = 0;
                virtualYPadding = 0;
                //measure every child
                int[] tSize = measureEveryChild(stretchXS,stretchYS,virtualXS,virtualYS,virtualXPadding,virtualYPadding);
                tPWidth = Math.min(pWidth,mDesignWidth);
                tPHeight = tempH;
            }
        }else if(wMode==MeasureSpec.UNSPECIFIED){
            if(hMode==MeasureSpec.EXACTLY){
                stretchYS = mDesignHeight!=0?pHeight/(float)mDesignHeight:0;
                stretchXS = stretchYS;
                virtualXS = virtualYS = stretchYS;
                virtualXPadding = 0;
                virtualYPadding = (int) ((pHeight-virtualYS*mDesignHeight)/2);
                //measure every child
                int[] tSize = measureEveryChild(stretchXS,stretchYS,virtualXS,virtualYS,virtualXPadding,virtualYPadding);
                tPHeight = pHeight;
                int tempW = (mDesignHeight!=0&&mDesignWidth!=0)?(int)(tPHeight/(float)mDesignHeight*mDesignWidth):0;
                tPWidth = Math.min(pWidth, tempW!=0?tempW:tSize[0]);
            }else if(hMode==MeasureSpec.AT_MOST){
                stretchYS = mDesignHeight!=0?Math.min(pHeight,mDesignHeight)/(float)mDesignHeight:0;
                int tempW = (mDesignHeight!=0&&mDesignWidth!=0)?(int)(pHeight/(float)mDesignHeight*mDesignWidth):0;
                stretchXS = stretchYS;
                virtualXS = virtualYS = stretchYS;
                virtualXPadding = 0;
                virtualYPadding = 0;
                //measure every child
                int[] tSize = measureEveryChild(stretchXS,stretchYS,virtualXS,virtualYS,virtualXPadding,virtualYPadding);
                tPWidth = tempW;
                tPHeight = Math.min(pHeight, mDesignHeight);
            }else if(hMode==MeasureSpec.UNSPECIFIED){
                //measure every child
                int[] tSize = measureEveryChild(1,1,1,1,0,0);
                tPWidth = mDesignWidth;
                tPHeight = mDesignHeight;
            }
        }

        int measureWidth = MeasureSpec.makeMeasureSpec(tPWidth,MeasureSpec.EXACTLY);
        int measureHeight = MeasureSpec.makeMeasureSpec(tPHeight,MeasureSpec.EXACTLY);
        setMeasuredDimension(measureWidth,measureHeight);
    }

    private int[] measureEveryChild(float stretchXS, float stretchYS, float virtualXS, float virtualYS, int virtualXPadding, int virtualYPadding){
        int[] tSize = new int[2];
        int l=0,r=0,t=0,b=0;
        int count = getChildCount();
        View view;
        LayoutParams lP;
        int wMSP, hMSP;
        for(int i=0;i<count;i++){
            view = getChildAt(i);
            Object obj = view.getLayoutParams();
            //If child didn't has a canvas layout params, ignore it.
            if(obj instanceof LayoutParams){
                lP = (LayoutParams) obj;
            }else {
                continue;
            }
            lP.width = lP.getWidth(stretchXS,virtualXS);
            lP.height = lP.getHeight(stretchYS,virtualYS);
            wMSP = MeasureSpec.makeMeasureSpec(lP.width,MeasureSpec.EXACTLY);
            hMSP = MeasureSpec.makeMeasureSpec(lP.height,MeasureSpec.EXACTLY);
            //measure child.
            view.measure(wMSP,hMSP);
            lP.mLeft = lP.getX(stretchXS,virtualXS,virtualXPadding);
            lP.mTop = lP.getY(stretchYS,virtualYS,virtualYPadding);
            lP.mRight = lP.mLeft + lP.width;
            lP.mBottom = lP.mTop + lP.height;
            //record the right and bottom position.
            r = Math.max(r, lP.mRight);
            b = Math.max(b, lP.mBottom);
        }
        tSize[0] = r - l;
        tSize[1] = b - t;
        return tSize;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        View view;
        mDrawList.clear();
        for(int i=0;i<count;i++){
            view = getChildAt(i);
            Object obj = view.getLayoutParams();
            //If child didn't has a canvas layout params, ignore it.
            if(obj instanceof LayoutParams){
                mDrawList.add(view);
            }else {
                continue;
            }
        }
        LayoutParams lP;
        for(View item:mDrawList){
            lP = (LayoutParams) item.getLayoutParams();
            item.layout(lP.mLeft, lP.mTop, lP.mRight, lP.mBottom);
            item.bringToFront();
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    public int compare(View lhs, View rhs) {
        LayoutParams lLp = (LayoutParams) lhs.getLayoutParams();
        LayoutParams rLp = (LayoutParams) rhs.getLayoutParams();
        return lLp.getZDepth()>rLp.getZDepth()?1:lLp.getZDepth()<rLp.getZDepth()?-1:0;
    }

    /**
     * Per-child layout information associated with CanvasLayout.
     * @attr ref R.styleable#CanvasLayout_Layout_layout_zDepth
     * @attr ref R.styleable#CanvasLayout_Layout_layout_design_width
     * @attr ref R.styleable#CanvasLayout_Layout_layout_design_height
     * @attr ref R.styleable#CanvasLayout_Layout_layout_design_width_scaling_mode
     * @attr ref R.styleable#CanvasLayout_Layout_layout_design_height_scaling_mode
     * @attr ref R.styleable#CanvasLayout_Layout_layout_design_x
     * @attr ref R.styleable#CanvasLayout_Layout_layout_design_y
     * @attr ref R.styleable#CanvasLayout_Layout_layout_design_x_scaling_mode
     * @attr ref R.styleable#CanvasLayout_Layout_layout_design_y_scaling_mode
     */
    public static class LayoutParams extends ViewGroup.LayoutParams{

        /** Design in virtual window. */
        public static final int VIRTUAL_DESIGN_MODE = 0;
        /** Design in stretch window. */
        public static final int STRETCH_DESIGN_MODE = 1;

        private float mZDepth = 0.0f;

        private int mDesignWidth = 0;

        private int mDesignHeight = 0;

        private int mDesignX = 0;

        private int mDesignY = 0;

        private int mWidthScalingMode = VIRTUAL_DESIGN_MODE;

        private int mHeightScalingMode = VIRTUAL_DESIGN_MODE;

        private int mXScalingMode = VIRTUAL_DESIGN_MODE;

        private int mYScalingMode = VIRTUAL_DESIGN_MODE;

        private int mLeft, mTop, mRight, mBottom;

        public LayoutParams() {
            super(0,0);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs,R.styleable.CanvasLayout_Layout);
            mZDepth = a.getFloat(R.styleable.CanvasLayout_Layout_layout_zDepth,0.0f);
            mDesignWidth = a.getInt(R.styleable.CanvasLayout_Layout_layout_design_width,0);
            mDesignHeight = a.getInt(R.styleable.CanvasLayout_Layout_layout_design_height,0);
            mDesignX = a.getInt(R.styleable.CanvasLayout_Layout_layout_design_x,0);
            mDesignY = a.getInt(R.styleable.CanvasLayout_Layout_layout_design_y,0);
            mWidthScalingMode = a.getInt(R.styleable.CanvasLayout_Layout_layout_design_width_scaling_mode,VIRTUAL_DESIGN_MODE);
            mHeightScalingMode = a.getInt(R.styleable.CanvasLayout_Layout_layout_design_height_scaling_mode,VIRTUAL_DESIGN_MODE);
            mXScalingMode = a.getInt(R.styleable.CanvasLayout_Layout_layout_design_x_scaling_mode,VIRTUAL_DESIGN_MODE);
            mYScalingMode = a.getInt(R.styleable.CanvasLayout_Layout_layout_design_y_scaling_mode,VIRTUAL_DESIGN_MODE);
            a.recycle();
        }

        public int getDesignHeight() {
            return mDesignHeight;
        }

        public void setDesignHeight(int designHeight) {
            mDesignHeight = designHeight;
        }

        public int getDesignWidth() {
            return mDesignWidth;
        }

        public void setDesignWidth(int designWidth) {
            mDesignWidth = designWidth;
        }

        public int getDesignX() {
            return mDesignX;
        }

        public void setDesignX(int designX) {
            mDesignX = designX;
        }

        public int getDesignY() {
            return mDesignY;
        }

        public void setDesignY(int designY) {
            mDesignY = designY;
        }

        public int getHeightScalingMode() {
            return mHeightScalingMode;
        }

        public void setHeightScalingMode(int heightScalingMode) {
            mHeightScalingMode = heightScalingMode;
        }

        public int getWidthScalingMode() {
            return mWidthScalingMode;
        }

        public void setWidthScalingMode(int widthScalingMode) {
            mWidthScalingMode = widthScalingMode;
        }

        public int getXScalingMode() {
            return mXScalingMode;
        }

        public void setXScalingMode(int xScalingMode) {
            mXScalingMode = xScalingMode;
        }

        public int getYScalingMode() {
            return mYScalingMode;
        }

        public void setYScalingMode(int yScalingMode) {
            mYScalingMode = yScalingMode;
        }

        public float getZDepth() {
            return mZDepth;
        }

        public void setZDepth(float zDepth) {
            mZDepth = zDepth;
        }

        public int getWidth(float stretchXS, float virtualXS){
            switch (mWidthScalingMode){
                case VIRTUAL_DESIGN_MODE:
                    return (int) (virtualXS*mDesignWidth);
                case STRETCH_DESIGN_MODE:
                    return (int) (stretchXS*mDesignWidth);
                default:
                    return (int) (virtualXS*mDesignWidth);
            }
        }

        public int getHeight(float stretchYS, float virtualYS){
            switch (mHeightScalingMode){
                case VIRTUAL_DESIGN_MODE:
                    return (int) (virtualYS*mDesignHeight);
                case STRETCH_DESIGN_MODE:
                    return (int) (stretchYS*mDesignHeight);
                default:
                    return (int) (virtualYS*mDesignHeight);
            }
        }

        public int getX(float stretchXS, float virtualXS, int virtualXPadding){
            switch (mXScalingMode){
                case VIRTUAL_DESIGN_MODE:
                    return (int) (virtualXS*mDesignX+virtualXPadding);
                case STRETCH_DESIGN_MODE:
                    return (int) (stretchXS*mDesignX);
                default:
                    return (int) (virtualXS*mDesignX+virtualXPadding);
            }
        }

        public int getY(float stretchYS, float virtualYS, int virtualYPadding){
            switch (mYScalingMode){
                case VIRTUAL_DESIGN_MODE:
                    return (int) (virtualYS*mDesignY+virtualYPadding);
                case STRETCH_DESIGN_MODE:
                    return (int) (stretchYS*mDesignY);
                default:
                    return (int) (virtualYS*mDesignY+virtualYPadding);
            }
        }
    }
}
