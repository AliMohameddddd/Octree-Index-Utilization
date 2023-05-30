package model;

import exceptions.DBAppException;
import exceptions.DBSchemaException;
import utils.Validation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

public class Octree implements Serializable {

    private final Octree[] children = new Octree[8];
    private final Vector<Octant> points; // A list to store duplicate points
    private Octant minXYZ, maxXYZ;

    public Octree(Comparable x1, Comparable y1, Comparable z1, Comparable x2, Comparable y2, Comparable z2) throws DBAppException {
        if (x2.compareTo(x1) < 0 || y2.compareTo(y1) < 0 || z2.compareTo(z1) < 0) {
            throw new DBSchemaException("node bounds are not properly set!");
        }

        this.points = null;
        this.minXYZ = new Octant(x1, y1, z1);
        this.maxXYZ = new Octant(x2, y2, z2);

        for (int i = 0; i <= 7; i++) {
            this.children[i] = new Octree();
        }
    }

    private Octree(Comparable x, Comparable y, Comparable z, int pageIndex) {
        this.points = new Vector<>();
        this.points.add(new Octant(x, y, z, pageIndex));
    }

    private Octree() {
        this.points = new Vector<>();
    }

    public void insert(Comparable x, Comparable y, Comparable z, int pageIndex) throws DBAppException {
        if (!isValid(x, y, z)) {
            System.out.println(x + " " + y + " " + z + " " + maxXYZ.toString() + " " + minXYZ.toString());
            throw new DBSchemaException("Invalid coordinates");
        }

        Comparable[] mids = getMidPoints();
        Comparable midX = mids[0], midY = mids[1], midZ = mids[2];

        int pos = getPosition(x, y, z);

        if (children[pos].points == null) {         // if null, then it's an Octree with children (range node)
            children[pos].insert(x, y, z, pageIndex);
        } else if (children[pos].points.isEmpty()) { // if empty, then it's an empty Octant (leaf node)
            children[pos] = new Octree(x, y, z, pageIndex);
        } else {                                     // else it is a non-empty Octant (leaf node)
            // handle multiple duplicates
            Octant octant = children[pos].points.get(0);
            Comparable x_ = octant.getX();
            Comparable y_ = octant.getY();
            Comparable z_ = octant.getZ();
            int pageIndex_ = octant.getPageIndex();
            if (x.equals(x_) && y.equals(y_) && z.equals(z_)) { // if is a duplicate
                children[pos].points.add(new Octant(x, y, z, pageIndex));
                return;
            }
            children[pos] = null;

            Comparable incrementedMidX = Validation.increment(midX);
            Comparable incrementedMidY = Validation.increment(midY);
            Comparable incrementedMidZ = Validation.increment(midZ);
            if (pos == OctLocations.TopLeftFront.getNumber()) {
                children[pos] = new Octree(minXYZ.getX(), minXYZ.getY(), minXYZ.getZ(), midX, midY, midZ);
            } else if (pos == OctLocations.TopRightFront.getNumber()) {
                children[pos] = new Octree(incrementedMidX, minXYZ.getY(), minXYZ.getZ(), maxXYZ.getX(), midY, midZ);
            } else if (pos == OctLocations.BottomRightFront.getNumber()) {
                children[pos] = new Octree(incrementedMidX, incrementedMidY, minXYZ.getZ(), maxXYZ.getX(), maxXYZ.getY(), midZ);
            } else if (pos == OctLocations.BottomLeftFront.getNumber()) {
                children[pos] = new Octree(minXYZ.getX(), incrementedMidY, minXYZ.getZ(), midX, maxXYZ.getY(), midZ);
            } else if (pos == OctLocations.TopLeftBottom.getNumber()) {
                children[pos] = new Octree(minXYZ.getX(), minXYZ.getY(), incrementedMidZ, midX, midY, maxXYZ.getZ());
            } else if (pos == OctLocations.TopRightBottom.getNumber()) {
                children[pos] = new Octree(incrementedMidX, minXYZ.getY(), incrementedMidZ, maxXYZ.getX(), midY, maxXYZ.getZ());
            } else if (pos == OctLocations.BottomRightBack.getNumber()) {
                children[pos] = new Octree(incrementedMidX, incrementedMidY, incrementedMidZ, maxXYZ.getX(), maxXYZ.getY(), maxXYZ.getZ());
            } else if (pos == OctLocations.BottomLeftBack.getNumber()) {
                children[pos] = new Octree(minXYZ.getX(), incrementedMidY, incrementedMidZ, midX, maxXYZ.getY(), maxXYZ.getZ());
            }

            children[pos].insert(x_, y_, z_, pageIndex_);
            children[pos].insert(x, y, z, pageIndex);
        }
    }

