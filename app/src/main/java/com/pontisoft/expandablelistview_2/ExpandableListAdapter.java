package com.pontisoft.expandablelistview_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;

public class ExpandableListAdapter extends SimpleExpandableListAdapter {

    public enum SplitMode { SPL_NONE, SPL_ADD, SPL_DEC };

    private ArrayList<HashMap<String, Object>> currentmaplist = null;
    private List<List<HashMap<String, Object>>> childlist = null;
    private List<HashMap<String, Object>> childobjlist = null;
    private Activity actctx;
    private ExpandableListView mListView;
    private boolean mItemPressed;
    private boolean mSwiping;

    private VelocityTracker mVelocityTracker = null;

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;

    private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET =
            {android.R.attr.state_expanded};
    private static final int[][] GROUP_STATE_SETS = {
            EMPTY_STATE_SET, // 0
            GROUP_EXPANDED_STATE_SET // 1
    };
    public ExpandableListAdapter(Context context,
                                            List<? extends Map<String, ?>> groupData, int expandedGroupLayout,
                                            int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
                                            List<? extends List<? extends Map<String, ?>>> childData,
                                            int childLayout, int lastChildLayout, String[] childFrom,
                                            int[] childTo) {
        super(context, groupData, expandedGroupLayout, collapsedGroupLayout, groupFrom,
                groupTo, childData, childLayout, lastChildLayout, childFrom, childTo);
        setEnviron(context, groupData, childData);
    }

    public ExpandableListAdapter(Context context,
                                            List<? extends Map<String, ?>> groupData, int expandedGroupLayout,
                                            int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
                                            List<? extends List<? extends Map<String, ?>>> childData,
                                            int childLayout, String[] childFrom, int[] childTo) {
        super(context, groupData, expandedGroupLayout, collapsedGroupLayout,
                groupFrom, groupTo, childData, childLayout, childFrom, childTo);
        setEnviron(context, groupData, childData);
    }

    public ExpandableListAdapter(Context context,
                                            List<? extends Map<String, ?>> groupData, int groupLayout,
                                            String[] groupFrom, int[] groupTo,
                                            List<? extends List<? extends Map<String, ?>>> childData,
                                            int childLayout, String[] childFrom, int[] childTo) {
        super(context, groupData, groupLayout, groupFrom, groupTo, childData,
                childLayout, childFrom, childTo);
        setEnviron(context, groupData, childData);
    }
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        float mDownX;
        private int mSwipeSlop = -1;

        void finalizeAnimation (final View v, SplitMode split, int splcount) {
            v.setAlpha(1);
            v.setTranslationX(0);
            if (split != SplitMode.SPL_NONE) {
                if (split == SplitMode.SPL_DEC) {
                    execSplit(mListView, v, 0-splcount);
                }
                else {
                    execSplit(mListView, v, splcount);
                }
            } else {
                mSwiping = false;
            }

        };

        @SuppressLint("NewApi")
        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            mListView = (ExpandableListView) actctx.findViewById(R.id.lvExp);
            final int lSplitCount = 1;

