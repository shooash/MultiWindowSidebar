package com.zst.app.multiwindowsidebar.sidebar;

import com.zst.app.multiwindowsidebar.IntentUtil;

import android.view.Gravity;
import android.view.ViewGroup;
import com.zst.app.multiwindowsidebar.*;

public class WindowPositionOutline {
	
	public static int[] getOutlineParams(int screen_width, int screen_height, int icon_position) {
		int[] outline_param = new int[3];
		switch (icon_position) {
		case IntentUtil.SIDE_LEFT:
			outline_param[0] = (screen_width / 2);
			outline_param[1] = ViewGroup.LayoutParams.MATCH_PARENT;
			outline_param[2] = Gravity.TOP | Gravity.LEFT;
			break;
		case IntentUtil.SIDE_RIGHT:
			outline_param[0] = (screen_width / 2);
			outline_param[1] = ViewGroup.LayoutParams.MATCH_PARENT;
			outline_param[2] = Gravity.TOP | Gravity.RIGHT;
			break;
		case IntentUtil.SIDE_TOP:
			outline_param[0] = ViewGroup.LayoutParams.MATCH_PARENT;
			outline_param[1] = (screen_height / 2);
			outline_param[2] = Gravity.TOP | Gravity.LEFT;
			break;
		case IntentUtil.SIDE_BOTTOM:
			outline_param[0] = ViewGroup.LayoutParams.MATCH_PARENT;
			outline_param[1] = (screen_height / 2);
			outline_param[2] = Gravity.BOTTOM | Gravity.LEFT;
			break;
		case IntentUtil.SIDE_TOPLEFT:
			outline_param[0] = (screen_width / 2);
			outline_param[1] = (screen_height / 2);
			outline_param[2] = Gravity.TOP | Gravity.LEFT;
			break;
		case IntentUtil.SIDE_TOPRIGHT:
				outline_param[0] = (screen_width / 2);
				outline_param[1] = (screen_height / 2);
				outline_param[2] = Gravity.TOP | Gravity.RIGHT;
				break;
		case IntentUtil.SIDE_BOTTOMLEFT:
				outline_param[0] = (screen_width / 2);
				outline_param[1] = (screen_height / 2);
				outline_param[2] = Gravity.BOTTOM | Gravity.LEFT;
				break;
		case IntentUtil.SIDE_BOTTOMRIGHT:
				outline_param[0] = (screen_width / 2);
				outline_param[1] = (screen_height / 2);
				outline_param[2] = Gravity.BOTTOM | Gravity.RIGHT;
				break;
		case IntentUtil.SIDE_PA_HALO:
			final boolean landscape = screen_width > screen_height;
			if (landscape) {
				outline_param[0] = (int) (screen_width * 0.7f);
				outline_param[1] = (int) (screen_height * 0.8f);
			} else {
				outline_param[0] = (int) (screen_width * 0.9f);
				outline_param[1] = (int) (screen_height * 0.7f);
			}
			outline_param[2] = Gravity.CENTER;
			break;
		case IntentUtil.SIDE_FULLSCREEN:
			outline_param[0] = ViewGroup.LayoutParams.MATCH_PARENT;
			outline_param[1] = ViewGroup.LayoutParams.MATCH_PARENT;
			outline_param[2] = Gravity.TOP | Gravity.LEFT;
			break;
		case IntentUtil.SIDE_NONE:
			outline_param[0] = -1000;
			outline_param[1] = -1000;
			outline_param[2] = -1000;
			break;
		}
		return outline_param;
	}
	
	public static int getPositionOfTouch(float x, float y, int screen_width, int screen_height) {
		boolean landscape = screen_width > screen_height;
		int snapGravity = 0;
		//int mRange = 100;
		if(x < screen_width/3) snapGravity = snapGravity | Gravity.LEFT;
		if(x > (screen_width*2/3)) snapGravity = snapGravity | Gravity.RIGHT;
		if(y < screen_height/3) snapGravity = snapGravity | Gravity.TOP;
		if(y > (screen_height*2/3)) snapGravity = snapGravity | Gravity.BOTTOM;
		
		switch (IntentUtil.sLaunchModeDrag) {
		case IntentUtil.DragMode.XMULTI_WINDOW:
				if (landscape) {
					if (x < (screen_width / 2)) {
						return IntentUtil.SIDE_LEFT;
					} else if (x > (screen_width / 2)) {
						return IntentUtil.SIDE_RIGHT;
					}
				} else {
					if (y < (screen_height / 2)) {
						return IntentUtil.SIDE_TOP;
					} else if (y > (screen_height / 2)) {
						return IntentUtil.SIDE_BOTTOM;
					}
				}
				break;
		case IntentUtil.DragMode.XHFW_PORTRAIT:
		case IntentUtil.DragMode.XHFW_LANDSCAPE:
				return Compatibility.snapGravityToSide(snapGravity);
		case IntentUtil.DragMode.PA_HALO:
			return IntentUtil.SIDE_PA_HALO;
		case IntentUtil.DragMode.NONE:
		default:
			break;
		}
		return IntentUtil.SIDE_FULLSCREEN;
	}
}
