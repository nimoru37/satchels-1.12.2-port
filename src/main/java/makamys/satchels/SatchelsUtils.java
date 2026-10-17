package makamys.satchels;

public class SatchelsUtils {

    public static boolean isPointInRange(int x, int min, int max) {
        return x >= min && x < max;
    }

    public static boolean isPointInRectangle(int x, int y, int x0, int y0, int w, int h) {
        return isPointInRange(x, x0, x0 + w) && isPointInRange(y, y0, y0 + h);
    }

}
