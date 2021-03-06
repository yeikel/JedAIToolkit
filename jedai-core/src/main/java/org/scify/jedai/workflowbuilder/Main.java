/*
* Copyright [2016-2018] [George Papadakis (gpapadis@yahoo.gr)]
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package org.scify.jedai.workflowbuilder;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import org.apache.log4j.BasicConfigurator;
import org.scify.jedai.blockbuilding.IBlockBuilding;
import org.scify.jedai.blockprocessing.IBlockProcessing;
import org.scify.jedai.datamodel.AbstractBlock;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.EquivalenceCluster;
import org.scify.jedai.datamodel.SimilarityPairs;
import org.scify.jedai.datareader.entityreader.EntitySerializationReader;
import org.scify.jedai.datareader.entityreader.IEntityReader;
import org.scify.jedai.datareader.groundtruthreader.GtSerializationReader;
import org.scify.jedai.datareader.groundtruthreader.IGroundTruthReader;
import org.scify.jedai.entityclustering.IEntityClustering;
import org.scify.jedai.entitymatching.IEntityMatching;
import org.scify.jedai.utilities.BlocksPerformance;
import org.scify.jedai.utilities.ClustersPerformance;
import org.scify.jedai.utilities.datastructures.AbstractDuplicatePropagation;
import org.scify.jedai.utilities.datastructures.BilateralDuplicatePropagation;
import org.scify.jedai.utilities.datastructures.UnilateralDuplicatePropagation;
import org.scify.jedai.utilities.enumerations.BlockBuildingMethod;
import org.scify.jedai.utilities.enumerations.BlockCleaningMethod;
import org.scify.jedai.utilities.enumerations.ComparisonCleaningMethod;
import org.scify.jedai.utilities.enumerations.EntityClusteringCcerMethod;
import org.scify.jedai.utilities.enumerations.EntityClusteringDerMethod;
import org.scify.jedai.utilities.enumerations.EntityMatchingMethod;

/**
 *
 * @author GAP2
 */
public class Main {

    private final static String MAIN_DIR_CCER_DATASETS = System.getProperty("user.dir") + File.separator +"data" + File.separator + "cleanCleanErDatasets" + File.separator;
    private final static String MAIN_DIR_DER_DATASETS = System.getProperty("user.dir") + File.separator + "data" + File.separator + "dirtyErDatasets" + File.separator;
    private final static String[] CCER_ENTITY_FILEPATHS = {"abtProfiles", "buyProfiles",
        "dblpProfiles", "acmProfiles",
        "dblpProfiles2", "scholarProfiles",
        "amazonProfiles", "gpProfiles",
        "imdbProfiles", "dbpediaProfiles"
    };
    private final static String[] CCER_GROUNDTRUTH_FILEPATHS = {"abtBuyIdDuplicates",
        "dblpAcmIdDuplicates",
        "dblpScholarIdDuplicates",
        "amazonGpIdDuplicates",
        "moviesIdDuplicates"
    };
    private final static String[] DER_FILEPATHS = {"abtBuy", "amazonGp", "cddb", "census", "cora", "dblpAcm", "dblpScholar", "movies", "restaurant"};
    private final static String[] ER_TYPES = {"Clean-clean Entity Resolution", "Dirty Entity Resolution"};
    private final static String[] CCER_DATASETS = {"Abt-Buy", "DBLP-ACM", "DBLP-Scholar", "Amazon-Google Products", "IMDB-DBPedia Movies"};
    private final static String[] DER_DATASETS = {"Restaurant", "Census", "Cora", "CdDb", "Abt-By", "DBLP-ACM", "DBLP-Scholar", "Amazon-Google Products", "Movies"};
    private final static String[] BLOCK_BUILDING_METHODS = {"Extended Q-Grams Blocking", "Extended Sorted Neighborhood", "Extended Suffix Arrays Blocking", "Q-Grams Blocking", "Sorted Neighborhood", "Standard/Token Blocking", "Suffix Arrays Blocking"};
    private final static String[] BLOCK_CLEANING_METHODS = {"Block Filtering", "Comparison-based Block Purging", "Size-based Block Purging"};
    private final static String[] COMPARISON_CLEANING_METHODS = {"Cardinality Edge Pruning", "Cardinality Node Pruning", "Comparison Propagation", "Reciprocal Cardinality Node Pruning", "Reciprocal Weighed Node Pruning", "Weighed Edge Pruning", "Weighed Node Pruning"};
    private final static String[] ENTITY_MATCHING_METHODS = {"Group Linkage", "Profile Matcher"};
    private final static String[] DIRTY_ER_ENTITY_CLUSTERING_METHODS = {"Center Clustering", "Connected Components Clustering", "Cut Clustering", "Markov Clustering", "Merge-Center Clustering", "Ricochet SR Clustering"};

