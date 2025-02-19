import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

import java.util.Optional;


public class Warning{

    public void selectTwo(){
        Alert msgBox = new Alert(Alert.AlertType.ERROR);
        msgBox.setTitle("Error!");
        msgBox.setHeaderText(null);
        msgBox.setContentText("Two places must be selected!");
        msgBox.showAndWait();
        msgBox.close();
    }

    public void emptyInput(){
        Alert msgBox = new Alert(Alert.AlertType.WARNING);
        msgBox.setTitle("WARNING");
        msgBox.setHeaderText(null);
        msgBox.setContentText("You can´t leave input fields empty");
        msgBox.showAndWait();
        msgBox.close();
    }

    public void edgeExist(){
        Alert msgBox = new Alert(Alert.AlertType.ERROR);
        msgBox.setTitle("Error!");
        msgBox.setHeaderText(null);
        msgBox.setContentText("Edge already exists");
        msgBox.showAndWait();
        msgBox.close();
    }

    public void noEdgeExist(){
        Alert msgBox = new Alert(Alert.AlertType.ERROR);
        msgBox.setTitle("Error!");
        msgBox.setHeaderText(null);
        msgBox.setContentText("Nodes are not connected");
        msgBox.showAndWait();
        msgBox.close();
    }

    public void noMapToOpen(){
        Alert msgBox = new Alert(Alert.AlertType.ERROR);
        msgBox.setTitle("Error!");
        msgBox.setHeaderText(null);
        msgBox.setContentText("There is no saved map to open.");
        msgBox.showAndWait();
        msgBox.close();
    }


    public boolean unsavedChanges(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Warning!");
        dialog.setHeaderText(null);
        dialog.setContentText("Unsaved changes,  Do you want to continue?");
        GridPane dialogGrid = new GridPane();
        dialogGrid.setHgap(10);
        dialogGrid.setVgap(10);
        dialogGrid.setPadding(new Insets(20, 150, 10, 10));

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(dialogGrid);

        Optional<ButtonType> result = dialog.showAndWait();

        return result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE;




    }


}