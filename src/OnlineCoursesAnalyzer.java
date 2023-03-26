import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class OnlineCoursesAnalyzer {
  List<Course> courses = new ArrayList<>();

  public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    try {
      br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                Double.parseDouble(info[14]),
                Double.parseDouble(info[15]), Double.parseDouble(info[16]),
                Double.parseDouble(info[17]),
                Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                Double.parseDouble(info[20]),
                Double.parseDouble(info[21]), Double.parseDouble(info[22]));
        courses.add(course);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  //1

  public Map<String, Integer> getPtcpCountByInst() {
    Map<String, Integer> result = courses.stream().collect(Collectors.toMap(
            Course::getInstitution,
            Course::getParticipants,
            Integer::sum
    ));

    return result.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new
            ));
  }

  //2
  public Map<String, Integer> getPtcpCountByInstAndSubject() {
    Map<String, Integer> result = courses.stream()
            .collect(Collectors.groupingBy(
                    c -> c.getInstitution() + "-" + c.getSubject(),
                    Collectors.summingInt(Course::getParticipants)
            ));

    return result.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                    .thenComparing(Map.Entry.comparingByKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                    (u, v) -> u, LinkedHashMap::new));
  }

  //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    Map<String, List<List<String>>> result = new HashMap<>();

    // group courses by instructor
    Map<String, List<Course>> coursesByInstructor = new HashMap<>();
    for (Course course : courses) {
      String[] instructors = course.getInstructor().split(",");
      for (String instructor : instructors) {
        instructor = instructor.trim();
        List<Course> courseListForInstructor =
                coursesByInstructor.computeIfAbsent(instructor, k -> new ArrayList<>());
        courseListForInstructor.add(course);
      }
    }

    // iterate through each instructor and group their courses by type
    for (String instructor : coursesByInstructor.keySet()) {
        List<Course> instructorCourses = coursesByInstructor.get(instructor);
        List<Course> independentCourses = new ArrayList<>();
        List<Course> coDevelopedCourses = new ArrayList<>();

        for (Course course : instructorCourses) {
            if (course.isIndependent()) {
                if (!independentCourses.contains(course)) {
                    independentCourses.add(course);
                }
            } else {
                if (!coDevelopedCourses.contains(course)) {
                    coDevelopedCourses.add(course);
                }
            }
        }

        // sort courses by title
        independentCourses.sort(Comparator.comparing(Course::getTitle));
        coDevelopedCourses.sort(Comparator.comparing(Course::getTitle));

        // create course lists and add to result map
        List<List<String>> courseLists = new ArrayList<>();
        List<String> independentCourseTitles = independentCourses.stream().map(Course::getTitle).collect(Collectors.toList());
        List<String> coDevelopedCourseTitles = coDevelopedCourses.stream().map(Course::getTitle).collect(Collectors.toList());
        //
        Set<String> setWithoutDuplicates1 = new LinkedHashSet<>(independentCourseTitles);
        List<String> listWithoutDuplicates1 = new ArrayList<>(setWithoutDuplicates1);

        Set<String> setWithoutDuplicates2 = new LinkedHashSet<>(coDevelopedCourseTitles);
        List<String> listWithoutDuplicates2 = new ArrayList<>(setWithoutDuplicates2);

        courseLists.add(listWithoutDuplicates1);
        courseLists.add(listWithoutDuplicates2);
        result.put(instructor, courseLists);
    }

    return result;
  }

    //4
    public List<String> getCourses(int topK, String by) {
        Comparator<Course> comparator;
        if (by.equals("hours")) {
            //
            comparator = Comparator.comparing(Course::getTotalHours).reversed()
                    .thenComparing(Comparator.comparing(Course::getTitle));
        } else if (by.equals("participants")) {
            //
            comparator = Comparator.comparing(Course::getParticipants).reversed()
                    .thenComparing(Comparator.comparing(Course::getTitle));
        } else {
            throw new IllegalArgumentException("Invalid sorting criteria: " + by);
        }
        //
        Collections.sort(courses, comparator);

        List<String> result = new ArrayList<>();
        Set<String> courseTitles = new HashSet<>(); //
        for (Course course : courses) {
            if (result.size() >= topK) {
                break; //
            }
            String courseTitle = course.getTitle();
            if (!courseTitles.contains(courseTitle)) {
                //
                result.add(courseTitle);
                courseTitles.add(courseTitle);
            }
        }
        return result;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> results = new ArrayList<>();

        // convert courseSubject to lowercase for case-insensitive comparison
        courseSubject = courseSubject.toLowerCase();

        Set<String> visitedTitles = new HashSet<>();

        // loop through each course
        for (Course course : courses) {
            // check if course subject matches (case-insensitive)
            if (course.getSubject().toLowerCase().contains(courseSubject)) {
                // check if percentAudited is met
                if (course.getPercentAudited() >= percentAudited) {
                    // check if totalCourseHours is met
                    if (course.getTotalHours() <= totalCourseHours) {
                        // check if course title has been visited before
                        if (!visitedTitles.contains(course.getTitle())) {
                            results.add(course.getTitle());
                            visitedTitles.add(course.getTitle());
                        }
                    }
                }
            }
        }

        // sort results alphabetically
        Collections.sort(results);

        return results;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        // Step 1: Calculate the averages of Median Age, % Male,
        // and % Bachelor's Degree or Higher for each course
        Map<String, Double[]> courseAverages = new HashMap<>();
//        Set<Course> noDup = new HashSet<>(courses);
//        List<Course> noDupCourses = new ArrayList<>(noDup);
        for (Course course : courses) {
            int count = 0;
            double totalMedianAge = 0;
            double totalPercentMale = 0;
            double totalPercentBachlor = 0;
            for (Course tmpcourse : courses) {
                if (course.getNumber().equals(tmpcourse.getNumber())) {
                    count++;
                    totalMedianAge += tmpcourse.getMedianAge();
                    totalPercentMale += tmpcourse.getPercentMale();
                    totalPercentBachlor += tmpcourse.getPercentDegree();
                }
            }
            Double[] courseAveragesArray = new Double[3];
            courseAveragesArray[0] = totalMedianAge / count; // Average Median Age
            courseAveragesArray[1] = totalPercentMale / count; // Average % Male
            courseAveragesArray[2] = totalPercentBachlor / count; // Average % Bachelor's Degree or Higher
            courseAverages.put(course.getNumber(), courseAveragesArray);
        }

        // Step 2: Calculate the similarity between the user and each course
        List<CourseSimilarity> similarities = new ArrayList<>();
        for (Course course : courses) {
            Double[] courseAveragesArray = courseAverages.get(course.getNumber());
            double similarityValue = Math.pow(age - courseAveragesArray[0], 2)
                    + Math.pow(gender * 100 - courseAveragesArray[1], 2)
                    + Math.pow(isBachelorOrHigher * 100 - courseAveragesArray[2], 2);
            similarities.add(new CourseSimilarity(course.getTitle(), course.getNumber(), similarityValue));
        }

        // Step 3: Sort the courses by similarity value and return the top 10
        Collections.sort(similarities);
        List<String> recommendedCourses = new ArrayList<>();
        Set<String> courseTitles = new HashSet<>();
        for (int i = 0; recommendedCourses.size() < 10 && i < similarities.size(); i++) {
            String courseNumber = similarities.get(i).getCourseNumber();
            String courseTitle = "";
            for (Course course : courses) {
                if (course.getNumber().equals(courseNumber) && !courseTitles.contains(course.getTitle())) {
                    courseTitle = course.getTitle();
                    courseTitles.add(courseTitle);
                }
            }
            if (!courseTitle.equals("")) {
                recommendedCourses.add(courseTitle);
            }
        }
        return recommendedCourses;
    }

    private static class CourseSimilarity implements Comparable<CourseSimilarity> {
        private final String title;
        private final String courseNumber;
        private final double similarityValue;

        public CourseSimilarity(String title, String courseNumber, double similarityValue) {
            this.title = title;
            this.courseNumber = courseNumber;
            this.similarityValue = similarityValue;
        }

        public String getTitle() {
            return title;
        }

        public String getCourseNumber() {
            return courseNumber;
        }

        public double getSimilarityValue() {
            return similarityValue;
        }

        @Override
        public int compareTo(CourseSimilarity other) {
            if (this.similarityValue < other.similarityValue) {
                return -1;
            } else if (this.similarityValue > other.similarityValue) {
                return 1;
            } else {
                return this.title.compareTo(other.title);
            }
        }
    }
}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }

    public String getInstitution() {
        return this.institution;
    }

    public boolean isIndependent() {
        String[] instructors = this.instructors.split(",");
        return instructors.length <= 1;
    }

    public String getInstructor() {
        return this.instructors;
    }

    public int getParticipants() {
        return this.participants;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSubject() {
        return this.subject;
    }

    public double getTotalHours() {
        return this.totalHours;
    }

    public double getPercentAudited() {
        return this.percentAudited;
    }

    public String getNumber() {
        return this.number;
    }

    public double getMedianAge() {
        return this.medianAge;
    }

    public double getPercentMale() {
        return this.percentMale;
    }

    public double getPercentDegree() {
        return this.percentDegree;
    }

    public double[] getStats() {
        double[] stats = new double[4];
        stats[0] = this.totalHours;
        stats[1] = this.participants;
        stats[2] = this.percentMale;
        stats[3] = this.medianAge;
        return stats;
    }
}