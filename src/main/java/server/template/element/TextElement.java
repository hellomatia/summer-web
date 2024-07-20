package server.template.element;

public class TextElement extends Element {
    public String content;

    public TextElement(String content) {
        super("text");
        this.content = content;
    }

    @Override
    public String toString() {
        return "TextElement{" +
                "content='" + content + '\'' +
                '}';
    }
}
