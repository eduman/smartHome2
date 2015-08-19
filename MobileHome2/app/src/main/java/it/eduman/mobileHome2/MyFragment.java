package it.eduman.mobileHome2;

import it.eduman.mobileHome2.commons.MobileHomeConstants;
import android.support.v4.app.Fragment;


public abstract class MyFragment extends Fragment{
	public static int CURRENT_VISIBLE_FRAGMENT = MobileHomeConstants.MAIN_FRAGMENT_POSITION;

	public abstract void update();
	
}
