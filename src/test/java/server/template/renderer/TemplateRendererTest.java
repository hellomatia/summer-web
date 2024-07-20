package server.template.renderer;

import static org.junit.jupiter.api.Assertions.*;

import server.template.element.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class TemplateRendererTest {
    private TemplateRenderer renderer;

    @BeforeEach
    void 테스트_준비() {
        renderer = new TemplateRenderer();
    }

    @Test
    void 텍스트_렌더링_테스트() {
        TextElement root = new TextElement("Hello, World!");
        Map<String, Object> data = new HashMap<>();
        String result = renderer.render(root, data);
        assertEquals("Hello, World!", result);
    }

    @Test
    void 변수_렌더링_테스트() {
        VariableElement root = new VariableElement("name");
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Alice");
        String result = renderer.render(root, data);
        assertEquals("Alice", result);
    }

    @Test
    void IF_조건_렌더링_테스트() {
        IfElement root = new IfElement("isActive == true");
        root.children.add(new TextElement("Active"));
        Map<String, Object> data = new HashMap<>();
        data.put("isActive", true);
        String result = renderer.render(root, data);
        assertEquals("Active", result);
    }

    @Test
    void FOR_반복_렌더링_테스트() {
        ForElement root = new ForElement("item", "items");
        root.children.add(new VariableElement("item"));
        Map<String, Object> data = new HashMap<>();
        data.put("items", Arrays.asList("A", "B", "C"));
        String result = renderer.render(root, data);
        assertEquals("ABC", result);
    }

    @Test
    void 복합_템플릿_렌더링_테스트() {
        Element root = new Element("root");
        root.children.add(new TextElement("Hello, "));
        root.children.add(new VariableElement("name"));
        root.children.add(new TextElement("! "));

        IfElement ifElement = new IfElement("isVIP == true");
        ifElement.children.add(new TextElement("You are a VIP."));
        root.children.add(ifElement);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Bob");
        data.put("isVIP", true);

        String result = renderer.render(root, data);
        assertEquals("Hello, Bob! You are a VIP.", result);
    }

    @Test
    void 빈_줄_제거_테스트() {
        Element root = new Element("root");
        root.children.add(new TextElement("Line 1\n\nLine 2\n\n\nLine 3"));
        Map<String, Object> data = new HashMap<>();
        String result = renderer.render(root, data);
        assertEquals("Line 1\nLine 2\nLine 3", result);
    }
}
