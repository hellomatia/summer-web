package server.template.element;

public class ForElement extends Element {
    public String itemName;
    public String collectionName;

    public ForElement(String itemName, String collectionName) {
        super("for");
        this.itemName = itemName;
        this.collectionName = collectionName;
    }

    @Override
    public String toString() {
        return "ForElement{" +
                "itemName='" + itemName + '\'' +
                ", collectionName='" + collectionName + '\'' +
                '}';
    }
}
