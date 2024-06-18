import processing.core.PApplet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/*
TODO: adjust font smaller automatically (or clip names) ?

 */

public class Main extends PApplet {
    private static final float TEXT_SIZE = 32;
    private static final int TOP_BUFF = 80;
    private static final int LEFT_BUFF = 80;
    private static final int LIST_DISPLAY = 0;

    private int studentsPerGroup = 3;

    SeatingChart chart = new SeatingChart(studentsPerGroup);
    ArrayList<DisplayBox> displayList = new ArrayList<DisplayBox>();
    float verticalBuffer = 10;
    float textHeight, boxHeight;
    int numNamesPerCol, numColumns;
    int columnWidth;

    int displayMode = LIST_DISPLAY;
    private String file = "DataFiles/block1.csv";


    public void settings() {
        size(1000, 1000);
    }

    public void setup() {
        textSize(TEXT_SIZE);
        float strAscent = textAscent();
        float strDescent = textDescent();
        textHeight = strAscent + strDescent;
        boxHeight = textHeight + verticalBuffer;
        numNamesPerCol = (int) (studentsPerGroup*(height - TOP_BUFF) / boxHeight);

        try {
            ArrayList<Student> studentData = loadStudents(file);
            System.out.println(studentData);

            chart.addStudents(studentData);
        } catch (IOException e) {
            System.err.println("Couldn't read the file: " + file);
        }

        numColumns = (int) (chart.getStudents().size() / numNamesPerCol) + 1;
        columnWidth = (width-LEFT_BUFF) / numColumns;

        chart.assignRandomly();
        displayList = makeDisplayListFor(chart);
    }

    private ArrayList<DisplayBox> makeDisplayListFor(SeatingChart chart) {
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
            }
        }

        return students;
    }

    public void draw() {
        background(255);

        drawRowNumbers();
        drawColHeaders();

        for (DisplayBox box : displayList) {
            box.draw(this);

            if (box.isMouseOver(mouseX, mouseY)) {
                box.highlight(this);
            }
        }
    }

    private void drawColHeaders() {
        for (int i = 0; i < studentsPerGroup; i++) {
            text("seat " + (i+1), LEFT_BUFF + i*(columnWidth/studentsPerGroup), 10);
        }
    }

    private void drawRowNumbers() {
        for (int i = 1; i <= displayList.size(); i++) {
            text(""+i, 5,TOP_BUFF + (i-1)*boxHeight);
        }
    }

    public void keyReleased() {
        if (key == 'r' || key == 'R') {
            reshuffle();
        }
    }

    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            for (DisplayBox box : displayList) {
                box.handleMouseClick(mouseX, mouseY, this);
            }
            displayList = makeDisplayListFor(chart);
        }
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
