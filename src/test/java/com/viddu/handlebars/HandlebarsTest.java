package com.viddu.handlebars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.FileNotFoundException;

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
