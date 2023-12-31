public class Student {
    private static final double LARGE_EXP_DIFF_THRESHOLD = 1.8;
    private static final double WORK_WITH_LESS_PENALTY = 1;
    private static final double WORK_WITH_SAME_PENALTY = 0.5;
    private static final double WORK_WITH_MORE_PENALTY = 0.3;
    private static final double LARGE_EXP_DIFF_PENALTY = 10;
    private static final double SOLO_DIFF_PENALTY = 0.3;
    private static final double COLLAB_DIFF_PENALTY = 0.3;

    private int id;
    private String fn, ln, displayName;
    private int gender;
    private double experienceLevel;
    private boolean wantsSame, wantsMore, wantsLess;
    private boolean likesSolo, likeCollab;

    private double min, max, mean, median, stdev;

    public Student(int id, String fn, String ln, String displayName, int gender, double experienceLevel, boolean same, boolean more, boolean less, boolean solo, boolean collab) {
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
        this.experienceLevel = experienceLevel;
    }

    public static Student makeStudentFromRow(String line) throws Exception {
        String[] vals = line.split(",");
        int id = Integer.parseInt(vals[0].trim());
        String fn = vals[1].trim();
        String ln = vals[2].trim();

        //double exp = averageGrades(vals, 3, 8);
        double exp = Double.parseDouble(vals[9]);
        boolean same = Boolean.parseBoolean(vals[10]);
        boolean more = Boolean.parseBoolean(vals[11]);
        boolean less = Boolean.parseBoolean(vals[12]);
        boolean solo = Boolean.parseBoolean(vals[13]);
        boolean collab = Boolean.parseBoolean(vals[14]);

        System.out.println("Making student obj for " + fn + " " + ln);
        Student s = new Student(id, fn, ln, fn + " " + ln.substring(0,1) + ".", 0, exp, same, more, less, solo, collab);
        return s;
    }

    private static double averageGrades(String[] vals, int firstIndex, int lastIndex) {
        double sum = 0;

        for (int i = firstIndex; i <= lastIndex; i++) {
            sum += getValFor(vals[i]);
        }

        return 10*(sum/((lastIndex-firstIndex+1)*4));
    }

    private static double getValFor(String grade) {
        grade = grade.trim().toLowerCase();
        if (grade.equals("a")) return 4;
        if (grade.equals("b")) return 3;
        if (grade.equals("c")) return 2;
        if (grade.equals("d")) return 1;
        if (grade.equals("f")) return 0;
        return 0;
    }

    public int getId() {
        return id;
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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
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
        Student other = (Student)obj;
        return other.getId() == this.getId();
    }

    public String toString() {
        return displayName + ": " + this.experienceLevel;
    }

    public double getMatchScoreFor(Student other) {
        double penalty = 0;
        if (other == null) return 0;

        double experienceCompare = this.compareExperience(other);
        if (experienceCompare > 0 && !this.wantsLess) penalty += WORK_WITH_LESS_PENALTY;
        if (experienceCompare == 0 && !this.wantsSame) penalty += WORK_WITH_SAME_PENALTY;
        if (experienceCompare < 0 && !this.wantsMore) penalty += WORK_WITH_MORE_PENALTY;
        if (this.largeExperienceDifference(other)) penalty += LARGE_EXP_DIFF_PENALTY;
        if (this.likesSolo != other.likesSolo) penalty += SOLO_DIFF_PENALTY/2.0; // because it will be double counted
        if (this.likeCollab != other.likeCollab) penalty += COLLAB_DIFF_PENALTY/2.0;
        return penalty;
    }

    private boolean largeExperienceDifference(Student other) {
        return Math.abs(compareExperience(other)) > LARGE_EXP_DIFF_THRESHOLD;
    }

    private double compareExperience(Student other) {
        return this.getExperienceLevel() - other.getExperienceLevel();
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getStdev() {
        return stdev;
    }

    public void setStdev(double stdev) {
        this.stdev = stdev;
    }
}
