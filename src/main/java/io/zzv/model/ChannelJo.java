package io.zzv.model;

import org.semux.net.Channel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ChannelJo {

    private final SimpleStringProperty num;
    private final SimpleStringProperty host;
    private final SimpleStringProperty active;
    private final SimpleStringProperty outbound;
    private final SimpleStringProperty latency;
    private final SimpleStringProperty dist;
    private final SimpleStringProperty level;
    private final SimpleStringProperty inrate;
    private final SimpleStringProperty outrate;
    private final SimpleStringProperty block;

    private Channel channel;

    public ChannelJo(Channel channel){
        this.channel= channel;
        this.num = new SimpleStringProperty(""+channel.getRemotePort());
        this.host = new SimpleStringProperty(channel.getRemoteIp());
        this.active = new SimpleStringProperty(""+ channel.isActive());
        this.outbound = new SimpleStringProperty(channel.isInbound()?"Input":"Output");
        this.latency = new SimpleStringProperty(""+channel.getRemotePeer().getLatency());
        this.dist = new SimpleStringProperty(""+channel.getRemotePeer().getClientId());
        this.level = new SimpleStringProperty("");
        this.inrate = new SimpleStringProperty("");
        this.outrate = new SimpleStringProperty("");
        this.block = new SimpleStringProperty(""+channel.getRemotePeer().getLatestBlockNumber());
    }

    public String toString() {
        return channel.toString();
    }

    public final String getNum(){
        return num.get();
    }
    public final void setNum(String value) {
        num.set(value);
    }
    public SimpleStringProperty numProperty() {
        return num;
    }

    public final String getHost(){ return host.get();  }
    public final void setHost(String value) {
        host.set(value);
    }
    public SimpleStringProperty hostProperty() {
        return host;
    }

    public final String getActive(){ return active.get();  }
    public final void setActive(String value) {
        active.set(value);
    }
    public SimpleStringProperty activeProperty() {
        return active;
    }

    public final String getOutbound(){ return outbound.get();  }
    public final void setOutbound(String value) {
        outbound.set(value);
    }
    public SimpleStringProperty outboundProperty() {
        return outbound;
    }

    public final String getLatency(){ return latency.get();  }
    public final void setLatency(String value) {
        latency.set(value);
    }
    public SimpleStringProperty latencyProperty() {
        return latency;
    }

    public final String getDist(){ return dist.get();  }
    public final void setDist(String value) {
        dist.set(value);
    }
    public SimpleStringProperty distProperty() {
        return dist;
    }

    public final String getLevel(){ return level.get();  }
    public final void setLevel(String value) {
        level.set(value);
    }
    public SimpleStringProperty levelProperty() {
        return level;
    }

    public final String getInrate(){ return inrate.get();  }
    public final void setInrate(String value) {
        inrate.set(value);
    }
    public SimpleStringProperty inrateProperty() {
        return inrate;
    }

    public final String getOutrate(){ return outrate.get();  }
    public final void setOutrate(String value) {
        outrate.set(value);
    }
    public SimpleStringProperty outrateProperty() {
        return outrate;
    }

    public final String getBlock(){ return block.get();  }
    public final void setBlock(String value) {
        block.set(value);
    }
    public SimpleStringProperty blockProperty() {
        return block;
    }


}
