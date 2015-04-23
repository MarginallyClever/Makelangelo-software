/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.kabeja.processing;

import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class PostProcessorConfig {
    private Map<String,String> properties = new HashMap<String,String>();
    private String postProcessorName;

    public PostProcessorConfig(Map<String,String> properties) {
        this.properties = properties;
    }

    public PostProcessorConfig() {
        this(new HashMap<String,String>());
    }

    public Map<String,String> getProperties() {
        return this.properties;
    }

    public void addProperty(String name, String value) {
        this.properties.put(name, value);
    }

    /**
     * @return Returns the filterName.
     */
    public String getPostProcessorName() {
        return postProcessorName;
    }

    /**
     * @param filterName The filterName to set.
     */
    public void setPostProcessorName(String filterName) {
        this.postProcessorName = filterName;
    }
}
