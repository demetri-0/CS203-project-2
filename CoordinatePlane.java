import java.util.*;

/*
A 2D coordinate plane with user-specified bounds and number of points.
 */
public class CoordinatePlane {

    final int numPoints;
    final int bounds; // the bounds of the plane in all directions (X, -X, Y, -Y)
    private final List<Point> pointsSortedNonDecrByX = new ArrayList<>();
    private final List<Point> pointsSortedNonDecrByY = new ArrayList<>();

    public CoordinatePlane(int numPoints, int bounds) throws IllegalArgumentException {

        // throws exception if plane cannot be built
        if (numPoints > Math.pow((bounds * 2) + 1, 2)) {
            throw new IllegalArgumentException(String.format("%d points cannot fit in a plane with a bounds of %d.", numPoints, bounds));
        }
        else if (numPoints < 3) {
            throw new IllegalArgumentException("Number of points must be at least 3.");
        }

        this.numPoints = numPoints;
        this.bounds = bounds;
        buildPlane();
    }

    /*
    Builds a coordinate plane by randomly generating points within a certain bounds.

    Input: none
    Output: none - the lists of points are updated
     */
    private void buildPlane() {

        Random rand = new Random();

        Set<Point> pointSet = new HashSet<>(); // a set is used to store the generated points to prevent duplicates

        // adds randomly generated points to the point set
        while (pointSet.size() < numPoints) {

            int x = rand.nextInt(bounds * -1, bounds + 1);
            int y = rand.nextInt(bounds * -1, bounds + 1);

            pointSet.add(new Point(x, y));
        }

        // transfers points from the set into lists
        pointsSortedNonDecrByX.addAll(pointSet);
        pointsSortedNonDecrByY.addAll(pointSet);

        // sorts the lists in ascending order based on X or Y coordinates respectively
        pointsSortedNonDecrByX.sort((p1, p2) -> Integer.compare(p1.x, p2.x));
        pointsSortedNonDecrByY.sort((p1, p2) -> Integer.compare(p1.y, p2.y));
    }

    /*
    Contains the main logic of the Divide and Conquer algorithm to solve the 2D Closest Pair problem.

    The plane is recursively split in half by X. The closest pair within each half is computed using brute force when
    numPoints <= 3. A vertical strip around the dividing line is examined to see if a closer pair exists between points
    on each side.

    Input: 2 lists of points, sorted by X and Y respectively
    Output: the closest pair and their distance among the provided points
     */
    Solution findClosestPair(List<Point> pointsSortedNonDecrByX, List<Point> pointsSortedNonDecrByY) throws Exception {

        Solution solution;
        Point[] closestPair = new Point[2];
        double minDistance, minDistanceSquared;

        List<Point> p = pointsSortedNonDecrByX;

        if (p.size() <= 3) {
            return findClosestPairByBruteForce(p);
        }
        else {

            List<Point> q = pointsSortedNonDecrByY;

            List<Point> pLeft = p.subList(0, p.size() / 2); // left half of p sorted by X
            Set<Point> pLeftSet = new HashSet<>(pLeft); // set is created and used to avoid nested linear operations

            List<Point> qLeft = q.stream() // left half of p sorted by Y
                    .filter(point -> pLeftSet.contains(point))
                    .toList();

            List<Point> pRight = p.subList(p.size() / 2, p.size()); // right half of p sorted by X
            Set<Point> pRightSet = new HashSet<>(pRight); // set is created and used to avoid nested linear operations

            List<Point> qRight = q.stream() // right half of p sorted by Y
                    .filter(point -> pRightSet.contains(point))
                    .toList();

            // finds the closest pair in the left and right regions containing (numPoints / 2) points
            Solution solutionLeft = findClosestPair(pLeft, qLeft);
            Solution solutionRight = findClosestPair(pRight, qRight);

            // finds the smallest distance among the two halves and builds the corresponding solution
            if (solutionLeft.getDistance() <= solutionRight.getDistance()) {
                minDistance = solutionLeft.getDistance();
                closestPair[0] = solutionLeft.getPair()[0];
                closestPair[1] = solutionLeft.getPair()[1];
            }
            else {
                minDistance = solutionRight.getDistance();
                closestPair[0] = solutionRight.getPair()[0];
                closestPair[1] = solutionRight.getPair()[1];
            }

            int midX = p.get(p.size() / 2 - 1).x; // represents the vertical line dividing the examined space in half

            // stores all points within the current minimum distance of the dividing line
            List<Point> s = q.stream()
                    .filter(point -> Math.abs(point.x - midX) < minDistance)
                    .toList();

            minDistanceSquared = minDistance * minDistance;
            // iterates through relevant points within the minimum distance to check for closer pairs
            for (int i = 0; i < s.size() - 2; i++) {
                int k = i + 1;
                while (k <= s.size() - 1 && (Math.pow(s.get(k).y - s.get(i).y, 2) < minDistanceSquared)) { // ensures that only points within a relevant Y distance from each other are checked
                    // if the newly calculated distance is less than the previous minimum, this pair becomes the new solution
                    if (Math.pow(s.get(k).x - s.get(i).x, 2) + Math.pow(s.get(k).y - s.get(i).y, 2) < minDistanceSquared) {
                        minDistanceSquared = Math.pow(s.get(k).x - s.get(i).x, 2) + Math.pow(s.get(k).y - s.get(i).y, 2);
                        closestPair[0] = s.get(i);
                        closestPair[1] = s.get(k);
                    }
                    k++;
                }
            }
        }

        solution = new Solution(closestPair, Math.sqrt(minDistanceSquared));

        return solution;
    }

