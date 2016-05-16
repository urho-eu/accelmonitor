package eu.urho.accelmonitor;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by sopi on 5/13/16.
 */
public class AccRowValue extends TextView {

  public AccRowValue(Context context)
  {
    super(context);
  }

  public AccRowValue(Context context, Number number, int textColor, int bgColor, int alignment) {
    super(context);
    this.setValues(number.toString(), textColor, bgColor, alignment);
  }

  public AccRowValue(Context context, CharSequence text, int textColor, int bgColor, int alignment) {
    super(context);
    this.setValues(text, textColor, bgColor, alignment);
  }

  /**
   *
   * @param text
   * @param textColor
   * @param bgColor
   * @param alignment
   */
  private void setValues(CharSequence text, int textColor, int bgColor, int alignment) {
    this.setText(text);
    this.setTextColor(textColor);
    this.setBackgroundColor(bgColor);

    if (alignment == 0) alignment = TEXT_ALIGNMENT_CENTER;
    this.setTextAlignment(alignment);

    int p = getResources().getDimensionPixelSize(R.dimen.value_pad);
    this.setPadding(p, p, p, p);
  }
}