    public boolean find(Comparable x, Comparable y, Comparable z) {
        if (!isValid(x, y, z))
            return false;

        int pos = getPosition(x, y, z);

        if (children[pos].points == null)
            return children[pos].find(x, y, z);
        if (children[pos].points.isEmpty())
            return false;
        return (x.equals(children[pos].points.get(0).getX()) && y.equals(children[pos].points.get(0).getY()) && z.equals(children[pos].points.get(0).getZ()));
    }

    public HashSet<Integer> get(Comparable x1, Comparable y1, Comparable z1, Comparable x2, Comparable y2, Comparable z2) {
        HashSet<Integer> result = new HashSet<>();

        if (!isValid(x1, y1, z1) || !isValid(x2, y2, z2))
            return result;
        if (!isRangesIntersect(x1, y1, z1, x2, y2, z2))
            return result;

        for (int i = 0; i < 8; i++) {
            if (children[i].points == null) // is non-leaf node
                result.addAll(children[i].get(x1, y1, z1, x2, y2, z2));
            else
                for (Octant octant : children[i].points) {
                    Comparable x = octant.getX();
                    Comparable y = octant.getY();
                    Comparable z = octant.getZ();
                    if (isInRange(x, y, z, x1, y1, z1, x2, y2, z2))
                        result.add(octant.getPageIndex());
                }
        }
        return result;
    }

    // return pageIndices of all points with the same x, y, z
    public HashSet<Integer> get(Comparable x, Comparable y, Comparable z) {
        HashSet<Integer> pageIndices = new HashSet<>();
        if (!isValid(x, y, z))
            return pageIndices;

        int pos = getPosition(x, y, z);

        if (children[pos].points == null)
            return children[pos].get(x, y, z);
        if (children[pos].points.isEmpty())
            return pageIndices;
        if (x.equals(children[pos].points.get(0).getX()) && y.equals(children[pos].points.get(0).getY()) && z.equals(children[pos].points.get(0).getZ())) {
            for (int i = 0; i < children[pos].points.size(); i++)
                pageIndices.add(children[pos].points.get(i).getPageIndex());
            return pageIndices;
        }
        return pageIndices;
    }

    public boolean remove(Comparable x, Comparable y, Comparable z, int pageIndex) throws DBAppException {
        if (!isValid(x, y, z))
            throw new DBSchemaException("Invalid coordinates");

        int pos = getPosition(x, y, z);

        if (children[pos].points == null)
            return children[pos].remove(x, y, z, pageIndex);
        else
            for (int i = 0; i < children[pos].points.size(); i++)
                if (x.equals(children[pos].points.get(i).getX()) && y.equals(children[pos].points.get(i).getY()) && z.equals(children[pos].points.get(i).getZ())) {
                    children[pos].points.remove(i);
                    return true;
                }

        return false;
    }

    public void update(Comparable x, Comparable y, Comparable z, int oldPageIndex, int newPageIndex) {
        if (!isValid(x, y, z))
            return;

        int pos = getPosition(x, y, z);

        if (children[pos].points == null)
            children[pos].update(x, y, z, oldPageIndex, newPageIndex);
        else
            for (int i = 0; i < children[pos].points.size(); i++)
                if (children[pos].points.get(i).getPageIndex() == oldPageIndex) {
                    children[pos].points.get(i).setPageIndex(newPageIndex);
                    break;
                }

    }