            if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(actctx).
                        getScaledTouchSlop();
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mItemPressed) {
                        // Multi-item swipes not handled
                        return false;
                    }
                    mItemPressed = true;
                    mDownX = event.getX();
                    if(mVelocityTracker == null) {
                        // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                        mVelocityTracker = VelocityTracker.obtain();
                    }
                    else {
                        // Reset the velocity tracker back to its initial state.
                        mVelocityTracker.clear();
                    }
                    // Add a user's movement to the tracker.
                    mVelocityTracker.addMovement(event);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1);
                    v.setTranslationX(0);
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    mItemPressed = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    mVelocityTracker.addMovement(event);
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    if (!mSwiping) {
                        if (deltaXAbs > mSwipeSlop) {
                            mSwiping = true;
                            mListView.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    if (mSwiping) {
                        v.setTranslationX((x - mDownX));
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
                    }
                }
                break;
                case MotionEvent.ACTION_UP:
                {
                    // User let go - figure out whether to animate the view out, or back into place
                    if (mSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - mDownX;
                        float deltaXAbs = Math.abs(deltaX);
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        final SplitMode splitmode;
                        if (deltaXAbs > (v.getWidth() / 6)) {
                            // Greater than a quarter of the width - animate it out
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0;
                            if (deltaX > 0) {
                                splitmode = SplitMode.SPL_ADD;
                            }
                            else {
                                splitmode = SplitMode.SPL_DEC;
                            }
                        } else {
                            // Not far enough - animate it back
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0;
                            endAlpha = 1;
                            splitmode = SplitMode.SPL_NONE;
                        }
                        mVelocityTracker.computeCurrentVelocity(1000);
                        long duration =  (int) ((1 - fractionCovered) * Math.min (500,(v.getWidth()*1000/Math.abs(mVelocityTracker.getXVelocity()))));
                        mListView.setEnabled(false);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            v.animate().setDuration(duration).alpha(endAlpha).translationX(endX).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    finalizeAnimation(v, splitmode, lSplitCount);
                                }
                            });
                        } else {
                            v.animate().setDuration(duration).alpha(endAlpha).translationX(endX).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    finalizeAnimation(v, splitmode, lSplitCount);
                                }
                            });
                        }
                    }
                    else {
                        mItemPressed = false;
                        if (mVelocityTracker != null) {
                            mVelocityTracker.recycle();
                            mVelocityTracker = null;
                        }
                        return false; // to allow for click events
                    }
                }
                mItemPressed = false;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
                default:
                    return false;
            }
            return true;
        }
    };

    void setEnviron (Context context, List<? extends Map<String, ?>> groupData, List<? extends List<? extends Map<String, ?>>> childData) {
        actctx = (Activity) context;
        currentmaplist = (ArrayList<HashMap<String, Object>>) groupData;
        childlist = (ArrayList<List<HashMap<String, Object>>>) childData;
    }

    @Override
    public View getGroupView (int groupPosition,
                              boolean isExpanded,
                              View convertView,
                              ViewGroup parent) {
        View v = super.getGroupView(groupPosition, isExpanded, convertView, parent);
        HashMap<String, Object> omap = currentmaplist.get(groupPosition);
        v.setTag(omap);
        v.setOnTouchListener(mTouchListener);
        View ind = v.findViewById( R.id.explist_indicator);
        if( ind != null ) {
            ImageView indicator = (ImageView) ind;
            if (getChildrenCount(groupPosition) == 0) {
                indicator.setVisibility(View.INVISIBLE);
            } else {
                indicator.setVisibility(View.VISIBLE);
                int stateSetIndex = (isExpanded ? 1 : 0);
                Drawable drawable = indicator.getDrawable();
                drawable.setState(GROUP_STATE_SETS[stateSetIndex]);
            }
        }
        return v;
    }

    private void execSplit(ExpandableListView lv, View v, int splcount) {
        HashMap<String, Object> omap = (HashMap<String, Object>)v.getTag();

        if (((splcount < 0) && (Double.parseDouble((String)omap.get("splitcount")) >= Math.abs(splcount))) ||
                ((splcount > 0) && ((Double.parseDouble((String) omap.get("count")) - splcount) >= 0)))
        {
            omap.put("splitcount", String.format(Locale.US, "%d", Integer.parseInt((String) omap.get("splitcount")) + splcount));
            omap.put("count", String.format(Locale.US, "%d", Integer.parseInt((String) omap.get("count")) - splcount));
            childobjlist = null;
            for (int j = 0; j < currentmaplist.size(); j++) {
                if (((String)(currentmaplist.get(j).get("isorder"))).equalsIgnoreCase("true")) {
                    if (((String)(currentmaplist.get(j).get("id"))).equalsIgnoreCase((String)omap.get("id"))) {
                        childobjlist = childlist.get(j);
                        if (childobjlist != null) {
                            if (childobjlist.size() > 0)
                            {
                                for (int i = 0; i < childobjlist.size(); i++) {
                                    HashMap<String, Object> cmap = childobjlist.get(i);
                                    if (splcount != 0) {
                                        cmap.put("splitcount", String.format(Locale.US, "%d", Integer.parseInt((String) cmap.get("splitcount")) + splcount));
                                        cmap.put("count", String.format(Locale.US, "%d", Integer.parseInt((String) cmap.get("count")) - splcount));
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
//            recalcSum();
            notifyDataSetChanged();
        }
    }



}