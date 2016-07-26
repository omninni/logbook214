/**
 * 
 */
package logbook.server.web;

/**
 * キーのエスケープ処理はスキップされるので注意
 */
public class JsonBuilder {
    StringBuilder sb = new StringBuilder();
    boolean hasPrecValue = false;

    public String build() {
        return this.sb.toString();
    }

    public JsonBuilder beginJson() {
        return this.beginObject();
    }

    public JsonBuilder endJson() {
        this.sb.append("}");
        this.hasPrecValue = true;
        return this;
    }

    public JsonBuilder beginObject() {
        this.prepareAddElement();
        this.sb.append("{");
        this.hasPrecValue = false;
        return this;
    }

    public JsonBuilder beginObject(String key) {
        this.addKey(key);
        this.sb.append("{");
        this.hasPrecValue = false;
        return this;
    }

    public JsonBuilder endObject() {
        this.sb.append("}");
        this.hasPrecValue = true;
        return this;
    }

    public JsonBuilder beginArray() {
        this.prepareAddElement();
        this.sb.append("[");
        this.hasPrecValue = false;
        return this;
    }

    public JsonBuilder beginArray(String key) {
        this.addKey(key);
        this.sb.append("[");
        this.hasPrecValue = false;
        return this;
    }

    public JsonBuilder endArray() {
        this.sb.append("]");
        this.hasPrecValue = true;
        return this;
    }

    private void prepareAddElement() {
        if (this.hasPrecValue)
            this.sb.append(",");
        this.hasPrecValue = true;
    }

    public JsonBuilder addKey(String key) {
        this.prepareAddElement();
        this.sb.append("\"").append(key).append("\":");
        return this;
    }

    public JsonBuilder addValue(boolean value) {
        this.prepareAddElement();
        this.sb.append(value);
        return this;
    }

    public JsonBuilder addValue(int value) {
        this.prepareAddElement();
        this.sb.append(value);
        return this;
    }

    public JsonBuilder addValue(long value) {
        this.prepareAddElement();
        this.sb.append(value);
        return this;
    }

    public JsonBuilder addValue(double value) {
        this.prepareAddElement();
        this.sb.append(value);
        return this;
    }

    public JsonBuilder addValue(String value) {
        this.prepareAddElement();
        this.appendEscapedString(value);
        return this;
    }

    public JsonBuilder add(String key, boolean value) {
        this.addKey(key);
        this.sb.append(value);
        return this;
    }

    public JsonBuilder add(String key, int value) {
        this.addKey(key);
        this.sb.append(value);
        return this;
    }

    public JsonBuilder add(String key, long value) {
        this.addKey(key);
        this.sb.append(value);
        return this;
    }

    public JsonBuilder add(String key, double value) {
        this.addKey(key);
        this.sb.append(value);
        return this;
    }

    public JsonBuilder add(String key, String value) {
        this.addKey(key);
        this.appendEscapedString(value);
        return this;
    }

    private void appendEscapedString(String str) {
        this.sb.append('\"');
        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            switch (ch) {
            case '\"':
                this.sb.append("\\\"");
                break;
            case '\\':
                this.sb.append("\\\\");
                break;
            case '/':
                this.sb.append("\\/");
                break;
            case '\r':
                this.sb.append("\\r");
                break;
            case '\n':
                this.sb.append("\\n");
                break;
            case '\t':
                this.sb.append("\\t");
                break;
            default:
                this.sb.append(ch);
                break;
            }
        }
        this.sb.append('\"');
    }
}
