package server.template.element;

public class VariableElement extends Element {
    public String variableName;

    public VariableElement(String variableName) {
        super("variable");
        this.variableName = variableName;
    }

    @Override
    public String toString() {
        return "VariableElement{" +
                "variableName='" + variableName + '\'' +
                '}';
    }
}
