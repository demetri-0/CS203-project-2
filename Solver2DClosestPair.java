/*
== Project 2 ==

Author: Demetri Karras
Class: CS203
Due: June 12th, 2025
 */

import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

public class Solver2DClosestPair {
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        // explains the program and obtains input
        System.out.println("""
                === FIND CLOSEST PAIR ===
                You are going to create a coordinate plane by specifying the number of points and the bounds.
                Points will be randomly generated within that bounds, and can only take integer values.
                For relevant results, it is recommended that { number_of_points / (4(bounds * bounds)) < 0.5 } to
                prevent collisions when generating random points for the board.
                """);

        System.out.print("What is the bounds of the coordinate plane? ");
        int bounds = scan.nextInt();
        System.out.print("How many random points should be generated (n > 2)? ");
        int numPoints = scan.nextInt();
        System.out.println();

        long startTime = System.nanoTime();

        CoordinatePlane plane = null;

        // coordinate plane is built, points are randomly generated within the bounds and are sorted
        try {
            plane = new CoordinatePlane(numPoints, bounds);
        } catch (IllegalArgumentException iae) {
            System.out.println(iae.getMessage());
            System.exit(0);
        }

        // lists of points are obtained from the plane
        List<CoordinatePlane.Point> pointsSortedNonDecrByX = plane.getPointsSortedNonDecrByX();
        List<CoordinatePlane.Point> pointsSortedNonDecrByY = plane.getPointsSortedNonDecrByY();

        CoordinatePlane.Solution solution = null;

        // the main algorithm is run, finding the closest pair within the plane
        try {
            solution = plane.findClosestPair(pointsSortedNonDecrByX, pointsSortedNonDecrByY);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        long endTime = System.nanoTime();

        long elapsedMilliseconds = (endTime - startTime) / 1000000;

        // the solution is printed to the user
        DecimalFormat df = new DecimalFormat("#.###");
        System.out.println("Closest Pair: " + solution.getPair()[0] + " and " + solution.getPair()[1] + "\nDistance: " + df.format(solution.getDistance()));
        System.out.println("Runtime: " + elapsedMilliseconds + " ms");
    }
}
