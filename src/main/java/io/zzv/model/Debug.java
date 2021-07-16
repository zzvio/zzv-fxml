package io.zzv.model;

import javafx.beans.property.*;

public class Debug {

    private final StringProperty group;
    private final StringProperty desc;
    private final BooleanProperty passed; // passed or not test

    public Debug(
            String group,
            String desc,
            Boolean passed
    ) {

        this.group = new SimpleStringProperty(group);
        this.desc = new SimpleStringProperty(desc);
        this.passed = new SimpleBooleanProperty(passed);
    }

    public String toString() {
        return ( this.getGroup() + "-" + this.getDesc() );
    }

    public final String getGroup() {
        return group.get();
    }

    public final void setGroup(String value) {
        group.set(value);
    }

    public StringProperty groupProperty() {
        return group;
    }

    public final String getDesc() {
        return desc.get();
    }

    public final void setDesc(String value) {
        desc.set(value);
    }

    public StringProperty descProperty() {
        return desc;
    }

    public final boolean getPassed() {
        return passed.get();
    }

    public final void setPassed(boolean value) {
        passed.set(value);
    }

    public BooleanProperty passedProperty() {
        return passed;
    }

}
