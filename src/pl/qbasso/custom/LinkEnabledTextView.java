package pl.qbasso.custom;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class LinkEnabledTextView extends TextView {

	public LinkEnabledTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public LinkEnabledTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public LinkEnabledTextView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Object obj = getText();
		if (obj instanceof Spannable) {
			Spannable s = (Spannable) obj;

	        int x = (int) event.getX();
            int y = (int) event.getY();

            x -= getTotalPaddingLeft();
            y -= getTotalPaddingTop();

            x += getScrollX();
            y += getScrollY();

            Layout layout = getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);
            
            ClickableSpan[] link = s.getSpans(off, off,
                    ClickableSpan.class);

            if (link.length != 0) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    link[0].onClick(this);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                     Selection.setSelection(s,
                             s.getSpanStart(link[0]),
                             s.getSpanEnd(link[0]));
                }
                return true;
            } else {
            	return false;
            }
		}
		return super.onTouchEvent(event);
	}
	
	
	
	

}
