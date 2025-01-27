package tech.wetech.flexmodel;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author cjbi
 */
public class Enum implements TypeWrapper {

    private final String name;
    private String comment;

    private Set<String> elements = new LinkedHashSet<>();

    public Enum(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public Enum setElements(Set<String> elements) {
        this.elements = elements;
        return this;
    }

    public Enum addElement(String element) {
        elements.add(element);
        return this;
    }

    public Set<String> getElements() {
        return elements;
    }

    public String getComment() {
        return comment;
    }

    public Enum setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public String getType() {
        return "enum";
    }
}
