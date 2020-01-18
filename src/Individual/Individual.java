package Individual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author Martim Viana
 *
 */
public class Individual {
    // List of problems to solve
    /*
    private static final Plate[] problems = {
            new Plate(new int[]{9, 4, 8, 6, 7, 5, 4, 1, 2, 3}),
            new Plate(new int[] {9, 2, 3, 5, 1, 0, 8})
    };
    */
    private Plate[] problems;

    // Number of guesses that the individual can make to solve the problems.
    private int MOVE_ATTEMPT_THRESHOLD = 50;

    // Individuals gene code
    private List<Integer> gene = new ArrayList<>();

    private double heuristic = 0.0;

    // Ammount of steps the individual takes to solve the problem.
    private int problemResolutionSize = 0;

    /**
     * Class constructor used to randomly generate individuals.
     * @param r Random number generator.
     * @param problems  Array of problems that the individual needs to solve.
     * @param moveThreshold Maximum gene size.
     */
    public Individual(Random r, Plate[] problems, int moveThreshold) {
        this.MOVE_ATTEMPT_THRESHOLD = moveThreshold;
        this.problems = problems;
        generateGenome(r);
    }

    /**
     * Class constructor used during recombination.
     * @param genes List of genes that, when concatinated, make the individuals gene.
     * @param problems  Array of problems that the individual needs to solve.
     * @param r Random number generator.
     * @param moveThreshold Maximum gene size.
     */
    public Individual(List<List<Integer>> genes, Plate[] problems, Random r, int moveThreshold) {
        this.problems = problems;
        for (List<Integer> gene: genes) this.gene.addAll(gene);
        this.MOVE_ATTEMPT_THRESHOLD = moveThreshold;
        generateGenome(r);
    }

