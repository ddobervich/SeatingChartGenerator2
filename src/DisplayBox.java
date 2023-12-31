import processing.core.PApplet;

public class DisplayBox {
    private static final float MAX_PENALTY = 6;
    public static final int LEFT_COLOR = 0xFF5D3FD3;
    public static final int RIGHT_COLOR = 0xFFEEBC1D;
    public static final int FROZEN_COLOR = 0xFFFF0000;
    private static int nextId = 1;

    private int x, y, w, h;
    private int id;
    private DeskPair desk;

    public DisplayBox(int x, int y, int w, int h, DeskPair desk) {
        this.id = nextId;
        nextId++;

        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
        this.desk = desk;
    }

    public DisplayBox(int id, int x, int y, int w, int h, DeskPair desk) {
        this.id = id;

        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
        this.desk = desk;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public String getText() {
        return getName1() + " and " + getName2();
    }

    // names stacked vertically
    public void drawLayoutDisplay(PApplet window) {
        window.fill(255);
        window.stroke(0);
       /* if (shadeConstraintViolations) {
            float val = 255 * window.map((float) desk.getPenalty(), 0, MAX_PENALTY, 0, 1);
            window.fill(255, 255 - val, 255 - val);
        }*/
        window.rect(x, y, w, h);
        window.fill(0);
        window.line(x, y + h/2, x + w, y + h/2);
        window.textAlign(window.LEFT, window.TOP);

        if (desk != null && desk.isLeftFrozen()) {
            window.fill(FROZEN_COLOR);
            window.stroke(FROZEN_COLOR);
        }
        window.text(getName1() + " ", x + 5, y + h/5);

        if (desk != null && desk.isRightFrozen()) {
            window.fill(FROZEN_COLOR);
            window.stroke(FROZEN_COLOR);
        }
        window.text(getName2(), x + 5, y + 3*h/5);

        window.fill(0);
        window.stroke(0);
    }

    public void drawListDisplay(PApplet window, boolean shadeConstraintViolations) {
        drawListDisplay(window, shadeConstraintViolations, false);
    }

    public void drawListDisplay(PApplet window, boolean shadeConstraintViolations, boolean displayDeskId) {
        window.fill(255);
        window.stroke(0);
        if (shadeConstraintViolations) {
            float val = 255 * window.map((float) desk.getPenalty(), 0, MAX_PENALTY, 0, 1);
            window.fill(255, 255 - val, 255 - val);
        }
        window.rect(x, y, w, h);
        window.fill(0);
        window.textAlign(window.LEFT, window.TOP);

        if (displayDeskId) {
            window.text("" + this.getId(), x + getW() / 2, y + getH());
        }

        window.fill(LEFT_COLOR);
        window.stroke(LEFT_COLOR);
        if (desk != null && desk.isLeftFrozen()) {
            window.fill(FROZEN_COLOR);
            window.stroke(FROZEN_COLOR);
        }
        window.text(getName1() + " ", x, y);
        float nextX = x + window.textWidth(this.getName1() + " ");

        window.fill(0);
        window.stroke(0);
        window.text("and ", nextX, y);

        nextX = nextX + window.textWidth("and ");
        window.fill(RIGHT_COLOR);
        window.fill(RIGHT_COLOR);
        if (desk != null && desk.isRightFrozen()) {
            window.fill(FROZEN_COLOR);
            window.stroke(FROZEN_COLOR);
        }
        window.text(getName2(), nextX, y);

        window.fill(0);
        window.stroke(0);
    }

    int getId() {
        return this.id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getName1() {
        if (desk == null) return " no one";
        return (this.desk.getLeft() != null ? this.desk.getLeft().getDisplayName() : " no one");
    }

    public String getName2() {
        if (desk == null) return " no one";
        return (this.desk.getRight() != null ? this.desk.getRight().getDisplayName() : " no one");
    }

    public boolean isMouseOver(int mousex, int mousey) {
        return (x < mousex && mousex < x + w) && (y < mousey && mousey < y + h);
    }

    public boolean isMouseOverLeftName(int mousex, int mousey, PApplet window) {
        if (!isMouseOver(mousex, mousey)) return false;

        float n1Width = window.textWidth(this.getName1());
        return (mousex < x + n1Width);
    }

    public boolean isMouseOverRightName(int mousex, int mousey, PApplet window) {
        if (!isMouseOver(mousex, mousey)) return false;

        float n2Width = window.textWidth(this.getName2());
        float textWidth = window.textWidth(this.getText());
        return mousex > x + textWidth - n2Width;
    }

    public void handleMouseClick(int mousex, int mousey, int lastMouseButton, PApplet window) {
        if (!isMouseOver(mousex, mousey)) return;

        float n1Width = window.textWidth(this.getName1());
        if (mousex < x + n1Width) {
            if (lastMouseButton == window.LEFT) {
                this.desk.deleteLeft();
            } else if (lastMouseButton == window.RIGHT) {
                this.desk.unseatLeft();
            }
        }

        float n2Width = window.textWidth(this.getName2());
        float textWidth = window.textWidth(this.getText());
        if (mousex > x + textWidth - n2Width)  {
            if (lastMouseButton == window.LEFT) {
                this.desk.deleteRight();
            } else if (lastMouseButton == window.RIGHT) {
                this.desk.unseatRight();
            }
        }
    }

    private void removeStudent2() {
        System.out.println("removing: " + this.getName2());
        this.desk.removeRight();
    }

    private void removeStudent1() {
        System.out.println("removing" + this.getName1());
        this.desk.removeLeft();
    }

    public void highlight(PApplet window) {
        window.fill(0, 0, 0, 0);
        window.stroke(0, 255, 0);
        window.rect(x, y, w, h);
    }

    public Student getStudent1() {
        return this.desk.getLeft();
    }

    public Student getStudent2() {
        return this.desk.getRight();
    }

    public void setWidthFromContents(PApplet window) {
        this.w = (int) (window.textWidth(this.getText())) + 1;
    }

    public DeskPair getDeskPair() {
        return this.desk;
    }
}