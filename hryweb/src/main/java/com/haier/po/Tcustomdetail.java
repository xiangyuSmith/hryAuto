package com.haier.po;

public class Tcustomdetail {
    private Integer id;

    private Integer customid;

    private Integer clientlevel;

    private Integer clientid;

    private String clientname;

    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCustomid() {
        return customid;
    }

    public void setCustomid(Integer customid) {
        this.customid = customid;
    }

    public Integer getClientlevel() {
        return clientlevel;
    }

    public void setClientlevel(Integer clientlevel) {
        this.clientlevel = clientlevel;
    }

    public Integer getClientid() {
        return clientid;
    }

    public void setClientid(Integer clientid) {
        this.clientid = clientid;
    }

    public String getClientname() {
        return clientname;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname == null ? null : clientname.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}