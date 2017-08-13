package meg.biblio.catalog;

public class BasicStat {

    private String label;
    private String addlLabel;
    private String imagepath;
    private String value;
    private Long stattype;
    private String display;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddlLabel() {
        return addlLabel;
    }

    public void setAddlLabel(String addlLabel) {
        this.addlLabel = addlLabel;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getStattype() {
        return stattype;
    }

    public void setStattype(Long stattype) {
        this.stattype = stattype;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }


}
