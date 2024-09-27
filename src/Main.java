import processing.core.PApplet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/*
TODO: bug: if some charts have 2 per group and some 3, display doesn't change as we arrow through them
TODO: add way to delete a chart that appropriately updates the partner histories
TODO: make color-coding for attainment (or other marking) so I can print and me/TA's can see
 */

public class Main extends PApplet {
    private ControlWindow controlWindow;

    private String BASE_PATH = "DataFiles/";
    private String file = "block7-2024.csv";

    private static final float TEXT_SIZE = 32;
    private static final int TOP_BUFF = 80;
    private static final int LEFT_BUFF = 80;

    private static final int NUM_TO_SEARCH = 1000000;
    private static final int LIST_LAYOUT = 0;
    private static final int ROOM_LAYOUT = 1;

    private int studentsPerGroup = 3;
    private int currentSelectionIndex = -1;

    SeatingChart chart = new SeatingChart(studentsPerGroup);
    ArrayList<DisplayBox> displayList = new ArrayList<DisplayBox>();
    float verticalBuffer = 10;
    float textHeight, boxHeight;
    int numNamesPerCol, numColumns;
    int columnWidth;

    int displayMode = ROOM_LAYOUT;
    private double currentScore = -1;
    private int currentChartIndex = -1;
    private int maxChartIndex = -1;
    private int[] draggingSeat = null;    // { group index, row-within-group, col-within-group }

    public void settings() {
        size(1200, 600);
    }

    public void setup() {
        controlWindow = new ControlWindow();

        textSize(TEXT_SIZE);
        float strAscent = textAscent();
        float strDescent = textDescent();
        textHeight = strAscent + strDescent;
        boxHeight = textHeight + verticalBuffer;
        numNamesPerCol = (int) (studentsPerGroup * (height - TOP_BUFF) / boxHeight);

        loadFile(BASE_PATH, file);
    }

    private void loadFile(String base_path, String file) {
        this.chart.clear();
        this.file = file;       // hackhackhack =(

        try {
            ArrayList<Student> studentData = loadStudents(base_path + file);
            System.out.println("Loaded " + studentData.size() + " students.");
            Student.fixDisplayNames(studentData);
            chart.addStudents(studentData);
            String partnerHistoryFileName = file.substring(0, file.indexOf(".")) + "-partnerHistories.csv";
            chart.loadPartnerHistoryFromFile(BASE_PATH + partnerHistoryFileName);
            String baseFileName = file.substring(0, file.indexOf("."));
            String nextNum = SeatingChart.getNumForNextSequentialFilename(BASE_PATH, baseFileName);
            this.maxChartIndex = Integer.parseInt(nextNum) - 1;
            System.out.println("Max chart index is: " + maxChartIndex);
        } catch (IOException e) {
            System.err.println("Couldn't read the file: " + file);
            maxChartIndex = -1;
        }

        numColumns = (int) (chart.getStudents().size() / numNamesPerCol) + 1;
        columnWidth = (width - 2 * LEFT_BUFF) / numColumns;

        chart.assignRandomly();
        displayList = makeDisplayListFor(chart);
        currentScore = chart.getPenalty();
        currentChartIndex = -1;
    }

    private ArrayList<DisplayBox> makeDisplayListFor(SeatingChart chart) {
        if (displayMode == LIST_LAYOUT) {
            return makeListDisplayFor(chart);
        } else if (displayMode == ROOM_LAYOUT) {
            return makeRm72DisplayChartRowsFor(chart);
        }
        System.err.println("Set DISPLAY_MODE to LIST or ROOM_LAYOUT");
        return null;
    }

