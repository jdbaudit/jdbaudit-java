package com.jthinking.jdbaudit.db.api.entity;

import us.springett.parsers.cpe.Cpe;

import java.util.List;

public class DBVersion {

    private String vendor;

    private String product;

    private String version;

    private String update;

    /**
     * 同一版本的多种CPE表示。
     * 用于增加规则命中率。
     */
    private List<Cpe> cpeList;

    public DBVersion(String vendor, String product, String version, String update, List<Cpe> cpeList) {
        this.vendor = vendor;
        this.product = product;
        this.version = version;
        this.update = update;
        this.cpeList = cpeList;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public List<Cpe> getCpeList() {
        return cpeList;
    }

    public void setCpeList(List<Cpe> cpeList) {
        this.cpeList = cpeList;
    }
}
