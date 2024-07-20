package server.template.renderer;

import server.template.element.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateRenderer {
    public String render(Element root, Map<String, Object> data) {
        StringBuilder result = new StringBuilder();
        renderElement(root, data, result);
        return removeEmptyLines(result.toString());
    }

    private void renderElement(Element element, Map<String, Object> data, StringBuilder result) {
        if (element instanceof TextElement) {
            result.append(((TextElement) element).content);
        } else if (element instanceof VariableElement) {
            String varName = ((VariableElement) element).variableName;
            Object value = resolveVariable(varName, data);
            result.append(value != null ? value.toString() : "");
        } else if (element instanceof IfElement) {
            renderIfElement((IfElement) element, data, result);
        } else if (element instanceof ForElement) {
            renderForElement((ForElement) element, data, result);
        } else {
            for (Element child : element.children) {
                renderElement(child, data, result);
            }
        }
    }

    private Object resolveVariable(String varName, Map<String, Object> data) {
        String[] parts = varName.split("\\.");
        Object value = data.get(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            if (value == null) {
                return null;
            }

            String part = parts[i];
            if (part.endsWith("()")) {
                // 메서드 호출
                String methodName = part.substring(0, part.length() - 2);
                value = invokeMethod(value, methodName);
            } else {
                // 필드 접근
                value = getFieldValue(value, part);
            }
        }

        return value;
    }

    private Object invokeMethod(Object obj, String methodName) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Method getter = obj.getClass().getMethod("get" + capitalize(fieldName));
            return getter.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void renderIfElement(IfElement ifElement, Map<String, Object> data, StringBuilder result) {
        if (evaluateCondition(ifElement.condition, data)) {
            for (Element child : ifElement.children) {
                if (!(child instanceof Element && ((Element) child).name.equals("else"))) {
                    renderElement(child, data, result);
                }
            }
        } else {
            Element elseElement = ifElement.children.stream()
                    .filter(child -> child instanceof Element && ((Element) child).name.equals("else"))
                    .findFirst()
                    .orElse(null);
            if (elseElement != null) {
                for (Element child : elseElement.children) {
                    renderElement(child, data, result);
                }
            }
        }
    }

    private void renderForElement(ForElement forElement, Map<String, Object> data, StringBuilder result) {
        Object listObj = resolveVariable(forElement.collectionName, data);
        if (listObj instanceof List) {
            List<?> list = (List<?>) listObj;
            for (Object item : list) {
                Map<String, Object> loopData = new HashMap<>(data);
                loopData.put(forElement.itemName, item);
                for (Element child : forElement.children) {
                    renderElement(child, loopData, result);
                }
            }
        }
    }

    private boolean evaluateCondition(String condition, Map<String, Object> data) {
        String[] parts = condition.split("==");
        if (parts.length == 2) {
            String left = parts[0].trim();
            String right = parts[1].trim().replaceAll("\"", "");
            Object leftValue = resolveVariable(left, data);
            Object rightValue = resolveVariable(right, data);

            if (leftValue != null && rightValue != null) {
                return leftValue.equals(rightValue);
            } else if (leftValue != null) {
                if (leftValue instanceof Boolean) {
                    return (Boolean) leftValue == Boolean.parseBoolean(right);
                }
                return leftValue.toString().equals(right);
            }
        } else {
            Object value = resolveVariable(condition, data);
            return value instanceof Boolean ? (Boolean) value : false;
        }
        return false;
    }

    private String removeEmptyLines(String input) {
        return input.replaceAll("(?m)^[ \t]*\r?\n", "");
    }
}
