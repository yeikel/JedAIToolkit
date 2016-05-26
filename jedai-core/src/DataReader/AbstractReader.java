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

import DataModel.EntityProfile;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public abstract class AbstractReader implements IDataReader {
    
    protected String inputFilePath;
    protected final List<EntityProfile> entityProfiles;
    
    public AbstractReader (String filePath) {
        inputFilePath = filePath;
        entityProfiles = new ArrayList<>();
    }
}