    private ArrayList<DisplayBox> makeListDisplayFor(SeatingChart chart) {
        ArrayList<DisplayBox> out = new ArrayList<>();
        int row = 0;
        int col = 0;

        for (Group desk : chart.getGroups()) {
            DisplayBox box = new DisplayBox(LEFT_BUFF + col * columnWidth, TOP_BUFF + (int) (row * boxHeight), this.columnWidth, (int) boxHeight, 1, desk.size(), desk);
            //box.setWidthFromContents(this);
            out.add(box);

            row++;
            if (row >= numNamesPerCol) {
                row = 0;
                col++;
            }
        }

        return out;
    }

    private ArrayList<DisplayBox> makeRm72DisplayChartRowsFor(SeatingChart chart) {
        ArrayList<DisplayBox> out = new ArrayList<>();
        int BOX_WIDTH = 350;
        int BOX_HEIGHT = 100;
        int X_SKIP = 400;
        int Y_SKIP = 200;
        int X_START = 25;
        int Y_START = 50;

        int nextGroup = 0;

        for (int col = 2; col >= 0; col--) {
            for (int row = 3; row >= 0; row--) {
                if (nextGroup >= chart.getGroups().size()) return out;
                Group group = chart.getGroups().get(nextGroup);
                nextGroup++;

                DisplayBox box = new DisplayBox(X_START + col * (BOX_WIDTH + 30), Y_START + row * (BOX_HEIGHT + 80), BOX_WIDTH, BOX_HEIGHT, 1, group.size(), group);
                out.add(box);
            }
        }

        return out;
    }

    private ArrayList<Student> loadStudents(String filePath) throws IOException {
        ArrayList<Student> students = new ArrayList<>();
        String file = readFile(filePath);
        String[] lines = file.split("\n");

        for (String line : lines) {
            line = line.trim();
            try {
                Student s = Student.makeStudentFromRow(line);
                students.add(s);
            } catch (Exception e) {
                System.err.println("Error making student from line: " + line);
                System.err.println(e);
            }
        }

        return students;
    }

    public void draw() {
        background(255);

        int i = 1;
        for (DisplayBox box : displayList) {
            box.draw(this, isOn("display conflicts"), true, isOn("mirror"));
            if (displayMode == ROOM_LAYOUT && isOn("group numbers")) {
                box.drawGroupNumber(i, this, isOn("mirror"));
            }
            if (displayMode == ROOM_LAYOUT && isOn("exp level")) {
                box.drawExperienceLevel(this, isOn("mirror"));
            }
            i++;
        }
        textSize(TEXT_SIZE);

        if (currentSelectionIndex >= 0) {
            displayList.get(currentSelectionIndex).highlight(this, color(0, 255, 255));
        }

        if (displayMode == LIST_LAYOUT) {
            drawRowNumbers();
            drawColHeaders();
        }

        if (currentScore >= 0) {
            fill(0);
            stroke(0);
            textAlign(LEFT);
            text("Score: " + currentScore, 10, height - 40);
        }

        if (currentChartIndex >= 0) {
            fill(0);
            stroke(0);
            textAlign(RIGHT);
            text("Chart " + (currentChartIndex+1) + " of " + (maxChartIndex + 1), width / 2, height - 40);
        }

        // Display block number in upper left
        textAlign(LEFT);
        text(file.substring(0, file.indexOf(".")-5), 20, 30);

        if (isOn("mirror")) {
            textAlign(LEFT);
            textSize(10);
            text("printed", width - 200, height - 20);
        }
    }

    private boolean isOn(String switchName) {
        ToggleSwitch s = ControlWindow.switches.get(switchName);
        if (s == null) {
            System.err.println("no such setting: " + switchName);
            return false;
        }

        return s.isOn();
    }

    private void toggle(String switchName) {
        ToggleSwitch s = ControlWindow.switches.get(switchName);
        if (s == null) {
            System.err.println("no such setting: " + switchName);
            return;
        }

        s.toggle();
    }

    private void drawColHeaders() {
        fill(0);
        stroke(0);
        for (int i = 0; i < studentsPerGroup; i++) {
            text("seat " + (i + 1), LEFT_BUFF + i * (columnWidth / studentsPerGroup), 10);
        }
    }

