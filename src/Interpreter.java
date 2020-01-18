import GeneticAlgorithm.GeneticAlgorithm;
import Individual.Plate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Martim Viana
 * CLI of the program.
 */
public class Interpreter {

    // STATIC VARIABLES
    private static int MAX_GENERATION = 100;

    private static String author="Martim Viana, 55099";
    private static Scanner sc = new Scanner(System.in);
    private static int NO_INTEGER = Integer.MIN_VALUE;

    private static final String
    EXIT = "exit",
    HELP = "help",

    // genetic algorithm related
    LAUNCH_GA_NORMAL = "start",
    LAUNCH_GA_UNTIL_FOUND = "start until found",
    LAUNCH_GA_GENERATIONS = "start generations",

    // problem management
    ADD_PROBLEM = "add problem",
    REMOVE_PROBLEM = "remove problem"
            ;

    // VARIABLES
    private static GeneticAlgorithm ga;
    private static List<Plate> problems = new ArrayList<>();

    /**
     * Prints all possible commands that the user can call.
     */
    private static void printCommands() {
        System.out.println("List of available commands:");
        System.out.print("\t"
                +"*"+EXIT+": Exits the program\n\t"
                +"*"+HELP+": Print a list of all available commands, as well as a short description.\n\t"
                +"*"+LAUNCH_GA_NORMAL+": Launch the Iterative-deepening genetic algorithm.\n\t"
                +"*"+LAUNCH_GA_UNTIL_FOUND+": Launch a normal genetic algorithm, whose stopping criteria is to find a solution.\n\t"
                +"*"+LAUNCH_GA_GENERATIONS+": Launch a normal genetic algorithm that will iterate over a selected ammount of generations.\n\t"
                +"*"+ADD_PROBLEM+": Add a problem for the individuals to solve.\n\t"
                +"*"+REMOVE_PROBLEM +": Remove a problem for the individuals to solve.\n");
    }

    /**
     * Launch the iterative deepening genetic algorithm.
     */
    private static void launchGA() {
        // get user initial conditions
        System.out.print("Seed: ");
        Random r = new Random(getNumberInput());
        System.out.print("Initial population size: ");
        int initialSize = getNumberInput();
        System.out.print("Move threshold: ");
        int moveThreshold = getNumberInput();
        System.out.print("Number of generations until move incrementation: ");
        int generationMax = getNumberInput();
        System.out.println("Show debug info?(0 for yes, 1 for no) ");
        int d = getNumberInput();
        boolean debug = d==0? true: false;

        // execute algorithm
        ga = new GeneticAlgorithm(r, convertProblems(), initialSize, generationMax, moveThreshold, debug);
        ga.run();
    }

    /**
     * Launch the genetic algorithm that will run until a solution was found.
     */
    private static void launchGAUntilFound() {
        // get user initial conditions
        System.out.print("Seed: ");
        Random r = new Random(getNumberInput());
        System.out.print("Initial population size: ");
        int initialSize = getNumberInput();
        System.out.print("Move threshold: ");
        int moveThreshold = getNumberInput();
        System.out.println("Show debug info?(0 for yes, 1 for no) ");
        int d = getNumberInput();
        boolean debug = d==0? true: false;

        // execute algorithm
        ga = new GeneticAlgorithm(r, convertProblems(), initialSize, Integer.MAX_VALUE, moveThreshold, debug);
        ga.runUntilFound(r);
    }

    /**
     * Launch the genetic algorithm that will run a specified ammount of generations.
     */
    private static void launchGAgenerations() {
        // get user initial conditions
        System.out.print("Seed: ");
        Random r = new Random(getNumberInput());
        System.out.print("Initial population size: ");
        int initialSize = getNumberInput();
        System.out.print("Move threshold: ");
        int moveThreshold = getNumberInput();
        System.out.print("Number of generations: ");
        int generationMax = getNumberInput();
        System.out.println("Show debug info?(0 for yes, 1 for no) ");
        int d = getNumberInput();
        boolean debug = d==0? true: false;

        // execute algorithm
        ga = new GeneticAlgorithm(r, convertProblems(), initialSize, generationMax, moveThreshold, debug);
        ga.runGenerations(r);
    }

    /**
     * Convert problems variable from a list to an array.
     * @return  Array containing all elements of the problems variable.
     */
    private static Plate[] convertProblems() {
        Plate[] result = new Plate[problems.size()];
        for (int i = 0; i < result.length; i++) result[i] = problems.get(i);
        return result;
    }

    /**
     * Add a user-specified problem to the to-solve problems list.
     */
    private static void addProblem() {
        // get input
        System.out.println("Insert problem in the following format: 1, 2, 3, 4");
        String strProblem = sc.nextLine();

        // get rid of all spaces
        strProblem = strProblem.replace(" ", "");

        // seperate all numbers by comma
        String[] numberString = strProblem.split(",");

        // convert all strings to integer
        int[] pArray =  new int[numberString.length];
        try {
            for (int i = 0; i < numberString.length; i++) {
                pArray[i] = Integer.parseInt(numberString[i]);
            }

            // add new problem to problems list
            problems.add(new Plate(pArray));
            System.out.println("Problem added to the list.");
        }
        catch (NumberFormatException nfe) {
            System.out.println("Couldn't interpret input.");
        }

    }

    /**
     * Remove a user-specified problem to the to-solve problems list.
     */
    private static void removeProblem() {
        System.out.println("Type index of what problem to remove:");
        for (int i = 0; i < problems.size(); i++) {
            System.out.println("\t"+i+": "+problems.get(i));
        }
        int i = getNumberInput();
        problems.remove(i);

        System.out.println("Problem "+i+" removed from the list.");
    }

    /**
     * Signals the user that he is printing a command.
     * @return  User input  User command.
     */
    private static String getCommandInput() {
        System.out.print("> ");
        return sc.nextLine().toLowerCase();
    }

    /**
     * Fetches a number from the user and signals user if he didn't type a number.
     * @return  The number that the user typed, of the NO_INTEGER flag if the user didn't type a number.
     */
    private static int getNumberInput() {
        try {
            return Integer.parseInt(sc.nextLine());
        }
        catch (NumberFormatException nfe) {
            System.out.println("Couldn't interpret input.");
            return NO_INTEGER;
        }
    }

    /**
     * Interprets the user input.
     * @param in    User input.
     */
    private static void interpret(String in) {
        if (in.equals(EXIT)) {
            System.out.println("Exiting program...");
            System.exit(0);
        }
        else if(in.equals(HELP)) printCommands();
        else if(in.equals(LAUNCH_GA_NORMAL)) launchGA();
        else if(in.equals(LAUNCH_GA_UNTIL_FOUND)) launchGAUntilFound();
        else if (in.equals(LAUNCH_GA_GENERATIONS)) launchGAgenerations();
        else if(in.equals(ADD_PROBLEM))addProblem();
        else if(in.equals(REMOVE_PROBLEM)) removeProblem();
        else {
            System.out.println("Couldn't understand command.");
        }
    }

    /**
     * Prints author and title, as well as executing the command-line interface.
     * @param args  Arguments.
     */
    public static void main(String[] args) {
        System.out.println("Iterative Deepening Genetic Algorithm to solve pancake sorting problems\nMade by "+author+"\n");
        while (true) {
            String input = getCommandInput();
            interpret(input);
        }
    }
}
