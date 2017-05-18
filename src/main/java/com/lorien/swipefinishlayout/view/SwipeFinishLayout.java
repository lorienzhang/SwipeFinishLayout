package com.lorien.swipefinishlayout.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.lorien.swipefinishlayout.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by lorienzhang on 2017/5/17.
 */

public class SwipeFinishLayout extends FrameLayout {

    public static final String TAG = SwipeFinishLayout.class.getSimpleName();
    /**
     * 滑动方向：水平
     */
    public static final int ORIENTATION_HORIZONTAL = 1;
    /**
     * 滑动方向：垂直
     */
    public static final int ORIENTATION_VERTICAL = 2;
    /**
     * 滑动方向：初始值
     */
    public static final int ORIENTATION_NONE = 0;

    /**
     * 记录手指滑动方向
     */
    private int mScrollOrientation = ORIENTATION_NONE;
    /**
     * 最小滑动距离
     */
    private int mTouchSlop;
    /**
     * 按下点的X坐标
     */
    private int mDownX;
    /**
     * 记录滑动过程中的X坐标
     */
    private int mTempX;
    /**
     * 记录滑动过程中的Y坐标
     */
    private int mTempY;
    /**
     * 按下点的Y坐标
     */
    private int mDownY;
    /**
     * 是否处于滑动状态
     */
    private boolean isSliding;
    /**
     * 判断是否处于scrolling状态
     */
    private boolean isScrolling = false;
    /**
     * 判断是否finish activity
     */
    private boolean isFinish = false;
    /**
     * view的宽度
     */
    private int mViewWidth;
    /**
     * view的高度
     */
    private int mViewHeight;
    /**
     * 滑动处理器
     */
    private Scroller mScroller;
    /**
     * 速度追踪，判断fling
     */
    private VelocityTracker mVelocityTracker;
    /**
     * fling速度的最小值
     */
    private int mMinFlingVelocity;
    /**
     * 关联的activity
     */
    private Activity mActivity;
    /**
     * content view
     */
    private View mContentView;
    /**
     * View左侧的阴影
     */
    private Drawable mShadowDrawable;
    /**
     * 半透明的阴影, 透明度跟随手指的滑动而改变
     */
    private int mBackgroundColor = 0xaa000000;
    /**
     * 比例值，用于计算alpha值
     */
    private float mRatio = 1.0f;
    /**
     * 记录子view中所有的AbsListView(ListView, GridView)
     */
    private List<AbsListView> mAbsListViews = new LinkedList<>();
    /**
     * 记录子view中所有的AbsListView(ListView, GridView)
     */
    private List<ScrollView> mScrollViews = new LinkedList<>();

    public SwipeFinishLayout(Context context) {
        this(context, null);
    }

