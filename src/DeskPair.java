public class DeskPair {
    private static int nextId = 1;
    private int id;
    private Student left, right;
    private SeatingChart chart;
    private boolean freezeLeft, freezeRight;

    public DeskPair(Student left, Student right) {
        this.id = nextId++;
        this.left = left;
        this.right = right;
        this.freezeRight = false;
        this.freezeLeft = false;
    }

    public DeskPair(DeskPair toCopy) {
        this.id = toCopy.id;
        this.left = toCopy.left;
        this.right = toCopy.right;
        this.freezeLeft = toCopy.freezeLeft;
        this.freezeRight = toCopy.freezeRight;
    }

    public DeskPair(Student left, Student right, SeatingChart chart) {
        this(left, right);
        this.chart = chart;
    }

    public boolean isLeftFrozen() {
        return freezeLeft;
    }

    public void setFreezeLeft(boolean freezeLeft) {
        this.freezeLeft = freezeLeft;
    }

    public boolean isRightFrozen() {
        return freezeRight;
    }

    public void setFreezeRight(boolean freezeRight) {
        this.freezeRight = freezeRight;
    }

    public Student getLeft() {
        return left;
    }

    public void setLeft(Student left) {
        this.left = left;
    }

    public Student getRight() {
        return right;
    }

    public void setRight(Student right) {
        this.right = right;
    }

    public Student removeRight() {
        Student toRemove = this.right;
        this.right = null;
        return toRemove;
    }

    public Student removeLeft() {
        Student toRemove = this.left;
        this.left = null;
        return toRemove;
    }

    public void setEmptySeatTo(Student s) {
        if (leftEmpty()) {
            setLeft(s);
            return;
        }
        if (rightEmpty()) {
            setRight(s);
            return;
        }
    }

    private boolean rightEmpty() {
        return right == null;
    }

    private boolean leftEmpty() {
        return left == null;
    }

    public boolean hasSpace() {
        return leftEmpty() || rightEmpty();
    }

    public boolean hasStudent(Student s) {
        if (s == null) return false;
        Student right = getRight();
        if (right != null && right.equals(s)) return true;

        Student left = getLeft();
        if (left != null && left.equals(s)) return true;

        return false;
    }

    public void removeStudent(Student s) {
        Student right = getRight();
        if (right != null && right.equals(s)) {
            removeRight();
            return;
        }

        Student left = getLeft();
        if (left != null && left.equals(s)) {
            removeLeft();
            return;
        }
    }

    public void clear() {
        removeLeft();
        removeRight();
    }

    public boolean isEmpty() {
        return leftEmpty() && rightEmpty();
    }

    public void deleteLeft() {
        if (getLeft() == null) return;
        if (chart != null) {
            chart.deleteStudent(getLeft());
            chart.consolodate();
        }
    }

    public void deleteRight() {
        if (getRight() == null) return;
        if (chart != null) {
            chart.deleteStudent(getRight());
            chart.consolodate();
        }
    }

    public double getPenalty() {
        double penalty = 0;
        if (getLeft() != null) {
            penalty += getLeft().getMatchScoreFor(getRight());
        }
        if (getRight() != null) {
            penalty += getRight().getMatchScoreFor(getLeft());
        }

        return penalty;
    }

    public void setChart(SeatingChart seatingChart) {
        this.chart = seatingChart;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int deskId) {
        this.id = deskId;
    }

    public void unseatLeft() {
        if (chart == null) return;
        if (leftEmpty()) return;
        this.chart.unseatStudent(this.left);
    }

    public void unseatRight() {
        if (chart == null) return;
        if (rightEmpty()) return;
        this.chart.unseatStudent(this.right);
    }

    public boolean seat(Student s) {
        if (!hasSpace()) return false;
        if (leftEmpty()) {
            setLeft(s);
            return true;
        } else if (rightEmpty()) {
            setRight(s);
            return true;
        }
        return false;
    }

    public void toggleFreezeLeft() {
        this.freezeLeft = !this.freezeLeft;
    }

    public void toggleFreezeRight() {
        this.freezeRight = !this.freezeRight;
    }
}