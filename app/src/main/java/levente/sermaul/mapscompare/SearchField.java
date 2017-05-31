package levente.sermaul.mapscompare;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Levente on 2016.11.29..
 */
public class SearchField extends AutoCompleteTextView {
    Context mContext;
    Geocoder geocoder;
    ArrayList<Address> mAddressList;
    private SearchObserver searchObserver;

    public SearchField(Context context) {
        super(context);
        mContext = context;
        geocoder = new Geocoder(mContext);

    }

    public SearchField(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        geocoder = new Geocoder(mContext);
    }

    public SearchField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        geocoder = new Geocoder(mContext);
    }

    protected void performFiltering(CharSequence text, int keyCode) {
        // nothing, block the default auto complete behavior
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

        Drawable x = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_clear_black_24dp, null);
        x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
        setCompoundDrawables(getCompoundDrawables()[0], null, getText().toString().equals("") ? null : x , null);

        if (s.length()!=0 && !s.equals("Search")) {
            try {
                List<Address> temp = geocoder.getFromLocationName(s.toString(), 4);
                mAddressList = new ArrayList<>(temp);

            } catch(Exception e) {
                e.printStackTrace();
            }
            if (searchObserver != null ){
                searchObserver.callback(mAddressList);
            }
        }
    }


    public void setObserver(SearchObserver observer) {
        searchObserver = observer;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode== KeyEvent.KEYCODE_ENTER) {
            String string = this.getText().toString();
            if (string.equals("")) return false;
            try {
                List<Address> temp = geocoder.getFromLocationName(string, 1);
                Intent intent = new Intent("SCROLL_MAP");
                intent.putExtra("latitude", temp.get(0).getLatitude());
                intent.putExtra("longitude", temp.get(0).getLongitude());
                mContext.sendBroadcast(intent);

            } catch(Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(event);
        }
        if (event.getX() > getWidth() - getPaddingRight() - getResources().getDrawable(R.drawable.ic_clear_black_24dp).getIntrinsicWidth()) {
            setText("");
            setCompoundDrawables(getCompoundDrawables()[0], null, null, null);
        }
        return super.onTouchEvent(event);

    }
}