    private static TIntList readMultipleInt(String message, String[] array) {
        System.out.println("\n\n" + message);
        for (int i = 0; i < array.length; i++) {
            System.out.println((i + 1) + " - " + array[i]);
        }
        System.out.println("This is an optional step. You can select none or all options. Choose -1 to terminate this step!");

        final TIntList selectedIds = new TIntArrayList();
        while (true) {
            int userInt;
            Scanner keyboard = new Scanner(System.in);

            try {
                userInt = keyboard.nextInt();
            } catch (Exception ex) {
                System.out.println("Invalid input. Please choose between 1 and " + array.length);
                continue;
            }

            if (userInt == -1) {
                break;
            }

            if (userInt < 1 || userInt > array.length) {
                System.out.println("Invalid input. Please choose between 1 and " + array.length);
                continue;
            }

            if (selectedIds.contains(userInt)) {
                System.out.println("You have already selected this option!");
                continue;
            }

            selectedIds.add(userInt);
            System.out.println(array[userInt - 1] + " has been selected!");
        }

        return selectedIds;
    }

    private static int readInt(String message, String[] array) {
        System.out.println("\n\n" + message);
        for (int i = 0; i < array.length; i++) {
            System.out.println((i + 1) + " - " + array[i]);
        }

        int userInt;
        while (true) {
            Scanner keyboard = new Scanner(System.in);

            try {
                userInt = keyboard.nextInt();
            } catch (Exception ex) {
                System.out.println("Invalid input. Please choose between 1 and " + array.length);
                continue;
            }

            if (userInt < 1 || userInt > array.length) {
                System.out.println("Invalid input. Please choose between 1 and " + array.length);
                continue;
            }

            break;
        }

        System.out.println(array[userInt - 1] + " has been selected!");
        return userInt;
    }

    private static int readOptionalInt(String message, String[] array) {
        System.out.println("\n\n" + message);
        for (int i = 0; i < array.length; i++) {
            System.out.println((i + 1) + " - " + array[i]);
        }
        System.out.println("This is an optional step. Choose -1 to select nothing!");

        int userInt;
        while (true) {
            Scanner keyboard = new Scanner(System.in);

            try {
                userInt = keyboard.nextInt();
            } catch (Exception ex) {
                System.out.println("Invalid input. Please choose between 1 and " + array.length);
                continue;
            }

            if (userInt == -1) {
                System.out.println("No option was selected!");
                return -1;
            }

            if (userInt < 1 || userInt > array.length) {
                System.out.println("Invalid input. Please choose between 1 and " + array.length);
                continue;
            }

            break;
        }

        if (0 <= userInt) {
            System.out.println(array[userInt - 1] + " has been selected!");
        }

        return userInt;
    }

    private static int getErType() {
        String message = "Please choose the type of Entity Resolution that will be applied:";
        return readInt(message, ER_TYPES);
    }

    private static int getCleanCleanErDataset() {
        String message = "Please choose one of the available Clean-clean ER datasets:";
        return readInt(message, CCER_DATASETS);
    }

