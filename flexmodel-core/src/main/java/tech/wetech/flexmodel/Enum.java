package tech.wetech.flexmodel;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author cjbi
 */
public class Enum implements TypeWrapper {

    private String name;
    private String comment;
    private Set<String> elements = new LinkedHashSet<>();
    protected Map<String, Object> additionalProperties = new HashMap<>();

    public Enum(String name) {
        this.name = name;
    }

    public Enum setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "enum";
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

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Enum addAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
