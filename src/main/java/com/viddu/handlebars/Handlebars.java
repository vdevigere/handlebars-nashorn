package com.viddu.handlebars;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.script.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Handlebars {

    private final ScriptEngine engine;

    private final ObjectMapper mapper;

    // This is required as using the mapper with
    // SerializationFeature.WRAP_ROOT_VALUE will result in the class name of map
    // (eg:- HashMap) being a part of the JSON string
    private final ObjectMapper mapperForMap;

    private final Logger logger = LoggerFactory.getLogger(Handlebars.class);

    public Handlebars(Reader reader) throws ScriptException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        this.engine = engineManager.getEngineByName("nashorn");
        engine.eval(reader);
        engine.eval("var renderFunction = function(template, contextJSON){var context = JSON.parse(contextJSON); return Handlebars.compile(template)(context);}");
        engine.eval("var registerPartialFunction = function(partialName, partialText){return Handlebars.registerPartial(partialName, partialText);}");
        this.mapper = new ObjectMapper();
        this.mapperForMap = new ObjectMapper();
        this.mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    }

    public Handlebars(InputStream is) throws ScriptException {
        this(new InputStreamReader(is));
    }

    public Handlebars(File file) throws FileNotFoundException, ScriptException {
        this(new FileReader(file));
    }

    public String render(String template, String contextJson) throws NoSuchMethodException, ScriptException {
        Invocable renderFunction = (Invocable) engine;
        String result = (String) renderFunction.invokeFunction("renderFunction", template, contextJson);
        logger.debug("Rendered Output:{}", result);
        return result;
    }

    public String render(String template, Object context) throws NoSuchMethodException, ScriptException,
            JsonProcessingException {
        String contextJson;
        if (context instanceof Map || context instanceof List) {
            contextJson = mapperForMap.writeValueAsString(context);
        } else {
            contextJson = mapper.writeValueAsString(context);
        }
        logger.debug("Context JSON={}", contextJson);
        return render(template, contextJson);
    }

    public String render(InputStream is, Object context) throws NoSuchMethodException, JsonProcessingException,
            ScriptException {
        Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
        String template = scanner.useDelimiter("\\A").next();
        String renderedOutput = render(template, context);
        scanner.close();
        return renderedOutput;
    }
    
    public void registerPartial(String partialName, InputStream partial) throws NoSuchMethodException, ScriptException{
        Scanner scanner = new Scanner(partial, StandardCharsets.UTF_8.name());
        String partialText = scanner.useDelimiter("\\A").next();
        Invocable registerPartial = (Invocable) engine;
        registerPartial.invokeFunction("registerPartialFunction", partialName, partialText);
    }
}
