import java.util.ArrayList;
import java.util.HashMap;

public class Student {
    private static final double LARGE_EXP_DIFF_THRESHOLD = 2;
    private static final double WORK_WITH_LESS_PENALTY = 1;
    private static final double WORK_WITH_SAME_PENALTY = 0.5;
    private static final double WORK_WITH_MORE_PENALTY = 0.3;
    private static final double LARGE_EXP_DIFF_PENALTY = 2;
    private static final double SOLO_DIFF_PENALTY = 0.3;
    private static final double COLLAB_DIFF_PENALTY = 0.3;

    private int id;
    private String fn, ln, displayName;
    private String gender;          // 'm', 'f', 'n'
    private String wantsGender;     // 'm', blank means any, could also be 'nf'  for n or f
    private double experienceLevel;
    private boolean wantsSame, wantsMore, wantsLess;
    private boolean likesSolo, likeCollab;
    private HashMap<String, Integer> partnerHistory;

    public Student(int id, String fn, String ln, String displayName, double experienceLevel, boolean same, boolean more, boolean less, boolean solo, boolean collab, String gender, String wantsGender) {
        this.wantsLess = less;
        this.wantsMore = more;
        this.wantsSame = same;
        this.likesSolo = solo;
        this.likeCollab = collab;
        this.id = id;
        this.fn = fn;
        this.ln = ln;
        this.displayName = displayName;
        this.gender = gender;
        this.wantsGender = wantsGender;
        this.experienceLevel = experienceLevel;
        this.partnerHistory = new HashMap<>();
    }

    public static Student makeStudentFromRow(String line) throws Exception {
        String[] vals = line.split(",");
        int id = Integer.parseInt(vals[0].trim());
        String fn = vals[1].trim();
        String ln = vals[2].trim();

/*        double exp = averageGrades(vals, 3, 8);
        boolean same = Boolean.parseBoolean(vals[9]);
        boolean more = Boolean.parseBoolean(vals[10]);
        boolean less = Boolean.parseBoolean(vals[11]);
        boolean solo = Boolean.parseBoolean(vals[12]);
        boolean collab = Boolean.parseBoolean(vals[13]);*/
        String gender = vals[3];
        double exp = Double.parseDouble(vals[4]);
        String wantsGender = "";
        if (vals.length > 10) {      // REMEMBER: if it's the 10th col, vals.length > 9, then access vals[9]
            wantsGender = vals[10];
        }
        Student s = new Student(id, fn, ln, fn, exp, false, true, true, true, true, gender, wantsGender);
        return s;
    }
    
    public Student clone() {
        Student s = new Student(id, fn, ln, fn, experienceLevel, false, true, true, true, true, gender, wantsGender);
        return s;
    }

    private static double averageGrades(String[] vals, int firstIndex, int lastIndex) {
        double sum = 0;
        for (int i = firstIndex; i <= lastIndex; i++) {
            sum += getValFor(vals[i]);
        }

        return (sum / (lastIndex - firstIndex + 1));
    }

    private static double getValFor(String grade) {
        grade = grade.trim().toLowerCase();
        if (grade.equals('a')) return 4;
        if (grade.equals('b')) return 3;
        if (grade.equals('c')) return 2;
        if (grade.equals('d')) return 1;
        if (grade.equals('f')) return 0;
        try {
            int score = Integer.parseInt(grade.trim());
            return score;
        } catch (Exception e) {
            return 0;
        }
    }

    /***
     * if there are multiple students with the same first name, modify their display names to include last initial
     * @param studentData
     */
    public static void fixDisplayNames(ArrayList<Student> studentData) {
        for (int i = 0; i < studentData.size(); i++) {
            for (int j = i+1; j < studentData.size(); j++) {
                Student s1 = studentData.get(i);
                Student s2 = studentData.get(j);
                if (s1.getDisplayName().equals(s2.getDisplayName())) {
                    s1.setDisplayName(s1.getFn() + " " + s1.getLn().substring(0,1));
                    s2.setDisplayName(s2.getFn() + " " + s2.getLn().substring(0,1));
                }
            }
        }
    }

    public String getId() {
        return "" + id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public String getLn() {
        return ln;
    }

    public void setLn(String ln) {
        this.ln = ln;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(double experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    @Override
    public boolean equals(Object obj) {
        Student other = (Student) obj;
        return other.getId().equals(this.getId());
    }

    public String toString() {
        return displayName + ": " + this.experienceLevel;
    }

    public double matchScoreWith(Student other) {
        double penalty = 0;
        double experienceCompare = this.compareExperience(other);
        if (experienceCompare > 0 && !this.wantsLess) penalty += WORK_WITH_LESS_PENALTY;
        if (experienceCompare == 0 && !this.wantsSame) penalty += WORK_WITH_SAME_PENALTY;
        if (experienceCompare < 0 && !this.wantsMore) penalty += WORK_WITH_MORE_PENALTY;
        if (this.largeExperienceDifference(other)) penalty += LARGE_EXP_DIFF_PENALTY;
        if (this.likesSolo != other.likesSolo) penalty += SOLO_DIFF_PENALTY / 2.0; // because it will be double counted
        if (this.likeCollab != other.likeCollab) penalty += COLLAB_DIFF_PENALTY / 2.0;
        return penalty;
    }

    public int timesSatWith(String id) {
        return (partnerHistory.containsKey(id) ? partnerHistory.get(id) : 0);
    }

    private boolean largeExperienceDifference(Student other) {
        return Math.abs(compareExperience(other)) > LARGE_EXP_DIFF_THRESHOLD;
    }

    private double compareExperience(Student other) {
        return this.getExperienceLevel() - other.getExperienceLevel();
    }

    public void recordSittingWith(Student seat) {
        String id = "" + seat.getId();
        if (!partnerHistory.containsKey(id)) {
            partnerHistory.put(id, 1);
        } else {
            int num = partnerHistory.get(id);
            partnerHistory.put(id, num + 1);
        }
    }

    public HashMap<String, Integer> getPartnerHistory() {
        return this.partnerHistory;
    }

    public void clearPartnerHistory() {
        this.partnerHistory.clear();
    }

    public void setPartnerHistoryFor(String idStr, int num) {
        this.partnerHistory.put(idStr, num);
    }

    /***
     * determine if sitting with Student other would be an affinity group violation
     * @param other other student
     * @return 0 for no violation, 1 for violation (to facilitate calculation when called)
     */
    public int isAffinityViolation(Student other) {
        if (this.wantsGender.length() == 0) return 0;
        return (wantsGender.contains(other.getGender()))?0:1;
    }
}
