package moe.plushie.armourers_workshop.core.math;

import moe.plushie.armourers_workshop.api.core.math.IVector3i;
import moe.plushie.armourers_workshop.core.utils.OpenDirection;

import java.util.EnumSet;
import java.util.Objects;

@SuppressWarnings("unused")
public class Vector3d {

    public static final Vector3d ZERO = new Vector3d(0.0D, 0.0D, 0.0D);

    public final double x;
    public final double y;
    public final double z;

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3d fromRGB24(int rgb) {
        double d0 = (double) (rgb >> 16 & 255) / 255.0D;
        double d1 = (double) (rgb >> 8 & 255) / 255.0D;
        double d2 = (double) (rgb & 255) / 255.0D;
        return new Vector3d(d0, d1, d2);
    }

    public static Vector3d atCenterOf(IVector3i pos) {
        return new Vector3d((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
    }

    public static Vector3d atLowerCornerOf(IVector3i pos) {
        return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vector3d atBottomCenterOf(IVector3i pos) {
        return new Vector3d((double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D);
    }

    public static Vector3d upFromBottomCenterOf(IVector3i pos, double offset) {
        return new Vector3d((double) pos.getX() + 0.5D, (double) pos.getY() + offset, (double) pos.getZ() + 0.5D);
    }

    public static Vector3d directionFromRotation(float a, float b) {
        double f = Math.cos(-b * (Math.PI / 180.0) - Math.PI);
        double f1 = Math.sin(-b * (Math.PI / 180.0) - Math.PI);
        double f2 = -Math.cos(-a * (Math.PI / 180.0));
        double f3 = Math.sin(-a * (Math.PI / 180.0));
        return new Vector3d(f1 * f2, f3, f * f2);
    }

    public Vector3d vectorTo(Vector3d pos) {
        return new Vector3d(pos.x - this.x, pos.y - this.y, pos.z - this.z);
    }

    public Vector3d normalize() {
        double d0 = Math.sqrt(x * x + y * y + z * z);
        return d0 < 1.0E-4D ? ZERO : new Vector3d(x / d0, y / d0, z / d0);
    }

    public double dot(Vector3d vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public Vector3d cross(Vector3d vec) {
        return new Vector3d(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
    }

    public Vector3d subtract(Vector3d delta) {
        return this.subtract(delta.x, delta.y, delta.z);
    }

    public Vector3d subtract(double tx, double ty, double tz) {
        return add(-tx, -ty, -tz);
    }

    public Vector3d add(Vector3d delta) {
        return this.add(delta.x, delta.y, delta.z);
    }

    public Vector3d add(double tx, double ty, double tz) {
        return new Vector3d(x + tx, y + ty, z + tz);
    }

    public double distanceTo(Vector3d pos) {
        double d0 = pos.x - x;
        double d1 = pos.y - y;
        double d2 = pos.z - z;
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double distanceToSqr(Vector3d pos) {
        double d0 = pos.x - x;
        double d1 = pos.y - y;
        double d2 = pos.z - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distanceToSqr(double tx, double ty, double tz) {
        double d0 = tx - x;
        double d1 = ty - y;
        double d2 = tz - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public Vector3d scale(double v) {
        return multiply(v, v, v);
    }

    public Vector3d reverse() {
        return scale(-1.0D);
    }

    public Vector3d multiply(Vector3d pos) {
        return multiply(pos.x, pos.y, pos.z);
    }

    public Vector3d multiply(double dx, double dy, double dz) {
        return new Vector3d(x * dx, y * dy, z * dz);
    }

    public double length() {
        return OpenMath.sqrt(OpenMath.fma(x, x, OpenMath.fma(y, y, z * z)));
    }

//    public void normalize() {
//        var scalar = OpenMath.invsqrt(OpenMath.fma(x, x, OpenMath.fma(y, y, z * z)));
//        this.x *= scalar;
//        this.y *= scalar;
//        this.z *= scalar;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector3d that)) return false;
        return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(z, that.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("(%g %g %g)", x, y, z);
    }

    public Vector3d xRot(float value) {
        double f = Math.cos(value);
        double f1 = Math.sin(value);
        double d0 = x;
        double d1 = y * f + z * f1;
        double d2 = z * f - y * f1;
        return new Vector3d(d0, d1, d2);
    }

    public Vector3d yRot(float p_178785_1_) {
        double f = Math.cos(p_178785_1_);
        double f1 = Math.sin(p_178785_1_);
        double d0 = x * f + z * f1;
        double d1 = y;
        double d2 = z * f - x * f1;
        return new Vector3d(d0, d1, d2);
    }

    public Vector3d zRot(float p_242988_1_) {
        double f = Math.cos(p_242988_1_);
        double f1 = Math.sin(p_242988_1_);
        double d0 = x * f + y * f1;
        double d1 = y * f - x * f1;
        double d2 = z;
        return new Vector3d(d0, d1, d2);
    }

    public Vector3d align(EnumSet<OpenDirection.Axis> set) {
        double d0 = set.contains(OpenDirection.Axis.X) ? Math.floor(x) : x;
        double d1 = set.contains(OpenDirection.Axis.Y) ? Math.floor(y) : y;
        double d2 = set.contains(OpenDirection.Axis.Z) ? Math.floor(z) : z;
        return new Vector3d(d0, d1, d2);
    }

    public double get(OpenDirection.Axis axis) {
        return axis.choose(x, y, z);
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }
}
