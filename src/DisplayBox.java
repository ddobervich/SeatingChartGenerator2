import processing.core.PApplet;

import java.util.ArrayList;

public class DisplayBox {
    private static final boolean DRAW_BORDER = true;
    private static final float HORIZ_BUFFER = 20;
    private static final float MAX_PENALTY = 100;
    private int x, y, w, h;
    private int rows, cols;     // how names are organized within the box
    private Group desk;
    private float textSize = 24;

    public DisplayBox(int x, int y, int w, int h, int rows, int cols, Group desk) {
        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
        this.rows = rows;
        this.cols = cols;
        this.desk = desk;
    }

    public static void swapLocations(DisplayBox b1, DisplayBox b2) {
        int x = b1.x;
        int y = b1.y;
        int w = b1.w;
        int h = b1.h;
        b1.x = b2.x;
        b1.y = b2.y;
        b1.w = b2.w;
        b1.h = b2.h;
        b2.x = x;
        b2.y = y;
        b2.w = w;
        b2.h = h;
    }

    public static void swapStudents(int[] draggingSeat, int[] targetSeat, ArrayList<DisplayBox> displayList) {
        DisplayBox source = displayList.get(draggingSeat[0]);
        DisplayBox target = displayList.get(targetSeat[0]);

        swapStudents(source, draggingSeat[1], draggingSeat[2], target, targetSeat[1], targetSeat[2]);
    }