    /**
     * Solves all problems by using the existent genes (if they exist) and, if the problem list couldn't be solved by
     * the gene code, solve the problem list using randomly-generated chromosomes and add them to the list.
     * @param r Random number generator.
     */
    private void generateGenome(Random r) {
        int g  = 0;
        problemResolutionSize = 0;
        Plate[] problems = cloneProblems();
        for (int p = 0; p < problems.length; p++) {
            while(!problems[p].isGoal()) {
                // if there are still chromosomes in the gene that weren't flipped yet, flip them.
                if (g < gene.size()) {
                    problems[p].flip(gene.get(g));
                    // increment amount of moves that the individuals takes to solve problem.
                    problemResolutionSize++;
                }

                // else, randomly generate new chromosome and flip problem with it.
                else {
                    int point = r.nextInt(problems[p].size());

                    // flip the problem and add it to the gene list
                    problems[p].flip(point);
                    gene.add(point);

                    // increment amount of moves that the individuals takes to solve problem.
                    problemResolutionSize++;
                }

                // increment current gene count
                g++;


                // if the number of moves attempted didn't solve all problems, immediately stop generating new
                // chromosomes.
                if (problemResolutionSize == MOVE_ATTEMPT_THRESHOLD && !solutionFound()) {
                    problemResolutionSize = Integer.MAX_VALUE;
                    //System.out.println("done!");
                    heuristic = calculateHeuristic(problems);
                    return;
                }
            }
        }
        // if the gene wasn't completely used, delete parts that weren't used.
        if (g < gene.size()) {
            while (g == gene.size()) {
                gene.remove(gene.size()-1);
            }
        }
        heuristic = calculateHeuristic(problems);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RECOMBINATION
    /**
     *  Perform the single point recombination algorithm with another individual, where their offspring are all possible
     *  combinations of both genes, split in a randomly generated point.
     * @param r Random number generator.
     * @param individual    Recombination partner.
     * @return  A list containing all offspring.
     */
    public List<Individual> singlePoint(Random r, Individual individual) {
        List<Individual> result =  new ArrayList<>();

        // to avoid errors, the point will be a number between 1 and the size of the smallest gene.
        int minGeneSize = Math.min(gene.size(), individual.gene.size());
        int point = r.nextInt(minGeneSize);

        // add resulting sub-genes to the new genes.
        List<List<Integer>> genes = new ArrayList<>();
        genes.add(gene.subList(0, point));
        genes.add(individual.gene.subList(point, individual.gene.size()));

        // create new individual A
        result.add(new Individual(genes, problems, r, MOVE_ATTEMPT_THRESHOLD));

        // do the opposite for individual B
        genes.clear();
        genes.add(individual.gene.subList(0, point));
        genes.add(gene.subList(point, gene.size()));
        result.add(new Individual(genes, problems, r, MOVE_ATTEMPT_THRESHOLD));

        // return offspring
        return result;
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MUTATION
    public void bitFlip(Random r) {
        int g = r.nextInt(gene.size());
        int flipValue = gene.get(r.nextInt(gene.size()));
        gene.remove(g);
        gene.add(g, flipValue);
        generateGenome(r);
    }

    public void restartGene(Random r) {
        Individual i = new Individual(r, problems, MOVE_ATTEMPT_THRESHOLD);
        this.gene = i.gene;
    }

    /**
     * Calculate a heuristic function based on the sum of the number of values that are in an ascending order.
     * @param solved    An array containing all pancakes to be compared.
     * @return  The heuristic number, obtaining by calculating what is said in the description.
     */
    private double calculateHeuristic(Plate[] solved) {
        double result = 0.0;
        for (int i = 0; i < solved.length; i++) {
            // if the problem isn't the goal, count the amount of pancakes that are already sorted and increment that
            // in the heuristic.
            if (!solved[i].isGoal()) {
                double score = 0, highest = Integer.MIN_VALUE;
                //score = solved[i].differentSpotsAmt(problems[i]);
                for (int j: solved[i].pancake) {

                    if (highest >= j) {
                        highest = j;
                        score++;
                    }
                }
                result += score;
            }
        }

        // penalize individuals that didn't even reached problems
        for (int i = solved.length; i < problems.length; i++) {
            result += problems[i].size();
        }

        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PRINT INFORMATION

    /**
     * Print the individual's resolution to it's problem(s).
     */
    public void printProblemResolution() {
        int g = 0;
        Plate[] problems = cloneProblems();
        for (Plate problem: problems) {
            System.out.println("\tProblem "+problem+"");
            while (!problem.isGoal()) {
                // If g surpasses the elements of the gene list, it means that the individual wasn't capable of solving
                // the problem.
                if (gene.size() == g) {
                    System.out.println("\tThe best individual wasn't capable of solving the problem...");
                    return;
                }
                problem.flip(gene.get(g++));
                System.out.println("\t\t\t"+problem);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MISC. METHODS


    /**
     * Clone all problems of this class.
     * @return  A list of cloned problems of this class.
     */
    private Plate[] cloneProblems() {
        Plate[] result = new Plate[problems.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Plate) problems[i].clone();
        }
        return result;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTER METHODS

    /**
     * Calculates if the individual solved the problem.
     * @return  True if the individual solved the problem, false otherwise.
     */
    public boolean solutionFound() {
        int g = 0;
        Plate[] problems = cloneProblems();
        for (Plate problem: problems) {
            //System.out.println("\tProblem "+problem+"");
            while (!problem.isGoal()) {
                // If g surpasses the elements of the gene list, it means that the individual wasn't capable of solving
                // the problem.
                if (gene.size() == g) {
                    return false;
                }
                problem.flip(gene.get(g++));
                //System.out.println("\t\t\t"+problem);
            }
        }
        return true;
    }

    /**
     * Returns the amount of moves that the individual takes to solve the problem.
     * @return  Integer.MAX_VALUE if no solution was found. A number lesser than Integer.MAX_VALUE otherwise.
     */
    public double getFitness() {return problemResolutionSize;}
    /**
     *
     * @return  A heuristic value that calculates the amount of individuals that are already sorted.
     */
    public double getHeuristic() {return  heuristic;}

    /**
     *
     * @return  A list of integers containing all positions that the individual uses to flip the problem.
     */
    public List<Integer> getGene() {return this.gene;}



}
