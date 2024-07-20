package server.template.element;

import java.util.ArrayList;
import java.util.List;

public class Element {
    public String name;
    public List<Element> children = new ArrayList<>();

    public Element(String name) {
        this.name = name;
    }

    public void addChild(Element child) {
        children.add(child);
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", children=" + children +
                '}';
    }
}
