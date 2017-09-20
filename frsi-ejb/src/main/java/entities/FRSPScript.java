package entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nuriddin on 6/1/16.
 */
public class FRSPScript implements Serializable {
    private List<InputSelectViewModel> inputSelectViewModels;

    public List<InputSelectViewModel> getInputSelectViewModels() {
        return inputSelectViewModels;
    }

    public void setInputSelectViewModels(List<InputSelectViewModel> inputSelectViewModels) {
        this.inputSelectViewModels = inputSelectViewModels;
    }
}
