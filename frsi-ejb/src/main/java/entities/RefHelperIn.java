package entities;
    import java.sql.Connection;
    import java.sql.ResultSet;
    import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 08.02.2017.
 */
public class RefHelperIn {

    private String refCode;
    private String typeAction;
    private Date date;
    private Long id;
    private AbstractReference item;
    private ResultSet cursor;
    private Connection connection;


    // region Getter and Setter
    public String getRefCode() {
        return refCode;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    public String getTypeAction() {
        return typeAction;
    }

    public void setTypeAction(String typeAction) {
        this.typeAction = typeAction;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AbstractReference getItem() {
        return item;
    }

    public void setItem(AbstractReference item) {
        this.item = item;
    }

    public ResultSet getCursor() {
        return cursor;
    }

    public void setCursor(ResultSet cursor) {
        this.cursor = cursor;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // endregion

    // region Equals and Hash

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefHelperIn that = (RefHelperIn) o;

        if (refCode != null ? !refCode.equals(that.refCode) : that.refCode != null) return false;
        if (typeAction != null ? !typeAction.equals(that.typeAction) : that.typeAction != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (item != null ? !item.equals(that.item) : that.item != null) return false;
        if (cursor != null ? !cursor.equals(that.cursor) : that.cursor != null) return false;
        if (connection != null ? !connection.equals(that.connection) : that.connection != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = refCode != null ? refCode.hashCode() : 0;
        result = 31 * result + (typeAction != null ? typeAction.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (cursor != null ? cursor.hashCode() : 0);
        result = 31 * result + (connection != null ? connection.hashCode() : 0);
        return result;
    }


    // endregion
}
