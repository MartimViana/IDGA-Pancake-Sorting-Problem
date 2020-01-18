package GeneticAlgorithm;

import Individual.Plate;
import Individual.Population;
import java.util.Random;

/**
 * @author Martim Viana
 * Class responsible for executing the genetic algorithms specified, as well as operating on the population by calling,
 * various methods during the algorithm's execution.
 */
public class GeneticAlgorithm {

    private boolean SHOW_INFO_DURING_RUN;

    private Population population;
    private int initialSize, generationMax, moveThreshold;
    private Random r;
    private Plate[] problems;
    /**
     * Class constructor
     * @param r Random number generator used for generating the initial population
     * @param problems  Problems that each individual needs to solve.
     * @param initialSize   Initial amount of individuals in the population.
     * @param generationMax Maximum number of generations that the runGenerations method will use.
     * @param moveThreshold Maximum gene size.
     * @param SHOW_INFO_DURING_RUN True to show debug info, false otherwise.
     */
    public GeneticAlgorithm(Random r, Plate[] problems, int initialSize, int generationMax, int moveThreshold, boolean SHOW_INFO_DURING_RUN) {
        this.SHOW_INFO_DURING_RUN = SHOW_INFO_DURING_RUN;
        this.r = r;
        this.problems = problems;
        this.initialSize = initialSize;
        this.generationMax = generationMax;
        this.moveThreshold = moveThreshold;

        // initialize new randomly generated population with size initialSize.
        this.population = new Population(r,problems, initialSize, moveThreshold);
        this.initialSize = initialSize;
    }


    /**
     * Run the genetic algorithm according to a specific amount of generations.
     * @param r Random number generations
     */
    public void runGenerations(Random r) {
        int i = 0;
        GeneticAlgorithm ga = new GeneticAlgorithm(r, problems, initialSize, generationMax, moveThreshold, SHOW_INFO_DURING_RUN);
        while (i < generationMax) {
            System.out.print("Generation "+i+"...");
            ga.iterate(r, i);
            if (SHOW_INFO_DURING_RUN) {
                ga.population.printPopulationsSize();
                //ga.population.printBestIndividual();
                //ga.population.printCurrentPopulation();

                System.out.println("-------------------------------------------------------------------------------------");
            }
            System.out.println("Done!");
            i++;
        }
        System.out.println("Done! Printing best individual resolution...");
        ga.population.printBestIndividualResolution();
    }

    /**
     * Execute the genetic algorithm, stopping only when a solution was reached.
     * @param r Random number generator.
     */
    public void runUntilFound(Random r) {
        boolean solutionNotFound = true;
        GeneticAlgorithm ga = new GeneticAlgorithm(r, problems, initialSize, generationMax, moveThreshold, SHOW_INFO_DURING_RUN);
        int i = 0;
        while (solutionNotFound) {
            System.out.println("Generation "+i);

            ga.iterate(r, i);
            solutionNotFound = !ga.population.solutionFound();


            if (SHOW_INFO_DURING_RUN) {
                System.out.println("Generation "+(i+1)+":");
                ga.population.printPopulationsSize();
                ga.population.printBestIndividual();
                ga.population.printCurrentPopulation();
                System.out.println("-------------------------------------------------------------------------------------");
            }
            System.out.println(" done!");
            i++;
        }
        System.out.println("Done! Printing best individual...");
        ga.population.printBestIndividual();
        System.out.println("Printing best individual resolution...");
        ga.population.printBestIndividualResolution();
    }

    /**
     * Iterate a generation of the genetic algorithm, according to methods chosen by the developer and criteria chosen
     * by the user.
     * @param r Random number generator
     * @param generation    Number indicating current generation count.
     */
    private void iterate(Random r, int generation) {
        int selectedAmt = initialSize/(generation+1);
        int participantsAmt = 2;
        int top  = 100;
        int mutatedAmt = population.size() - top;
        int decimatedAmt = selectedAmt;
        int multiBitFlipAmt=population.size();

        // selection
        //population.rouletteWheelSelection(r, selectedAmt);
        //population.tournamentSelection(r, "best", selectedAmt, participantsAmt);
        population.randomSelection(r, selectedAmt);

        // recombination
        population.singlePoint(r);

        population.calculateBestIndividual();

        // mutation
        //population.restartGeneWorst(r, mutatedAmt);
        //population.restartGeneIfNoSolutionFound(r);
        //population.restartGene(r, 0.7);
        population.bitFlip(r, 0.8);
        //population.multiBitFlip(r, multiBitFlipAmt, 0.8);

        // decimation
        population.decimatePopulationByFitness("worst", decimatedAmt);

        // ESSENTIAL TO DETERMINE BEST INDIVIDUAL
        population.calculateBestIndividual();
    }

    /**
     * Execute the iterative deepening genetic algorithm based on chosen user criteria
     */
    public void run() {
        for (int m = 1; m < moveThreshold; m++) {
            System.out.print("Move threshold "+m);
            GeneticAlgorithm ga = new GeneticAlgorithm(r, problems, initialSize, generationMax, m, SHOW_INFO_DURING_RUN);
            for(int i = 0; i < ga.generationMax; i++) {
                ga.iterate(r, i);

                if (SHOW_INFO_DURING_RUN) {
                    System.out.println("Generation "+(i+1)+":");
                    ga.population.printPopulationsSize();
                    ga.population.printBestIndividual();
                    ga.population.printCurrentPopulation();
                    System.out.println("-------------------------------------------------------------------------------------");
                }
            }
            System.out.println(" done!");

            // if a solution was found, print solution move set and print individual solving problem(s).

            if (ga.population.solutionFound()) {
                System.out.println("Done! Printing best individual...");
                ga.population.printBestIndividual();
                System.out.println("Printing best individual resolution...");
                ga.population.printBestIndividualResolution();
                return;
            }

        }
    }
}
