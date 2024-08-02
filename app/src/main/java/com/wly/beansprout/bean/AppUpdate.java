package com.wly.beansprout.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * APP升级(实体类)
 *
 * @author 王力杨
 */
public class AppUpdate implements Parcelable {
    /**
     * 自增
     */
    private int _id;
    /**
     * 版本号(1)
     */
    private int verCode = 1;
    /**
     * 版本名称(1.0.0)
     */
    private String verName = "1.0.0.1";
    /**
     * APP名称(显示名称：21chinamall)
     */
    private String name;
    /**
     * 文件名称(cm.apk)
     */
    private String fileName;
    /**
     * 文件下载地址(http://www.21chinamall.com/download/cm.apk)
     */
    private String filePath;
    /**
     * 是否强制升级（1强制；0可选）
     */
    private int appForce = 0;
    /**
     * 新APP文件大小
     */
    private float newAppSize = 10;
    /**
     * 更新日志
     */
    private String newAppUpdateDesc = "更新内容：\n1.修复一些已知问题，提升应用稳定性";

    public AppUpdate() {
    }

    public AppUpdate(int verCode, String verName, String name, String fileName, String filePath, int appForce, float newAppSize, String newAppUpdateDesc) {
        this.verCode = verCode;
        this.verName = verName;
        this.name = name;
        this.fileName = fileName;
        this.filePath = filePath;
        this.appForce = appForce;
        this.newAppSize = newAppSize;
        this.newAppUpdateDesc = newAppUpdateDesc;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getVerCode() {
        return verCode;
    }

    public void setVerCode(int verCode) {
        this.verCode = verCode;
    }

    public String getVerName() {
        return verName;
    }

    public void setVerName(String verName) {
        this.verName = verName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getAppForce() {
        return appForce;
    }

    public void setAppForce(int appForce) {
        this.appForce = appForce;
    }

    public float getNewAppSize() {
        return newAppSize;
    }

    public void setNewAppSize(float newAppSize) {
        this.newAppSize = newAppSize;
    }

    public String getNewAppUpdateDesc() {
        return newAppUpdateDesc;
    }

    public void setNewAppUpdateDesc(String newAppUpdateDesc) {
        this.newAppUpdateDesc = newAppUpdateDesc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this._id);
        dest.writeInt(this.verCode);
        dest.writeString(this.verName);
        dest.writeString(this.name);
        dest.writeString(this.fileName);
        dest.writeString(this.filePath);
        dest.writeInt(this.appForce);
        dest.writeFloat(this.newAppSize);
        dest.writeString(this.newAppUpdateDesc);
    }

    public void readFromParcel(Parcel source) {
        this._id = source.readInt();
        this.verCode = source.readInt();
        this.verName = source.readString();
        this.name = source.readString();
        this.fileName = source.readString();
        this.filePath = source.readString();
        this.appForce = source.readInt();
        this.newAppSize = source.readFloat();
        this.newAppUpdateDesc = source.readString();
    }

    protected AppUpdate(Parcel in) {
        this._id = in.readInt();
        this.verCode = in.readInt();
        this.verName = in.readString();
        this.name = in.readString();
        this.fileName = in.readString();
        this.filePath = in.readString();
        this.appForce = in.readInt();
        this.newAppSize = in.readFloat();
        this.newAppUpdateDesc = in.readString();
    }

    public static final Creator<AppUpdate> CREATOR = new Creator<AppUpdate>() {
        @Override
        public AppUpdate createFromParcel(Parcel source) {
            return new AppUpdate(source);
        }

        @Override
        public AppUpdate[] newArray(int size) {
            return new AppUpdate[size];
        }
    };
}