    private void drawRowNumbers() {
        fill(0);
        stroke(0);
        for (int i = 1; i <= displayList.size(); i++) {
            text("" + i, 5, TOP_BUFF + (i - 1) * boxHeight);
        }
    }

    public void keyReleased() {
        if (keyCode == UP) {
            if (maxChartIndex < 0) return;
            currentChartIndex++;
            if (currentChartIndex > maxChartIndex) {
                currentChartIndex = maxChartIndex;
            }

            String basename = file.substring(0, file.indexOf("."));     // TODO: clean up many times we do this
            String nextChartName = basename + "-" + String.format("%02d", currentChartIndex) + ".csv";
            chart.loadSeatingChartFromFile(BASE_PATH + nextChartName);
            displayList = makeDisplayListFor(chart);
            currentScore = chart.getPenalty();                            // TODO: clean up having to remember to makeDisplayLIst For
            // make more helper methods to make this cleaner
        }

        if (keyCode == DOWN) {
            if (maxChartIndex < 0) return;
            currentChartIndex--;
            if (currentChartIndex < 0) {
                currentChartIndex = 0;
            }

            String basename = file.substring(0, file.indexOf("."));
            String nextChartName = basename + "-" + String.format("%02d", currentChartIndex) + ".csv";
            chart.loadSeatingChartFromFile(BASE_PATH + nextChartName);
            displayList = makeDisplayListFor(chart);
            currentScore = chart.getPenalty();
        }

        if (key == 'f' || key == 'F') {
            for (DisplayBox box : displayList) {
                if (box.isMouseOver(mouseX, mouseY)) {
                    box.toggleFreezeNameFor(mouseX, mouseY);
                }
            }
        }

        if (key == 'p' || key == 'P') {
            toggle("mirror");
        }

        if (key == 'c' || key == 'C') {
            toggle("display conflicts");
            System.out.println("Display conflicts: " + isOn("display conflicts"));
        }

        if (key == 'm' || key == 'M') {
            displayMode = (displayMode == LIST_LAYOUT) ? ROOM_LAYOUT : LIST_LAYOUT;
            displayList = makeDisplayListFor(chart);
        }

        if (key == 'e' || key == 'E') {
            toggle("exp level");
        }
        if (key == 'j' || key == 'J') {
            long millisStart = System.currentTimeMillis();
            chart.assignStudentsEfficiently();
            displayList = makeDisplayListFor(chart);
            currentScore = chart.getPenalty();
            currentChartIndex = -1;
            if(isOn("show sorting time")) {
                System.out.println("time to sort:" + (System.currentTimeMillis() - millisStart));
            }
        }
        if (key == 'r' || key == 'R') {
            reshuffle();
            currentScore = chart.getPenalty();
            currentChartIndex = -1;
        }
        if (key == 'h' || key == 'H') {
            toggle("hide errors");
            chart.setDisplayErrors(isOn("hide errors"));
        }
        if (key == 'n' || key == 'N') {
            toggle("group numbers");
        }

        if (key == 'o' || key == 'O') {
            long millisStart = System.currentTimeMillis();
            SeatingChart best = randomSearchForBestChart(NUM_TO_SEARCH);
            chart = best;
            displayList = makeDisplayListFor(chart);
            currentScore = chart.getPenalty();
            currentChartIndex = -1;
            if(isOn("show sorting time")) {
                System.out.println((System.currentTimeMillis() - millisStart));
            }
        }

        if (key == 'u' || key == 'U') {     // USE
            String baseFileName = file.substring(0, file.indexOf("."));
            chart.save(BASE_PATH, baseFileName);
            currentChartIndex = maxChartIndex;
            maxChartIndex++;
            System.out.println("Max chart index now: "+ maxChartIndex);
            currentSelectionIndex++;
        }

        if (key == '2') {
            loadFile(BASE_PATH, "block2-2024.csv");
        }
        if (key == '3') {
            loadFile(BASE_PATH, "block3-2024.csv");
        }
        if (key == '4') {
            loadFile(BASE_PATH, "block4-2024.csv");
        }
        if (key == '6') {
            loadFile(BASE_PATH, "block6-2024.csv");
        }
        if (key == '7') {
            loadFile(BASE_PATH, "block7-2024.csv");
        }
    }


