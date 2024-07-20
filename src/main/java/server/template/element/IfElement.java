package server.template.element;

public class IfElement extends Element {
    public String condition;

    public IfElement(String condition) {
        super("if");
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "IfElement{" +
                "condition='" + condition + '\'' +
                '}';
    }
}
