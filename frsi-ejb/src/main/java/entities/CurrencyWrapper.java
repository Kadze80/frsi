package entities;

import java.io.Serializable;

/**
 * Entity wrapper
 *
 * @author Ardak Saduakassov
 */
public class CurrencyWrapper extends AbstractReference implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long minorUnits;
    private String creditRate;
    private String ratingAgencyCode;
    private String ratingAgencyName;

    public Long getMinorUnits() {
        return minorUnits;
    }

    public void setMinorUnits(Long minorUnits) {
        this.minorUnits = minorUnits;
    }

    public String getCreditRate() {
        return creditRate;
    }

    public void setCreditRate(String creditRate) {
        this.creditRate = creditRate;
    }

    public String getRatingAgencyCode() {
        return ratingAgencyCode;
    }

    public void setRatingAgencyCode(String ratingAgencyCode) {
        this.ratingAgencyCode = ratingAgencyCode;
    }

    public String getRatingAgencyName() {
        return ratingAgencyName;
    }

    public void setRatingAgencyName(String ratingAgencyName) {
        this.ratingAgencyName = ratingAgencyName;
    }
}