    public void mousePressed() {
        draggingSeat = getSeatAt(mouseX, mouseY, displayList);
    }

    private int[] getSeatAt(int mouseX, int mouseY, ArrayList<DisplayBox> displayList) {
        for (int i = 0; i < displayList.size(); i++) {
            DisplayBox desk = displayList.get(i);

            if (desk.isMouseOver(mouseX, mouseY)) {
                int[] indicies = desk.getNameBoxIndicies(mouseX, mouseY);
                return new int[] {i, indicies[0], indicies[1]};
            }
        }

        return null;
    }

    private SeatingChart randomSwapHighestViolator(int numToSearch, int numHighestToSwap) {
        System.out.println("Finding a good chart.  Searching " + numToSearch + " possibilities.");
        double minScore = Double.MAX_VALUE;
        SeatingChart bestChart = null;

        for (int i = 0; i < numToSearch; i++) {
            chart.reAssignWorstOffenders(numHighestToSwap);
            double score = chart.getPenalty();
            if (score < minScore) {
                minScore = score;
                System.out.println("On chart " + i + " found new best with score " + minScore);
                bestChart = new SeatingChart(chart);
                if (minScore == 0) return bestChart;
            }
        }

        System.out.println("Done.  Best chart has score: " + bestChart.getPenalty());
        return bestChart;
    }

    private SeatingChart randomSearchForBestChart(int numToSearch) {
        System.out.println("Finding a good chart.  Searching " + numToSearch + " possibilities.");
        double minScore = Double.MAX_VALUE;
        SeatingChart bestChart = null;

        for (int i = 0; i < numToSearch; i++) {
            chart.assignRandomly();
            double score = chart.getPenalty();
            if (score < minScore) {
                minScore = score;
                System.out.println("On chart " + i + " found new best with score " + minScore);
                bestChart = new SeatingChart(chart);
                if (minScore == 0) return bestChart;
            }

        }

        System.out.println("Done.  Best chart has score: " + bestChart.getPenalty());
        return bestChart;
    }

    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            for (DisplayBox box : displayList) {
                box.handleMouseClick(mouseX, mouseY, this);
            }
            displayList = makeDisplayListFor(chart);
        }

        if (mouseButton == LEFT) {
            if (draggingSeat != null) {
                int[] targetSeat = getSeatAt(mouseX, mouseY, displayList);

                if (targetSeat != null) {
                    DisplayBox.swapStudents(draggingSeat, targetSeat, displayList);
                }

                draggingSeat = null;
            }
        }
    }

    private int getClickedBox(int mouseX, int mouseY) {
        for (int i = 0; i < displayList.size(); i++) {
            DisplayBox box = displayList.get(i);
            if (box.isMouseOver(mouseX, mouseY)) return i;
        }
        return -1;
    }

    private void reshuffle() {
        chart.assignRandomly();
        displayList = makeDisplayListFor(chart);
    }

/*    private ArrayList<Student> getNamesFrom(ArrayList<DisplayBox> students) {
        ArrayList<Student> currentStudents = new ArrayList<>();
        for ( DisplayBox box : students) {
            Student s1 = box.getStudent1();
            Student s2 = box.getStudent2();
            if (s1 != null) currentStudents.add(s1);
            if (s2 != null) currentStudents.add(s2);
        }
        return currentStudents;
    }*/

    public static String readFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public static void main(String[] args) {
        PApplet.main("Main");
    }
}
