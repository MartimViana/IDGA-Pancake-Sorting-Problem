package Individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Martim Viana
 * Class-oriented representation of the pancake sorting problem.
 */
public class Plate {
    protected int[] pancake;
    protected int[] initialPancake;
    public Plate(int[] pancake) {
        this.initialPancake = pancake.clone();
        this.pancake = pancake;
    }

    /**
     * Checks if the pancake variable is in an ascending order.
     * @return  True if the pancake variable is in ascending order, false otherwise.
     */
    public boolean isGoal() {
        int current = pancake[0];
        for (int i: pancake) {
            if (current < i) return false;
            current = i;
        }
        return true;
    }

    /**
     * Execute the "flip" action in the pancake sorting algorithm, where the program will select a sub-array from the
     * pancake variable, reverse it, and insert it back into the array.
     * @param point startIndex of the sub-array.
     */
    public void flip(int point) {
        List<Integer> sub = new ArrayList<>();
        for (int i = point; i < pancake.length; i++) sub.add(pancake[i]);
        Collections.reverse(sub);
        int s = 0;
        for (int i = point; i < pancake.length; i++) pancake[i] = sub.get(s++);
    }

    /**
     * Calculates the amount of array positions whose values are different from the given plate.
     * @param p Plate that will be compared with the pancake variable.
     * @return  Number of spots whose values differ from variable p to variable pancake.
     */
    public double differentSpotsAmt(Plate p) {
        double result = 0;
        for (int i = 0; i < pancake.length; i++) {
            if (pancake[i] != p.pancake[i]) result++;
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "";
        for (int p: pancake) result+=" "+p;
        return result;
    }

    /**
     * Getter method for pancake size.
     * @return  Ammount of "pancakes" present in the pancake variable.
     */
    public int size() {return pancake.length;}

    public Object clone() {
        return new Plate(initialPancake.clone());

    }
}
