package com.dgtz.api.crone.beans;

import com.google.gson.GsonBuilder;

import java.io.Serializable;

/**
 * Created by sardor on 4/10/17.
 */
public class LiveLockBean implements Serializable{
    private static final long serialVersionUID = 1L;

    public LiveLockBean() {
    }

    public String idUser;
    public String idLive;
    public String app;
    public String streamFull;
    public Integer rotation;
    public Long time;
    public String queueName;
    public Boolean debate;

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdLive() {
        return idLive;
    }

    public void setIdLive(String idLive) {
        this.idLive = idLive;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getStreamFull() {
        return streamFull;
    }

    public void setStreamFull(String streamFull) {
        this.streamFull = streamFull;
    }

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Boolean getDebate() {
        return debate;
    }

    public void setDebate(Boolean debate) {
        this.debate = debate;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
