package min3d;

/**
 * Created by sreejith on 17/11/15.
 */
public final class Vector3 {

    public double x, y, z;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Vector3(double ix, double iy, double iz) {
        x = ix;
        y = iy;
        z = iz;
    }

    public void set(double ix, double iy, double iz) {
        x = ix;
        y = iy;
        z = iz;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public void multiply(double f) {
        x *= f;
        y *= f;
        z *= f;
    }

    public void normalise() {
        double mag = magnitude();
        x /= mag;
        y /= mag;
        z /= mag;
    }
}
