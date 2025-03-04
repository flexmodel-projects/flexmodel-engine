package tech.wetech.flexmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class Enum implements TypeWrapper {

    private String name;
    private String comment;
    private List<String> elements = new ArrayList<>();
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
        return "ENUM";
    }

    public Enum setElements(List<String> elements) {
        this.elements = elements;
        return this;
    }

    public Enum addElement(String element) {
        elements.add(element);
        return this;
    }

    public List<String> getElements() {
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

    @Override
    public boolean equals(Object obj) {
        if (this.getName() != null && obj instanceof Entity) {
            return this.getName().equals(((Entity) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "<" + getName() + ">";
    }

}
