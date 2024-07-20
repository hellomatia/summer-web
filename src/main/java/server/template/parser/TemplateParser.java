package server.template.parser;

import server.template.element.*;

public class TemplateParser {
    public static Element parse(String template) {
        Element root = new Element("root");
        parseRecursive(template, root);
        return root;
    }

    private static void parseRecursive(String template, Element parent) {
        int start = 0;
        while (start < template.length()) {
            int forStart = template.indexOf("{% for ", start);
            int ifStart = template.indexOf("{% if ", start);
            int varStart = template.indexOf("{{", start);

            if (forStart == -1 && ifStart == -1 && varStart == -1) {
                parent.addChild(new TextElement(template.substring(start)));
                break;
            }

            int nextSpecialChar = Math.min(
                    forStart != -1 ? forStart : Integer.MAX_VALUE,
                    Math.min(ifStart != -1 ? ifStart : Integer.MAX_VALUE,
                            varStart != -1 ? varStart : Integer.MAX_VALUE)
            );

            if (nextSpecialChar > start) {
                parent.addChild(new TextElement(template.substring(start, nextSpecialChar)));
            }

            if (nextSpecialChar == forStart) {
                int forEnd = template.indexOf("%}", forStart);
                String forContent = template.substring(forStart + 7, forEnd);
                String[] parts = forContent.split(" in ");
                ForElement forElement = new ForElement(parts[0].trim(), parts[1].trim());
                parent.addChild(forElement);

                int endForIndex = findMatchingEndTag(template, forEnd + 2, "{% for ", "{% endfor %}");
                parseRecursive(template.substring(forEnd + 2, endForIndex), forElement);
                start = endForIndex + 12;
            } else if (nextSpecialChar == ifStart) {
                int ifEnd = template.indexOf("%}", ifStart);
                String condition = template.substring(ifStart + 6, ifEnd);
                IfElement ifElement = new IfElement(condition.trim());
                parent.addChild(ifElement);

                int endIfIndex = findMatchingEndTag(template, ifEnd + 2, "{% if ", "{% endif %}");
                int elseIndex = template.indexOf("{% else %}", ifEnd);

                if (elseIndex != -1 && elseIndex < endIfIndex) {
                    parseRecursive(template.substring(ifEnd + 2, elseIndex), ifElement);
                    Element elseElement = new Element("else");
                    ifElement.addChild(elseElement);
                    parseRecursive(template.substring(elseIndex + 10, endIfIndex), elseElement);
                } else {
                    parseRecursive(template.substring(ifEnd + 2, endIfIndex), ifElement);
                }
                start = endIfIndex + 11;
            } else if (nextSpecialChar == varStart) {
                int varEnd = template.indexOf("}}", varStart);
                String varName = template.substring(varStart + 2, varEnd).trim();
                parent.addChild(new VariableElement(varName));
                start = varEnd + 2;
            }
        }
    }

    private static int findMatchingEndTag(String template, int startIndex, String startTag, String endTag) {
        int depth = 1;
        int currentIndex = startIndex;
        while (depth > 0 && currentIndex < template.length()) {
            int nextStartTag = template.indexOf(startTag, currentIndex);
            int nextEndTag = template.indexOf(endTag, currentIndex);

            if (nextEndTag == -1) {
                throw new RuntimeException("Missing end tag: " + endTag);
            }

            if (nextStartTag != -1 && nextStartTag < nextEndTag) {
                depth++;
                currentIndex = nextStartTag + startTag.length();
            } else {
                depth--;
                currentIndex = nextEndTag + endTag.length();
            }
        }
        return currentIndex - endTag.length();
    }
}
