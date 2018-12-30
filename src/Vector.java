public class Vector{
    private int x;
    private int y;

    public Vector() {
    }

    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Vector) {
            return ((Vector)obj).getX() == this.x && ((Vector)obj).getY() == this.y;
        } else {
            return super.equals(obj);
        }
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public String toString() {
        return "x: " + this.x + " y: " + this.y;
    }
}
