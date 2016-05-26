/*
 * Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
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
package DataReader;

import DataModel.Attribute;
import DataModel.EntityProfile;
import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class CsvReader extends AbstractReader {

    private final static Logger LOGGER = Logger.getLogger(CsvReader.class.getName());

    private boolean attributeNamesInFirstRow;
    private char separator;
    private int idIndex;
    private String[] attributeNames; // FIX set this!!!
    private final Set<Integer> attributesToExclude;

    public CsvReader(String filePath) {
        super(filePath);
        attributeNamesInFirstRow = false;
        attributeNames = null;
        idIndex = -1;
        separator = ',';
        attributesToExclude = new HashSet<>();
    }

    @Override
    public List<EntityProfile> getEntityProfiles() {
        try {
            //creating reader
            CSVReader reader = new CSVReader(new FileReader(inputFilePath), separator);

            //getting first line
            String[] firstLine = reader.readNext();
            int noOfAttributes = firstLine.length;
            if (noOfAttributes - 1 < idIndex) {
                LOGGER.log(Level.SEVERE, "Id index is does not correspond to a valid column index! Counting starts from 0.");
                return null;
            }

            //setting attribute names
            int entityCounter = 0;
            if (attributeNamesInFirstRow) {
                attributeNames = Arrays.copyOf(firstLine, noOfAttributes);
            } else { // no attribute names in csv file
                attributeNames = new String[noOfAttributes];
                for (int i = 0; i < noOfAttributes; i++) {
                    attributeNames[i] = "attribute" + (i + 1);
                }
                
                entityCounter++; //first line corresponds to entity
                readEntity(entityCounter, firstLine);
            }

            //read entity profiles
            String[] nextLine = null;
            while ((nextLine = reader.readNext()) != null) {
                entityCounter++;
                
                if (nextLine.length < attributeNames.length - 1) { 
                    LOGGER.log(Level.WARNING, "Line with missing attribute names : {0}", Arrays.toString(nextLine));
                    continue;
                }                
                    
                readEntity(entityCounter, nextLine);
            }

            return entityProfiles;
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getMethodInfo() {
        return "CSV Reader: converts a csv file into a set of entity profiles.";
    }

    @Override
    public String getMethodParameters() {
        return "The CSV Reader involves 4 parameters:\n"
                + "1) attributeNamesInFirstRow : boolean, Default value: false.\n"
                + "If true, it reades the attribute name from the first line of the CSV file.\n"
                + "If false, the first line is converted into an entity profile.\n"
                + "2) separator : character, default value: ','.\n"
                + "It determines the character used to tokenize every line into attribute values.\n"
                + "3) Id index: integer parameter, default value: -1. Counting starts from 0.\n"
                + "If id>0, the values of corresponding column are used for assigning the id of every entity.\n"
                + "If the given id is larger than the number of columns, an exception is thrown and getEntityProfiles() returns null.\n"
                + "If id<0, an auto-incremented integer is assigned as id to every entity.\n"
                + "4) attributesToExclude: int[], default value: empty. Counting starts from 0.\n"
                + "The column ids assigned to this parameter will be ignored during the creation of entity profiles.\n";
    }

    private void readEntity(int index, String[] currentLine) throws IOException {
        String entityId;
        if (idIndex < 0) {
            entityId = "id" + index;
        } else {
            entityId = currentLine[idIndex];
        }
        
        EntityProfile newProfile = new EntityProfile(entityId);
        for (int i = 0; i < currentLine.length; i++) {
            if (attributesToExclude.contains(i)) {
                continue;
            }
            newProfile.addAttribute(attributeNames[i], currentLine[i]);
        }
        entityProfiles.add(newProfile);
    }

    public void setAttributesToExclude(int[] attributesIndicesToExclude) {
        for (int attributeIndex : attributesIndicesToExclude) {
            attributesToExclude.add(attributeIndex);
        }
    }

    public void setAttributeNamesInFirstRow(boolean attributeNamesInFirstRow) {
        this.attributeNamesInFirstRow = attributeNamesInFirstRow;
    }

    public void setIdIndex(int idIndex) {
        this.idIndex = idIndex;
        attributesToExclude.add(idIndex);
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    public static void main(String[] args) {
        String filePath = "C:\\Users\\G.A.P. II\\Downloads\\cd.csv";
        CsvReader csvReader = new CsvReader(filePath);
        csvReader.setAttributeNamesInFirstRow(true);
        csvReader.setSeparator(';');
        csvReader.setAttributesToExclude(new int[]{0, 1});
        csvReader.setIdIndex(1);
        List<EntityProfile> profiles = csvReader.getEntityProfiles();
        for (EntityProfile profile : profiles) {
            System.out.println("\n\n" + profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                System.out.println(attribute.toString());
            }
        }
    }
}