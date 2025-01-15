package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.models.Course;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter your moodle mobile web service key.");
        String token = sc.next();
        int user_id = getUserId(token);

        ArrayList<Course> courses = getUsersAllCourses(token, user_id);

        for (Course course : courses) {
            System.out.println(course);
        }
    }

    public static int getUserId(String token) {
        String url = "https://moodle.astanait.edu.kz//webservice/rest/server.php?" +
                "wstoken=" + token +
                "&moodlewsrestformat=" + "json" +
                "&wsfunction=" + "core_webservice_get_site_info";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status code: " + response.statusCode());
            System.out.println("Body code: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = response.body();
            JsonNode jsonNode = mapper.readTree(jsonResponse);

            return jsonNode.get("userid").asInt();

        } catch(Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static ArrayList<Course> getUsersAllCourses(String token, int user_id) {
        ArrayList<Course> courses = new ArrayList<>();

        String url = "https://moodle.astanait.edu.kz//webservice/rest/server.php?" +
                "wstoken=" + token +
                "&moodlewsrestformat=" + "json" +
                "&wsfunction=" + "core_enrol_get_users_courses" +
                "&userid=" + user_id;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status code: " + response.statusCode());
            System.out.println("Body code: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = response.body();

            JsonNode jsonNode = mapper.readTree(jsonResponse);
            if (jsonNode != null && jsonNode.isArray()) {
                for (JsonNode jsonPart : jsonNode) {
                    int course_id = jsonPart.get("id").asInt();
                    String name = jsonPart.get("fullname").asText();
                    double attendance = getAttendance(token, user_id, course_id);

                    courses.add(new Course(course_id, name, attendance));
                }
            }

            return courses;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static double getAttendance(String token, int user_id, int course_id) {
        String url = "https://moodle.astanait.edu.kz//webservice/rest/server.php?" +
                "wstoken=" + token +
                "&moodlewsrestformat=" + "json" +
                "&wsfunction=" + "gradereport_user_get_grade_items" +
                "&courseid=" + course_id +
                "&userid=" + user_id;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status code: " + response.statusCode());
            System.out.println("Body code: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = response.body();

            JsonNode jsonNode = mapper.readTree(jsonResponse);
            if (jsonNode.has("usergrades") && jsonNode.get("usergrades").isArray()) {
                for (JsonNode userGrades : jsonNode.get("usergrades")) {
                    if (userGrades.has("gradeitems") && userGrades.get("gradeitems").isArray()) {
                        for (JsonNode gradeItems : userGrades.get("gradeitems")) {
                            if ("Attendance".equals(gradeItems.get("itemname").asText())) {
                                String attendance = gradeItems.get("gradeformatted").asText();
                                if (attendance != null && !attendance.isEmpty()) {

                                    return Double.parseDouble(attendance);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}