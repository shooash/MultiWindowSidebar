package com.zst.app.multiwindowsidebar;

import android.view.*;
import java.util.*;

public class Compatibility
{
	public class AeroSnap{
		public final static int SNAP_NONE = 0;
		public final static int SNAP_LEFT = 1;
		public final static int SNAP_TOP = 2;
		public final static int SNAP_RIGHT = 3;
		public final static int SNAP_BOTTOM = 4;
		//4WAYMOD snaps
		public final static int SNAP_TOPLEFT = 21;
		public final static int SNAP_TOPRIGHT = 23;
		public final static int SNAP_BOTTOMLEFT = 41;
		public final static int SNAP_BOTTOMRIGHT = 43;
	}

	public final static int AeroSnapNone = 0;
	final static ArrayList<Integer> snapSideReplaceTable = new ArrayList<Integer>(Arrays.asList(AeroSnap.SNAP_TOPLEFT, AeroSnap.SNAP_TOP, AeroSnap.SNAP_TOPRIGHT,
																								AeroSnap.SNAP_RIGHT,
																								AeroSnap.SNAP_BOTTOMRIGHT, AeroSnap.SNAP_BOTTOM, AeroSnap.SNAP_BOTTOMLEFT,
																								AeroSnap.SNAP_LEFT, AeroSnap.SNAP_NONE));
	final static ArrayList<Integer> snapGravityReplaceTable = new ArrayList<Integer>(Arrays.asList(Gravity.TOP | Gravity.LEFT, Gravity.TOP, Gravity.TOP | Gravity.RIGHT,
																								   Gravity.RIGHT,
																								   Gravity.BOTTOM | Gravity.RIGHT, Gravity.BOTTOM, Gravity.BOTTOM | Gravity.LEFT,
																								   Gravity.LEFT, 0));

	public static int snapGravityToSide(int snapGravity){
		int index = snapGravityReplaceTable.indexOf(snapGravity);
		if(index >=0) return snapSideReplaceTable.get(index);
		else return AeroSnap.SNAP_NONE;
	}

	public static int snapSideToGravity(int snapSide){
		int index = snapSideReplaceTable.indexOf(snapSide);
		if(index>=0) return snapGravityReplaceTable.get(index);
		else return 0;
	}
}
