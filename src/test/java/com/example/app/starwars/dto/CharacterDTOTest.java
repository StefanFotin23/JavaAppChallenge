package com.example.app.starwars.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import java.util.Collections;

class CharacterDTOTest {

    @Test
    void testParseValidValues() {
        CharacterDTO character = new CharacterDTO("Luke Skywalker", "172", "77", "19BBY", Collections.emptyList(), "2014-12-09T13:50:51.644000Z");
        assertEquals(172, character.height());
        assertEquals(77, character.mass());
    }

    @Test
    void testParseDoubleValues() {
        CharacterDTO character = new CharacterDTO("R2-D2", "96.5", "32.2", "33BBY", Collections.emptyList(), "2014-12-09T13:50:51.644000Z");
        assertEquals(96, character.height());
        assertEquals(32, character.mass());
    }

    @Test
    void testParseInvalidOrUnknownValues() {
        CharacterDTO character = new CharacterDTO("Unknown Character", "unknown", "unknown", "unknown", Collections.emptyList(), "2014-12-09T13:50:51.644000Z");
        assertEquals(-1, character.height());
        assertEquals(-1, character.mass());
    }

    @Test
    void testParseNullValues() {
        CharacterDTO character = new CharacterDTO("Null Character", (String) null, null, null, Collections.emptyList(), null);
        assertEquals(-1, character.height());
        assertEquals(-1, character.mass());
    }
}
