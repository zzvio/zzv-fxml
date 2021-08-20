package io.zzv.model;

import org.semux.message.GuiMessages;
import org.semux.net.Channel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

public class ChannelJo {

    private final StringProperty numProperty;
    private final StringProperty hostProperty;
    private final StringProperty activeProperty;
    private final StringProperty outboundProperty;
    private final StringProperty latencyProperty;
    private final StringProperty distProperty;
    private final StringProperty levelProperty;
    private final StringProperty inrateProperty;
    private final StringProperty outrateProperty;
    private final StringProperty blockProperty;

    private Channel channel;

    public ChannelJo(Channel channel){
        this.channel= channel;
        this.numProperty = new SimpleStringProperty(""+channel.getRemotePort());
        this.hostProperty = new SimpleStringProperty(channel.getRemoteIp());
        this.activeProperty = new SimpleStringProperty(""+ channel.isActive());
        this.outboundProperty = new SimpleStringProperty(channel.isInbound()?"Input":"Output");
        this.latencyProperty = new SimpleStringProperty(""+channel.getRemotePeer().getLatency());
        this.distProperty = new SimpleStringProperty(""+channel.getRemotePeer().getClientId());
        this.levelProperty = new SimpleStringProperty("");
        this.inrateProperty = new SimpleStringProperty("");
        this.outrateProperty = new SimpleStringProperty("");
        this.blockProperty = new SimpleStringProperty(""+channel.getRemotePeer().getLatestBlockNumber());
    }

    public String toString() {
        return channel.toString();
    }

    public final String getNum(){
        return numProperty.get();
    }
    public final void setNum(String value) {
        numProperty.set(value);
    }
    public StringProperty numProperty() {
        return numProperty;
    }

    public final String getHost(){ return hostProperty.get();  }
    public final void setHost(String value) {
        hostProperty.set(value);
    }
    public StringProperty hostProperty() {
        return hostProperty;
    }

    public final String getActive(){ return activeProperty.get();  }
    public final void setActive(String value) {
        activeProperty.set(value);
    }
    public StringProperty activeProperty() {
        return activeProperty;
    }

    public final String getOutbound(){ return outboundProperty.get();  }
    public final void setOutbound(String value) {
        outboundProperty.set(value);
    }
    public StringProperty outboundProperty() {
        return outboundProperty;
    }

    public final String getLatency(){ return latencyProperty.get();  }
    public final void setLatency(String value) {
        latencyProperty.set(value);
    }
    public StringProperty latencyProperty() {
        return latencyProperty;
    }

    public final String getDist(){ return distProperty.get();  }
    public final void setDist(String value) {
        distProperty.set(value);
    }
    public StringProperty distProperty() {
        return distProperty;
    }

    public final String getLevel(){ return levelProperty.get();  }
    public final void setLevel(String value) {
        levelProperty.set(value);
    }
    public StringProperty levelProperty() {
        return levelProperty;
    }

    public final String getInrate(){ return inrateProperty.get();  }
    public final void setInrate(String value) {
        inrateProperty.set(value);
    }
    public StringProperty inrateProperty() {
        return inrateProperty;
    }

    public final String getOutrate(){ return outrateProperty.get();  }
    public final void setOutrate(String value) {
        outrateProperty.set(value);
    }
    public StringProperty outrateProperty() {
        return outrateProperty;
    }

    public final String getBlock(){ return blockProperty.get();  }
    public final void setBlock(String value) {
        blockProperty.set(value);
    }
    public StringProperty blockProperty() {
        return blockProperty;
    }


}
