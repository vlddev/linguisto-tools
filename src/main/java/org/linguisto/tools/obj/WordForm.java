package org.linguisto.tools.obj;

import org.linguisto.tools.imp.core.annotation.FormatField;
import org.linguisto.tools.imp.core.annotation.FormatObject;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.converter.annotation.StringToString;

@FormatObject( name = "wf")
public class WordForm extends BaseObj {

    Integer fkInf;
    String wf;
    String language;
    //Form ID
    String fid;

    public WordForm(String wf){
        this.wf = wf;
    }

    public WordForm(String fid, String wf){
        this.wf = wf;
        this.fid = fid;
    }

    public WordForm(Integer id, String wf){
        this.id = id;
        this.wf = wf;
    }

    /**
     * @return
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return
     */
    public Integer getFkInf() {
        return fkInf;
    }

    /**
     * @param fkInf
     */
    public void setFkInf(Integer fkInf) {
        this.fkInf = fkInf;
    }

    /**
     * @return
     */
    public String getWf() {
        return wf;
    }

    /**
     * @param wf
     */
    @StringToString
    @FormatField(name = "fv")
    public void setWf(String wf) {
        this.wf = wf;
    }

    public String getFid() {
        return fid;
    }

    @StringToString
    @FormatField(name = "fid")
    public void setFid(String fid) {
        this.fid = fid;
    }

    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof WordForm) {
            if (getId() != null) {
                ret = getId().equals(((WordForm)obj).getId());
            }
        }
        return ret;
    }

}
