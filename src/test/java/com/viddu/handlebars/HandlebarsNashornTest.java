package com.viddu.handlebars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonRootName;

public class HandlebarsNashornTest {

    private static final String HANDLEBARS_MIN_JS = "handlebars.min.js";
    private static Handlebars handlebars;
    private Person headOfFamily;

    private final Logger logger = LoggerFactory.getLogger(HandlebarsNashornTest.class);

    @BeforeClass
    public static void init() throws HandlebarsException, ScriptException {
        InputStreamReader isReader = new InputStreamReader(HandlebarsNashornTest.class.getClassLoader()
                .getResourceAsStream(HANDLEBARS_MIN_JS));
        handlebars = new HandlebarsNashorn(isReader);
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn");
    }

    @Before
    public void createPerson() {
        headOfFamily = new Person("Viddu", "Devigere");
        headOfFamily.setRelatives(new Person("Vivaan, Devigere"), new Person("Bhuvan, Devigere"), new Person(
                "Swetha, Rao"));
    }

    @Test
    public void testRenderStringTemplate() throws HandlebarsException {
        String result = handlebars.render("Hello {{name}}", "{\"name\":\"Viddu\"}");
        assertThat(result, equalTo("Hello Viddu"));
    }

    @Test
    public void testRenderStringTemplateWithMap() throws HandlebarsException {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Viddu");
        map.put("age", 23);
        String result = handlebars.render("Hello {{name}}, {{age}}", map);
        assertThat(result, equalTo("Hello Viddu, 23"));
    }

    @Test
    public void testRenderStringTemplateCollection() throws HandlebarsException {
        List<Person> personList = new LinkedList<HandlebarsNashornTest.Person>();
        personList.add(new Person("Viddu", "Devigere"));
        personList.add(new Person("John", "Smith"));
        String result = handlebars.render("Hello {{#each .}}<li>{{firstName}}</li>{{/each}}", personList);
        assertThat(result, equalTo("Hello <li>Viddu</li><li>John</li>"));
    }

    @Test
    public void testRenderStringTemplateObject() throws HandlebarsException {
        Person person = new Person("Viddu", "Devigere");
        String result = handlebars.render("Hello {{name.firstName}}, {{name.lastName}}", person);
        assertThat(result, equalTo("Hello Viddu, Devigere"));
    }

    @Test
    public void testRenderInputStreamString() throws HandlebarsException {
        InputStream is = HandlebarsNashornTest.class.getClassLoader().getResourceAsStream("template.html");
        String result = handlebars
                .render(is,
                        "{\"name\":{\"lastName\":\"Devigere\",\"firstName\":\"Viddu\",\"relatives\":[{\"lastName\":\" Devigere\",\"firstName\":\"Vivaan\",\"relatives\":null},{\"lastName\":\" Devigere\",\"firstName\":\"Bhuvan\",\"relatives\":null},{\"lastName\":\" Rao\",\"firstName\":\"Swetha\",\"relatives\":null}]}}");
        assertThat(result, equalToIgnoringWhiteSpace("<!DOCTYPE html>\n" + "<html>\n" + "    <head></head>\n"
                + "    <body>\n" + "        <h1>Hello Devigere, Viddu</h1>\n" + "        <h2>Relatives:</h2>\n"
                + "        <ul>\n" + "            \n" + "            <li>\n" + "                Vivaan,  Devigere\n"
                + "            </li>\n" + "            \n" + "            <li>\n"
                + "                Bhuvan,  Devigere\n" + "            </li>\n" + "            \n"
                + "            <li>\n" + "                Swetha,  Rao\n" + "            </li>\n" + "            \n"
                + "        </ul>\n" + "    </body>\n" + "</html>\n" + ""));
    }

    @Test
    public void testRenderInputStreamObject() throws HandlebarsException {
        InputStream is = HandlebarsNashornTest.class.getClassLoader().getResourceAsStream("template.html");
        String result = handlebars.render(is, headOfFamily);
        assertThat(result, equalToIgnoringWhiteSpace("<!DOCTYPE html>\n" + "<html>\n" + "    <head></head>\n"
                + "    <body>\n" + "        <h1>Hello Devigere, Viddu</h1>\n" + "        <h2>Relatives:</h2>\n"
                + "        <ul>\n" + "            \n" + "            <li>\n" + "                Vivaan,  Devigere\n"
                + "            </li>\n" + "            \n" + "            <li>\n"
                + "                Bhuvan,  Devigere\n" + "            </li>\n" + "            \n"
                + "            <li>\n" + "                Swetha,  Rao\n" + "            </li>\n" + "            \n"
                + "        </ul>\n" + "    </body>\n" + "</html>\n" + ""));
    }

    @Test
    public void testRegisterHelper() throws HandlebarsException {
        handlebars.registerHelper("link", (ScriptObjectMirror context, ScriptObjectMirror options) -> {
//            return (String) options.callMember("fn", context);
            String url = (String) context.get("url");
            String name = (String) context.get("name");
            return "<a href=\""+url+"\">"+name+"</a>";
        });
        String template = "{{#link myLink}}{{/link}}";
        String myLink="{\"myLink\":{\"url\":\"http://www.google.com\", \"name\":\"Google\"}}";
        String result = handlebars.render(template, myLink);
        logger.debug("Result:{}", result);
        assertThat(result, equalTo("<a href=\"http://www.google.com\">Google</a>"));
    }

    @JsonRootName("name")
    class Person {

        private final String lastName;
        private final String firstName;
        private Person[] relatives;

        public Person(String fullName) {
            String[] names = fullName.split(",");
            this.firstName = names[0];
            this.lastName = names[1];
        }

        public Person(String fName, String lName) {
            this.firstName = fName;
            this.lastName = lName;
        }

        public void setRelatives(Person... relatives) {
            this.relatives = relatives;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public Person[] getRelatives() {
            return relatives;
        }

    }
}
