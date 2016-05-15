package eu.urho.accelmonitor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

  private String[] sections;

  public SectionsPagerAdapter(FragmentManager fm, String[] sections)
  {
    super(fm);
    this.sections = sections;
  }

  @Override
  public Fragment getItem(int position) {
    // getItem is called to instantiate the fragment for the given page.
    Log.d("SectionsPageAdapter", "title: " + getPageTitle(position).toString().toLowerCase());
    return PlaceholderFragment.newInstance(getPageTitle(position).toString().toLowerCase());
  }

  @Override
  public int getCount() {
    return this.sections.length;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return this.sections[position];
  }
}