    public void print() {
        System.out.println("minXYZ: " + minXYZ);
        System.out.println("maxXYZ: " + maxXYZ);
        System.out.println("points: " + points);
        System.out.println("children: " + Arrays.toString(children));
        System.out.println();
        for (int i = 0; i < 8; i++)
            if (children[i].points == null)
                children[i].print();
    }


    // Helpers
    private boolean isValid(Comparable x, Comparable y, Comparable z) {
        if (x.compareTo(minXYZ.getX()) < 0 || x.compareTo(maxXYZ.getX()) > 0
                || y.compareTo(minXYZ.getY()) < 0 || y.compareTo(maxXYZ.getY()) > 0
                || z.compareTo(minXYZ.getZ()) < 0 || z.compareTo(maxXYZ.getZ()) > 0)
            return false;
        return true;
    }

    private Comparable[] getMidPoints() {
        Comparable[] midPoints = new Comparable[3];
        Comparable x1 = minXYZ.getX(), x2 = maxXYZ.getX();
        Comparable y1 = minXYZ.getY(), y2 = maxXYZ.getY();
        Comparable z1 = minXYZ.getZ(), z2 = maxXYZ.getZ();

        midPoints[0] = Validation.getMidComparable(x1, x2);
        midPoints[1] = Validation.getMidComparable(y1, y2);
        midPoints[2] = Validation.getMidComparable(z1, z2);

        return midPoints;
    }

    private int getPosition(Comparable x, Comparable y, Comparable z) {
        Comparable[] mids = getMidPoints();
        Comparable midX = mids[0], midY = mids[1], midZ = mids[2];

        int pos;
        if (x.compareTo(midX) <= 0) {
            if (y.compareTo(midY) <= 0) {
                if (z.compareTo(midZ) <= 0)
                    pos = OctLocations.TopLeftFront.getNumber();
                else
                    pos = OctLocations.TopLeftBottom.getNumber();
            } else {
                if (z.compareTo(midZ) <= 0)
                    pos = OctLocations.BottomLeftFront.getNumber();
                else
                    pos = OctLocations.BottomLeftBack.getNumber();
            }
        } else {
            if (y.compareTo(midY) <= 0) {
                if (z.compareTo(midZ) <= 0)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBottom.getNumber();
            } else {
                if (z.compareTo(midZ) <= 0)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber();
            }
        }
        return pos;
    }

    private boolean isRangesIntersect(Comparable x1, Comparable y1, Comparable z1, Comparable x2, Comparable y2, Comparable z2) {
        if (x1.compareTo(maxXYZ.getX()) > 0 || x2.compareTo(minXYZ.getX()) < 0
                || y1.compareTo(maxXYZ.getY()) > 0 || y2.compareTo(minXYZ.getY()) < 0
                || z1.compareTo(maxXYZ.getZ()) > 0 || z2.compareTo(minXYZ.getZ()) < 0)
            return false;
        return true;
    }

    private boolean isInRange(Comparable x, Comparable y, Comparable z, Comparable x1, Comparable y1, Comparable z1, Comparable x2, Comparable y2, Comparable z2) {
        if (x.compareTo(x1) >= 0 && x.compareTo(x2) <= 0
                && y.compareTo(y1) >= 0 && y.compareTo(y2) <= 0
                && z.compareTo(z1) >= 0 && z.compareTo(z2) <= 0)
            return true;
        return false;
    }

}

class Octant implements Serializable {

    private Comparable x;
    private Comparable y;
    private Comparable z;
    private int pageIndex;

    public Octant(Comparable x, Comparable y, Comparable z, int pageIndex) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pageIndex = pageIndex;
    }

    public Octant(Comparable x, Comparable y, Comparable z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Octant() {
    }

    public Comparable getX() {
        return x;
    }

    public Comparable getY() {
        return y;
    }

    public Comparable getZ() {
        return z;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}

enum OctLocations {
    TopLeftFront(0),
    TopRightFront(1),
    BottomRightFront(2),
    BottomLeftFront(3),
    TopLeftBottom(4),
    TopRightBottom(5),
    BottomRightBack(6),
    BottomLeftBack(7);

    int num;

    OctLocations(int num) {
        this.num = num;
    }

    int getNumber() {
        return num;
    }
}