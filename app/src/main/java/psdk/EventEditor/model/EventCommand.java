package psdk.EventEditor.model;

import libs.json.JSONArray;

public class EventCommand {
    private int code;
    private String name;
    private String indent;
    private JSONArray parameters;

    public EventCommand(int code, String name, String indent, JSONArray parameters) {
        this.code = code;
        this.name = name;
        this.indent = indent;
        this.parameters = parameters;
    }

    public EventCommand(int code, String indent, JSONArray parameters) {
        this(code, "Command " + code, indent, parameters);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndent() {
        return indent;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public JSONArray getParameters() {
        return parameters;
    }

    public void setParameters(JSONArray parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "EventCommand [code=" + code + ", name='" + name + "', indent='" + indent + "', parameters=" + parameters + "]";
    }
}