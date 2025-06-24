package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AssignmentSeeder {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AssignmentSeeder(AssignmentRepository assignmentRepository,
                            CourseRepository courseRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public void seedAssignments() {
        Role teacherRole = roleRepository.findByName("teacher").orElse(null);
        List<User> teachers = userRepository.findByUserRolesContaining(Collections.singleton(teacherRole));
        if (assignmentRepository.count() > 0) {
            System.out.println("Assignments already exist, skipping assignment seeding...");
            return;
        }

        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            System.out.println("No courses found, skipping assignment seeding...");
            return;
        }


        List<Assignment> assignmentsToSave = new ArrayList<>();
        int totalAssignments = 0;

        for (Course course : courses) {
            List<AssignmentData> assignmentDataList = getAssignmentsForCourse(course.getCode());

            for (int i = 0; i < assignmentDataList.size(); i++) {
                AssignmentData data = assignmentDataList.get(i);

                Assignment assignment = new Assignment();
                assignment.setTitle(data.getTitle());
                assignment.setDescription(data.getDescription());

                assignment.setDueDate(OffsetDateTime.now().plusDays(7 + (i * 7)));//weekly
                assignment.setTimeLimit(2 * 60 * 60 * 1000L);// 2 hours in ms
                assignment.setTotalPoints(BigDecimal.valueOf(100));
                assignment.setCreatedByTeacher(teachers.get(0));
                assignment.setCourse(course);

                assignment.setResource(data.resources);
                assignment.setStarterCode("public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Your application's starting point\n" +
                        "        System.out.println(\"Hello, Java Auto Grader!\");\n" +
                        "    }\n" +
                        "}");

                assignmentsToSave.add(assignment);
                totalAssignments++;
            }

            System.out.println(String.format("Created %d assignments for course: %s - %s",
                    assignmentDataList.size(),
                    course.getCode(),
                    course.getName()));
        }

        List<Assignment> savedAssignments = assignmentRepository.saveAll(assignmentsToSave);
        System.out.println("Successfully seeded " + savedAssignments.size() + " assignments across " + courses.size() + " courses");
    }


    private List<AssignmentData> getAssignmentsForCourse(String courseCode) {
        List<AssignmentData> assignments = new ArrayList<>();

        switch (courseCode) {
            // ... existing code ...

            case "JAVA101": // Java Basics: Syntax & Data
                assignments.add(new AssignmentData(
                        "Hello World & Basic Syntax",
                        "Learn the absolute basics of Java: writing your first program, understanding `main` methods, and simple output.",
                        "**Task:**\n" +
                                "*   Create a Java class named `HelloWorld`.\n" +
                                "*   Implement a `main` method within `HelloWorld`.\n" +
                                "*   Inside `main`, use `System.out.println()` to print the exact phrase: 'Hello, Java Learner!'\n" +
                                "*   Add at least one single-line comment and one multi-line comment to your code explaining parts of it.\n\n" +
                                "**Example Structure:**\n" +
                                "```java\n" +
                                "public class HelloWorld {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        // Your code here\n" +
                                "        /* Multi-line\n" +
                                "           comment */\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Variables and Primitive Data Types",
                        "Understand how to declare, initialize, and use Java's fundamental primitive data types and variables.",
                        "**Task:**\n" +
                                "*   Create a Java class named `DataTypeFun`.\n" +
                                "*   Inside its `main` method, declare and initialize the following variables:\n" +
                                "    *   An `int` variable named `age` with your age.\n" +
                                "    *   A `double` variable named `height` with your height in meters (e.g., 1.75).\n" +
                                "    *   A `boolean` variable named `isStudent` set to `true` or `false`.\n" +
                                "    *   A `char` variable named `initial` with the first letter of your name.\n" +
                                "    *   A `String` variable named `firstName` with your first name.\n" +
                                "*   Print the value of each variable to the console, clearly labeling what each value represents (e.g., \"Age: \" + age).\n\n" +
                                "**Example for one variable:**\n" +
                                "```java\n" +
                                "int age = 30;\n" +
                                "System.out.println(\"Age: \" + age);\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Basic Arithmetic Operations",
                        "Practice performing fundamental arithmetic operations in Java and understand operator precedence.",
                        "**Task:**\n" +
                                "*   Create a Java class named `SimpleMath`.\n" +
                                "*   Inside its `main` method, declare two `int` variables: `num1` initialized to `25` and `num2` initialized to `7`.\n" +
                                "*   Perform the following operations and print the result of each, clearly labeled:\n" +
                                "    *   Sum (`num1 + num2`)\n" +
                                "    *   Difference (`num1 - num2`)\n" +
                                "    *   Product (`num1 * num2`)\n" +
                                "    *   Quotient (`num1 / num2`)\n" +
                                "    *   Remainder / Modulo (`num1 % num2`)\n" +
                                "*   Observe and include a comment about the result of the integer division. For example, `25 / 7` will be `3`.\n\n" +
                                "**Example:**\n" +
                                "```java\n" +
                                "int num1 = 25;\n" +
                                "int num2 = 7;\n" +
                                "int sum = num1 + num2;\n" +
                                "System.out.println(\"Sum: \" + sum);\n" +
                                "// ... other operations\n" +
                                "```"
                ));
                break;

            case "JAVA102": // Java Basics: Control Flow
                assignments.add(new AssignmentData(
                        "Conditional Statements (If-Else)",
                        "Implement programs that make decisions based on various conditions using `if`, `else if`, and `else`.",
                        "**Task:**\n" +
                                "*   Create a `NumberChecker` class.\n" +
                                "*   In the `main` method, declare an `int` variable named `number` and set its value (e.g., `10`, `-5`, `0`).\n" +
                                "*   Use an `if-else if-else` structure to check the value of `number`:\n" +
                                "    *   If `number` is greater than 0, print 'The number is positive.'\n" +
                                "    *   If `number` is less than 0, print 'The number is negative.'\n" +
                                "    *   If `number` is equal to 0, print 'The number is zero.'\n" +
                                "*   Test your code by changing the initial value of `number` to positive, negative, and zero.\n\n" +
                                "**Example Structure:**\n" +
                                "```java\n" +
                                "public class NumberChecker {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        int number = 10;\n" +
                                "        if (number > 0) {\n" +
                                "            System.out.println(\"The number is positive.\");\n" +
                                "        } else if (number < 0) {\n" +
                                "            System.out.println(\"The number is negative.\");\n" +
                                "        } else {\n" +
                                "            System.out.println(\"The number is zero.\");\n" +
                                "        }\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Grade Calculator with Switch",
                        "Utilize the `switch` statement to handle multiple possible execution paths based on a single variable's value.",
                        "**Task:**\n" +
                                "*   Create a `GradeMessage` class.\n" +
                                "*   In the `main` method, declare a `char` variable named `grade` and assign it a letter grade (e.g., `'A'`, `'B'`, `'C'`, `'D'`, `'F'`).\n" +
                                "*   Use a `switch` statement with `grade` as the expression to print a message based on the grade:\n" +
                                "    *   `'A'` or `'a'`: \"Excellent work!\"\n" +
                                "    *   `'B'` or `'b'`: \"Good job!\"\n" +
                                "    *   `'C'` or `'c'`: \"You passed.\"\n" +
                                "    *   `'D'` or `'d'`: \"You can do better.\"\n" +
                                "    *   `'F'` or `'f'`: \"Unfortunately, you failed.\"\n" +
                                "    *   `default`: \"Invalid grade entered.\"\n" +
                                "*   Remember to use `break` statements after each `case`.\n" +
                                "*   Test with different grade characters, including a lowercase letter and an invalid character.\n\n" +
                                "**Example Structure:**\n" +
                                "```java\n" +
                                "public class GradeMessage {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        char grade = 'B';\n" +
                                "        switch (grade) {\n" +
                                "            case 'A':\n" +
                                "            case 'a':\n" +
                                "                System.out.println(\"Excellent work!\");\n" +
                                "                break;\n" +
                                "            // ... other cases\n" +
                                "            default:\n" +
                                "                System.out.println(\"Invalid grade entered.\");\n" +
                                "        }\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Loops: Sum of N Numbers",
                        "Apply both `for` and `while` loops to perform repetitive tasks, specifically calculating a sum.",
                        "**Task:**\n" +
                                "*   Create a `Summation` class.\n" +
                                "*   **Part 1: Using a `for` loop**\n" +
                                "    *   In the `main` method, calculate the sum of integers from 1 to 100 using a `for` loop.\n" +
                                "    *   Print the result, e.g., \"Sum using for loop: [sum]\".\n" +
                                "*   **Part 2: Using a `while` loop**\n" +
                                "    *   In the same `main` method, calculate the sum of integers from 1 to 100 again, but this time using a `while` loop.\n" +
                                "    *   Print the result, e.g., \"Sum using while loop: [sum]\".\n" +
                                "*   Ensure both loops yield the same correct sum (which should be 5050).\n\n" +
                                "**Example `for` loop:**\n" +
                                "```java\n" +
                                "int sumFor = 0;\n" +
                                "for (int i = 1; i <= 100; i++) {\n" +
                                "    sumFor += i;\n" +
                                "}\n" +
                                "System.out.println(\"Sum using for loop: \" + sumFor);\n" +
                                "```\n\n" +
                                "**Example `while` loop:**\n" +
                                "```java\n" +
                                "int sumWhile = 0;\n" +
                                "int i = 1;\n" +
                                "while (i <= 100) {\n" +
                                "    sumWhile += i;\n" +
                                "    i++;\n" +
                                "}\n" +
                                "System.out.println(\"Sum using while loop: \" + sumWhile);\n" +
                                "```"
                ));
                break;

            case "JAVA103": // Java Basics: Methods & Modularity
                assignments.add(new AssignmentData(
                        "Simple Calculator Methods",
                        "Learn to define and call methods to encapsulate specific pieces of logic, improving code organization.",
                        "**Task:**\n" +
                                "*   Create a class named `Calculator`.\n" +
                                "*   Inside this class, but outside `main`, implement the following `public static` methods:\n" +
                                "    *   `int add(int a, int b)`: Returns the sum of `a` and `b`.\n" +
                                "    *   `int subtract(int a, int b)`: Returns the difference of `a` and `b`.\n" +
                                "    *   `int multiply(int a, int b)`: Returns the product of `a` and `b`.\n" +
                                "    *   `double divide(int a, int b)`: Returns the quotient of `a` and `b`. Be careful with integer division; cast one of the operands to `double`.\n" +
                                "*   In your `main` method:\n" +
                                "    *   Call each of these `Calculator` methods with example numbers.\n" +
                                "    *   Print the results of each operation, clearly labeling them (e.g., \"Sum: \", \"Difference: \").\n\n" +
                                "**Example Method Signature:**\n" +
                                "```java\n" +
                                "public class Calculator {\n" +
                                "    public static int add(int a, int b) {\n" +
                                "        return a + b;\n" +
                                "    }\n" +
                                "    \n" +
                                "    public static void main(String[] args) {\n" +
                                "        int result = add(5, 3);\n" +
                                "        System.out.println(\"Sum: \" + result);\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Greeting Method with Parameters",
                        "Practice creating methods that accept parameters to make them more flexible and reusable.",
                        "**Task:**\n" +
                                "*   Create a class named `Greeter`.\n" +
                                "*   Inside this class, but outside `main`, implement a `public static void` method named `greetUser` that takes one `String` parameter named `name`.\n" +
                                "*   The `greetUser` method should print a personalized greeting to the console using the provided `name`, for example: 'Hello, [name]! Welcome to Java exercises.'\n" +
                                "*   In your `main` method, call `greetUser()` at least twice with different names (e.g., your name, a friend's name).\n\n" +
                                "**Example:**\n" +
                                "```java\n" +
                                "public class Greeter {\n" +
                                "    public static void greetUser(String name) {\n" +
                                "        System.out.println(\"Hello, \" + name + \"! Welcome to Java exercises.\");\n" +
                                "    }\n" +
                                "    \n" +
                                "    public static void main(String[] args) {\n" +
                                "        greetUser(\"Alice\");\n" +
                                "        greetUser(\"Bob\");\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Method Overloading - Area Calculator",
                        "Demonstrate method overloading by defining multiple methods with the same name but different parameter lists.",
                        "**Task:**\n" +
                                "*   Create an `AreaCalculator` class.\n" +
                                "*   Inside this class, implement overloaded `public static` methods named `calculateArea` for different shapes:\n" +
                                "    *   `double calculateArea(double radius)`: Calculates the area of a circle. (Formula: `π * radius * radius` Use `Math.PI` for pi).\n" +
                                "    *   `double calculateArea(double length, double width)`: Calculates the area of a rectangle.\n" +
                                "    *   `double calculateArea(double base, double height, boolean isTriangle)`: Calculates the area of a triangle. The `isTriangle` boolean is just to make the signature unique. (Formula: `0.5 * base * height`).\n" +
                                "*   In your `main` method:\n" +
                                "    *   Call each overloaded `calculateArea` method with appropriate arguments.\n" +
                                "    *   Print the calculated area for each shape, clearly indicating which shape it is (e.g., \"Circle Area: \", \"Rectangle Area: \").\n\n" +
                                "**Example Circle Area Method:**\n" +
                                "```java\n" +
                                "public class AreaCalculator {\n" +
                                "    public static double calculateArea(double radius) {\n" +
                                "        return Math.PI * radius * radius;\n" +
                                "    }\n" +
                                "    \n" +
                                "    public static void main(String[] args) {\n" +
                                "        double circleArea = calculateArea(5.0);\n" +
                                "        System.out.println(\"Circle Area: \" + circleArea);\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                break;

            case "JAVA104": // Java Basics: Arrays
                assignments.add(new AssignmentData(
                        "Basic Array Declaration and Access",
                        "Understand how to declare, initialize, and access elements in a one-dimensional array.",
                        "**Task:**\n" +
                                "*   Create a class named `ArrayBasics`.\n" +
                                "*   In its `main` method, declare an array of 5 integers. You can choose any name for the array (e.g., `numbers`).\n" +
                                "*   Initialize the array with values of your choice when you declare it (e.g., `{10, 20, 30, 40, 50}`).\n" +
                                "*   Print the following elements to the console, labeling each output:\n" +
                                "    *   The element at the first index (index 0).\n" +
                                "    *   The element at the third index (index 2).\n" +
                                "    *   The element at the last index of the array (use `arrayName.length - 1`).\n\n" +
                                "**Example:**\n" +
                                "```java\n" +
                                "import java.util.Arrays;\n" +
                                "\n" +
                                "public class ArrayBasics {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        int[] numbers = {10, 20, 30, 40, 50};\n" +
                                "        System.out.println(\"First element: \" + numbers[0]);\n" +
                                "        // ... access other elements\n" +
                                "\n" +
                                "        numbers[1] = 99;\n" +
                                "        System.out.println(\"Array after modification: \" + Arrays.toString(numbers));\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Array Iteration and Sum",
                        "Use loops to iterate through an array and perform calculations on its elements.",
                        "**Task:**\n" +
                                "*   Create a class named `ArraySum`.\n" +
                                "*   In its `main` method, declare and initialize an array of `double` values (e.g., test scores, prices: `{15.5, 20.0, 10.2, 5.8, 12.3}`).\n" +
                                "*   Calculate the sum of all elements in the array using a `for` loop.\n" +
                                "*   Print the calculated sum, labeled appropriately.\n" +
                                "*   Then, calculate the average of these elements and print it.\n\n" +
                                "**Example:**\n" +
                                "```java\n" +
                                "public class ArraySum {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        double[] values = {15.5, 20.0, 10.2, 5.8, 12.3};\n" +
                                "        double sum = 0;\n" +
                                "        for (int i = 0; i < values.length; i++) {\n" +
                                "            sum += values[i];\n" +
                                "        }\n" +
                                "        System.out.println(\"Sum: \" + sum);\n" +
                                "        System.out.println(\"Average: \" + (sum / values.length));\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Finding Max/Min in Array",
                        "Develop logic to find the maximum and minimum values within an array.",
                        "**Task:**\n" +
                                "*   Create a class named `ArrayMinMax`.\n" +
                                "*   In its `main` method, declare and initialize an array of integers (e.g., `{4, 9, 1, 7, 5, 2, 8}`).\n" +
                                "*   Find the maximum value in the array using a loop. Initialize a `max` variable with the first element of the array.\n" +
                                "*   Find the minimum value in the array using another loop. Initialize a `min` variable with the first element of the array.\n" +
                                "*   Print both the maximum and minimum values found, clearly labeled.\n\n" +
                                "**Example for finding Max:**\n" +
                                "```java\n" +
                                "public class ArrayMinMax {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        int[] numbers = {4, 9, 1, 7, 5, 2, 8};\n" +
                                "        int max = numbers[0];\n" +
                                "        for (int i = 1; i < numbers.length; i++) {\n" +
                                "            if (numbers[i] > max) {\n" +
                                "                max = numbers[i];\n" +
                                "            }\n" +
                                "        }\n" +
                                "        System.out.println(\"Maximum value: \" + max);\n" +
                                "        // ... logic for min\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                break;


            case "JAVA201": // OOP: Classes & Objects
                assignments.add(new AssignmentData(
                        "Basic Class and Object Creation",
                        "Define your first custom classes, create objects from them, and access their state and behavior.",
                        "**Task:**\n" +
                                "*   Create a new class named `Dog`.\n" +
                                "*   Inside the `Dog` class, define two instance variables (attributes):\n" +
                                "    *   `String name`\n" +
                                "    *   `String breed`\n" +
                                "*   Implement a constructor for the `Dog` class that takes `name` and `breed` as arguments and initializes the instance variables.\n" +
                                "*   Add a `public void speak()` method to the `Dog` class that prints something like \"[Dog's Name] says Woof!\" to the console.\n" +
                                "*   In a separate `Main` class (or within a `main` method in `Dog`):\n" +
                                "    *   Create two `Dog` objects with different names and breeds.\n" +
                                "    *   Call the `speak()` method for each `Dog` object.\n" +
                                "    *   Print the `name` and `breed` of each `Dog` object directly (e.g., `myDog.name`).\n\n" +
                                "**Example `Dog` Class Structure:**\n" +
                                "```java\n" +
                                "public class Dog {\n" +
                                "    String name;\n" +
                                "    String breed;\n" +
                                "\n" +
                                "    public Dog(String name, String breed) {\n" +
                                "        this.name = name;\n" +
                                "        this.breed = breed;\n" +
                                "    }\n" +
                                "\n" +
                                "    public void speak() {\n" +
                                "        System.out.println(name + \" says Woof!\");\n" +
                                "    }\n" +
                                "\n" +
                                "    public static void main(String[] args) { // Can be in a separate Main.java\n" +
                                "        Dog myDog = new Dog(\"Buddy\", \"Golden Retriever\");\n" +
                                "        System.out.println(\"My dog's name is \" + myDog.name + \", breed: \" + myDog.breed);\n" +
                                "        myDog.speak();\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Encapsulation with Getters and Setters",
                        "Apply the principle of encapsulation by making instance variables private and providing public getter and setter methods.",
                        "**Task:**\n" +
                                "*   Refactor the `Dog` class from the previous assignment.\n" +
                                "*   Change the `name` and `breed` instance variables to `private`.\n" +
                                "*   Add public `getter` methods for both `name` (e.g., `public String getName()`) and `breed` (e.g., `public String getBreed()`).\n" +
                                "*   Add public `setter` methods for both `name` (e.g., `public void setName(String name)`) and `breed` (e.g., `public void setBreed(String breed)`).\n" +
                                "*   In your `main` method (or `Main` class):\n" +
                                "    *   Create a `Dog` object.\n" +
                                "    *   Use the `setter` methods to change its `name` or `breed`.\n" +
                                "    *   Use the `getter` methods to retrieve and print the `Dog`'s details. You should no longer directly access `myDog.name`.\n\n" +
                                "**Example Getter/Setter:**\n" +
                                "```java\n" +
                                "public class Dog {\n" +
                                "    private String name; // Changed to private\n" +
                                "    private String breed;\n" +
                                "\n" +
                                "    // Constructor remains similar\n" +
                                "\n" +
                                "    public String getName() {\n" +
                                "        return name;\n" +
                                "    }\n" +
                                "\n" +
                                "    public void setName(String name) {\n" +
                                "        this.name = name;\n" +
                                "    }\n" +
                                "    // ... getBreed, setBreed, speak methods\n" +
                                "\n" +
                                "    public static void main(String[] args) {\n" +
                                "        Dog myDog = new Dog(\"Max\", \"Labrador\");\n" +
                                "        System.out.println(\"Original name: \" + myDog.getName()); // Use getter\n" +
                                "        myDog.setName(\"Charlie\"); // Use setter\n" +
                                "        System.out.println(\"New name: \" + myDog.getName());\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                assignments.add(new AssignmentData(
                        "Static Members and Methods",
                        "Understand the difference between instance and static members, and how to use static methods and variables.",
                        "**Task:**\n" +
                                "*   Create a class named `Circle`.\n" +
                                "*   Inside `Circle`, define:\n" +
                                "    *   An `instance double` variable `radius`.\n" +
                                "    *   A `static final double` variable `PI` initialized to `Math.PI`.\n" +
                                "    *   A `static int` variable `numberOfCircles` initialized to 0, which increments every time a new `Circle` object is created (i.e., in the constructor).\n" +
                                "*   Implement a constructor `Circle(double radius)` that initializes `radius` and increments `numberOfCircles`.\n" +
                                "*   Add an `instance public double getArea()` method that calculates `PI * radius * radius`.\n" +
                                "*   Add a `static public int getTotalCircles()` method that returns `numberOfCircles`.\n" +
                                "*   In your `main` method:\n" +
                                "    *   Create several `Circle` objects with different radii.\n" +
                                "    *   Print the area of each circle using its `getArea()` method.\n" +
                                "    *   Print the total number of circles created using `Circle.getTotalCircles()`.\n" +
                                "    *   (Optional): Try accessing `Circle.PI` directly.\n\n" +
                                "**Example:**\n" +
                                "```java\n" +
                                "public class Circle {\n" +
                                "    private double radius;\n" +
                                "    public static final double PI = Math.PI;\n" +
                                "    private static int numberOfCircles = 0;\n" +
                                "\n" +
                                "    public Circle(double radius) {\n" +
                                "        this.radius = radius;\n" +
                                "        numberOfCircles++;\n" +
                                "    }\n" +
                                "\n" +
                                "    public double getArea() {\n" +
                                "        return PI * radius * radius;\n" +
                                "    }\n" +
                                "\n" +
                                "    public static int getTotalCircles() {\n" +
                                "        return numberOfCircles;\n" +
                                "    }\n" +
                                "\n" +
                                "    public static void main(String[] args) {\n" +
                                "        Circle c1 = new Circle(5.0);\n" +
                                "        Circle c2 = new Circle(10.0);\n" +
                                "        System.out.println(\"Area of c1: \" + c1.getArea());\n" +
                                "        System.out.println(\"Total Circles: \" + Circle.getTotalCircles());\n" +
                                "    }\n" +
                                "}\n" +
                                "```"
                ));
                break;
            default:
                // Default assignments for any other courses (if any unexpected courseCode comes)
                assignments.add(new AssignmentData(
                        "General Programming Exercise",
                        "Complete a general programming assignment for this course.",
                        "**Task:**\n" +
                                "*   Follow the specific instructions provided for this course ID. This is a placeholder exercise.\n" +
                                "*   Ensure your code compiles without errors and addresses the core problem statement."
                ));
                break;
        }

        return assignments;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AssignmentData {
        private String title;
        private String description;
        private String resources;
    }
}

