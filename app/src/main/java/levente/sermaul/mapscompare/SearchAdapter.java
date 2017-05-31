package levente.sermaul.mapscompare;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.List;

/**
 * Created by Levente on 2016.12.01..
 */
public class SearchAdapter extends ArrayAdapter<String> {
    NoFilter noFilter;

    public SearchAdapter(Context context, int resource) {
        super(context, resource);
    }

    public SearchAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public SearchAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    public SearchAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public SearchAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    public SearchAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    /**
     * Override ArrayAdapter.getFilter() to return our own filtering.
     */
    public Filter getFilter() {
        if (noFilter == null) {
            noFilter = new NoFilter();
        }
        return noFilter;
    }

    /**
     * Class which does not perform any filtering.
     * Filtering is already done by the web service when asking for the list,
     * so there is no need to do any more as well.
     * This way, ArrayAdapter.mOriginalValues is not used when calling e.g.
     * ArrayAdapter.add(), but instead ArrayAdapter.mObjects is updated directly
     * and methods like getCount() return the expected result.
     */
    private class NoFilter extends Filter {
        protected FilterResults performFiltering(CharSequence prefix) {
            return new FilterResults();
        }

        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Do nothing
        }
    }
}