    public SwipeFinishLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeFinishLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 初始化最小滑动距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMinFlingVelocity = 6 * ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        Log.d(TAG, "mMinFlingVelocity: " + mMinFlingVelocity);
        // 初始化滑动处理器
        mScroller = new Scroller(context);
        mShadowDrawable = getResources().getDrawable(R.drawable.shadow);
    }

    /**
     * 将SwipeFinishLayout关联到指定activity中
     * @param activity
     */
    public void attachToActivity(Activity activity) {
        mActivity = activity;
        TypedArray ta = activity.getTheme().obtainStyledAttributes(
                new int[] {android.R.attr.windowBackground});
        int background = ta.getResourceId(0, 0);
        ta.recycle();

        // 设置window样式，FEATURE_NO_TITLE
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        // 系统内置，/platforms/android-25/data/res/layout/screen_custom_title.xml
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);
        decorChild.setBackgroundResource(background);
        decorView.removeView(decorChild);
        addView(decorChild);
        // 设置content view
        mContentView = (View) decorChild.getParent();
        // 将SwipeFinishLayout添加到decorView中
        decorView.addView(this);
    }

    /**
     * 确定content view的宽度
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 递归遍历子view，记录所有的AbsListView, ScrollView
        getAllCanScrollView(mAbsListViews, mScrollViews, this);

        setBackgroundColor(mBackgroundColor);
    }

    /**
     * 递归遍历子view，记录所有的AbsListView, ScrollView
     *
     * @param absListViews
     * @param parent
     */
    private void getAllCanScrollView(List<AbsListView> absListViews,
                                        List<ScrollView> scrollViews,
                                        ViewGroup parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            if (child instanceof AbsListView) {
                absListViews.add((AbsListView) child);
            } else if (child instanceof ScrollView) {
                scrollViews.add((ScrollView) child);
            } else {
                if (child instanceof ViewGroup) {
                    // 递归遍历子View
                    getAllCanScrollView(absListViews, scrollViews, (ViewGroup) child);
                }
            }
        }
    }

    /**
     * 返回我们touch的AbsListView
     * @param absListViews
     * @param ev
     * @return
     */
    private AbsListView getTouchAbsListView(List<AbsListView> absListViews, MotionEvent ev) {
        if (absListViews == null || absListViews.size() == 0) {
            return null;
        }
        Rect rect = new Rect();
        for (AbsListView absListView : absListViews) {
            absListView.getHitRect(rect);
            if (rect.contains((int)ev.getX(), (int)ev.getY())) {
                return absListView;
            }
        }
        return null;
    }

    /**
     * 返回我们touch的ScrollView
     * @param scrollViews
     * @param ev
     * @return
     */
    private ScrollView getTouchScrollView(List<ScrollView> scrollViews, MotionEvent ev) {
        if (scrollViews == null || scrollViews.size() == 0) {
            return null;
        }
        Rect rect = new Rect();
        for (ScrollView sv : scrollViews) {
            sv.getHitRect(rect);
            if (rect.contains((int)ev.getX(), (int)ev.getY())) {
                return sv;
            }
        }
        return null;
    }

    /**
     * 给View的左侧加上阴影，阴影的宽度是 = (mViewWidth / 20)
     * @param canvas
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mShadowDrawable != null && mContentView != null) {
            int left = mContentView.getLeft()
                    - mViewWidth / 20;
            int right = left + mViewWidth / 20;
            int top = mContentView.getTop();
            int bottom = mContentView.getBottom();

            mShadowDrawable.setBounds(left, top, right, bottom);
            mShadowDrawable.draw(canvas);

            // 动态更新背景的alpha
            int alphaValue = (mBackgroundColor >> 24) & 0xFF;
            alphaValue *= mRatio;
            int backGroundColor = alphaValue << 24;
            setBackgroundColor(backGroundColor);
        }
    }

    /**
     * 事件拦截操作
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTempX = mDownX = (int) ev.getRawX();
                mTempY = mDownY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getRawX();
                int moveY = (int) ev.getRawY();
                // 横向滑动：满足此条件就对事件进行拦截
                // TODO: ViewPager
                if (moveX - mDownX > mTouchSlop
                        && Math.abs((int)ev.getRawY() - mDownY) < mTouchSlop) {
                    return true;
                }
                // 纵向滑动：满足此条件就对事件进行拦截
                if (moveY - mDownY > mTouchSlop
                        && Math.abs(moveX - mDownX) < mTouchSlop) {
                    // 1.处理AbsListView的冲突
                    AbsListView alv = getTouchAbsListView(mAbsListViews, ev);
                    Log.d(TAG, "AbsListView: " + alv);
                    if (alv != null) {
                        if (alv.canScrollVertically(-1)) {
                            return super.onInterceptTouchEvent(ev);
                        }
                    }
                    // 2.处理ScrollView的冲突
                    ScrollView sv = getTouchScrollView(mScrollViews, ev);
                    Log.d(TAG, "ScrollView: " + sv);
                    if (sv != null) {
                        if (sv.canScrollVertically(-1)) {
                            return super.onInterceptTouchEvent(ev);
                        }
                    }
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    /**
     * 滑动事件处理
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                startVelocityTracker(event);
                int moveY = (int) event.getRawY();
                int deltaY = mTempY - moveY;
                mTempY = moveY;
                int moveX = (int) event.getRawX();
                int deltaX = mTempX - moveX;
                mTempX = moveX;
                // 横向滑动
                if (moveX - mDownX > mTouchSlop
                        && Math.abs((int)event.getRawY() - mDownY) < mTouchSlop
                        && mScrollOrientation != ORIENTATION_VERTICAL) {
                    isSliding = true;
                    mScrollOrientation = ORIENTATION_HORIZONTAL;
                }
                // 纵向滑动
                if (moveY - mDownY > mTouchSlop
                        && Math.abs(moveX - mDownX) < mTouchSlop
                        && mScrollOrientation != ORIENTATION_HORIZONTAL) {
                    isSliding = true;
                    mScrollOrientation = ORIENTATION_VERTICAL;
                }
                if (isSliding) {
                    if (mScrollOrientation == ORIENTATION_HORIZONTAL) {
                        mContentView.scrollBy(deltaX, 0);
                        // 边界控制
                        if (mContentView.getScrollX() > 0) {
                            mContentView.scrollTo(0, 0);
                        }
                        // 计算Scroll的比例值
                        mRatio = 1- Math.abs(mContentView.getScrollX() * 1.0f / mViewWidth);
                    }
                    if (mScrollOrientation == ORIENTATION_VERTICAL) {
                        mContentView.scrollBy(0, deltaY);
                        if (mContentView.getScrollY() > 0) {
                            mContentView.scrollTo(0, 0);
                        }
                        // 计算Scroll的比例值
                        mRatio = 1- Math.abs(mContentView.getScrollY() * 1.0f / mViewHeight);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isSliding = false;
                if (!isScrolling) {
                    if (mScrollOrientation == ORIENTATION_HORIZONTAL) {
                        int scrollVelocityX = getScrollVelocityX();
                        Log.d(TAG, "scrollVelocityX: " + scrollVelocityX);
                        if (Math.abs(scrollVelocityX) <= mMinFlingVelocity) {
                            if (mContentView.getScrollX() < -mViewWidth / 2) {
                                scrollToRight();
                            } else {
                                scrollToOriginX();
                            }
                        } else if (scrollVelocityX > mMinFlingVelocity) {
                            scrollToRight();
                        } else if (scrollVelocityX < -mMinFlingVelocity) {
                            scrollToOriginX();
                        }
                    }
                    if (mScrollOrientation == ORIENTATION_VERTICAL) {
                        int scrollVelocityY = getScrollVelocityY();
                        Log.d(TAG, "scrollVelocityY: " + scrollVelocityY);
                        if (Math.abs(scrollVelocityY) <= mMinFlingVelocity) {
                            if (mContentView.getScrollY() < -mViewHeight / 6) {
                                scrollToBottom();
                            } else {
                                scrollToOriginY();
                            }
                        } else if (scrollVelocityY > mMinFlingVelocity) {
                            scrollToBottom();
                        } else if (scrollVelocityY < -mMinFlingVelocity) {
                            scrollToOriginY();
                        }
                    }
                }
                mScrollOrientation = ORIENTATION_NONE;
                break;
            case MotionEvent.ACTION_CANCEL:
                stopVelocityTracker();
        }
        return true;
    }

    /**
     * 平滑滚动View
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mContentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();

            if (mScroller.isFinished()) {
                isScrolling = false;
                if (isFinish) {
                    mActivity.finish();
                }
            }

            // 计算mRatio
            if (mContentView.getScrollX() != 0) {
                // 横向滑动
                mRatio = 1 - Math.abs(mContentView.getScrollX() * 1.0f / mViewWidth);
            } else {
                // 纵向滑动
                mRatio = 1- Math.abs(mContentView.getScrollY() * 1.0f / mViewHeight);
            }
        }
    }

    /**
     * view从bottom滑出，finish activity
     */
    public void finishActivityBottomOut() {
        scrollToBottom();
    }

    public boolean attachedActivityShouldFinish() {
        return mScroller.isFinished() & isFinish;
    }

    /**
     * View滑动到初始位置
     */
    private void scrollToOriginX() {
        isFinish = false;
        isScrolling = true;
        final int delta = mContentView.getScrollX();
        mScroller.startScroll(mContentView.getScrollX(), 0, -delta, 0, 600);
        postInvalidate();
    }

    /**
     * View滑出界面右部
     */
    private void scrollToRight() {
        isFinish = true;
        isScrolling = true;
        final int delta = mViewWidth + mContentView.getScrollX();
        mScroller.startScroll(mContentView.getScrollX(), 0, -delta, 0, 600);
        postInvalidate();
    }

    /**
     * View滑动到初始位置
     */
    private void scrollToOriginY() {
        isFinish = false;
        isScrolling = true;
        final int delta = mContentView.getScrollY();
        mScroller.startScroll(0, mContentView.getScrollY(), 0, -delta, 600);
        postInvalidate();
    }

    /**
     * View滑出界面底部
     */
    private void scrollToBottom() {
        isFinish = true;
        isScrolling = true;
        final int delta = mViewHeight + mContentView.getScrollY();
        mScroller.startScroll(0, mContentView.getScrollY(), 0, -delta, 600);
        postInvalidate();
    }

    /**
     * 开始追踪速度
     * @param event
     */
    private void startVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void stopVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 获取追踪到的X方向的速度
     * @return
     */
    private int getScrollVelocityX() {
        if (mVelocityTracker != null) {
            // 设置单位, 1000表示1s内移动的像素值
            mVelocityTracker.computeCurrentVelocity(1000);
            int velocityX = (int) mVelocityTracker.getXVelocity();
            return velocityX;
        } else {
            return 0;
        }
    }

    /**
     * 获取追踪到的Y方向的速度
     * @return
     */
    private int getScrollVelocityY() {
        if (mVelocityTracker != null) {
            // 设置单位, 1000表示1s内移动的像素值
            mVelocityTracker.computeCurrentVelocity(1000);
            int velocityY = (int) mVelocityTracker.getYVelocity();
            return velocityY;
        } else {
            return 0;
        }
    }


}