    private static int getDirtyErDataset() {
        String message = "Please choose one of the available Dirty ER datasets:";
        return readInt(message, DER_DATASETS);
    }

    private static int getBlockBuildingMethod() {
        String message = "Please choose one of the available Block Building methods:";
        return readInt(message, BLOCK_BUILDING_METHODS);
    }

    private static TIntList getBlockCleaningMethod() {
        String message = "Please choose one, several or none of the available Block Cleaning methods:";
        return readMultipleInt(message, BLOCK_CLEANING_METHODS);
    }

    private static int getComparisonCleaningMethod() {
        String message = "Please choose at most one of the available Comparison Cleaning methods:";
        return readOptionalInt(message, COMPARISON_CLEANING_METHODS);
    }

    private static int getEntityMatchingMethod() {
        String message = "Please choose one of the available Entity Matching methods:";
        return readInt(message, ENTITY_MATCHING_METHODS);
    }

    private static int getEntityClusteringMethod() {
        String message = "Please choose one of the available Entity Clustering methods for Dirty ER:";
        return readInt(message, DIRTY_ER_ENTITY_CLUSTERING_METHODS);
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();

        System.out.println("\n\nWelcome to JedAI-core command line interface.");

        // Entity Resolution type selection
        int erType = getErType();

        // Data Reading
        final AbstractDuplicatePropagation duplicatePropagation;
        final List<EntityProfile> profilesD1, profilesD2;
        if (erType == 1) {
            int datasetId = getCleanCleanErDataset();

            final IEntityReader eReader1 = new EntitySerializationReader(MAIN_DIR_CCER_DATASETS + CCER_ENTITY_FILEPATHS[datasetId * 2 - 2]);
            profilesD1 = eReader1.getEntityProfiles();
            System.out.println("Input Entity Profiles D1\t:\t" + profilesD1.size());

            final IEntityReader eReader2 = new EntitySerializationReader(MAIN_DIR_CCER_DATASETS + CCER_ENTITY_FILEPATHS[datasetId * 2 - 1]);
            profilesD2 = eReader2.getEntityProfiles();
            System.out.println("Input Entity Profiles D2\t:\t" + profilesD2.size());

            final IGroundTruthReader gtReader = new GtSerializationReader(MAIN_DIR_CCER_DATASETS + CCER_GROUNDTRUTH_FILEPATHS[datasetId]);
            duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
            System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());
        } else {
            profilesD2 = null;
            int datasetId = getDirtyErDataset();

            final IEntityReader eReader = new EntitySerializationReader(MAIN_DIR_DER_DATASETS + DER_FILEPATHS[datasetId] + "Profiles");
            profilesD1 = eReader.getEntityProfiles();
            System.out.println("Input Entity Profiles\t:\t" + profilesD1.size());

            final IGroundTruthReader gtReader = new GtSerializationReader(MAIN_DIR_DER_DATASETS + DER_FILEPATHS[datasetId] + "IdDuplicates");
            duplicatePropagation = new UnilateralDuplicatePropagation(gtReader.getDuplicatePairs(eReader.getEntityProfiles()));
            System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());
        }

        final StringBuilder workflowConf = new StringBuilder();
        final StringBuilder workflowName = new StringBuilder();

        // Block Building
        int bbMethodId = getBlockBuildingMethod();
        double time1 = System.currentTimeMillis();

        final IBlockBuilding blockBuildingMethod = BlockBuildingMethod.getDefaultConfiguration(BlockBuildingMethod.values()[bbMethodId - 1]);
        List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profilesD1, profilesD2);

        double time2 = System.currentTimeMillis();

        workflowConf.append(blockBuildingMethod.getMethodConfiguration());
        workflowName.append(blockBuildingMethod.getMethodName());

        BlocksPerformance blStats = new BlocksPerformance(blocks, duplicatePropagation);
        blStats.setStatistics();
        blStats.printStatistics(time2 - time1, workflowConf.toString(), workflowName.toString());

        // Block Cleaning
        final TIntList bcMethodIds = getBlockCleaningMethod();
        if (!bcMethodIds.isEmpty()) {
            bcMethodIds.sort();
            bcMethodIds.reverse();
            final TIntIterator iterator = bcMethodIds.iterator();
            while (iterator.hasNext()) {
                double time3 = System.currentTimeMillis();

                final IBlockProcessing blockCleaningMethod = BlockCleaningMethod.getDefaultConfiguration(BlockCleaningMethod.values()[iterator.next() - 1]);
                blocks = blockCleaningMethod.refineBlocks(blocks);

                double time4 = System.currentTimeMillis();

                workflowConf.append("\n").append(blockCleaningMethod.getMethodConfiguration());
                workflowName.append("->").append(blockCleaningMethod.getMethodName());

                blStats = new BlocksPerformance(blocks, duplicatePropagation);
                blStats.setStatistics();
                blStats.printStatistics(time4 - time3, workflowConf.toString(), workflowName.toString());
            }
        }

        // Comparison Cleaning
        int ccMethodId = getComparisonCleaningMethod();
        if (0 <= ccMethodId) {
            double time5 = System.currentTimeMillis();

            IBlockProcessing comparisonCleaningMethod = ComparisonCleaningMethod.getDefaultConfiguration(ComparisonCleaningMethod.values()[ccMethodId - 1]);
            blocks = comparisonCleaningMethod.refineBlocks(blocks);

            double time6 = System.currentTimeMillis();

            workflowConf.append("\n").append(comparisonCleaningMethod.getMethodConfiguration());
            workflowName.append("->").append(comparisonCleaningMethod.getMethodName());

            blStats = new BlocksPerformance(blocks, duplicatePropagation);
            blStats.setStatistics();
            blStats.printStatistics(time6 - time5, blockBuildingMethod.getMethodConfiguration(), blockBuildingMethod.getMethodName());
        }

        // Entity Matching
        int emMethodId = getEntityMatchingMethod();
        double time7 = System.currentTimeMillis();

        final IEntityMatching entityMatchingMethod = EntityMatchingMethod.getDefaultConfiguration(EntityMatchingMethod.values()[emMethodId - 1]);
        final SimilarityPairs simPairs = entityMatchingMethod.executeComparisons(blocks, profilesD1, profilesD2);

        double time8 = System.currentTimeMillis();

        workflowConf.append("\n").append(entityMatchingMethod.getMethodConfiguration());
        workflowName.append("->").append(entityMatchingMethod.getMethodName());
        System.out.println("Entity Matching overhead time\t:\t" + (time8 - time7));

        // Entity Clustering
        final IEntityClustering entityClusteringMethod;
        if (erType == 1) { // Clean-Clean ER
            System.out.println("\n\nUnique Mapping Clustering is the only Entity Clustering method compatible with Clean-Clean ER");
            entityClusteringMethod = EntityClusteringCcerMethod.getDefaultConfiguration(EntityClusteringCcerMethod.UNIQUE_MAPPING_CLUSTERING);
        } else { // Dirty ER
            int ecMethodId = getEntityClusteringMethod();
            entityClusteringMethod = EntityClusteringDerMethod.getDefaultConfiguration(EntityClusteringDerMethod.values()[ecMethodId - 1]);
        }

        long time9 = System.currentTimeMillis();

        final EquivalenceCluster[] entityClusters = entityClusteringMethod.getDuplicates(simPairs);

        long time10 = System.currentTimeMillis();

        workflowConf.append("\n").append(entityClusteringMethod.getMethodConfiguration());
        workflowName.append("->").append(entityClusteringMethod.getMethodName());

        ClustersPerformance clp = new ClustersPerformance(entityClusters, duplicatePropagation);
        clp.setStatistics();
        clp.printStatistics(time10 - time9, workflowName.toString(), workflowConf.toString());

    }
}
