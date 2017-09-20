package entities;

/**
 * Created by Zhanar.Sanaupova on 25.01.2017.
 */
public class HolidayItem extends AbstractReference {

     private String nameDate;
     private String dayValue;
     private String mesValue;
     private Integer status;
     private Integer transferDays;
     private Boolean isChecked;

    public String getNameDate() {
        return nameDate;
    }

    public void setNameDate(String nameDate) {
        this.nameDate = nameDate;
    }

    public String getDayValue() {
        return dayValue;
    }

    public void setDayValue(String dayValue) {
        this.dayValue = dayValue;
    }

    public Integer getTransferDays() {
        return transferDays;
    }

    public void setTransferDays(Integer transferDays) {
        this.transferDays = transferDays;
    }

    public String getMesValue() {
        return mesValue;
    }

    public void setMesValue(String mesValue) {
        this.mesValue = mesValue;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        this.isChecked = isChecked;
    }
}
