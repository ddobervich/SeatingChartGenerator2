import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SeatingChart {
    private ArrayList<DeskPair> desks;
    private ArrayList<Student> allStudents, needSeats;

    public SeatingChart() {
        allStudents = new ArrayList<>();
        needSeats = new ArrayList<>();
        desks = new ArrayList<>();
    }

    public SeatingChart(ArrayList<Student> students) {
        this.allStudents.addAll(students);
        needSeats = new ArrayList<>();

        for (int i = 0; i < this.allStudents.size() / 2 + 1; i++) {
            desks.add(new DeskPair(null, null, this));
        }
    }

    public SeatingChart(SeatingChart toCopy) {
        allStudents = new ArrayList<>();
        desks = new ArrayList<>();
        needSeats = new ArrayList<>();
        allStudents.addAll(toCopy.allStudents);
        needSeats.addAll(toCopy.needSeats);
        for (DeskPair desk : toCopy.desks) {
            DeskPair copy = new DeskPair(desk);
            copy.setChart(this);
            this.desks.add(copy);
        }
    }

    public ArrayList<Student> getAllStudents() {
        return this.allStudents;
    }

    public ArrayList<DeskPair> getDesks() {
        return this.desks;
    }

    public void seatStudent(Student s) {
        if (!allStudents.contains(s)) allStudents.add(s);
        if (needSeats.contains(s)) needSeats.remove(s);

        if (desks.size() * 2 < allStudents.size()) {   // we need to add a desk
            desks.add(new DeskPair(s, null, this));
        } else {
            DeskPair desk = findDeskWithSpace();
            if (desk != null) {
                desk.setEmptySeatTo(s);
            } else {
                desks.add(new DeskPair(s, null, this));
            }
        }
    }

    public void unseatStudent(Student s) {
        if (s == null) return;
        DeskPair desk = getStudentsDesk(s);
        if (desk == null) return;

        desk.removeStudent(s);
        this.needSeats.add(s);
        // BUT we won't remove the desk since we'll re-seat someone
        // and student is still in allStudents list.
    }

    public DeskPair getStudentsDesk(Student s) {
        if (s == null) return null;

        for (DeskPair desk : desks) {
            if (desk.hasStudent(s)) {
               return desk;
            }
        }
        return null;
    }

    public void deleteStudent(Student s) {
        DeskPair toRemove = null;
        allStudents.remove(s);

        DeskPair desk = getStudentsDesk(s);
        if (desk == null) return;

        desk.removeStudent(s);
        if (desk.isEmpty()) {
            desks.remove(desk);
        }
    }

    private DeskPair findDeskWithSpace() {
        for (DeskPair desk : desks) {
            if (desk.hasSpace()) return desk;
        }
        return null;
    }

    public void reAssignRandomly() {
        unseatAllStudents(false);
        randomlyAssignAllUnseated();
    }

    public void randomlyAssignAllUnseated() {
        Collections.shuffle(needSeats);

        // TODO: ensure there are enough desks before doing this
        for (Student s : needSeats) {
            DeskPair d = findDeskWithSpace();
            d.setEmptySeatTo(s);
        }

        needSeats.clear();
    }

    public void unseatAllStudents(boolean unseatFrozen) {
        for (DeskPair desk : desks) {
            if (unseatFrozen || !desk.isRightFrozen()) {
                Student right = desk.removeRight();
                needSeats.add(right);
            }
            if (unseatFrozen || !desk.isLeftFrozen()) {
                Student left = desk.removeLeft();
                needSeats.add(left);
            }
        }
    }

    public void forceReassignAllRandomly() {
        Collections.shuffle(this.allStudents);
        needSeats.clear();

        for (int i = 0; i < allStudents.size(); i += 2) {
            Student s1 = allStudents.get(i);
            Student s2 = (i + 1 < allStudents.size()) ? allStudents.get(i + 1) : null;

            DeskPair desk = desks.get(i / 2);
            desk.clear();
            desk.setLeft(s1);
            desk.setRight(s2);
        }
    }

    public void seatStudents(ArrayList<Student> studentData) {
        for (Student toAdd : studentData) {
            seatStudent(toAdd);
        }
    }

    // If more than 1 desk with 1 person missing, we'll combine folks who don't have partners
    public void consolodate() {
        ArrayList<DeskPair> desksWithOne = new ArrayList<>();
        ArrayList<DeskPair> emptyDesks = new ArrayList<>();
        for (DeskPair desk : desks) {
            if (desk.hasSpace()) {
                if (!desk.isEmpty()) {
                    desksWithOne.add(desk);
                } else {
                    emptyDesks.add(desk);
                }
            }
        }

        if (desksWithOne.size() <= 1) return;

        for (int i = 0; i < desksWithOne.size(); i += 2) {
            DeskPair d1 = desksWithOne.get(i);
            if (i+1 >= desksWithOne.size()) break;
            DeskPair d2 = desksWithOne.get(i+1);

            // move single student from d2 to d1.  Add d2 to empty desks list.
            Student s = (d2.getRight() == null ? d2.removeLeft() : d2.removeRight());
            d1.seat(s);
            emptyDesks.add(d2);
        }

        // remove all empty desks.  TODO:  do I really want to do this here??
        for (int i = 0; i < desks.size(); i++) {
            DeskPair desk = desks.get(i);
            if (desk.isEmpty()) {
                desks.remove(desk);
                i--;
            }
        }
    }

    /***
     * Return the penalty for this seating chart.
     * @return
     */
    public double getScore() {
        double penalty = 0;
        for (DeskPair desk : desks) {
            penalty += desk.getPenalty();
        }
        return penalty;
    }

    /***
     * Genereate mean, median, min and max penalties for
     * @param student
     * @return array containing [ min, max, mean, median, standard devation ].
     */
    public void assignPenaltyStatsTo(Student student) {
        List<Double> penalties = getPenaltyListFor(student);

        double minimum = Collections.min(penalties);
        double maximum = Collections.max(penalties);

        double sum = 0;
        for (double value : penalties) {
            sum += value;
        }
        double mean = sum / penalties.size();

        Collections.sort(penalties);
        int size = penalties.size();
        double median;
        if (size % 2 == 0) {
            int middle = size / 2;
            double median1 = penalties.get(middle - 1);
            double median2 = penalties.get(middle);
            median = (median1 + median2) / 2;
        } else {
            median = penalties.get(size / 2);
        }

        double sumOfSquaredDifferences = 0;
        for (double value : penalties) {
            double diff = value - mean;
            sumOfSquaredDifferences += diff * diff;
        }
        double variance = sumOfSquaredDifferences / penalties.size();
        double standardDeviation = Math.sqrt(variance);

        student.setMax(maximum);
        student.setMin(minimum);
        student.setMean(mean);
        student.setMedian(median);
        student.setStdev(standardDeviation);
    }

    private List<Double> getPenaltyListFor(Student student) {
        List<Double> data = new ArrayList<>();
        for (Student s : this.getAllStudents()) {
            if (!s.equals(student)) {
                data.add(student.getMatchScoreFor(s));
            }
        }
        return data;
    }

    public void calculatePenaltyDistributions() {
        System.out.println("Running calculatePenaltyDistriutions()");
        for (Student s : getAllStudents()) {
            assignPenaltyStatsTo(s);
        }
    }

    public void printStatsForMostAndLeast() {
        System.out.println("Must run calculatePenaltyDistributions() first!");
        Collections.sort(this.allStudents, Comparator.comparingDouble(Student::getMin));
        Student least = allStudents.get(0);
        Student most = allStudents.get(allStudents.size() - 1);

        System.out.println(least.getDisplayName() + ": min" + least.getMin() + " median: " + least.getMedian() + " max: " + least.getMax());
        System.out.println(most.getDisplayName() + ": min" + most.getMin() + " median: " + most.getMedian() + " max: " + most.getMax());
    }

    public void saveChartToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            String headers = "desk id, left student id, left student fn, left student ln, right student id, right student fn, right student ln";
            writer.write(headers);
            writer.newLine();

            for (DeskPair desk : this.desks) {
                Student left = desk.getLeft();
                Student right = desk.getRight();

                String row = desk.getId() + ", ";
                if (left != null) {
                    row +=  + left.getId() + ", " + left.getFn() + ", " + left.getLn() + ", ";

                } else {
                    row += ",,,";
                }

                if (right != null) {
                    row += right.getId() + ", " + right.getFn() + ", " + right.getLn();
                } else {
                    row += ",,,";
                }

                writer.write(row);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Student> loadStudents(String filePath) throws IOException {
        ArrayList<Student> students = new ArrayList<>();
        String file = readFile(filePath);
        String[] lines = file.split("\n");

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
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

    public static SeatingChart createChartFromFile(String chartFilePath, String studentsFilePath) {
        SeatingChart chart = new SeatingChart();

        try {
            ArrayList<Student> students = loadStudents(studentsFilePath);
            chart.allStudents = students;  // directly set the student list
        } catch (Exception e) {
            System.out.println("Couldn't read file " + studentsFilePath);
        }

        try {
            String raw = readFile(chartFilePath);
            String[] rows = raw.split("\n");
            for (int i = 1; i < rows.length; i++) {
                String row = rows[i];
                String[] vals = row.split(",");
                int deskId = Integer.parseInt( vals[0].trim() );
                int leftStudentId = Integer.parseInt( vals[1].trim() );
                int rightStudentId = Integer.parseInt( vals[4].trim() );
                Student left = chart.getStudentById(leftStudentId);
                Student right = chart.getStudentById(rightStudentId);
                DeskPair desk = new DeskPair(left, right);
                desk.setId(deskId);
                chart.desks.add( desk );
            }
        } catch (IOException e) {
            System.out.println("Couldn't read file " + chartFilePath);
        }

        return chart;
    }

    private Student getStudentById(int id) {
        for (Student s : this.allStudents) {
            if (s.getId() == id) return s;
        }

        System.err.println("Couldn't find student with id: " + id);
        return null;
    }

    public static String readFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public String toString() {
        return "Score: " + this.getScore() + " : " + this.desks;
    }
}