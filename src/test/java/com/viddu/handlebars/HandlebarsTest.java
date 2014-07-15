package com.viddu.handlebars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.script.ScriptException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;

public class HandlebarsTest {
    private static final String HANDLEBARS_MIN_JS = "handlebars.min.js";
    private static Handlebars handlebars;
    private Person headOfFamily;

    private final Logger logger = LoggerFactory.getLogger(HandlebarsTest.class);

    @BeforeClass
    public static void init() throws FileNotFoundException, ScriptException {
        handlebars = new Handlebars(HandlebarsTest.class.getClassLoader().getResourceAsStream(HANDLEBARS_MIN_JS));
    }

    @Before
    public void createPerson() {
        headOfFamily = new Person("Viddu", "Devigere");
        headOfFamily.setRelatives(new Person("Vivaan, Devigere"), new Person("Bhuvan, Devigere"), new Person(
                "Swetha, Rao"));
    }

    @Test
    public void testRenderStringTemplate() throws NoSuchMethodException, ScriptException {
        String result = handlebars.render("Hello {{name}}", "{\"name\":\"Viddu\"}");
        assertThat(result, equalTo("Hello Viddu"));
    }

    @Test
    public void testRenderObject() throws NoSuchMethodException, JsonProcessingException, ScriptException {
        String result = handlebars.render("Hello {{name.lastName}}, {{name.firstName}}", headOfFamily);
        assertThat(result, equalTo("Hello Devigere, Viddu"));
    }

    @Test
    public void testRenderWithHelper() throws NoSuchMethodException, JsonProcessingException, ScriptException {
        String result = handlebars
                .render("Hello {{name.lastName}}, {{name.firstName}} \n Relatives:{{#name.relatives}} {{firstName}}-{{lastName}}\n {{/name.relatives}}",
                        headOfFamily);
        logger.debug("Result String:{}", result);
        // assertEquals("Hello Devigere, Viddu \n Relatives: Vivaan- Devigere\n  Bhuvan- Devigere\n  Swetha- Rao\n ",
        // result);
        assertThat(result,
                equalTo("Hello Devigere, Viddu \n Relatives: Vivaan- Devigere\n  Bhuvan- Devigere\n  Swetha- Rao\n "));
    }

    @Test
    public void testRenderTemplatefile() throws NoSuchMethodException, JsonProcessingException, ScriptException {
        InputStream is = HandlebarsTest.class.getClassLoader().getResourceAsStream("template.html");
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
    public void testRegisterPartial() throws NoSuchMethodException, ScriptException, JsonProcessingException{
        String template = "<a href=\"/people/{{lastName}}\">{{firstName}}</a>";
        handlebars.registerPartial("link", new ByteArrayInputStream(template.getBytes()));
        String result = handlebars.render("<ul>{{#name}}<li>{{> link}}</li>{{/name}}</ul>", new Person("Viddu", "Devigere"));
        assertThat(result, equalTo("<ul><li><a href=\"/people/Devigere\">Viddu</a></li></ul>"));
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
