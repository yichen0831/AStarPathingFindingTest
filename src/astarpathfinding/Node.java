package astarpathfinding;

public class Node {

    int gCost; // distance to the source
    int hCost; // distance to the target
    int fCost; // total cost of gCost and hCost

    boolean closed;

    int x;
    int y;

    Node prev;
    Node next;

    public Node(int x, int y) {
        this(x, y, false);
    }

    public Node(int x, int y, boolean closed) {
        this.x = x;
        this.y = y;
        this.closed = closed;

        gCost = 0;
        hCost = 0;
        fCost = 0;
    }

    public boolean isXY(int x, int y) {
        return this.x == x && this.y == y;
    }

    @Override
    public String toString() {
        return "Node - (" + x + ", " + y + ")";
    }
}
