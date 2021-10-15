module norseninja {
    requires javafx.controls;
    requires javafx.fxml;

    opens norseninja to javafx.fxml, javafx.base;
    opens norseninja.logic to javafx.base;
    exports norseninja;
}