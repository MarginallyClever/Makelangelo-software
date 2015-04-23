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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.processing.event.ProcessingListener;
import org.kabeja.xml.SAXFilter;
import org.kabeja.xml.SAXGenerator;
import org.kabeja.xml.SAXSerializer;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class ProcessingManager {
    private Map<String,SAXFilter> saxfilters = new HashMap<String,SAXFilter>();
    private Map<String,SAXSerializer> saxserializers = new HashMap<String,SAXSerializer>();
    private Map<String,PostProcessor> postprocessors = new HashMap<String,PostProcessor>();
    private Map<String,ProcessPipeline> pipelines = new HashMap<String,ProcessPipeline>();
    private Map<String,SAXGenerator> saxgenerators = new HashMap<String,SAXGenerator>();
    private List<Parser> parsers = new ArrayList<Parser>();

    public void addSAXFilter(SAXFilter filter, String name) {
        this.saxfilters.put(name, filter);
    }

    public SAXFilter getSAXFilter(String name) {
        return (SAXFilter) this.saxfilters.get(name);
    }

    public Map<String,SAXFilter> getSAXFilters() {
        return this.saxfilters;
    }

    public void addSAXSerializer(SAXSerializer serializer, String name) {
        this.saxserializers.put(name, serializer);
    }

    public SAXSerializer getSAXSerializer(String name) {
        return (SAXSerializer) this.saxserializers.get(name);
    }

    public Map<String,SAXSerializer> getSAXSerializers() {
        return this.saxserializers;
    }

    public void addPostProcessor(PostProcessor pp, String name) {
        this.postprocessors.put(name, pp);
    }

    public void addParser(Parser parser) {
        this.parsers.add(parser);
    }

    public List<Parser> getParsers() {
        return this.parsers;
    }

    protected Parser getParser(String extension) {
        Iterator<Parser> i = this.parsers.iterator();

        while (i.hasNext()) {
            Parser parser = i.next();

            if (parser.supportedExtension(extension)) {
                return parser;
            }
        }

        return null;
    }

    public PostProcessor getPostProcessor(String name) {
        return this.postprocessors.get(name);
    }

    public Map<String,PostProcessor> getPostProcessors() {
        return this.postprocessors;
    }

    public void addProcessPipeline(ProcessPipeline pp) {
        pp.setProcessorManager(this);
        this.pipelines.put(pp.getName(), pp);
    }

    public ProcessPipeline getProcessPipeline(String name) {
        return (ProcessPipeline) this.pipelines.get(name);
    }

    public Map<String,ProcessPipeline> getProcessPipelines() {
        return this.pipelines;
    }

    public void process(InputStream stream, String extension, Map<String,String> context,
        String pipeline, OutputStream out) throws ProcessorException {
        Parser parser = this.getParser(extension);

        if (parser != null) {
            try {
                parser.parse(stream, null);

                DXFDocument doc = parser.getDocument();
                this.process(doc, context, pipeline, out);
            } catch (ParseException e) {
                throw new ProcessorException(e);
            }
        }
    }

    public void process(DXFDocument doc, Map<String,String> context, String pipeline,
        OutputStream out) throws ProcessorException {
        if (this.pipelines.containsKey(pipeline)) {
            ProcessPipeline pp = (ProcessPipeline) this.pipelines.get(pipeline);
            pp.prepare();
            pp.process(doc, context, out);
        } else {
            throw new ProcessorException("No pipeline found for name:" +
                pipeline);
        }
    }

    public void process(DXFDocument doc, Map<String,String> context, String pipeline,
        String sourceFile) throws ProcessorException {
        if (this.pipelines.containsKey(pipeline)) {
            try {
                ProcessPipeline pp = (ProcessPipeline) this.pipelines.get(pipeline);
                String suffix = pp.getSAXSerializer().getSuffix();
                String file = sourceFile.substring(0,
                        sourceFile.lastIndexOf('.') + 1) + suffix;
                FileOutputStream out = new FileOutputStream(file);
                process(doc, context, pipeline, out);
            } catch (FileNotFoundException e) {
                throw new ProcessorException(e);
            }
        } else {
            throw new ProcessorException("No pipeline found for name:" +
                pipeline);
        }
    }

    public void addSAXGenerator(SAXGenerator saxgenerator, String name) {
        this.saxgenerators.put(name, saxgenerator);
    }

    public SAXGenerator getSAXGenerator(String name) {
        return (SAXGenerator) this.saxgenerators.get(name);
    }

    public Map<String,SAXGenerator> getSAXGenerators() {
        return this.saxgenerators;
    }

    public void addProcessingListener(ProcessingListener l) {
    }

    public void removeProcessingListener(ProcessingListener l) {
    }
}
