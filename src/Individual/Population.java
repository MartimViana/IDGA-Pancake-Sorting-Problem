package Individual;

import javax.naming.InitialContext;
import java.util.*;

/**
 * @author Martim Viana
 *
 */
public class Population {

    // Main population
    private List<Individual> population = new ArrayList<>();

    // Population selected for reproduction
    private List<Individual> selected = new ArrayList<>();

    // Offspring of population selected for reproduction
    private List<Individual> offspring = new ArrayList<>();

    private Individual bestIndividual = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  CLASS CONSTRUCTOR
    /**
     * Randomly generate a population of individuals.
     * @param r Random number generator.
     * @param problems Array of problems that the individuals need to solve.
     * @param initialPopulationSize Amount of individuals to be randomly generated.s
     * @param moveThreshold Maximum gene size.
     */
    public Population(Random r, Plate[] problems, int initialPopulationSize, int moveThreshold) {
        for (int i = 0; i < initialPopulationSize; i++)
            population.add(new Individual(r, problems, moveThreshold));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  SELECTION

    /**
     * Applies the tournament selection to the population and adds the selected individuals to the selected list.
     * @param r Random number generator.
     * @param fitnessType Flag that indicates type of individual to be selected from the participants.
     * @param n Number of rounds of each tournament selection.
     * @param ps    Number of participants of each tournament selection.
     */
    public void tournamentSelection(Random r, String fitnessType, int n, int ps) {
        this.selected.clear();

        for (int i = 0; i < n; i++) {
            List<Individual> participants = new ArrayList<>();
            for (int j = 0; j < ps; j++) {
                // get random individual from the population
                Individual individual = population.get(r.nextInt(population.size()));

                // if the individual is already present in the list, discard it.
                if (participants.contains(individual))
                    j--;

                // else, add it to the list
                else participants.add(individual);
            }
            // add to the list the best individual of all participants
            if (fitnessType.equals("best")) {
                Individual best = compareBest(participants);
                this.selected.add(best);
                //this.population.remove(best);
            }
            else if(fitnessType.equals("worst")) {
                Individual worst = compareWorst(participants);
                this.selected.add(worst);
                //this.population.remove(worst);
            }
        }
    }

    /**
     * Randomly selects n individuals, without considering it's fitness and add's selected individuals to the selected
     * list.
     * @param r Random number generator.
     * @param n Amount of individuals to select.
     */
    public void randomSelection(Random r, int n) {
        selected.clear();
        for (int i = 0; i < n; i++) {
            int p = r.nextInt(population.size());
            selected.add(population.get(p));
        }
    }

    /**
     * Applies the roulette wheel selection to the population and adds the selected individuals to the selected list.
     * @param r Random number generator.
     * @param n Amount of individuals to select.
     */
    public void rouletteWheelSelection(Random r, int n) {
        this.selected.clear();
        // get list of fitness probability
        List<Double> cumulativeFitness = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
                cumulativeFitness.add(1 / population.get(i).getHeuristic());
        }

        Collections.sort(cumulativeFitness);

        // select n individuals from the population
        for (int i = 0; i < n; i++) {
            double p = r.nextDouble();
            double acc = 0;

            // select individual based on fitness
            for (int j = 0; j < population.size(); j++) {
                acc += cumulativeFitness.get(j);
                if (p > acc) {
                    selected.add(population.get(j));
                    //population.remove(population.get(j));
                    break;
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  RECOMBINATION

    /**
     * Applies the single point recombination algorithm to the selected list, where all selected individuals will
     * generate offspring with each other. The offspring will be added to the population.
     * @param r Random number generator.
     */
    public void singlePoint(Random r) {
        // make all individuals reproduce with each other once.
        for (int i = 0; i < selected.size(); i++) {
            for (int j = i+1; j < selected.size(); j++) {
                offspring.addAll(selected.get(i).singlePoint(r, selected.get(j)));
            }
        }
        population.addAll(offspring);
        offspring.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MUTATION

    /**
     * Perform the bit flip mutation algorithm to the population, the individual being selected based on chance.
     * @param r Random number generator
     * @param threshold Fraction that, if the number generated by r is lesser than, will apply the bit flip algorithm
     *                  to.
     */
    public void bitFlip(Random r, double threshold) {
        for (Individual i: selected) {
            // if the generated double is lower than the threshold, mutate the individual!
            if (r.nextDouble() < threshold) {
                i.bitFlip(r);
                population.add(i);
            }

        }
        selected.clear();
    }

    /**
     * Perform the multi bit flip mutation algorithm to the population, the individual being selected based on chance.
     * @param r Random number generator
     * @param amount   Amount of points to apply the bit flip mutation.
     * @param threshold Fraction that, if the number generated by r is lesser than, will apply the bit flip algorithm
     *                  to.
     */
    public void multiBitFlip(Random r, int amount, double threshold) {
        for (Individual i: selected) {
            // if the generated double is lower than the threshold, mutate the individual!
            if (r.nextDouble() < threshold) {
                for (int j = 0; j < amount; j++) {
                    i.bitFlip(r);
                }
                population.add(i);
            }

        }
        selected.clear();
    }

    /**
     * Randomly select individuals whose generated value is lesser than the threshold, and restart their genes.
     * @param r Random number generator.
     * @param threshold Fraction that, if the number generated by r is lesser than, will apply the bit flip algorithm
     *                  to.
     */
    public void restartGene(Random r, double threshold) {
        for (Individual i: selected) {
            // if the generated double is lower than the threshold, mutate the individual!
            if (r.nextDouble() < threshold) {
                i.restartGene(r);
                population.add(i);
                //selected.remove(i);
            }
        }
        selected.clear();
    }

    /**
     * Restart the genes of individuals with the worst fitness.
     * @param r Random number generator
     * @param amount    number of individuals with the worst fitness to get their genes restarted.
     */
    public void restartGeneWorst(Random r, int amount) {
        for (int i = 0; i < amount; i++) {
            Individual in = compareWorst(population);
            population.remove(in);
            in.restartGene(r);
            population.add(in);
        }
        selected.clear();
    }

    /**
     *
     * @param r Random number generator.
     */
    public void restartGeneIfNoSolutionFound(Random r) {
        for (Individual i: population) {
            if (i.getHeuristic() > 0) i.restartGene(r);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DECIMATION

    /**
     * Decimates a given amount of individuals from the population.
     * @param fitnessType   "best" for the individuals with the highest fitness, "worst" otherwise.
     * @param amount    Number of individuals to decimate.
     */
    public void decimatePopulationByFitness(String fitnessType, int amount) {
        decimateByFitness(fitnessType, amount, population);
    }
    /**
     * Decimates a given amount of individuals from the selected population.
     * @param fitnessType   "best" for the individuals with the highest fitness, "worst" otherwise.
     * @param amount    Number of individuals to decimate.
     */
    public void decimateSelectedByFitness(String fitnessType, int amount) {
        decimateByFitness(fitnessType, amount, selected);
    }

    /**
     * Decimates a given amount of individuals from the offspring population.
     * @param fitnessType   "best" for the individuals with the highest fitness, "worst" otherwise.
     * @param amount    Number of individuals to decimate.
     */
    public void decimateOffspringByFitness(String fitnessType, int amount) {
        decimateByFitness(fitnessType, amount, offspring);
    }

    /**
     * Decimates a given amount of individuals from the given list.
     * @param fitnessType   "best" for the individuals with the highest fitness, "worst" otherwise.
     * @param amount    Number of individuals to decimate.
     * @param population    List of individuals to decimate from.
     */
    private void decimateByFitness(String fitnessType, int amount, List<Individual> population) {
        for (int i = 0; i < amount; i++) {
            Individual in = null;
            if (fitnessType.equals("best")) {
                in = compareBest(population);
                population.remove(in);
            }
            else if(fitnessType.equals("worst")) {
                in = compareWorst(population);
                population.remove(in);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INFORMATION

    /**
     * Print information about the population size.
     */
    public void printPopulationsSize() {
        System.out.println(
                "\n\t_Population size_"
                +"\n\t\tPopulation size: "+population.size()
                //+"\n\t\tIndividuals selected for recombination: "+selected.size()
                //+"\n\t\tOffspring: "+offspring.size()
        );
    }

    /**
     * Print the best individual of all time
     */
    public void printBestIndividual() {
        calculateBestIndividual();
        Individual best = bestIndividual;
        System.out.println(
                "\n\t_Best Individual_"
                +"\n\t\tGene: "+best.getGene()
                +"\n\t\tFitness: "+best.getFitness()
                +"\n\t\tHeuristic: "+best.getHeuristic()
        );
    }

    /**
     * Used for debugging.
     */
    public void printCurrentPopulation() {
        System.out.println("\n\t_Population_");
        for (Individual i: population) {
            System.out.print("\t\t"+i.getFitness()+"; "+i.getHeuristic()+"\n");
        }
    }

    /**
     * Prints the best individual's solution to it's problem(s).
     */
    public void printBestIndividualResolution() { bestIndividual.printProblemResolution(); }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MISC.

    /**
     * Calculate the best individual of all time.
     */
    public void calculateBestIndividual() {
        Individual bestLocal = compareBest(population);
        if (bestIndividual == null) bestIndividual = bestLocal;
        else {
            List<Individual> test = new ArrayList<>();
            test.add(bestLocal);
            test.add(bestIndividual);
            bestIndividual = compareBest(test);
        }
        //System.out.println("best: "+bestIndividual);
    }


    /**
     * Compares all individuals of a list and returns the individual with the best fitness.
     * @param list  List of individuals.
     * @return  Individual with best fitness.
     */
    public Individual compareBest(List<Individual> list) {
        Individual result = null;
        for (Individual next: list) {
            //if (result == null || (next.getHeuristic() == 0 && next.getFitness() < result.getFitness())) result = next;
            if (result == null || (next.getFitness() < result.getFitness())) result = next;
        }
        return result;
    }

    public Individual compareWorst(List<Individual> list) {
        Iterator<Individual> it = list.iterator();
        Individual result = null;
        while (it.hasNext()) {
            Individual next = it.next();
            if (result == null || (next.getHeuristic() > result.getHeuristic() && next.getFitness() > result.getFitness())) result = next;
        }
        return result;
    }

    public boolean solutionFound() {
        return compareBest(population).solutionFound();
    }
    public int size() {return population.size();}
}
