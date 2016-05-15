package eu.urho.accelmonitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
  /**
   * Representing the section name.
   */
  private static final String ARG_SECTION = "section_name";

  public PlaceholderFragment() {
  }

  /**
   * Returns a new instance of this fragment for the given section name.
   */
  public static PlaceholderFragment newInstance(String sectionName) {
    PlaceholderFragment fragment = new PlaceholderFragment();
    Bundle args = new Bundle();
    args.putString(ARG_SECTION, sectionName);
    fragment.setArguments(args);
    return fragment;
  }

  /**
   *
   * @param inflater
   * @param container
   * @param savedInstanceState
   * @return
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // find the layout based on the section name
    // layout file should be named after the section name
    String title = getArguments().getString(ARG_SECTION);
    int resID = getResources().getIdentifier(title, "layout", getContext().getPackageName());
    View rootView = inflater.inflate(getContext().getResources().getLayout(resID), container, false);
    return rootView;
  }
}