    public static void swapStudents(DisplayBox source, int sr, int sc, DisplayBox target, int tr, int tc) {
        int sourcePosition = sr * source.cols + sc;
        int targetPosition = tr * target.cols + tc;
        Student sourceStudent = source.desk.get(sourcePosition);
        Student targetStudent = target.desk.get(targetPosition);
        source.desk.set(sourcePosition, targetStudent);
        target.desk.set(targetPosition, sourceStudent);
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

/*
    public String getText() {
        return getName1() + " and " + getName2();
    }
*/

    public void draw(PApplet window, boolean shadeConstraintViolations, boolean autoSizeText, boolean horizontallyReflect) {
        window.fill(0);
        window.textAlign(window.LEFT, window.TOP);

        int position = 0;
        float colWidth = (float)(w / (double) cols);
        float rowHeight = (float)((h / (double) rows));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int deskX = (int) (x + col * colWidth);
                int deskY = (int) (y + row * rowHeight);

                if (horizontallyReflect) {
                    deskX = (int)(window.width - deskX);
                }

                if (DRAW_BORDER) {
                    window.stroke(0);
                    if (shadeConstraintViolations) {
                        float val = 255 * window.map((float) desk.getPenalty(), 0, MAX_PENALTY, 0, 1);
                        window.fill(255, 255 - val, 255 - val);
                    } else {
                        window.fill(0, 0, 0, 0);
                    }
                    if (horizontallyReflect) {
                        window.rect(deskX - colWidth, deskY, colWidth, rowHeight);
                    } else {
                        window.rect(deskX, deskY, colWidth, rowHeight);
                    }
                }

                if (desk.isFrozen(position)) {
                    window.fill(255, 0, 0);
                    window.stroke(255, 0, 0);
                } else {
                    window.fill(0);
                    window.stroke(0);
                }
                String name = getName(position);

                getFittingTextSize(window, name, colWidth, textSize);
                if (horizontallyReflect) {
                    window.text(name, deskX - colWidth, deskY);
                } else {
                    window.text(name, deskX, deskY);
                }
                position++;
            }
        }
    }

    public void draw(PApplet window) {
        draw(window, false, true, false);
    }

    // Use defaultMax size if text will fit.  Otherwise calculate new smaller size
    private float getFittingTextSize(PApplet window, String text, float targetWidth, float defaultMax) {
        window.textSize(defaultMax);
        if (window.textWidth(text) <= targetWidth) return defaultMax;

        float minSize = 8;    // Smallest possible text size
        float maxSize = defaultMax;  // Start with a reasonably large text size

        // Binary search for the correct text size
        while (minSize < maxSize) {
            float midSize = (minSize + maxSize) / 2;
            window.textSize(midSize);
            float textWidth = window.textWidth(text);

            if (textWidth > targetWidth) {
                maxSize = midSize - 0.5f;  // Reduce the max size
            } else {
                minSize = midSize + 0.5f;  // Increase the min size
            }
        }

        // Use the smaller value of minSize to ensure it fits
        window.textSize(minSize);
        return minSize;
    }

    public String getName(int position) {
        return (this.desk.get(position) != null) ? this.desk.get(position).getDisplayName() : " no one";
    }

    public String getExperience(int position) {
        Student student = this.desk.get(position);
        if (student == null) return "";

        double exp = student.getExperienceLevel();
        return " " + displayExpStringFor(exp);
    }

    private String displayExpStringFor(double exp) {
        if (exp > 82) return "Adv";
        if (exp > 78) return "Mid+";
        if (exp > 65) return "Mid";
        if (exp > 60) return "Mid-";
        if (exp > 55) return "Beg+";
        if (exp > 45) return "Beg";
        return "Beg-";
    }

    public boolean isMouseOver(int mousex, int mousey) {
        return (x <= mousex && mousex <= x + w) && (y <= mousey && mousey <= y + h);
    }

    public int[] getNameBoxIndicies(int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return null;

        int r = (int) ((mouseY - y) / (h / (double) rows));
        int c = (int) ((mouseX - x) / (w / (double) cols));

        return new int[]{r, c};
    }



    public void mouseOverHighlight(int mouseX, int mouseY, PApplet window) {
        int[] indicies = getNameBoxIndicies(mouseX, mouseY);
        if (indicies == null) return;

        int r = indicies[0];
        int c = indicies[1];

        window.fill(255);
        window.stroke(window.color(255, 255, 128)); // yellow highlight
        window.rect(x + (int) (c * ((double) w / cols)), y + (int) (r * ((double) h / rows)), (int) (w / cols), (int) (h / rows));
    }

    /***
     * Handle the mouse click.  If (mouseX, mouseY) isn't on this displaybox, do nothing.
     * If 'b' is being held, we block the desk being clicked on (Ranodmly re-assigning a student who may already be there)
     * Otherwise, we remove the student from this desk location entirely. (eg, if they're absent).
     *
     * @param mouseX
     * @param mouseY
     * @param window
     */
    public void handleMouseClick(int mouseX, int mouseY, PApplet window) {
        int position = getPositionOf(mouseX, mouseY);
        if (position == -1) return;

        if (window.keyPressed && window.key == 'b') {       // block this desk
            desk.blockSeat(position);
        } else {                                            // delete the desk
            this.desk.delete(position);
        }
    }

    public int getPositionOf(int mouseX, int mouseY) {
        int[] indicies = getNameBoxIndicies(mouseX, mouseY);
        if (indicies == null) return -1;

        int r = indicies[0];
        int c = indicies[1];

        int position = r * cols + c;
        return position;
    }

    public void highlight(PApplet window, int color) {
        window.fill(0, 0, 0, 0);
        window.stroke(color);
        window.rect(x + 1, y + 1, w - 2, h - 2);
    }

    private int positionFor(int row, int col) {
        return row * cols + col;
    }

    private int getMaxRowWidth(PApplet window) {
        int maxWidth = 0;

        for (int row = 0; row < rows; row++) {
            int rowWidth = 0;
            for (int col = 0; col < cols; col++) {
                Student s = desk.get(positionFor(row, col));
                String name = (s == null) ? " no one" : s.getDisplayName();
                rowWidth += window.textWidth(name) + HORIZ_BUFFER;
            }
            rowWidth -= HORIZ_BUFFER;       // because we don't want buffer on the right

            if (rowWidth > maxWidth) {
                maxWidth = rowWidth;
            }
        }

        return maxWidth;
    }

    public void setWidthFromContents(PApplet window) {
        int width = getMaxRowWidth(window);
        this.w = width;
    }

    public void toggleFreezeNameFor(int mouseX, int mouseY) {
        int position = 0;
        double boxWidth = w / (double) cols;
        double boxHeight = h / (double) rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x1 = (x + col * (w / (double) cols));
                double y1 = (y + row * (h / (double) rows));
                double x2 = x1 + boxWidth;
                double y2 = y1 + boxHeight;
                if (x1 <= mouseX && mouseX <= x2) {
                    if (y1 <= mouseY && mouseY <= y2) {
                        desk.toggleFreeze(position);
                    }
                }
                position++;
            }
        }
    }

    public void drawGroupNumber(int i, PApplet window, boolean mirror) {
        window.textSize(50);
        window.textAlign(window.CENTER, window.CENTER);
        if (mirror) {
            window.text("" + i, window.width - (x + w / 2.0f), y - h / 3.0f);
        } else {
            window.text("" + i, x + w / 2.0f, y - h / 3.0f);
        }
    }

    public void drawExperienceLevel(PApplet window, boolean mirror) {   // TODO: clean this up.  copied from draw()
        window.fill(0);
        window.textAlign(window.LEFT, window.TOP);
        window.textSize(20);    // TODO: no hardcode!
        int position = 0;
        float colWidth = (float)(w / (double) cols);
        float rowHeight = (float)((h / (double) rows));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int deskX = (int) (x + col * colWidth);
                int deskY = (int) (y + row * rowHeight);

                if (mirror) {
                    deskX = (int)(window.width - deskX);
                }

                String name = getExperience(position);

                if (mirror) {
                    window.text(name, deskX - colWidth, deskY + rowHeight/2);
                } else {
                    window.text(name, deskX, deskY + rowHeight/2);
                }
                position++;
            }
        }
    }

    public void drawTableNumbers(int groupNum, PApplet window, boolean mirror) {
        window.textSize(20);
        window.textAlign(window.CENTER, window.CENTER);
        int seatNum = (groupNum-1)*this.rows*this.cols + 1;        // assumes all tables have same number of seats!
        float boxWidth = w / (float)cols;
        float boxHeight = h / (float)rows;

        if (mirror) {
            for (int displayRow = 0; displayRow < rows; displayRow++) {
                for (int displayCol = 0; displayCol < cols; displayCol++) {
                    float displayX = x + boxWidth/2 + displayCol*boxWidth;
                    float displayY = y + boxHeight/2 + displayRow*boxHeight;
                    window.text("" + seatNum++, window.width - displayX, displayY);
                }
            }
        } else {
            for (int displayRow = 0; displayRow < rows; displayRow++) {
                for (int displayCol = 0; displayCol < cols; displayCol++) {
                    float displayX = x + boxWidth/2 + displayCol*boxWidth;
                    float displayY = y + boxHeight/2 + displayRow*boxHeight;
                    window.text("" + seatNum++, displayX, displayY);
                }
            }
        }
    }
}