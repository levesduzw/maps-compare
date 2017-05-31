package levente.sermaul.mapscompare;

/**
 * Created by Levente on 2016.12.03..
 */
public class FavoritePlace {
    private String name;
    private String coords;
    private String date;

    public FavoritePlace() {
        name = null;
        coords = null;
        date = null;
    }

    public FavoritePlace(String _name, String _coords, String _date) {
        name = _name;
        coords = _coords;
        date = _date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoords() {
        return coords;
    }

    public void setCoords(String coords) {
        this.coords = coords;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
