package util;

public class PointFlow {
    int x, y, direction, regionId;
    double magnitude;

    public PointFlow(int x, int y, int direction, double magnitude, int regionId) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.magnitude = magnitude;
        this.regionId = regionId;
    }
}

