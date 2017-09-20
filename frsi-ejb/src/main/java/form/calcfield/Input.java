package form.calcfield;

/**
 * Created by Nuriddin.Baideuov on 17.09.2015.
 */
public class Input {
    public String id;
    public String value;

    public Input() {
    }

    public Input(String id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Input{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
