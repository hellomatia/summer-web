package server.template.parser;

import static org.junit.jupiter.api.Assertions.*;

import server.template.element.*;
import org.junit.jupiter.api.Test;

class TemplateParserTest {
    @Test
    void 단순_텍스트_파싱_테스트() {
        String template = "Hello, world!";
        Element root = TemplateParser.parse(template);

        assertEquals(1, root.children.size());
        assertTrue(root.children.get(0) instanceof TextElement);
        assertEquals("Hello, world!", ((TextElement) root.children.get(0)).content);
    }

    @Test
    void 변수_파싱_테스트() {
        String template = "Hello, {{ name }}!";
        Element root = TemplateParser.parse(template);

        assertEquals(3, root.children.size());
        assertTrue(root.children.get(0) instanceof TextElement);
        assertTrue(root.children.get(1) instanceof VariableElement);
        assertTrue(root.children.get(2) instanceof TextElement);
        assertEquals("Hello, ", ((TextElement) root.children.get(0)).content);
        assertEquals("name", ((VariableElement) root.children.get(1)).variableName);
        assertEquals("!", ((TextElement) root.children.get(2)).content);
    }

    @Test
    void For_루프_파싱_테스트() {
        String template = "{% for item in items %}{{ item }}{% endfor %}";
        Element root = TemplateParser.parse(template);

        assertEquals(1, root.children.size());
        assertTrue(root.children.get(0) instanceof ForElement);
        ForElement forElement = (ForElement) root.children.get(0);
        assertEquals("item", forElement.itemName);
        assertEquals("items", forElement.collectionName);
        assertEquals(1, forElement.children.size());
        assertTrue(forElement.children.get(0) instanceof VariableElement);
        assertEquals("item", ((VariableElement) forElement.children.get(0)).variableName);
    }

    @Test
    void If_조건문_파싱_테스트() {
        String template = "{% if condition %}True{% else %}False{% endif %}";
        Element root = TemplateParser.parse(template);

        assertEquals(1, root.children.size());
        assertTrue(root.children.get(0) instanceof IfElement);
        IfElement ifElement = (IfElement) root.children.get(0);
        assertEquals("condition", ifElement.condition);
        assertEquals(2, ifElement.children.size());
        assertTrue(ifElement.children.get(0) instanceof TextElement);
        assertEquals("True", ((TextElement) ifElement.children.get(0)).content);
        assertTrue(ifElement.children.get(1) instanceof Element);
        Element elseElement = ifElement.children.get(1);
        assertEquals("else", elseElement.name);
        assertEquals(1, elseElement.children.size());
        assertTrue(elseElement.children.get(0) instanceof TextElement);
        assertEquals("False", ((TextElement) elseElement.children.get(0)).content);
    }

    @Test
    void 중첩_구조_파싱_테스트() {
        String template = "{% for category in categories %}{{ category }}: {% for item in items %}{% if item.category == category %}{{ item.name }}{% endif %}{% endfor %}{% endfor %}";
        Element root = TemplateParser.parse(template);

        assertEquals(1, root.children.size());
        assertTrue(root.children.get(0) instanceof ForElement);
        ForElement outerFor = (ForElement) root.children.get(0);
        assertEquals("category", outerFor.itemName);
        assertEquals("categories", outerFor.collectionName);

        assertEquals(3, outerFor.children.size());
        assertTrue(outerFor.children.get(0) instanceof VariableElement);
        assertTrue(outerFor.children.get(1) instanceof TextElement);
        assertTrue(outerFor.children.get(2) instanceof ForElement);

        ForElement innerFor = (ForElement) outerFor.children.get(2);
        assertEquals("item", innerFor.itemName);
        assertEquals("items", innerFor.collectionName);

        assertEquals(1, innerFor.children.size());
        assertTrue(innerFor.children.get(0) instanceof IfElement);
        IfElement ifElement = (IfElement) innerFor.children.get(0);
        assertEquals("item.category == category", ifElement.condition);

        assertEquals(1, ifElement.children.size());
        assertTrue(ifElement.children.get(0) instanceof VariableElement);
        assertEquals("item.name", ((VariableElement) ifElement.children.get(0)).variableName);
    }
}
