package tech.wetech.flexmodel.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class EnumDefinition implements SchemaObject {

    private String name;
    private String comment;
    private List<String> elements = new ArrayList<>();
    protected Map<String, Object> additionalProperties = new HashMap<>();

    public EnumDefinition(String name) {
        this.name = name;
    }

    public EnumDefinition setName(String name) {
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

    public EnumDefinition setElements(List<String> elements) {
        this.elements = elements;
        return this;
    }

    public EnumDefinition addElement(String element) {
        elements.add(element);
        return this;
    }

    public List<String> getElements() {
        return elements;
    }

    public String getComment() {
        return comment;
    }

    public EnumDefinition setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public EnumDefinition addAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this.getName() != null && obj instanceof EntityDefinition) {
            return this.getName().equals(((EntityDefinition) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "<" + getName() + ">";
    }

}
