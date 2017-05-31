package levente.sermaul.mapscompare;

import android.location.Address;

import java.util.ArrayList;

/**
 * Created by Levente on 2016.12.01..
 */
public interface SearchObserver {
    void callback(ArrayList<Address> l);
}