    /*
    Finds the closest pair of points for lists of 2 or 3 points by Brute Force (testing every combination).

    Input: list of 2 or 3 points for which to find the closest pair
    Output: the closest pair and its distance
     */
    private Solution findClosestPairByBruteForce(List<Point> pointsSortedNonDecrByX) throws Exception {

        List<Point> p = pointsSortedNonDecrByX;

        Point[] closestPair = new Point[2];
        double minDistance;
        Solution solution;

        if (p.size() == 3) { // finds the closest pair among 3 points

            // gets the distances between all combinations of points and finds the minimum
            double d1 = getDistance(p.get(0), p.get(1));
            double d2 = getDistance(p.get(1), p.get(2));
            double d3 = getDistance(p.get(0), p.get(2));

            List<Double> distances = new ArrayList<>();
            distances.add(d1); distances.add(d2); distances.add(d3);

            minDistance = Collections.min(distances);

            // builds and returns a Solution based on the closest pair
            if (minDistance == d1) {
                closestPair[0] = p.get(0);
                closestPair[1] = p.get(1);
            }
            else if (minDistance == d2) {
                closestPair[0] = p.get(1);
                closestPair[1] = p.get(2);
            }
            else {
                closestPair[0] = p.get(0);
                closestPair[1] = p.get(2);
            }

            solution = new Solution(closestPair, minDistance);

            return solution;
        }
        else if (p.size() == 2) { // the closest pair among 2 points is those 2 points

            closestPair[0] = p.get(0);
            closestPair[1] = p.get(1);

            solution = new Solution(closestPair, getDistance(p.get(0), p.get(1)));

            return solution;
        }
        else {
            throw new Exception("Method <findClosestPairByBruteForce> can ONLY solve lists with 2 or 3 points.");
        }
    }

    /*
    Calculates the distance between two points using the Euclidean distance formula.

    Input: 2 points for which to calculate their distance
    Output: the distance between the specified points
     */
    private double getDistance(Point p1, Point p2) {

        int d1 = p2.x - p1.x;
        int d2 = p2.y - p1.y;

        return Math.sqrt(Math.pow(d1, 2) + Math.pow(d2, 2));
    }

    public List<Point> getPointsSortedNonDecrByX() {
        return pointsSortedNonDecrByX;
    }

    public List<Point> getPointsSortedNonDecrByY() {
        return pointsSortedNonDecrByY;
    }

    public String toString() {

        String result = "";

        for (int i = 0; i < numPoints; i++) {
            result = result.concat(pointsSortedNonDecrByX.get(i).toString() + " ");
        }

        return result;
    }

    /*
    Represents a point on the coordinate plane.
     */
    public static class Point {
        int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /*
        Compares the X and Y values of two Point objects for equality. Original method was overridden to allow for
        correct sorting in a collection of points.

        Input: Object (Point) to be compared
        Output: boolean representing the equality of points
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point) {
                return ((Point) obj).x == this.x && ((Point) obj).y == this.y;
            }
            return false;
        }

        /*
        Returns the hash code of a point object based on X and Y coordinates. Original method was overridden to allow
        for correct duplicate detection in a set.

        Input: none
        Output: the hash code representing the point
         */
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        /*
        Returns a string representation of the point formatted as follows: (x, y)

        Input: none
        Output: the string representation of the point
         */
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    /*
    Represents the solution to the Closest Pair problem. Contains the two closest points and their distance apart.
     */
    public static class Solution {

        private final Point[] pair;
        private final double distance;

        public Solution(Point[] pair, double distance) {
            this.pair = pair;
            this.distance = distance;
        }

        Point[] getPair() {
            return pair;
        }

        double getDistance() {
            return distance;
        }
    }
}
